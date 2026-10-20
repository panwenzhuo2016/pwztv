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


class BuildAllTest(unittest.TestCase):

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

            # 解析参数并选择架构与Python支持。
            options = build.build_options([])
            output = root / 'Release'
            for attribute, value in (('ROOT', root), ('OUTPUT', output),
                                     ('os', SimpleNamespace(name='nt', environ={'JAVA_HOME': str(java.parent.parent)}))):
                stack.enter_context(patch.object(build, attribute, value))
            stack.enter_context(patch.object(build, 'build_options', return_value=options))
            stack.enter_context(patch.object(build, 'find_sdk', return_value=(sdk, signer)))
            stack.enter_context(patch.object(build, 'signing_environment', return_value=(keystore, 'release', {})))
            stack.enter_context(patch.object(build, 'release_apks', return_value=sources))
            stack.enter_context(patch.object(build, 'run', side_effect=fake_run))
            messages = stack.enter_context(redirect_stdout(io.StringIO()))
            # 构建并签名校验所选安卓发布包。
            build.main()
            self.assertEqual(2, state['signed'])
            original = {path.name: (path.read_bytes(), path.stat().st_mtime_ns) for path in output.iterdir()}
            # 构建并签名校验所选安卓发布包。
            build.main()
            self.assertEqual(2, state['signed'])
            self.assertIn('Reusing verified release: p.apk', messages.getvalue())
            for name, expected in original.items():
                actual = output / name
                self.assertEqual(expected, (actual.read_bytes(), actual.stat().st_mtime_ns))
            with zipfile.ZipFile(sources['p.apk'], 'a') as archive:
                archive.writestr('new.txt', b'updated source')
            # 构建并签名校验所选安卓发布包。
            build.main()
            self.assertEqual(3, state['signed'])
            with zipfile.ZipFile(output / 'p.apk') as archive:
                self.assertEqual(b'updated source', archive.read('new.txt'))
            (output / 'p-tv.apk').write_bytes(b'corrupt release')
            # 构建并签名校验所选安卓发布包。
            build.main()
            self.assertEqual(4, state['signed'])
            with zipfile.ZipFile(output / 'p-tv.apk') as archive:
                self.assertEqual(b'fixture', archive.read('classes.dex'))
            with zipfile.ZipFile(sources['p.apk'], 'a') as archive:
                archive.writestr('v4-disabled.txt', b'fixture without idsig')
            state['idsig'] = False
            # 构建并签名校验所选安卓发布包。
            build.main()
            self.assertEqual(5, state['signed'])
            self.assertFalse((output / 'p.apk.idsig').exists())
            with zipfile.ZipFile(output / 'p.apk') as archive:
                self.assertEqual(b'fixture without idsig', archive.read('v4-disabled.txt'))
            # 构建并签名校验所选安卓发布包。
            build.main()
            self.assertEqual(5, state['signed'])
            published = {path.name: path.read_bytes() for path in output.iterdir()}
            for path in sources.values():
                with zipfile.ZipFile(path, 'a') as archive:
                    archive.writestr('fail.txt', b'not published')
            state['fail_verify'] = True
            with self.assertRaises(subprocess.CalledProcessError):
                # 构建并签名校验所选安卓发布包。
                build.main()
            self.assertEqual(published, {path.name: path.read_bytes() for path in output.iterdir()})

    # 验证默认只选两种64位包且明确关闭Python。
    def test_default_build_excludes_python_and_32bit(self):
        # 解析参数并选择架构与Python支持。
        options = build.build_options([])
        self.assertEqual(('arm64_v8a',), options.abis)
        self.assertFalse(options.with_python)
        # 显式选择Python依赖避免复用其他构建设置。
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
                # 解析参数并选择架构与Python支持。
                options = build.build_options(arguments)
                # 显式选择Python依赖避免复用其他构建设置。
                command = build.gradle_command(options)
                self.assertTrue(options.with_python)
                self.assertIn('-PwithPython=true', command)
                self.assertIn('--no-configuration-cache', command)
                self.assertNotIn('--configuration-cache', command)
                self.assertEqual(count, sum(part.startswith(':app:') for part in command))
        # 解析参数并选择架构与Python支持。
        options = build.build_options(['--include-32bit'])
        # 显式选择Python依赖避免复用其他构建设置。
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
