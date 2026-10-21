"""Offline checks for architecture, runtime selection and APK content admission."""

from pathlib import Path
from contextlib import ExitStack, redirect_stdout
import io
import subprocess
import tempfile
from types import SimpleNamespace
import unittest
from unittest.mock import patch
import zipfile

import build_all as build


class BuildAllTest(unittest.TestCase):

    # 反查四个入口仅构建发布所选包并保留其他产物。
    def test_each_entry_builds_and_publishes_only_its_selected_apk(self):
        entries = [('build_32.bat', '32', 'mobile', 'p32.apk', 'MobileArmeabi_v7a'),
                   ('build_64.bat', '64', 'mobile', 'p.apk', 'MobileArm64_v8a'),
                   ('build_32_tv.bat', '32', 'leanback', 'p-tv32.apk', 'LeanbackArmeabi_v7a'),
                   ('build_64_tv.bat', '64', 'leanback', 'p-tv.apk', 'LeanbackArm64_v8a')]
        for entry, abi, mode, name, variant in entries:
            with self.subTest(entry=entry), tempfile.TemporaryDirectory() as directory, ExitStack() as stack:
                wrapper = (build.ROOT / entry).read_bytes()
                self.assertFalse(wrapper.startswith(b'\xef\xbb\xbf'))
                command = next(line for line in wrapper.decode('ascii').splitlines() if line.startswith('py -3.10 "%~dp0'))
                arguments = command.split(' %* ', 1)[1].split()
                self.assertEqual(['--abi', abi, '--mode', mode], arguments)
                # 解析设备架构及Python运行时选项。
                options = build.build_options(arguments)
                root = Path(directory)
                sdk = root / 'sdk'
                java = root / 'jdk' / 'bin' / 'java.exe'
                signer = root / 'apksigner.jar'
                for tool in (java, signer, root / 'scripts' / 'release_cache.py'):
                    tool.parent.mkdir(parents=True, exist_ok=True)
                    tool.write_bytes(b'fixture tool')
                for (device, architecture), apk_name in build.APK_NAMES.items():
                    folder = root / 'app' / 'build' / 'outputs' / 'apk' / device / architecture
                    folder.mkdir(parents=True)
                    with zipfile.ZipFile(folder / apk_name, 'w') as archive:
                        archive.writestr('classes.dex', b'fresh selected APK')
                        archive.writestr('assets/libmitv.so', b'fixture asset' * 100)
                    (folder / 'output-metadata.json').write_text(build.json.dumps({
                        'variantName': device + architecture.capitalize() + 'Debug',
                        'elements': [{'outputFile': apk_name}],
                    }), encoding='utf-8')
                    release = folder / 'release'
                    release.mkdir()
                    (release / apk_name).write_bytes(b'old Release APK must not be exported')
                    (release / 'output-metadata.json').write_text(build.json.dumps({
                        'variantName': device + architecture.capitalize() + 'Release',
                        'elements': [{'outputFile': apk_name}],
                    }), encoding='utf-8')
                output = root / 'Release'
                output.mkdir()
                for apk_name in build.APK_NAMES.values():
                    (output / apk_name).write_bytes(b'old unselected APK')
                calls = []

                # 仅记录外部调用，保留原样复制与校验流程。
                def fake_run(command, env):
                    calls.append(command)

                for attribute, value in (('ROOT', root), ('OUTPUT', output),
                                         ('os', SimpleNamespace(name='nt', environ={'JAVA_HOME': str(java.parent.parent)}))):
                    stack.enter_context(patch.object(build, attribute, value))
                stack.enter_context(patch.object(build, 'build_options', return_value=options))
                stack.enter_context(patch.object(build, 'find_sdk', return_value=(sdk, signer)))
                stack.enter_context(patch.object(build, 'run', side_effect=fake_run))
                stack.enter_context(redirect_stdout(io.StringIO()))
                # 构建并原样导出Gradle自动签名的Debug包。
                build.main()
                self.assertEqual([f':app:assemble{variant}Debug'],
                                 [part for part in calls[0] if str(part).startswith(':app:')])
                with zipfile.ZipFile(output / name) as archive:
                    self.assertEqual(b'fresh selected APK', archive.read('classes.dex'))
                self.assertEqual((output / name).read_bytes(), (root / 'app' / 'build' / 'outputs' / 'apk' / mode / options.abis[0] / name).read_bytes())
                self.assertFalse((root / '.signing').exists())
                self.assertTrue(all('sign' not in call and '-genkeypair' not in call for call in calls))
                for apk_name in build.APK_NAMES.values():
                    if apk_name != name:
                        self.assertEqual(b'old unselected APK', (output / apk_name).read_bytes())

    # 反查发布内容，验证复用、失效和失败保护。
    def test_repeated_build_reuses_only_unchanged_verified_outputs(self):
        with tempfile.TemporaryDirectory() as directory, ExitStack() as stack:
            root = Path(directory)
            sdk = root / 'sdk'
            java = root / 'jdk' / 'bin' / 'java.exe'
            signer = root / 'apksigner.jar'
            for path in (java, signer, root / 'scripts' / 'release_cache.py'):
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_bytes(b'fixture tool or key')
            sources = {name: root / name for name in ('p.apk', 'p-tv.apk')}
            for path in sources.values():
                with zipfile.ZipFile(path, 'w') as archive:
                    archive.writestr('classes.dex', b'fixture')
                    archive.writestr('assets/libmitv.so', b'fixture asset' * 100)
            for source in sources.values():
                source.with_name(source.name + '.idsig').write_bytes(b'fixture v4')
            state = {'verified': 0, 'fail_verify': False}

            # 模拟验签并验证失败时不发布任何产物。
            def fake_run(command, env):
                if len(command) > 3 and command[3] == 'verify':
                    if state['fail_verify'] and command[-1].name == 'p.apk':
                        raise subprocess.CalledProcessError(1, ['fixture verify'])
                    state['verified'] += 1

            # 解析设备架构及Python运行时选项。
            options = build.build_options([])
            output = root / 'Release'
            for attribute, value in (('ROOT', root), ('OUTPUT', output),
                                     ('os', SimpleNamespace(name='nt', environ={'JAVA_HOME': str(java.parent.parent)}))):
                stack.enter_context(patch.object(build, attribute, value))
            stack.enter_context(patch.object(build, 'build_options', return_value=options))
            stack.enter_context(patch.object(build, 'find_sdk', return_value=(sdk, signer)))
            stack.enter_context(patch.object(build, 'debug_apks', return_value=sources))
            stack.enter_context(patch.object(build, 'run', side_effect=fake_run))
            messages = stack.enter_context(redirect_stdout(io.StringIO()))
            # 构建并原样导出Gradle自动签名的Debug包。
            build.main()
            self.assertEqual(2, state['verified'])
            original = {path.name: (path.read_bytes(), path.stat().st_mtime_ns) for path in output.iterdir()}
            # 构建并原样导出Gradle自动签名的Debug包。
            build.main()
            self.assertEqual(2, state['verified'])
            self.assertIn('Reusing verified Debug APK: p.apk', messages.getvalue())
            for name, expected in original.items():
                actual = output / name
                self.assertEqual(expected, (actual.read_bytes(), actual.stat().st_mtime_ns))
            with zipfile.ZipFile(sources['p.apk'], 'a') as archive:
                archive.writestr('new.txt', b'updated source')
            # 构建并原样导出Gradle自动签名的Debug包。
            build.main()
            self.assertEqual(3, state['verified'])
            with zipfile.ZipFile(output / 'p.apk') as archive:
                self.assertEqual(b'updated source', archive.read('new.txt'))
            (output / 'p-tv.apk').write_bytes(b'corrupt release')
            # 构建并原样导出Gradle自动签名的Debug包。
            build.main()
            self.assertEqual(4, state['verified'])
            with zipfile.ZipFile(output / 'p-tv.apk') as archive:
                self.assertEqual(b'fixture', archive.read('classes.dex'))
            with zipfile.ZipFile(sources['p.apk'], 'a') as archive:
                archive.writestr('v4-disabled.txt', b'fixture without idsig')
            sources['p.apk'].with_name('p.apk.idsig').unlink()
            # 构建并原样导出Gradle自动签名的Debug包。
            build.main()
            self.assertEqual(5, state['verified'])
            self.assertFalse((output / 'p.apk.idsig').exists())
            with zipfile.ZipFile(output / 'p.apk') as archive:
                self.assertEqual(b'fixture without idsig', archive.read('v4-disabled.txt'))
            # 构建并原样导出Gradle自动签名的Debug包。
            build.main()
            self.assertEqual(5, state['verified'])
            published = {path.name: path.read_bytes() for path in output.iterdir()}
            for path in sources.values():
                with zipfile.ZipFile(path, 'a') as archive:
                    archive.writestr('fail.txt', b'not published')
            state['fail_verify'] = True
            with self.assertRaises(subprocess.CalledProcessError):
                # 构建并原样导出Gradle自动签名的Debug包。
                build.main()
            self.assertEqual(published, {path.name: path.read_bytes() for path in output.iterdir()})

    # 验证默认只选两种64位包且明确关闭Python。
    def test_default_build_excludes_python_and_32bit(self):
        # 解析设备架构及Python运行时选项。
        options = build.build_options([])
        self.assertEqual(('arm64_v8a',), options.abis)
        self.assertFalse(options.with_python)
        # 按所选设备架构构建Debug并显式设置Python依赖。
        command = build.gradle_command(options)
        self.assertIn('-PwithPython=false', command)
        self.assertIn('--configuration-cache', command)
        self.assertNotIn('--no-configuration-cache', command)
        self.assertEqual({':app:assembleMobileArm64_v8aDebug', ':app:assembleLeanbackArm64_v8aDebug'},
                         {part for part in command if part.startswith(':app:')})

    # 验证Python开关与32位参数可独立组合。
    def test_optional_python_can_be_combined_with_32bit(self):
        for arguments, count in [(['--with-python'], 2), (['--include-32bit', '--with-python', '--no-pause'], 4)]:
            with self.subTest(arguments=arguments):
                # 解析设备架构及Python运行时选项。
                options = build.build_options(arguments)
                # 按所选设备架构构建Debug并显式设置Python依赖。
                command = build.gradle_command(options)
                self.assertTrue(options.with_python)
                self.assertIn('-PwithPython=true', command)
                self.assertIn('--no-configuration-cache', command)
                self.assertNotIn('--configuration-cache', command)
                self.assertEqual(count, sum(part.startswith(':app:') for part in command))
        # 解析设备架构及Python运行时选项。
        options = build.build_options(['--include-32bit'])
        # 按所选设备架构构建Debug并显式设置Python依赖。
        self.assertIn('-PwithPython=false', build.gradle_command(options))

    # 反查ZIP内容以拒绝运行时误带或漏带。
    def test_checks_written_apk_contents_against_selected_runtime(self):
        cases = [([], False, True), ([], True, False),
                 (['assets/chaquopy/app.imy'], False, False),
                 (['assets/chaquopy/app.imy'], True, False),
                 (['lib/arm64-v8a/libcrypto_python.so'], False, False),
                 (['assets/chaquopy/app.imy', 'lib/arm64-v8a/libpython3.10.so'], True, True)]
        with tempfile.TemporaryDirectory() as directory:
            apk = Path(directory) / 'sample.apk'
            for names, enabled, valid in cases:
                with self.subTest(names=names, enabled=enabled):
                    with zipfile.ZipFile(apk, 'w') as archive:
                        archive.writestr('classes.dex', b'test fixture')
                        for name in names:
                            archive.writestr(name, b'test fixture')
                    with zipfile.ZipFile(apk) as archive:
                        self.assertEqual(set(names + ['classes.dex']), set(archive.namelist()))
                    if valid:
                        # 反查安装包中的Python组件与构建选项一致。
                        build.verify_python_contents(apk, enabled)
                    else:
                        with self.assertRaises(RuntimeError):
                            # 反查安装包中的Python组件与构建选项一致。
                            build.verify_python_contents(apk, enabled)


if __name__ == '__main__':
    unittest.main()
