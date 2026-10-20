"""Reuse verified release outputs only while every input and artifact matches."""

import hashlib
import json
from pathlib import Path
import tempfile

from apk_size import stream_digest


# 分块校验文件内容，避免按时间戳误用缓存。
def file_digest(path):
    with Path(path).open('rb') as reader:
        # 分块计算内容哈希，避免大文件占用额外内存。
        return stream_digest(reader).hex()


class ReleaseCache:
    # 汇总工具、密钥、脚本和构建选项指纹。
    def __init__(self, directory, output, inputs, settings):
        self.directory = Path(directory)
        self.output = Path(output)
        # 分块校验文件内容，避免按时间戳误用缓存。
        files = {str(path): file_digest(path) for path in inputs}
        self.context = {'files': files, 'settings': settings}

    # 将原始安装包内容纳入缓存身份。
    def fingerprint(self, source):
        # 分块校验文件内容，避免按时间戳误用缓存。
        data = dict(self.context, source=file_digest(source))
        return hashlib.sha256(json.dumps(data, sort_keys=True).encode()).hexdigest()

    # 只复用内容一致且附属签名完整的产物。
    def matches(self, name, fingerprint):
        try:
            record = json.loads((self.directory / (name + '.json')).read_text(encoding='utf-8'))
            if record['fingerprint'] != fingerprint:
                return False
            for suffix in ('', '.idsig'):
                artifact = self.output / (name + suffix)
                expected = record['artifacts'][suffix]
                if expected is None:
                    if suffix == '' or artifact.exists():
                        return False
                # 分块校验文件内容，避免按时间戳误用缓存。
                elif file_digest(artifact) != expected:
                    return False
            return True
        except (OSError, ValueError, KeyError, TypeError):
            return False

    # 原子记录已验签产物，发布后才能命中。
    def save(self, name, fingerprint, staged):
        artifacts = {}
        for suffix in ('', '.idsig'):
            artifact = Path(staged) / (name + suffix)
            # 分块校验文件内容，避免按时间戳误用缓存。
            artifacts[suffix] = file_digest(artifact) if artifact.is_file() else None
        if artifacts[''] is None:
            raise FileNotFoundError(f'Missing verified release APK: {name}')
        self.directory.mkdir(parents=True, exist_ok=True)
        temporary = None
        try:
            with tempfile.NamedTemporaryFile(mode='w', encoding='utf-8', dir=self.directory,
                                             delete=False) as writer:
                temporary = Path(writer.name)
                json.dump({'fingerprint': fingerprint, 'artifacts': artifacts}, writer, sort_keys=True)
                writer.write('\n')
            temporary.replace(self.directory / (name + '.json'))
        finally:
            if temporary is not None:
                temporary.unlink(missing_ok=True)
