"""Offline checks for architecture, runtime selection and APK content admission."""

from pathlib import Path
from contextlib import ExitStack, redirect_stdout
import io
import shutil
import subprocess
import tempfile
from types import SimpleNamespace
import unittest
from unittest.mock import patch
import zipfile

import build_all as build


STAMP = '20261030_153045'


class BuildAllTest(unittest.TestCase):
    # 固定导出时间便于反查四种安装包和缓存。
    def setUp(self):
        clock = patch.object(build, 'datetime')
        self.clock = clock.start()
        self.clock.now.return_value.strftime.return_value = STAMP
        self.addCleanup(clock.stop)


    # 反查两种构建目录缓存签名和产物互不影响。
    def test_debug_and_release_outputs_remain_separate_in_both_directions(self):
        wrapper = (build.ROOT / 'build_all_debug.bat').read_bytes()
        self.assertFalse(wrapper.startswith(b'\xef\xbb\xbf'))
        line = next(line for line in wrapper.decode('ascii').splitlines() if line.startswith('py -3.10 "%~dp0'))
        # 解析设备架构构建类型及Python选项。
        debug_options = build.build_options(line.split(' %* ', 1)[1].split())
        self.assertTrue(debug_options.debug)
        release_wrapper = (build.ROOT / 'build_all.bat').read_text(encoding='ascii')
        line = next(line for line in release_wrapper.splitlines() if line.startswith('py -3.10 "%~dp0'))
        # 解析设备架构构建类型及Python选项。
        release_options = build.build_options(line.split(' %* ', 1)[1].split())
        self.assertFalse(release_options.debug)
        with tempfile.TemporaryDirectory() as directory, ExitStack() as stack:
            root = Path(directory)
            sdk = root / 'sdk'
            aligner = sdk / 'build-tools' / '35.0.0' / 'zipalign.exe'
            java = root / 'jdk' / 'bin' / 'java.exe'
            signer = root / 'apksigner.jar'
            keystore = root / 'release.jks'
            for tool in (aligner, java, java.with_name('keytool.exe'), signer, keystore,
                         root / 'scripts' / 'apk_size.py', root / 'scripts' / 'release_cache.py'):
                tool.parent.mkdir(parents=True, exist_ok=True)
                tool.write_bytes(b'fixture tool')
            debug_sources = {}
            for (mode, abi), name in build.APK_NAMES.items():
                for kind in ('Debug', 'Release'):
                    folder = root / 'app' / 'build' / 'outputs' / 'apk' / mode / abi / kind
                    folder.mkdir(parents=True)
                    with zipfile.ZipFile(folder / name, 'w') as archive:
                        archive.writestr('classes.dex', kind.encode())
                        archive.writestr('assets/libmitv.so', b'fixture asset' * 100)
                    (folder / 'output-metadata.json').write_text(build.json.dumps({
                        'variantName': mode + abi.capitalize() + kind,
                        'elements': [{'outputFile': name}],
                    }), encoding='utf-8')
                    if kind == 'Debug':
                        debug_sources[name] = folder / name
            output = root / 'Release'
            output.mkdir()
            for name in build.APK_NAMES.values():
                (output / build.export_name(name, STAMP)).write_bytes(b'existing Release output')
            calls = []

            # 模拟外部工具并保留实际压缩复制发布流程。
            def fake_run(command, env):
                calls.append(command)
                if command[0] == aligner and '-f' in command:
                    shutil.copyfile(command[-2], command[-1])
                elif len(command) > 3 and command[3] == 'sign':
                    shutil.copyfile(command[-1], command[command.index('--out') + 1])

            for attribute, value in (('ROOT', root), ('OUTPUT', output),
                                     ('os', SimpleNamespace(name='nt', environ={'JAVA_HOME': str(java.parent.parent)}))):
                stack.enter_context(patch.object(build, attribute, value))
            options = stack.enter_context(patch.object(build, 'build_options', return_value=debug_options))
            stack.enter_context(patch.object(build, 'find_sdk', return_value=(sdk, signer)))
            signing = stack.enter_context(patch.object(build, 'signing_environment', return_value=(keystore, 'release', {})))
            stack.enter_context(patch.object(build, 'run', side_effect=fake_run))
            stack.enter_context(redirect_stdout(io.StringIO()))
            # 按构建类型验签并导出到独立目录。
            build.main()
            signing.assert_not_called()
            self.assertEqual(4, len([task for task in calls[0] if str(task).startswith(':app:')]))
            self.assertTrue(all('sign' not in call and '-genkeypair' not in call for call in calls))
            for name, source in debug_sources.items():
                self.assertEqual(source.read_bytes(), (output / 'debug' / build.export_name(name, STAMP)).read_bytes())
                self.assertEqual(b'existing Release output', (output / build.export_name(name, STAMP)).read_bytes())
            debug_snapshot = {name: (output / 'debug' / build.export_name(name, STAMP)).read_bytes()
                              for name in build.APK_NAMES.values()}
            options.return_value = release_options
            # 按构建类型验签并导出到独立目录。
            build.main()
            signing.assert_called_once()
            for name in build.APK_NAMES.values():
                with zipfile.ZipFile(output / build.export_name(name, STAMP)) as archive:
                    self.assertEqual(b'Release', archive.read('classes.dex'))
                    self.assertEqual(zipfile.ZIP_DEFLATED, archive.getinfo('assets/libmitv.so').compress_type)
                self.assertEqual(debug_snapshot[name], (output / 'debug' / build.export_name(name, STAMP)).read_bytes())
            release_snapshot = {name: (output / build.export_name(name, STAMP)).read_bytes()
                                for name in build.APK_NAMES.values()}
            options.return_value = debug_options
            # 按构建类型验签并导出到独立目录。
            build.main()
            for name in build.APK_NAMES.values():
                self.assertEqual(release_snapshot[name], (output / build.export_name(name, STAMP)).read_bytes())
                self.assertEqual(debug_snapshot[name], (output / 'debug' / build.export_name(name, STAMP)).read_bytes())
            self.assertTrue((root / '.gradle' / 'debug-build-cache').is_dir())
            self.assertTrue((root / '.gradle' / 'release-cache').is_dir())

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
                # 解析设备架构构建类型及Python选项。
                options = build.build_options(arguments)
                root = Path(directory)
                sdk = root / 'sdk'
                aligner = sdk / 'build-tools' / '35.0.0' / 'zipalign.exe'
                java = root / 'jdk' / 'bin' / 'java.exe'
                signer = root / 'apksigner.jar'
                keystore = root / 'release.jks'
                for tool in (aligner, java, java.with_name('keytool.exe'), signer, keystore,
                             root / 'scripts' / 'apk_size.py', root / 'scripts' / 'release_cache.py'):
                    tool.parent.mkdir(parents=True, exist_ok=True)
                    tool.write_bytes(b'fixture tool')
                for (device, architecture), apk_name in build.APK_NAMES.items():
                    folder = root / 'app' / 'build' / 'outputs' / 'apk' / device / architecture
                    folder.mkdir(parents=True)
                    with zipfile.ZipFile(folder / apk_name, 'w') as archive:
                        archive.writestr('classes.dex', b'fresh selected APK')
                        archive.writestr('assets/libmitv.so', b'fixture asset' * 100)
                    (folder / 'output-metadata.json').write_text(build.json.dumps({
                        'variantName': device + architecture.capitalize() + 'Release',
                        'elements': [{'outputFile': apk_name}],
                    }), encoding='utf-8')
                output = root / 'Release'
                output.mkdir()
                for apk_name in build.APK_NAMES.values():
                    (output / apk_name).write_bytes(b'old unselected APK')
                calls = []

                # 模拟工具输出并保留实际压缩校验发布流程。
                def fake_run(command, env):
                    calls.append(command)
                    if command[0] == aligner and '-f' in command:
                        shutil.copyfile(command[-2], command[-1])
                    elif len(command) > 3 and command[3] == 'sign':
                        shutil.copyfile(command[-1], command[command.index('--out') + 1])

                for attribute, value in (('ROOT', root), ('OUTPUT', output),
                                         ('os', SimpleNamespace(name='nt', environ={'JAVA_HOME': str(java.parent.parent)}))):
                    stack.enter_context(patch.object(build, attribute, value))
                stack.enter_context(patch.object(build, 'build_options', return_value=options))
                stack.enter_context(patch.object(build, 'find_sdk', return_value=(sdk, signer)))
                stack.enter_context(patch.object(build, 'signing_environment', return_value=(keystore, 'release', {})))
                stack.enter_context(patch.object(build, 'run', side_effect=fake_run))
                stack.enter_context(redirect_stdout(io.StringIO()))
                # 按构建类型验签并导出到独立目录。
                build.main()
                self.assertEqual([f':app:assemble{variant}Release'],
                                 [part for part in calls[0] if str(part).startswith(':app:')])
                with zipfile.ZipFile(output / build.export_name(name, STAMP)) as archive:
                    self.assertEqual(b'fresh selected APK', archive.read('classes.dex'))
                for apk_name in build.APK_NAMES.values():
                    if apk_name != name:
                        self.assertEqual(b'old unselected APK', (output / apk_name).read_bytes())

    # 反查发布内容，验证复用、失效和失败保护。
    def test_repeated_build_reuses_only_unchanged_verified_outputs(self):
        with tempfile.TemporaryDirectory() as directory, ExitStack() as stack:
            root = Path(directory)
            sdk = root / 'sdk'
            aligner = sdk / 'build-tools' / '35.0.0' / 'zipalign.exe'
            java = root / 'jdk' / 'bin' / 'java.exe'
            signer = root / 'apksigner.jar'
            keystore = root / 'release.jks'
            for path in (java, java.with_name('keytool.exe'), signer, aligner, keystore,
                         root / 'scripts' / 'apk_size.py', root / 'scripts' / 'release_cache.py'):
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_bytes(b'fixture tool or key')
            sources = {name: root / name for name in ('p.apk', 'p-tv.apk')}
            for path in sources.values():
                with zipfile.ZipFile(path, 'w') as archive:
                    archive.writestr('classes.dex', b'fixture')
                    archive.writestr('assets/libmitv.so', b'fixture asset' * 100)
            state = {'signed': 0, 'fail_verify': False, 'idsig': True}

            # 模拟对齐和签名工具，仅处理临时测试产物。
            def fake_run(command, env):
                if command[0] == aligner and '-f' in command:
                    shutil.copyfile(command[-2], command[-1])
                elif len(command) > 3 and command[3] == 'sign':
                    destination = command[command.index('--out') + 1]
                    shutil.copyfile(command[-1], destination)
                    if state['idsig']:
                        destination.with_name(destination.name + '.idsig').write_bytes(b'fixture v4')
                    state['signed'] += 1
                elif len(command) > 3 and command[3] == 'verify' and state['fail_verify']:
                    if command[-1].name == 'p.apk':
                        raise subprocess.CalledProcessError(1, ['fixture verify'])

            # 解析设备架构构建类型及Python选项。
            options = build.build_options([])
            output = root / 'Release'
            for attribute, value in (('ROOT', root), ('OUTPUT', output),
                                     ('os', SimpleNamespace(name='nt', environ={'JAVA_HOME': str(java.parent.parent)}))):
                stack.enter_context(patch.object(build, attribute, value))
            stack.enter_context(patch.object(build, 'build_options', return_value=options))
            stack.enter_context(patch.object(build, 'find_sdk', return_value=(sdk, signer)))
            stack.enter_context(patch.object(build, 'signing_environment', return_value=(keystore, 'release', {})))
            stack.enter_context(patch.object(build, 'build_apks', return_value=sources))
            stack.enter_context(patch.object(build, 'run', side_effect=fake_run))
            messages = stack.enter_context(redirect_stdout(io.StringIO()))
            # 按构建类型验签并导出到独立目录。
            build.main()
            self.assertEqual(2, state['signed'])
            original = {path.name: path.read_bytes() for path in output.iterdir()}
            # 按构建类型验签并导出到独立目录。
            build.main()
            self.assertEqual(2, state['signed'])
            self.assertIn('Reusing verified Release: p.apk', messages.getvalue())
            for name, expected in original.items():
                actual = output / name
                self.assertEqual(expected, actual.read_bytes())
            self.clock.now.return_value.strftime.return_value = '20261030_153046'
            # 按构建类型验签并导出到独立目录。
            build.main()
            self.assertEqual(2, state['signed'])
            for name in sources:
                previous = output / build.export_name(name, STAMP)
                current = output / build.export_name(name, '20261030_153046')
                self.assertEqual(previous.read_bytes(), current.read_bytes())
                self.assertEqual(b'fixture v4', current.with_name(current.name + '.idsig').read_bytes())
            self.clock.now.return_value.strftime.return_value = STAMP
            with zipfile.ZipFile(sources['p.apk'], 'a') as archive:
                archive.writestr('new.txt', b'updated source')
            # 按构建类型验签并导出到独立目录。
            build.main()
            self.assertEqual(3, state['signed'])
            with zipfile.ZipFile(output / build.export_name('p.apk', STAMP)) as archive:
                self.assertEqual(b'updated source', archive.read('new.txt'))
            (root / '.gradle' / 'release-cache' / 'outputs' / 'p-tv.apk').write_bytes(b'corrupt release')
            # 按构建类型验签并导出到独立目录。
            build.main()
            self.assertEqual(4, state['signed'])
            with zipfile.ZipFile(output / build.export_name('p-tv.apk', STAMP)) as archive:
                self.assertEqual(b'fixture', archive.read('classes.dex'))
            with zipfile.ZipFile(sources['p.apk'], 'a') as archive:
                archive.writestr('v4-disabled.txt', b'fixture without idsig')
            state['idsig'] = False
            # 按构建类型验签并导出到独立目录。
            build.main()
            self.assertEqual(5, state['signed'])
            self.assertFalse((output / (build.export_name('p.apk', STAMP) + '.idsig')).exists())
            with zipfile.ZipFile(output / build.export_name('p.apk', STAMP)) as archive:
                self.assertEqual(b'fixture without idsig', archive.read('v4-disabled.txt'))
            # 按构建类型验签并导出到独立目录。
            build.main()
            self.assertEqual(5, state['signed'])
            published = {path.name: path.read_bytes() for path in output.iterdir()}
            for path in sources.values():
                with zipfile.ZipFile(path, 'a') as archive:
                    archive.writestr('fail.txt', b'not published')
            state['fail_verify'] = True
            with self.assertRaises(subprocess.CalledProcessError):
                # 按构建类型验签并导出到独立目录。
                build.main()
            self.assertEqual(published, {path.name: path.read_bytes() for path in output.iterdir()})

    # 验证默认只选两种64位包且明确关闭Python。
    def test_default_build_excludes_python_and_32bit(self):
        # 解析设备架构构建类型及Python选项。
        options = build.build_options([])
        self.assertEqual(('arm64_v8a',), options.abis)
        self.assertFalse(options.with_python)
        # 按构建类型选择任务并显式设置Python依赖。
        command = build.gradle_command(options)
        self.assertIn('-PwithPython=false', command)
        self.assertIn('--configuration-cache', command)
        self.assertNotIn('--no-configuration-cache', command)
        self.assertEqual({':app:assembleMobileArm64_v8aRelease', ':app:assembleLeanbackArm64_v8aRelease'},
                         {part for part in command if part.startswith(':app:')})

    # 验证Python开关与32位参数可独立组合。
    def test_optional_python_can_be_combined_with_32bit(self):
        for arguments, count in [(['--with-python'], 2), (['--include-32bit', '--with-python', '--no-pause'], 4)]:
            with self.subTest(arguments=arguments):
                # 解析设备架构构建类型及Python选项。
                options = build.build_options(arguments)
                # 按构建类型选择任务并显式设置Python依赖。
                command = build.gradle_command(options)
                self.assertTrue(options.with_python)
                self.assertIn('-PwithPython=true', command)
                self.assertIn('--no-configuration-cache', command)
                self.assertNotIn('--configuration-cache', command)
                self.assertEqual(count, sum(part.startswith(':app:') for part in command))
        # 解析设备架构构建类型及Python选项。
        options = build.build_options(['--include-32bit'])
        # 按构建类型选择任务并显式设置Python依赖。
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
