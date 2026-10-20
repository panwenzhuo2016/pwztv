"""Offline checks for architecture, runtime selection and APK content admission."""

from pathlib import Path
import tempfile
import unittest
import zipfile

import build_all as build


class BuildAllTest(unittest.TestCase):

    # 验证默认只选两种64位包且明确关闭Python。
    def test_default_build_excludes_python_and_32bit(self):
        # 解析参数并选择架构与Python支持。
        options = build.build_options([])
        self.assertEqual(('arm64_v8a',), options.abis)
        self.assertFalse(options.with_python)
        # 显式选择Python依赖避免复用其他构建设置。
        command = build.gradle_command(options)
        self.assertIn('-PwithPython=false', command)
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
