"""Build Gradle-signed Debug APKs for the selected device and architecture."""

import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import tempfile
import zipfile

from release_cache import ReleaseCache

ROOT = Path(__file__).resolve().parent.parent
OUTPUT = ROOT / "Release"
MODES = ("mobile", "leanback")
APK_NAMES = {
    ("mobile", "arm64_v8a"): "p.apk",
    ("leanback", "arm64_v8a"): "p-tv.apk",
    ("mobile", "armeabi_v7a"): "p32.apk",
    ("leanback", "armeabi_v7a"): "p-tv32.apk",
}
PYTHON_NATIVE_PREFIXES = (
    "libpython", "libchaquopy", "libcrypto_python", "libssl_python", "libsqlite3_python",
    "libcrypto_chaquopy", "libssl_chaquopy", "libsqlite3_chaquopy",
)


# 解析设备架构及Python运行时选项。
def build_options(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    architecture = parser.add_mutually_exclusive_group()
    architecture.add_argument("--include-32bit", action="store_true", help="also build ARM 32-bit APKs")
    architecture.add_argument("--abi", choices=("32", "64"), help="build only the selected ARM architecture")
    parser.add_argument("--mode", choices=MODES, help="build only the selected device type")
    parser.add_argument("--with-python", action="store_true", help="include support for Python script sources")
    parser.add_argument("--no-pause", action="store_true", help=argparse.SUPPRESS)
    options = parser.parse_args(argv)
    options.abis = (("armeabi_v7a",) if options.abi == "32" else ("arm64_v8a", "armeabi_v7a")
                    if options.include_32bit else ("arm64_v8a",))
    options.modes = (options.mode,) if options.mode else MODES
    return options


# 按所选设备架构构建Debug并显式设置Python依赖。
def gradle_command(options):
    tasks = [f":app:assemble{mode.capitalize()}{abi.capitalize()}Debug" for mode in options.modes for abi in options.abis]
    return [os.environ.get("COMSPEC", "cmd.exe"), "/d", "/c", "gradlew.bat", *tasks,
            f"-PwithPython={str(options.with_python).lower()}",
            "--no-configuration-cache" if options.with_python else "--configuration-cache",
            "--console=plain", "--stacktrace"]


# 反查安装包中的Python组件与构建选项一致。
def verify_python_contents(apk, enabled):
    with zipfile.ZipFile(apk) as archive:
        names = archive.namelist()
    python_assets = any(name.startswith("assets/chaquopy/") for name in names)
    python_native = any(name.startswith("lib/") and name.rsplit("/", 1)[-1].startswith(PYTHON_NATIVE_PREFIXES)
                        for name in names)
    if enabled and not (python_assets and python_native):
        raise RuntimeError(f"Python-enabled APK is missing runtime components: {apk.name}")
    if not enabled and (python_assets or python_native):
        raise RuntimeError(f"Default APK unexpectedly contains Python runtime components: {apk.name}")


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


# 按构建元数据只收集本次Debug安装包。
def debug_apks(expected):
    packages = {}
    for metadata in (ROOT / "app" / "build" / "outputs" / "apk").rglob("output-metadata.json"):
        data = json.loads(metadata.read_text(encoding="utf-8"))
        if not data.get("variantName", "").endswith("Debug"):
            continue
        for item in data.get("elements", []):
            name = item["outputFile"]
            if name in expected:
                apk = metadata.parent / name
                if name in packages or not apk.is_file():
                    raise RuntimeError(f"Duplicate or missing Debug APK: {name}")
                packages[name] = apk
    if set(packages) != expected:
        raise RuntimeError("Missing Debug APKs: " + ", ".join(sorted(expected - packages.keys())))
    return packages


# 构建并原样导出Gradle自动签名的Debug包。
def main():
    # 解析设备架构及Python运行时选项。
    options = build_options()
    abis = options.abis
    expected = {APK_NAMES[(mode, abi)] for mode in options.modes for abi in abis}
    if os.name != "nt":
        raise RuntimeError("Run build_32.bat, build_64.bat, build_32_tv.bat or build_64_tv.bat on Windows.")
    env = dict(os.environ)
    java_home = env.get("JAVA_HOME", "").strip('"')
    java = Path(java_home) / "bin" / "java.exe" if java_home else Path(shutil.which("java") or "")
    if not java.is_file():
        raise RuntimeError("JDK 17 or newer is required. Set JAVA_HOME to your JDK directory.")
    env["JAVA_HOME"] = str(java.parent.parent)
    # 查找本机安卓平台与签名工具。
    sdk, signer = find_sdk()
    env.update(ANDROID_HOME=str(sdk), ANDROID_SDK_ROOT=str(sdk))
    print("Build mode: Debug; signing is managed by Gradle.", flush=True)
    print(f"Python source support: {'enabled' if options.with_python else 'disabled'}", flush=True)
    # 按所选设备架构构建Debug并显式设置Python依赖。
    command = gradle_command(options)
    # 执行外部工具并保留失败退出码。
    run(command, env)
    # 按构建元数据只收集本次Debug安装包。
    packages = debug_apks(expected)
    OUTPUT.mkdir(exist_ok=True)
    # 汇总工具、脚本和构建选项指纹。
    cache = ReleaseCache(ROOT / '.gradle' / 'debug-build-cache', OUTPUT,
                         [Path(__file__), ROOT / 'scripts' / 'release_cache.py', java, signer],
                         {'build_type': 'Debug', 'with_python': options.with_python})
    with tempfile.TemporaryDirectory(prefix=".staging-", dir=OUTPUT) as staging:
        staged = Path(staging)
        prepared = []
        for name, source in sorted(packages.items()):
            # 反查安装包中的Python组件与构建选项一致。
            verify_python_contents(source, options.with_python)
            # 将原始安装包内容纳入缓存身份。
            fingerprint = cache.fingerprint(source)
            # 只复用内容一致且附属签名完整的产物。
            if cache.matches(name, fingerprint):
                print(f'Reusing verified Debug APK: {name}', flush=True)
                continue
            # 执行外部工具并保留失败退出码。
            run([java, "-jar", signer, "verify", "--verbose", "--print-certs", source], env)
            shutil.copyfile(source, staged / name)
            idsig = source.with_name(source.name + ".idsig")
            if idsig.is_file():
                shutil.copyfile(idsig, staged / idsig.name)
            # 原子记录已验签产物，发布后才能命中。
            cache.save(name, fingerprint, staged)
            prepared.append(name)
        for name in prepared:
            (staged / name).replace(OUTPUT / name)
            idsig = staged / (name + ".idsig")
            if idsig.exists():
                idsig.replace(OUTPUT / idsig.name)
            else:
                (OUTPUT / (name + '.idsig')).unlink(missing_ok=True)
    print(f"\nSUCCESS: {len(packages)} Gradle-signed Debug APKs are in {OUTPUT}", flush=True)
    for name in sorted(packages):
        apk = OUTPUT / name
        digest = hashlib.sha256(apk.read_bytes()).hexdigest()
        print(f"{name}  {apk.stat().st_size / 1048576:.2f} MiB  SHA256={digest}", flush=True)


if __name__ == "__main__":
    try:
        # 构建并原样导出Gradle自动签名的Debug包。
        main()
    except subprocess.CalledProcessError as error:
        print(f"Build failed (exit code {error.returncode}); no successful output was published.", file=sys.stderr)
        sys.exit(error.returncode or 1)
    except (OSError, ValueError, KeyError, RuntimeError) as error:
        print(f"ERROR: {error}", file=sys.stderr)
        sys.exit(1)
