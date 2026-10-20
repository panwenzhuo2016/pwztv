# 影視

### Windows 一键打包

双击根目录 `build_all.bat`，或在终端运行它。需要 JDK 17 或更新版本、Python 3.10、Android SDK Platform 36.1 和 Build-Tools 35.0.0。通过 `ANDROID_HOME` 环境变量或 `local.properties` 中的 `sdk.dir` 指定 SDK；也支持项目根目录的 `.android-sdk`。首次构建需联网下载 Gradle、依赖及 Python 包。

默认只构建手机、电视两种 **64 位**安装包。需要同时构建 32 位时运行 `build_all.bat --include-32bit`；无人值守可追加 `--no-pause`，参数顺序不限。

脚本构建启用代码与资源裁剪的 Release 版本，逐个签名并校验成功后，集中输出到根目录 `Release`：

| 安装包 | 设备 |
| --- | --- |
| `mobile-arm64_v8a.apk` | 64 位 ARM 安卓手机、平板 |
| `mobile-armeabi_v7a.apk` | 32 位 ARM 安卓手机、平板，需 `--include-32bit` |
| `leanback-arm64_v8a.apk` | 64 位 ARM 安卓电视、盒子、投影 |
| `leanback-armeabi_v7a.apk` | 32 位 ARM 安卓电视、盒子、投影，需 `--include-32bit` |

按设备的安卓系统架构选择安装包。极米需支持安装第三方安卓应用；iPhone、iPad 不支持这些 APK。

首次运行自动创建本地签名，之后复用。**请备份整个 `.signing` 目录，其中包含密钥和密码，勿上传或分享。** 更换签名后无法覆盖安装旧版本；原有安装包若使用其他签名，也需先卸载再安装，卸载会清除应用数据。密钥或密码缺失时脚本会停止，避免无意更换签名。

构建失败会返回非零退出码；`Release` 中可能保留上次成功的产物，以本次终端的 `SUCCESS` 和列出的文件为准。默认构建不会更新已存在的 32 位包。

### 来源聚合与自动换源

默认直播入口聚合国内、国际清单，同台节目合并备用线路，保留每条线路的请求头。国内按央视、卫视、地方和港澳台分组，国际按新闻、影视、体育、纪录及其他分类，频道名保留地区区别。排除 RTP 组播和明显内网地址，IPv6 来源作为后备；播放能力仍取决于设备网络和来源限制。

内置来源目录位于 `app/src/main/assets/sources.json`，随包快照位于 `sources/`。来源包括 [iptv-org](https://github.com/iptv-org/iptv)、[Free-TV](https://github.com/Free-TV/IPTV)、[Guovin](https://github.com/Guovin/iptv-api)、[fanmingming](https://github.com/fanmingming/live) 和 [qist](https://github.com/qist/tvbox)。点播保留原有站点，按站点编号去重由 84 个增至 107 个，净增加 23 个接口；其中量子、非凡新增 JSON 接口，另含无尽、魔都、最大、如意、红牛、新浪、极速、光速、速播、豆瓣、金鹰、豪华、360、闪电、iKun、电影天堂、U酷、1080资源库、CK资源、樱花资源、艾旦影视。直接采集接口共 26 个，均参与原有点播和搜索流程。公开清单不等于全部节目可播，也不代表已收齐全网来源。

默认每 6 小时在加载时安排后台更新；下载超时、返回网页或格式错误均保留最后有效缓存，首次使用内置快照。更新不打断正在播放的节目，重新打开或刷新直播清单后使用新缓存。策略可在 `sources.json` 调整：单次下载最多 8MB、失败冷却 5 分钟、同台最多 24 条线路、每轮点播最多 20 个候选。

开启应用的自动换源后，直播失败会有限尝试同台其他线路，全部失败时停止。点播依次尝试解析、播放线路、其他站点；跨站校验片名和已知年份，寻找同一集并保留进度。用户关闭自动换源或关闭某站换源时尊重设置。

手机和电视的直播控制栏均可点击线路名称，打开同频道来源列表并直接选择。遇到能播但内容不符的线路，可点击“内容不符，跳过”，应用会记住该地址并尝试下一条；重启、换台和自动换线都会避开已标记地址。在列表中点选“已跳过”线路即可恢复。单线路频道也保留此入口，全部被标记时停止播放并提示恢复；内容是否正确由用户判断。

通过现有配置导入功能，也可添加自定义直播聚合订阅，无需部署服务器：

```json
{
  "lives": [{
    "name": "我的聚合直播",
    "url": "builtin://aggregate",
    "sources": [
      {"name": "来源一", "url": "https://example.com/live.m3u"},
      {"name": "来源二", "url": "https://example.org/live.txt", "header": {"User-Agent": "MyPlayer"}}
    ]
  }]
}
```

自定义清单支持现有 M3U/TXT 格式，最多 20 份订阅；没有快照时并行下载，首次等待总计最多 12 秒。只合并有可靠频道标识的同台节目；国际无标识的同名频道保守保留，避免混合不同国家节目。

运行 `py -3.10 scripts/probe_sources.py --samples-per-source 3` 可抽样检查直播清单引用、点播列表与详情以及 HLS 媒体分片，结果保存到 `build/source-research/media-probe.json`。这项检查不等于全量播放验证，也不覆盖地域限制或后续失效。

### 基於 CatVod 項目

https://github.com/CatVodTVOfficial/CatVodTVJarLoader

### 點播欄位

| 欄位名稱       | 預設值  | 說明   | 其他         |
|------------|------|------|------------|
| searchable | 1    | 是否搜索 | 0：關閉；1：啟用  |
| changeable | 1    | 是否換源 | 0：關閉；1：啟用  |
| quickserch | 1    | 是否快搜 | 0：關閉；1：啟用  |
| indexs     | 0    | 是否聚搜 | 0：關閉；1：啟用  |
| hide       | 0    | 是否隱藏 | 0：顯示；1：隱藏  |
| timeout    | 15   | 播放超時 | 單位：秒       |
| header     | none | 請求標頭 | 格式：json    |
| click      | none | 點擊js | javascript |

### 直播欄位

| 欄位名稱     | 預設值   | 說明    | 其他         |
|----------|-------|-------|------------|
| ua       | none  | 用戶代理  |            |
| origin   | none  | 來源    |            |
| referer  | none  | 參照地址  |            |
| epg      | none  | 節目地址  |            |
| logo     | none  | 台標地址  |            |
| pass     | false | 是否免密碼 |            |
| boot     | false | 是否自啟動 |            |
| timeout  | 15    | 播放超時  | 單位：秒       |
| header   | none  | 請求標頭  | 格式：json    |
| click    | none  | 點擊js  | javascript |
| catchup  | none  | 回看參數  |            |
| timeZone | none  | 時區    |            |

### 樣式

| 欄位名稱  | 值    | 說明  |
|-------|------|-----|
| type  | rect | 矩形  |
|       | oval | 橢圓  |
|       | list | 列表  |
| ratio | 0.75 | 3：4 |
|       | 1.33 | 4：3 |

直式

```json
{
  "style": {
    "type": "rect"
  }
}
```

橫式

```json
{
  "style": {
    "type": "rect",
    "ratio": 1.33
  }
}
```

正方

```json
{
  "style": {
    "type": "rect",
    "ratio": 1
  }
}
```

正圓

```json
{
  "style": {
    "type": "oval"
  }
}
```

橢圓

```json
{
  "style": {
    "type": "oval",
    "ratio": 1.1
  }
}
```

### API

播放控制

type 包含 stop、prev、next、loop、play、pause、replay
```
http://127.0.0.1:9978/action?do=control&type=next
```

刷新詳情

```
http://127.0.0.1:9978/action?do=refresh&type=detail
```

刷新播放

```
http://127.0.0.1:9978/action?do=refresh&type=player
```

刷新直播

```
http://127.0.0.1:9978/action?do=refresh&type=live
```

推送字幕

```
http://127.0.0.1:9978/action?do=refresh&type=subtitle&path=http://xxx
```

推送彈幕

```
http://127.0.0.1:9978/action?do=refresh&type=danmaku&path=http://xxx
```

新增緩存字串

```
http://127.0.0.1:9978/cache?do=set&key=xxx&value=xxx
```

取得緩存字串

```
http://127.0.0.1:9978/cache?do=get&key=xxx
```

刪除緩存字串

```
http://127.0.0.1:9978/cache?do=del&key=xxx
```

### Proxy

支持 http, https, socks4, socks5

```
scheme://username:password@host:port
```

配置新增 proxy 可指定代理
靠前的 host 匹配到則使用該代理

```json
{
  "spider": "",
  "proxy": [
    {
      "name": "自訂",
      "hosts": [
        "googlevideo.com",
        "raw.githubusercontent.com"
      ],
      "urls": [
        "http://127.0.0.1:7890"
      ]
    },
    {
      "name": "全局",
      "hosts": [
        ".*."
      ],
      "urls": [
        "socks5://127.0.0.1:7891"
      ]
    }
  ]
}
```

### Hosts

```json
{
  "spider": "",
  "hosts": [
    "cache.ott.*.itv.cmvideo.cn=base-v4-free-mghy.e.cdn.chinamobile.com"
  ]
}
```

### Headers

```json
{
  "spider": "",
  "headers": [
    {
      "host": "gslbserv.itv.cmvideo.cn",
      "header": {
        "User-Agent": "okhttp/3.12.13",
        "Referer": "test"
      }
    }
  ]
}
```

### 爬蟲本地代理

Java

```
proxy://
```

```
Proxy.getUrl(boolean local)
```

Python

```
proxy://do=py
```

```
getProxyUrl(boolean local)
```

JS

```
proxy://do=js
```

```
getProxy(boolean local)
```

### 配置範例

[本地/線上](other/sample/config.json)

### 飛機群

[討論群組](https://t.me/fongmi_official)  
[發布頻道](https://t.me/fongmi_release)

### 贊助

![photo_2024-01-10_11-39-12](https://github.com/FongMi/TV/assets/3471963/fdc12771-386c-4d5d-9a4d-d0bec0276fa7)

### Star

[![Star History Chart](https://api.star-history.com/svg?repos=FongMi/TV&type=Date)](https://www.star-history.com/#FongMi/TV&Date)
