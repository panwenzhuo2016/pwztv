"""Check asset compression, payload integrity and native-library alignment."""

from pathlib import Path
import struct
import subprocess
import tempfile
import unittest
import zipfile

import apk_size

ALIGNER = Path(__file__).resolve().parents[1] / '.android-sdk/build-tools/35.0.0/zipalign.exe'


class ApkSizeTest(unittest.TestCase):

    # 生成包含资源表和原生库的混合压缩测试包。
    def fixture(self, directory):
        source = Path(directory) / 'source.zip'
        with zipfile.ZipFile(source, 'w') as archive:
            archive.writestr(apk_size.MITV_ASSET, b'MITV streaming asset' * 10000)
            archive.writestr('lib/arm64-v8a/libquickjs.so', b'native library' * 1000)
            archive.writestr('resources.arsc', b'resources' * 100)
            archive.writestr('assets/world.m3u', b'international channels' * 100,
                             compress_type=zipfile.ZIP_DEFLATED)
            archive.writestr('classes.dex', b'dex' * 100, compress_type=zipfile.ZIP_DEFLATED)
        return source

    # 反查所有资源完整且仅目标条目改为压缩。
    def test_compression_preserves_payloads_and_native_storage(self):
        with tempfile.TemporaryDirectory() as directory:
            # 生成包含资源表和原生库的混合压缩测试包。
            source = self.fixture(directory)
            destination = Path(directory) / 'compressed.zip'
            # 仅压缩流式读取的MITV资源并保留其他条目属性。
            apk_size.compress_mitv(source, destination)
            # 反查对齐后的全部内容与非目标压缩方式。
            apk_size.verify_contents(source, destination)
            with zipfile.ZipFile(source) as original, zipfile.ZipFile(destination) as output:
                self.assertIsNone(output.testzip())
                for entry in original.infolist():
                    self.assertEqual(original.read(entry), output.read(entry.filename))
                mitv = output.getinfo(apk_size.MITV_ASSET)
                self.assertLess(mitv.compress_size, mitv.file_size)
                self.assertEqual(zipfile.ZIP_STORED, output.getinfo('lib/arm64-v8a/libquickjs.so').compress_type)
                self.assertEqual(zipfile.ZIP_STORED, output.getinfo('resources.arsc').compress_type)

    # 拒绝覆盖输入并保留原文件的内容。
    def test_rejects_in_place_rewrite(self):
        with tempfile.TemporaryDirectory() as directory:
            # 生成包含资源表和原生库的混合压缩测试包。
            source = self.fixture(directory)
            before = source.read_bytes()
            with self.assertRaises(ValueError):
                # 仅压缩流式读取的MITV资源并保留其他条目属性。
                apk_size.compress_mitv(source, source)
            self.assertEqual(before, source.read_bytes())

    # 拒绝漏带目标资产且不生成输出。
    def test_missing_asset_fails_before_writing(self):
        with tempfile.TemporaryDirectory() as directory:
            source, output = Path(directory) / 'empty.zip', Path(directory) / 'output.zip'
            with zipfile.ZipFile(source, 'w') as archive:
                archive.writestr('classes.dex', b'dex')
            with self.assertRaises(ValueError):
                # 仅压缩流式读取的MITV资源并保留其他条目属性。
                apk_size.compress_mitv(source, output)
            self.assertFalse(output.exists())

    # 内容即使CRC正确，被改写也必须校验失败。
    def test_payload_changes_are_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            # 生成包含资源表和原生库的混合压缩测试包。
            source = self.fixture(directory)
            destination = Path(directory) / 'modified.zip'
            with zipfile.ZipFile(source) as original, zipfile.ZipFile(destination, 'w') as output:
                for entry in original.infolist():
                    payload = original.read(entry)
                    if entry.filename == apk_size.MITV_ASSET:
                        entry.compress_type = zipfile.ZIP_DEFLATED
                    if entry.filename == 'assets/world.m3u':
                        payload = b'changed channels'
                    output.writestr(entry, payload)
            with self.assertRaisesRegex(RuntimeError, 'content changed'):
                # 反查对齐后的全部内容与非目标压缩方式。
                apk_size.verify_contents(source, destination)

    # 用真实安卓工具反查16KB对齐及压缩后内容。
    @unittest.skipUnless(ALIGNER.is_file(), 'Android zipalign is unavailable')
    def test_zipalign_preserves_compression_and_16kb_native_alignment(self):
        with tempfile.TemporaryDirectory() as directory:
            # 生成包含资源表和原生库的混合压缩测试包。
            source = self.fixture(directory)
            compressed, aligned = Path(directory) / 'compressed.zip', Path(directory) / 'aligned.zip'
            # 仅压缩流式读取的MITV资源并保留其他条目属性。
            apk_size.compress_mitv(source, compressed)
            subprocess.run([str(ALIGNER), '-P', '16', '-f', '4', str(compressed), str(aligned)], check=True)
            subprocess.run([str(ALIGNER), '-c', '-P', '16', '4', str(aligned)], check=True)
            # 反查对齐后的全部内容与非目标压缩方式。
            apk_size.verify_contents(source, aligned)
            with zipfile.ZipFile(aligned) as archive, aligned.open('rb') as raw:
                for entry in archive.infolist():
                    if entry.compress_type != zipfile.ZIP_STORED:
                        continue
                    raw.seek(entry.header_offset + 26)
                    name_size, extra_size = struct.unpack('<HH', raw.read(4))
                    offset = entry.header_offset + 30 + name_size + extra_size
                    alignment = 16384 if entry.filename.endswith('.so') else 4
                    self.assertEqual(0, offset % alignment, entry.filename)


if __name__ == '__main__':
    unittest.main()
