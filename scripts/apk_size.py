"""Compress the stream-loaded MITV asset before APK alignment and signing."""

import copy
import hashlib
import shutil
import zipfile

MITV_ASSET = 'assets/libmitv.so'


# 仅压缩流式读取的MITV资源并保留其他条目属性。
def compress_mitv(source, destination):
    if source.resolve() == destination.resolve():
        raise ValueError('Compression requires a separate output file')
    with zipfile.ZipFile(source) as original:
        names = original.namelist()
        if len(names) != len(set(names)):
            raise ValueError('Duplicate ZIP entries are not supported')
        if MITV_ASSET not in names:
            raise ValueError('MITV asset is missing')
        with zipfile.ZipFile(destination, 'w') as output:
            output.comment = original.comment
            for entry in original.infolist():
                info = copy.copy(entry)
                if info.filename == MITV_ASSET:
                    info.compress_type = zipfile.ZIP_DEFLATED
                    info._compresslevel = 9
                with original.open(entry) as reader, output.open(info, 'w') as writer:
                    shutil.copyfileobj(reader, writer)


# 反查对齐后的全部内容与非目标压缩方式。
def verify_contents(source, destination):
    with zipfile.ZipFile(source) as original, zipfile.ZipFile(destination) as output:
        names = original.namelist()
        if names != output.namelist():
            raise RuntimeError('APK entries changed during compression/alignment')
        for entry in original.infolist():
            actual = output.getinfo(entry.filename)
            expected = zipfile.ZIP_DEFLATED if entry.filename == MITV_ASSET else entry.compress_type
            if actual.compress_type != expected:
                raise RuntimeError(f'Unexpected compression: {entry.filename}')
            with original.open(entry) as reader, output.open(actual) as writer:
                # 分块计算内容哈希，避免大文件占用额外内存。
                if stream_digest(reader) != stream_digest(writer):
                    raise RuntimeError(f'APK content changed: {entry.filename}')


# 分块计算内容哈希，避免大文件占用额外内存。
def stream_digest(stream):
    digest = hashlib.sha256()
    for block in iter(lambda: stream.read(1024 * 1024), b''):
        digest.update(block)
    return digest.digest()
