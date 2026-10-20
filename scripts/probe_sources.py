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
def fetch(url, limit=LIMIT, headers=None):
    if urlsplit(url).scheme not in ("http", "https"):
        raise ValueError("only HTTP(S) source requests are supported")
    request = Request(url, headers={"User-Agent": "Mozilla/5.0", **(headers or {}), "Range": f"bytes=0-{limit - 1}"})
    with urlopen(request, timeout=6) as response:
        return response.read(limit), response.geturl(), response.status


# 保留原查询参数并设置采集接口动作。
def action_url(url, **params):
    parts = urlsplit(url)
    query = dict(parse_qsl(parts.query))
    query.update(params)
    return urlunsplit(parts._replace(query=urlencode(query)))


# 检查媒体清单并验证明文或解密后分片。
def media(url, headers=None):
    encryption = None
    hosts = []
    for depth in range(4):
        # 有限下载并保留真实响应地址。
        body, resolved, status = fetch(url, headers=headers)
        hosts.append(urlsplit(resolved).hostname)
        if not body.lstrip().startswith(b"#EXTM3U"):
            if depth == 0:
                raise ValueError("not an HLS manifest")
            if encryption is not None:
                try:
                    from Crypto.Cipher import AES
                except ImportError as error:
                    raise ValueError("AES-128 probe requires pycryptodome") from error
                key_url, explicit_iv, sequence = encryption
                iv = (sequence if explicit_iv is None else explicit_iv).to_bytes(16, "big")
                # 有限下载并保留真实响应地址。
                key, key_resolved, _ = fetch(key_url, 17, headers)
                hosts.append(urlsplit(key_resolved).hostname)
                if len(key) != 16 or len(body) < 16:
                    raise ValueError("invalid AES-128 key or segment")
                body = AES.new(key, AES.MODE_CBC, iv).decrypt(body[:len(body) // 16 * 16])
            transport = any(len(body) > offset + 376 and body[offset] == body[offset + 188] == body[offset + 376] == 0x47 for offset in range(min(188, len(body))))
            mp4 = len(body) > 8 and body[4:8] in (b"ftyp", b"styp", b"moof", b"sidx", b"mdat")
            if not transport and not mp4:
                raise ValueError("invalid media data")
            return {"status": status, "media_bytes": len(body), "media_hosts": list(dict.fromkeys(hosts)), "encrypted": encryption is not None}
        lines = body.decode("utf-8-sig").splitlines()
        sequence = 0
        encryption = None
        candidate = None
        for raw in lines:
            line = raw.strip()
            if line.startswith("#EXT-X-MEDIA-SEQUENCE:"):
                sequence = int(line.split(":", 1)[1])
            elif line.startswith("#EXT-X-KEY:"):
                attributes = dict((key, quoted or plain) for key, quoted, plain in re.findall(r'([A-Z0-9-]+)=(?:"([^"]*)"|([^,]*))', line.split(":", 1)[1]))
                method = attributes.get("METHOD")
                if method == "NONE":
                    encryption = None
                elif method == "AES-128" and attributes.get("KEYFORMAT", "identity") == "identity":
                    iv = int(attributes["IV"], 0) if "IV" in attributes else None
                    encryption = (urljoin(resolved, attributes["URI"]), iv, sequence)
                else:
                    raise ValueError("unsupported HLS encryption")
            elif line and not line.startswith("#"):
                candidate = line
                if encryption is not None:
                    encryption = (encryption[0], encryption[1], sequence)
                break
        if candidate is None:
            raise ValueError("manifest has no variant or segment")
        url = urljoin(resolved, candidate)
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
    # 检查媒体清单并验证明文或解密后分片。
    return media(candidate)


# 验证候选接口搜索语义并抽样多个影片。
def vod_candidate(site, samples):
    # 保留原查询参数并设置采集接口动作。
    # 有限下载并保留真实响应地址。
    body, _, _ = fetch(action_url(site["api"], ac="list"), 2 * 1024 * 1024)
    listing = json.loads(body).get("list", [])
    if not listing:
        raise ValueError("empty video list")
    # 保留原查询参数并设置采集接口动作。
    # 有限下载并保留真实响应地址。
    body, _, _ = fetch(action_url(site["api"], wd=listing[0]["vod_name"], quick="true", extend=""), 2 * 1024 * 1024)
    if not any(str(row.get("vod_id")) == str(listing[0]["vod_id"]) for row in json.loads(body).get("list", [])):
        raise ValueError("expected video missing from search")
    # 保留原查询参数并设置采集接口动作。
    # 有限下载并保留真实响应地址。
    body, _, _ = fetch(action_url(site["api"], wd="__pwztv_missing_820432__", quick="true", extend=""), 2 * 1024 * 1024)
    if json.loads(body).get("list") != []:
        raise ValueError("search keyword ignored")
    results = []
    for item in listing[:samples]:
        sample = {"id": str(item["vod_id"]), "ok": False}
        try:
            # 保留原查询参数并设置采集接口动作。
            # 有限下载并保留真实响应地址。
            body, _, _ = fetch(action_url(site["api"], ac="detail", ids=sample["id"]), 2 * 1024 * 1024)
            details = json.loads(body)["list"]
            detail = next(row for row in details if str(row.get("vod_id")) == sample["id"])
            urls = list(dict.fromkeys(re.findall(r"https?://[^$#\s]+", detail.get("vod_play_url", ""))))
            errors = []
            for url in [value for value in urls if ".m3u8" in value][:2]:
                try:
                    # 检查媒体清单并验证明文或解密后分片。
                    sample.update(media(url), ok=True)
                    break
                except Exception as error:
                    errors.append(type(error).__name__ + ": " + str(error)[:160])
            if not sample["ok"]:
                sample["errors"] = errors or ["no direct HLS URL"]
        except Exception as error:
            sample["errors"] = [type(error).__name__ + ": " + str(error)[:160]]
        results.append(sample)
    return {"search_ok": True, "media_ok": any(item["ok"] for item in results), "samples": results}


# 独立探测候选接口且保留来源追溯信息。
def probe_candidate(job):
    site, samples = job
    try:
        # 验证候选接口搜索语义并抽样多个影片。
        result = vod_candidate(site, samples)
        return {"kind": "candidate", "source": site.get("key", site["name"]), "name": site["name"], "api": site["api"], "from": site.get("from", ""), "ok": result["media_ok"], **result}
    except Exception as error:
        return {"kind": "candidate", "name": site["name"], "api": site["api"], "ok": False, "error": type(error).__name__ + ": " + str(error)[:160]}


# 探测单个样本并记录失败类型。
def probe(job):
    kind, identity, value = job
    try:
        # 从点播列表详情提取实际媒体地址。
        # 检查媒体清单并验证明文或解密后分片。
        result = vod_sample(value) if kind == "vod" else media(value["url"], value.get("headers"))
        return {"kind": kind, "source": identity, "ok": True, **result}
    except Exception as error:
        return {"kind": kind, "source": identity, "ok": False, "error": type(error).__name__ + ": " + str(error)[:160]}


# 提取直播样本且保留每条线路声明的请求头。
def live_samples(text, limit):
    selected, seen, headers = [], set(), {}
    for raw in text.splitlines():
        line = raw.strip()
        if line.startswith("#EXTINF:"):
            agent = re.search(r'http-user-agent="([^"]+)"', line)
            headers = {"User-Agent": agent[1]} if agent else {}
        elif line.startswith("#EXTVLCOPT:"):
            key, _, value = line.split(":", 1)[1].partition("=")
            name = {"http-user-agent": "User-Agent", "http-referrer": "Referer", "http-origin": "Origin"}.get(key)
            if name:
                headers[name] = value
        elif line.startswith("#EXTHTTP:"):
            headers.update(json.loads(line.split(":", 1)[1]))
        elif line.startswith(("http://", "https://")):
            url = line.split("$", 1)[0]
            if url in seen:
                continue
            seen.add(url)
            selected.append({"url": url, "headers": dict(headers)})
            if len(selected) >= limit:
                break
    return selected


# 抽样探测来源并写入可反查报告。
def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--samples-per-source", type=int, choices=range(1, 9), default=2)
    parser.add_argument("--candidates", type=Path, help="Validate a JSON array of candidate sites without installing them")
    parser.add_argument("--output", type=Path, default=ROOT / "build/source-research/media-probe.json")
    args = parser.parse_args()
    assets = ROOT / "app/src/main/assets"
    manifest = json.loads((assets / "sources.json").read_text(encoding="utf-8"))
    jobs = []
    for entry in ([] if args.candidates else manifest["live"]):
        text = (assets / entry["snapshot"]).read_text(encoding="utf-8-sig")
        # 提取直播样本且保留每条线路声明的请求头。
        for sample in live_samples(text, args.samples_per_source):
            jobs.append(("live", entry["id"], sample))
    for site in ([] if args.candidates else json.loads((assets / "sources/direct.json").read_text(encoding="utf-8"))):
        jobs.append(("vod", site["key"], site))
    with ThreadPoolExecutor(max_workers=6) as pool:
        if args.candidates:
            candidates = json.loads(args.candidates.read_text(encoding="utf-8"))
            if not isinstance(candidates, list) or len(candidates) > 200:
                raise ValueError("candidates must be an array with at most 200 entries")
            if any(not isinstance(site, dict) or not isinstance(site.get("name"), str)
                   or not isinstance(site.get("api"), str) or urlsplit(site["api"]).scheme not in ("http", "https") for site in candidates):
                raise ValueError("each candidate requires a name and HTTP(S) api")
            # 独立探测候选接口且保留来源追溯信息。
            results = list(pool.map(probe_candidate, ((site, args.samples_per_source) for site in candidates)))
        else:
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
