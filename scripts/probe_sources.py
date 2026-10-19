"""Probe a bounded sample of subscribed playlists and their actual media data."""

import argparse
from concurrent.futures import ThreadPoolExecutor
import json
from pathlib import Path
import re
from urllib.parse import parse_qsl, urlencode, urljoin, urlsplit, urlunsplit
from urllib.request import Request, urlopen

ROOT = Path(__file__).resolve().parent.parent
LIMIT = 128 * 1024


# 有限下载并保留真实响应地址。
def fetch(url, limit=LIMIT):
    request = Request(url, headers={"User-Agent": "Mozilla/5.0", "Range": f"bytes=0-{limit - 1}"})
    with urlopen(request, timeout=6) as response:
        return response.read(limit), response.geturl(), response.status


# 保留原查询参数并设置采集接口动作。
def action_url(url, **params):
    parts = urlsplit(url)
    query = dict(parse_qsl(parts.query))
    query.update(params)
    return urlunsplit(parts._replace(query=urlencode(query)))


# 检查媒体清单及至少一段实际媒体数据。
def media(url):
    for depth in range(4):
        # 有限下载并保留真实响应地址。
        body, resolved, status = fetch(url)
        if not body.lstrip().startswith(b"#EXTM3U"):
            if depth == 0:
                raise ValueError("not an HLS manifest")
            transport = len(body) > 376 and body[0] == body[188] == body[376] == 0x47
            mp4 = len(body) > 8 and body[4:8] in (b"ftyp", b"styp", b"moof", b"sidx")
            if not transport and not mp4:
                raise ValueError("invalid media data")
            return {"status": status, "media_bytes": len(body)}
        lines = body.decode("utf-8-sig").splitlines()
        candidates = [line.strip() for line in lines if line.strip() and not line.startswith("#")]
        if not candidates:
            raise ValueError("manifest has no variant or segment")
        url = urljoin(resolved, candidates[0])
    raise ValueError("manifest nesting exceeds limit")


# 从点播列表详情提取实际媒体地址。
def vod_sample(site):
    # 保留原查询参数并设置采集接口动作。
    url = action_url(site["api"], ac="list")
    # 有限下载并保留真实响应地址。
    body, _, _ = fetch(url, 2 * 1024 * 1024)
    listing = json.loads(body)["list"]
    if not listing:
        raise ValueError("empty video list")
    # 保留原查询参数并设置采集接口动作。
    url = action_url(site["api"], ac="detail", ids=str(listing[0]["vod_id"]))
    # 有限下载并保留真实响应地址。
    body, _, _ = fetch(url, 2 * 1024 * 1024)
    detail = json.loads(body)["list"][0]
    urls = re.findall(r"https?://[^$#\s]+", detail.get("vod_play_url", ""))
    candidate = next((item for item in urls if ".m3u8" in item), None)
    if not candidate:
        raise ValueError("no direct HLS URL in detail")
    # 检查媒体清单及至少一段实际媒体数据。
    return media(candidate)


# 探测单个样本并记录失败类型。
def probe(job):
    kind, identity, value = job
    try:
        # 从点播列表详情提取实际媒体地址。
        # 检查媒体清单及至少一段实际媒体数据。
        result = vod_sample(value) if kind == "vod" else media(value)
        return {"kind": kind, "source": identity, "ok": True, **result}
    except Exception as error:
        return {"kind": kind, "source": identity, "ok": False, "error": type(error).__name__ + ": " + str(error)[:160]}


# 抽样探测来源并写入可反查报告。
def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--samples-per-source", type=int, choices=range(1, 9), default=2)
    parser.add_argument("--output", type=Path, default=ROOT / "build/source-research/media-probe.json")
    args = parser.parse_args()
    assets = ROOT / "app/src/main/assets"
    manifest = json.loads((assets / "sources.json").read_text(encoding="utf-8"))
    jobs = []
    for entry in manifest["live"]:
        text = (assets / entry["snapshot"]).read_text(encoding="utf-8-sig")
        urls = list(dict.fromkeys(line.strip() for line in text.splitlines() if line.startswith(("http://", "https://"))))
        for url in urls[:args.samples_per_source]:
            jobs.append(("live", entry["id"], url))
    for site in json.loads((assets / "sources/direct.json").read_text(encoding="utf-8")):
        jobs.append(("vod", site["key"], site))
    with ThreadPoolExecutor(max_workers=6) as pool:
        # 探测单个样本并记录失败类型。
        results = list(pool.map(probe, jobs))
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(results, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    saved = json.loads(args.output.read_text(encoding="utf-8"))
    if saved != results:
        raise RuntimeError("probe report read-back mismatch")
    print(f"Sampled {len(saved)}; media data received: {sum(item['ok'] for item in saved)}; report: {args.output}")


if __name__ == "__main__":
    # 抽样探测来源并写入可反查报告。
    main()
