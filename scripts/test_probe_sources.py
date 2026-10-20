"""Offline regression tests for source admission probes."""

import json
import unittest
from unittest.mock import patch
from urllib.parse import parse_qs, urlsplit

import probe_sources as probe


class ProbeSourcesTest(unittest.TestCase):

    # 验证直播探测不携带线路标签且保留独立请求头。
    def test_live_samples_preserve_headers_and_deduplicate_urls(self):
        text = '''#EXTM3U
#EXTINF:-1 http-user-agent="PlayerA",First
#EXTVLCOPT:http-referrer=https://example.org/
https://example.org/one.m3u8$Source A
#EXTINF:-1,Duplicate
https://example.org/one.m3u8$Source B
#EXTINF:-1,Second
#EXTHTTP:{"User-Agent":"PlayerB"}
https://example.org/two.m3u8
'''
        # 提取直播样本且保留每条线路声明的请求头。
        samples = probe.live_samples(text, 2)
        self.assertEqual(['https://example.org/one.m3u8', 'https://example.org/two.m3u8'], [sample['url'] for sample in samples])
        self.assertEqual({'User-Agent': 'PlayerA', 'Referer': 'https://example.org/'}, samples[0]['headers'])
        self.assertEqual({'User-Agent': 'PlayerB'}, samples[1]['headers'])

    # 校验序号推导和显式向量均可解密真实分片。
    def test_aes_segments_use_sequence_or_explicit_iv_and_headers(self):
        try:
            from Crypto.Cipher import AES
        except ImportError:
            self.skipTest('pycryptodome not installed')
        key = b'0123456789abcdef'
        transport = bytearray(188 * 8)
        for offset in range(0, len(transport), 188):
            transport[offset] = 0x47
        headers = {'User-Agent': 'DeclaredPlayer', 'Referer': 'https://example.org/'}
        for explicit in (False, True):
            with self.subTest(explicit=explicit):
                iv = 41 if explicit else 7
                ciphertext = AES.new(key, AES.MODE_CBC, iv.to_bytes(16, 'big')).encrypt(bytes(transport))
                declaration = ',IV=0x29' if explicit else ''
                manifest = ('#EXTM3U\n#EXT-X-KEY:METHOD=AES-128,URI="key"' + declaration + '\n#EXT-X-MEDIA-SEQUENCE:7\n#EXTINF:5,\nsegment.ts\n').encode()
                seen = []

                # 模拟媒体链路并记录请求头与密钥读取范围。
                def fake_fetch(url, limit=probe.LIMIT, headers=None):
                    seen.append((url, limit, headers))
                    body = {'https://example.org/live.m3u8': manifest,
                            'https://example.org/segment.ts': ciphertext,
                            'https://example.org/key': key}[url]
                    return body, url, 200

                # 模拟媒体链路并记录请求头与密钥读取范围。
                with patch.object(probe, 'fetch', side_effect=fake_fetch):
                    # 检查媒体清单并验证明文或解密后分片。
                    result = probe.media('https://example.org/live.m3u8', headers)
                self.assertTrue(result['encrypted'])
                self.assertEqual(len(transport), result['media_bytes'])
                self.assertTrue(all(request[2] == headers for request in seen))
                self.assertEqual(17, seen[-1][1])

    # 拒绝忽略搜索词却返回成功的采集接口。
    def test_candidate_rejects_ignored_search_keyword(self):
        item = {'vod_id': 1, 'vod_name': 'Example'}
        response = json.dumps({'list': [item]}).encode()
        with patch.object(probe, 'fetch', return_value=(response, 'https://example.org/api', 200)) as fetch:
            with self.assertRaisesRegex(ValueError, 'search keyword ignored'):
                # 验证候选接口搜索语义并抽样多个影片。
                probe.vod_candidate({'api': 'https://example.org/api'}, 3)
        self.assertEqual(3, fetch.call_count)

    # 首片失效时继续检验后续影片且校验详情编号。
    def test_candidate_checks_multiple_titles_after_first_media_fails(self):
        listing = [{'vod_id': 1, 'vod_name': 'Example'}, {'vod_id': 2, 'vod_name': 'Second'}]
        requests = []
        transport = b'\x47' + bytes(187) + b'\x47' + bytes(187) + b'\x47' + bytes(187)

        # 模拟搜索详情和第二部影片的实际媒体数据。
        def fake_fetch(url, limit=probe.LIMIT, headers=None):
            requests.append(url)
            parts = urlsplit(url)
            query = parse_qs(parts.query)
            if parts.path == '/api':
                self.assertEqual(['keep'], query['token'])
                if 'wd' in query:
                    data = {'list': [] if query['wd'][0].startswith('__pwztv_missing') else listing}
                elif 'ids' in query:
                    identity = int(query['ids'][0])
                    data = {'list': [{'vod_id': identity, 'vod_play_url': f'episode$https://example.org/{identity}.m3u8'}]}
                else:
                    data = {'list': listing}
                return json.dumps(data).encode(), url, 200
            if parts.path == '/1.m3u8':
                return b'<html>unavailable</html>', url, 200
            if parts.path == '/2.m3u8':
                return b'#EXTM3U\n#EXTINF:5,\nsegment.ts\n', url, 200
            return transport, url, 200

        # 模拟搜索详情和第二部影片的实际媒体数据。
        with patch.object(probe, 'fetch', side_effect=fake_fetch):
            # 验证候选接口搜索语义并抽样多个影片。
            result = probe.vod_candidate({'api': 'https://example.org/api?token=keep'}, 3)
        self.assertTrue(result['search_ok'])
        self.assertTrue(result['media_ok'])
        self.assertEqual([False, True], [item['ok'] for item in result['samples']])
        self.assertTrue(any('ids=2' in request for request in requests))

    # 拒绝无效媒体和不支持的加密而非误报成功。
    def test_rejects_invalid_media_and_unsupported_encryption(self):
        cases = [
            (b'#EXTM3U\n#EXT-X-KEY:METHOD=SAMPLE-AES,URI="key"\nsegment.ts\n', 'unsupported HLS encryption'),
            (b'#EXTM3U\n#EXTINF:5,\nsegment.ts\n', 'invalid media data'),
        ]
        for manifest, message in cases:
            with self.subTest(message=message):
                with patch.object(probe, 'fetch', side_effect=[(manifest, 'https://example.org/live.m3u8', 200), (b'not video', 'https://example.org/segment.ts', 200)]):
                    with self.assertRaisesRegex(ValueError, message):
                        # 检查媒体清单并验证明文或解密后分片。
                        probe.media('https://example.org/live.m3u8')


if __name__ == '__main__':
    unittest.main()
