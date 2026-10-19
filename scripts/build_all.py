"""Build and sign all Android release variants using the local SDK and key."""

import hashlib
import json
import os
from pathlib import Path
import re
import secrets
import shutil
import subprocess
import sys
import tempfile

ROOT = Path(__file__).resolve().parent.parent
OUTPUT = ROOT / "Release"
SIGNING = ROOT / ".signing"
APK_NAMES = {
    f"{mode}-{abi}.apk"
    for mode in ("mobile", "leanback")
    for abi in ("arm64_v8a", "armeabi_v7a")
}


# 执行外部工具并保留失败退出码。
def run(command, env):
    subprocess.run([str(part) for part in command], cwd=ROOT, env=env, check=True)


# 查找本机安卓平台与签名工具。
def find_sdk():
    candidates = [os.environ.get(name) for name in ("ANDROID_HOME", "ANDROID_SDK_ROOT")]
    properties = ROOT / "local.properties"
    if properties.exists():
        match = re.search(r"^sdk\.dir\s*=\s*(.+)$", properties.read_text(encoding="utf-8"), re.M)
        if match:
            candidates.insert(0, match[1].strip().replace("\\:", ":").replace("\\\\", "\\"))
    candidates.extend([
        str(ROOT / ".android-sdk"),
        str(Path(os.environ.get("LOCALAPPDATA", "")) / "Android" / "Sdk"),
    ])
    for candidate in candidates:
        if not candidate:
            continue
        sdk = Path(candidate).expanduser().resolve()
        tools = sdk / "build-tools" / "35.0.0" / "lib" / "apksigner.jar"
        platform = sdk / "platforms" / "android-36.1" / "android.jar"
        if tools.is_file() and platform.is_file():
            return sdk, tools
    raise RuntimeError(
        "Android SDK missing. Install Android SDK Platform 36.1 and Build-Tools 35.0.0; "
        "set ANDROID_HOME or sdk.dir in local.properties."
    )


# 创建或复用本地长期签名密钥。
def signing_environment(keytool, env):
    config = SIGNING / "release.json"
    keystore = SIGNING / "release.jks"
    try:
        SIGNING.mkdir()
        created = True
    except FileExistsError:
        created = False
        if not config.is_file() or not keystore.is_file():
            raise RuntimeError(
                "Incomplete .signing directory or another build is creating the key. "
                "Wait for it to finish, or restore release.json and release.jks from backup."
            )
    if created:
        settings = {"alias": "pwztv", "password": secrets.token_urlsafe(32)}
    else:
        settings = json.loads(config.read_text(encoding="utf-8"))
    env = dict(env, PWZTV_STORE_PASSWORD=settings["password"])
    if created:
        config.write_text(json.dumps(settings, indent=2) + "\n", encoding="utf-8")
        # 执行外部工具并保留失败退出码。
        run([
            keytool, "-genkeypair", "-keystore", keystore, "-storetype", "PKCS12",
            "-alias", settings["alias"], "-storepass:env", "PWZTV_STORE_PASSWORD",
            "-keypass:env", "PWZTV_STORE_PASSWORD", "-keyalg", "RSA", "-keysize", "3072",
            "-validity", "10000", "-dname", "CN=PWZTV, OU=Android, O=PWZTV, C=CN",
        ], env)
    # 执行外部工具并保留失败退出码。
    run([
        keytool, "-list", "-keystore", keystore, "-alias", settings["alias"],
        "-storepass:env", "PWZTV_STORE_PASSWORD",
    ], env)
    print("Back up the entire .signing directory; updates require the same key.", flush=True)
    return keystore, settings["alias"], env


# 按构建元数据校验四种发布产物。
def release_apks():
    packages = {}
    for metadata in (ROOT / "app" / "build" / "outputs" / "apk").rglob("output-metadata.json"):
        data = json.loads(metadata.read_text(encoding="utf-8"))
        if not data.get("variantName", "").endswith("Release"):
            continue
        for item in data.get("elements", []):
            name = item["outputFile"]
            if name in APK_NAMES:
                apk = metadata.parent / name
                if name in packages or not apk.is_file():
                    raise RuntimeError(f"Duplicate or missing release APK: {name}")
                packages[name] = apk
    if set(packages) != APK_NAMES:
        raise RuntimeError("Missing release APKs: " + ", ".join(sorted(APK_NAMES - packages.keys())))
    return packages


# 构建并签名校验全部安卓发布包。
def main():
    if os.name != "nt":
        raise RuntimeError("Run build_all.bat on Windows.")
    env = dict(os.environ)
    java_home = env.get("JAVA_HOME", "").strip('"')
    java = Path(java_home) / "bin" / "java.exe" if java_home else Path(shutil.which("java") or "")
    keytool = java.parent / "keytool.exe"
    if not java.is_file() or not keytool.is_file():
        raise RuntimeError("JDK 17 or newer is required. Set JAVA_HOME to your JDK directory.")
    env["JAVA_HOME"] = str(java.parent.parent)
    # 查找本机安卓平台与签名工具。
    sdk, signer = find_sdk()
    env.update(ANDROID_HOME=str(sdk), ANDROID_SDK_ROOT=str(sdk))
    # 创建或复用本地长期签名密钥。
    keystore, alias, env = signing_environment(keytool, env)
    # 执行外部工具并保留失败退出码。
    run([os.environ.get("COMSPEC", "cmd.exe"), "/d", "/c", "gradlew.bat",
         ":app:assembleRelease", "--console=plain", "--stacktrace"], env)
    # 按构建元数据校验四种发布产物。
    packages = release_apks()
    OUTPUT.mkdir(exist_ok=True)
    with tempfile.TemporaryDirectory(prefix=".staging-", dir=OUTPUT) as staging:
        staged = Path(staging)
        for name, source in sorted(packages.items()):
            # 执行外部工具并保留失败退出码。
            run([
                java, "-jar", signer, "sign", "--ks", keystore, "--ks-key-alias", alias,
                "--ks-pass", "env:PWZTV_STORE_PASSWORD", "--key-pass", "env:PWZTV_STORE_PASSWORD",
                "--out", staged / name, source,
            ], env)
            # 执行外部工具并保留失败退出码。
            run([java, "-jar", signer, "verify", "--verbose", "--print-certs", staged / name], env)
        for name in sorted(packages):
            (staged / name).replace(OUTPUT / name)
            idsig = staged / (name + ".idsig")
            if idsig.exists():
                idsig.replace(OUTPUT / idsig.name)
    print(f"\nSUCCESS: all 4 signed release APKs are in {OUTPUT}", flush=True)
    for name in sorted(packages):
        apk = OUTPUT / name
        digest = hashlib.sha256(apk.read_bytes()).hexdigest()
        print(f"{name}  {apk.stat().st_size / 1048576:.2f} MiB  SHA256={digest}", flush=True)


if __name__ == "__main__":
    try:
        # 构建并签名校验全部安卓发布包。
        main()
    except subprocess.CalledProcessError as error:
        print(f"Build failed (exit code {error.returncode}); no successful output was published.", file=sys.stderr)
        sys.exit(error.returncode or 1)
    except (OSError, ValueError, KeyError, RuntimeError) as error:
        print(f"ERROR: {error}", file=sys.stderr)
        sys.exit(1)
