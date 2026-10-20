"""Verify cache invalidation and persisted release artifacts without building APKs."""

import json
from pathlib import Path
import tempfile
import unittest

from release_cache import ReleaseCache


class ReleaseCacheTest(unittest.TestCase):
    # 建立独立签名产物和缓存输入。
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name)
        self.output = self.root / 'Release'
        self.output.mkdir()
        self.source = self.root / 'unsigned.apk'
        self.source.write_bytes(b'unsigned APK')
        self.tool = self.root / 'tool'
        self.tool.write_bytes(b'tool v1')
        self.settings = {'alias': 'release', 'with_python': False}
        # 汇总工具、密钥、脚本和构建选项指纹。
        self.cache = ReleaseCache(self.root / 'cache', self.output, [self.tool], self.settings)
        # 将原始安装包内容纳入缓存身份。
        self.key = self.cache.fingerprint(self.source)
        self.staged = self.root / 'staged'
        self.staged.mkdir()
        (self.staged / 'p.apk').write_bytes(b'verified signed APK')
        (self.staged / 'p.apk.idsig').write_bytes(b'v4 signature')

    # 发布后反查持久化缓存，验证进程间复用。
    def test_saved_entry_only_matches_after_publication(self):
        # 只复用内容一致且附属签名完整的产物。
        self.assertFalse(self.cache.matches('p.apk', self.key))
        # 原子记录已验签产物，发布后才能命中。
        self.cache.save('p.apk', self.key, self.staged)
        # 只复用内容一致且附属签名完整的产物。
        self.assertFalse(self.cache.matches('p.apk', self.key))
        for path in self.staged.iterdir():
            (self.output / path.name).write_bytes(path.read_bytes())
        # 汇总工具、密钥、脚本和构建选项指纹。
        restored = ReleaseCache(self.root / 'cache', self.output, [self.tool], self.settings)
        # 只复用内容一致且附属签名完整的产物。
        self.assertTrue(restored.matches('p.apk', self.key))
        self.assertEqual(b'verified signed APK', (self.output / 'p.apk').read_bytes())
        record = json.loads((self.root / 'cache' / 'p.apk.json').read_text())
        self.assertEqual(self.key, record['fingerprint'])
        self.assertEqual({'', '.idsig'}, set(record['artifacts']))

    # 源包、工具和签名选项变化均拒绝旧缓存。
    def test_input_changes_invalidate_cache(self):
        # 原子记录已验签产物，发布后才能命中。
        self.cache.save('p.apk', self.key, self.staged)
        for path in self.staged.iterdir():
            (self.output / path.name).write_bytes(path.read_bytes())
        self.source.write_bytes(b'different APK')
        # 将原始安装包内容纳入缓存身份。
        changed = self.cache.fingerprint(self.source)
        # 只复用内容一致且附属签名完整的产物。
        self.assertFalse(self.cache.matches('p.apk', changed))
        self.source.write_bytes(b'unsigned APK')
        for settings in ({'alias': 'other', 'with_python': False},
                         {'alias': 'release', 'with_python': True}):
            # 汇总工具、密钥、脚本和构建选项指纹。
            cache = ReleaseCache(self.root / 'cache', self.output, [self.tool], settings)
            # 将原始安装包内容纳入缓存身份。
            changed = cache.fingerprint(self.source)
            # 只复用内容一致且附属签名完整的产物。
            self.assertFalse(cache.matches('p.apk', changed))
        self.tool.write_bytes(b'tool v2')
        # 汇总工具、密钥、脚本和构建选项指纹。
        cache = ReleaseCache(self.root / 'cache', self.output, [self.tool], self.settings)
        # 将原始安装包内容纳入缓存身份。
        changed = cache.fingerprint(self.source)
        # 只复用内容一致且附属签名完整的产物。
        self.assertFalse(cache.matches('p.apk', changed))

    # 输出被篡改、删除或缓存损坏时安全重建。
    def test_missing_or_changed_outputs_and_invalid_records_miss(self):
        # 原子记录已验签产物，发布后才能命中。
        self.cache.save('p.apk', self.key, self.staged)
        for path in self.staged.iterdir():
            (self.output / path.name).write_bytes(path.read_bytes())
        for name in ('p.apk', 'p.apk.idsig'):
            path = self.output / name
            original = path.read_bytes()
            path.write_bytes(b'x' * len(original))
            # 只复用内容一致且附属签名完整的产物。
            self.assertFalse(self.cache.matches('p.apk', self.key))
            path.unlink()
            # 只复用内容一致且附属签名完整的产物。
            self.assertFalse(self.cache.matches('p.apk', self.key))
            path.write_bytes(original)
        for invalid in ('{', 'null', '[]', '{}', '{"fingerprint":null}'):
            (self.root / 'cache' / 'p.apk.json').write_text(invalid)
            # 只复用内容一致且附属签名完整的产物。
            self.assertFalse(self.cache.matches('p.apk', self.key))

    # 无附属签名也可复用，出现旧签名则重建。
    def test_optional_idsig_must_match_its_recorded_absence(self):
        (self.staged / 'p.apk.idsig').unlink()
        # 原子记录已验签产物，发布后才能命中。
        self.cache.save('p.apk', self.key, self.staged)
        (self.output / 'p.apk').write_bytes((self.staged / 'p.apk').read_bytes())
        # 只复用内容一致且附属签名完整的产物。
        self.assertTrue(self.cache.matches('p.apk', self.key))
        (self.output / 'p.apk.idsig').write_bytes(b'stale signature')
        # 只复用内容一致且附属签名完整的产物。
        self.assertFalse(self.cache.matches('p.apk', self.key))


if __name__ == '__main__':
    unittest.main()
