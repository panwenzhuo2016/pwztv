package com.fongmi.android.tv.api.config;

public class BuiltinConfig {
    public static final String URL = "builtin";
    public static final String BASE = "https://raw.githubusercontent.com/qist/tvbox/refs/heads/master/";
    public static final String M3U = """
#EXTM3U
#EXTINF:-1 channel-number="1" tvg-id="1" tvg-name="CCTV-1高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/CCTV/CCTV-1.png" group-title="央视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221225880/10000100000000060000000000025506_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",CCTV-1 综合
rtp://239.3.1.129:8008
#EXTINF:-1 channel-number="2" tvg-id="2" tvg-name="CCTV-2高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/CCTV/CCTV-2.png" group-title="央视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226036/10000100000000060000000000285832_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",CCTV-2 财经
rtp://239.3.1.60:8084
#EXTINF:-1 channel-number="3" tvg-id="3" tvg-name="CCTV-3高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/CCTV/CCTV-3.png" group-title="央视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226100/10000100000000060000000000831268_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",CCTV-3 综艺
rtp://239.3.1.172:8001
#EXTINF:-1 channel-number="4" tvg-id="4" tvg-name="CCTV-4高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/CCTV/CCTV-4.png" group-title="央视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226081/10000100000000060000000000545704_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",CCTV-4 中文国际
rtp://239.3.1.105:8092
#EXTINF:-1 channel-number="341" tvg-id="341" tvg-name="CCTV-4欧洲高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/CCTV/CCTV-4-EUO.png" group-title="央视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226138/10000100000000060000000001154838_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",CCTV-4 中文国际欧洲
rtp://239.3.1.213:4220
#EXTINF:-1 channel-number="342" tvg-id="342" tvg-name="CCTV-4美洲高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/CCTV/CCTV-4-AME.png" group-title="央视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226149/10000100000000060000000001154841_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",CCTV-4 中文国际美洲
rtp://239.3.1.214:4220
#EXTINF:-1 channel-number="5" tvg-id="5" tvg-name="CCTV-5高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/CCTV/CCTV-5.png" group-title="央视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226113/10000100000000060000000000831269_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",CCTV-5 体育
rtp://239.3.1.173:8001
#EXTINF:-1 channel-number="18" tvg-id="18" tvg-name="CCTV-5+" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/CCTV/CCTV-5+.png" group-title="央视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221225819/10000100000000060000000000025043_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",CCTV-5+ 体育赛事
rtp://239.3.1.130:8004
#EXTINF:-1 channel-number="6" tvg-id="6" tvg-name="CCTV-6高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/CCTV/CCTV-6.png" group-title="央视" zz-definition="HD",CCTV-6 电影
rtp://239.3.1.174:8001
#EXTINF:-1 channel-number="7" tvg-id="7" tvg-name="CCTV-7高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/CCTV/CCTV-7.png" group-title="央视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226031/10000100000000060000000000285833_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",CCTV-7 国防军事
rtp://239.3.1.61:8104
#EXTINF:-1 channel-number="8" tvg-id="8" tvg-name="CCTV-8高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/CCTV/CCTV-8.png" group-title="央视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226115/10000100000000060000000000831271_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",CCTV-8 电视剧
rtp://239.3.1.175:8001
#EXTINF:-1 channel-number="9" tvg-id="9" tvg-name="CCTV-9高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/CCTV/CCTV-9.png" group-title="央视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226038/10000100000000060000000000285834_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",CCTV-9 纪录
rtp://239.3.1.62:8112
#EXTINF:-1 channel-number="10" tvg-id="10" tvg-name="CCTV-10高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/CCTV/CCTV-10.png" group-title="央视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226033/10000100000000060000000000285835_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",CCTV-10 科教
rtp://239.3.1.63:8116
#EXTINF:-1 channel-number="11" tvg-id="11" tvg-name="CCTV-11高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/CCTV/CCTV-11.png" group-title="央视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226086/10000100000000060000000000753288_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",CCTV-11 戏曲
rtp://239.3.1.152:8120
#EXTINF:-1 channel-number="12" tvg-id="12" tvg-name="CCTV-12高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/CCTV/CCTV-12.png" group-title="央视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226040/10000100000000060000000000285836_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",CCTV-12 社会与法
rtp://239.3.1.64:8124
#EXTINF:-1 channel-number="13" tvg-id="13" tvg-name="CCTV-13高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/CCTV/CCTV-13.png" group-title="央视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226145/10000100000000060000000001103864_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",CCTV-13 新闻
rtp://239.3.1.124:8128
#EXTINF:-1 channel-number="14" tvg-id="14" tvg-name="CCTV-14高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/CCTV/CCTV-14.png" group-title="央视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226035/10000100000000060000000000285837_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",CCTV-14 少儿
rtp://239.3.1.65:8132
#EXTINF:-1 channel-number="15" tvg-id="15" tvg-name="CCTV-15高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/CCTV/CCTV-15.png" group-title="央视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226099/10000100000000060000000000753287_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",CCTV-15 音乐
rtp://239.3.1.153:8136
#EXTINF:-1 channel-number="16" tvg-id="16" tvg-name="CCTV-16高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/CCTV/CCTV-16.png" group-title="央视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226148/10000100000000060000000001234273_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",CCTV-16 奥林匹克
rtp://239.3.1.184:8001
#EXTINF:-1 channel-number="17" tvg-id="17" tvg-name="CCTV-17高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/CCTV/CCTV-17.png" group-title="央视" zz-definition="HD" zz-raw-name="CCTV17-高清" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226095/10000100000000060000000000705941_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",CCTV-17 农业农村
rtp://239.3.1.151:8144
#EXTINF:-1 channel-number="343" tvg-id="343" tvg-name="CGTN 英语高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/CCTV/CGTN.png" group-title="央视" zz-definition="HD" zz-raw-name="CGTN英语" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226151/10000100000000060000000001154843_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",CGTN英语
rtp://239.3.1.215:4220
#EXTINF:-1 channel-number="344" tvg-id="344" tvg-name="CGTN 英文纪录高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/CCTV/CGTN-Doc.png" group-title="央视" zz-definition="HD" zz-raw-name="CGTN英文纪录" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226153/10000100000000060000000001154845_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",CGTN纪录
rtp://239.3.1.216:4220
#EXTINF:-1 channel-number="345" tvg-id="345" tvg-name="CGTN 西班牙语高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/CCTV/CGTN-Espanol.png" group-title="央视" zz-definition="HD" zz-raw-name="CGTN西班牙语" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226140/10000100000000060000000001154848_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",CGTN西语
rtp://239.3.1.217:4220
#EXTINF:-1 channel-number="346" tvg-id="346" tvg-name="CGTN 法语高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/CCTV/CGTN-Francis.png" group-title="央视" zz-definition="HD" zz-raw-name="CGTN法语" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226142/10000100000000060000000001154852_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",CGTN法语
rtp://239.3.1.218:4220
#EXTINF:-1 channel-number="347" tvg-id="347" tvg-name="CGTN 阿拉伯语高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/CCTV/CGTN-Arabic.png" group-title="央视" zz-definition="HD" zz-raw-name="CGTN阿拉伯语" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226155/10000100000000060000000001154853_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",CGTN阿语
rtp://239.3.1.219:4220
#EXTINF:-1 channel-number="348" tvg-id="348" tvg-name="CGTN 俄语高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/CCTV/CGTN-Russian.png" group-title="央视" zz-definition="HD" zz-raw-name="CGTN俄语" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226144/10000100000000060000000001154854_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",CGTN俄语
rtp://239.3.1.220:4220
#EXTINF:-1 channel-number="21" tvg-id="21" tvg-name="BRTV北京卫视高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/WeiShi/BeiJing.png" group-title="卫视" zz-definition="HD" catchup="default" catchup-source="http://61.135.88.136/TVOD/88888892/224/3221225870/10000100000000060000000000025099_0.smil/index.m3u8?fmt=ts2hls&playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",北京卫视
rtp://239.3.1.22:8001
#EXTINF:-1 channel-number="41" tvg-id="41" tvg-name="湖南卫视高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/WeiShi/HuNan.png" group-title="卫视" zz-definition="HD" catchup="default" catchup-source="http://61.135.88.136/TVOD/88888892/224/3221225871/10000100000000060000000000025095_0.smil/index.m3u8?fmt=ts2hls&playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",湖南卫视
rtp://239.3.1.112:3002
#EXTINF:-1 channel-number="44" tvg-id="44" tvg-name="江苏卫视高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/WeiShi/JiangSu.png" group-title="卫视" zz-definition="HD" catchup="default" catchup-source="http://61.135.88.136/TVOD/88888892/224/3221225881/10000100000000060000000000025507_0.smil/index.m3u8?fmt=ts2hls&playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",江苏卫视
rtp://239.3.1.113:3003
#EXTINF:-1 channel-number="45" tvg-id="45" tvg-name="东方卫视高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/WeiShi/DongFang.png" group-title="卫视" zz-definition="HD" catchup="default" catchup-source="http://61.135.88.136/TVOD/88888892/224/3221225883/10000100000000060000000000025534_0.smil/index.m3u8?fmt=ts2hls&playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",东方卫视
rtp://239.3.1.119:3006
#EXTINF:-1 channel-number="46" tvg-id="46" tvg-name="浙江卫视高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/WeiShi/ZheJiang.png" group-title="卫视" zz-definition="HD" catchup="default" catchup-source="http://61.135.88.136/TVOD/88888892/224/3221225957/10000100000000060000000000146902_0.smil/index.m3u8?fmt=ts2hls&playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",浙江卫视
rtp://239.3.1.114:3004
#EXTINF:-1 channel-number="50" tvg-id="50" tvg-name="山东卫视高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/WeiShi/ShanDong.png" group-title="卫视" zz-definition="HD" catchup="default" catchup-source="http://61.135.88.136/TVOD/88888892/224/3221226002/10000100000000060000000000205202_0.smil/index.m3u8?fmt=ts2hls&playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",山东卫视
rtp://239.3.1.144:3005
#EXTINF:-1 channel-number="72" tvg-id="72" tvg-name="广东卫视高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/WeiShi/GuangDong.png" group-title="卫视" zz-definition="HD" catchup="default" catchup-source="http://61.135.88.136/TVOD/88888892/224/3221225983/10000100000000060000000000176932_0.smil/index.m3u8?fmt=ts2hls&playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",广东卫视
rtp://239.3.1.27:2000
#EXTINF:-1 channel-number="74" tvg-id="74" tvg-name="深圳卫视高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/WeiShi/ShenZhen.png" group-title="卫视" zz-definition="HD" catchup="default" catchup-source="http://61.135.88.136/TVOD/88888892/224/3221225869/10000100000000060000000000025093_0.smil/index.m3u8?fmt=ts2hls&playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",深圳卫视
rtp://239.3.1.28:2000
#EXTINF:-1 channel-number="142" tvg-id="142" tvg-name="四川卫视" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/WeiShi/SiChuan.png" group-title="卫视" zz-definition="SD" catchup="default" catchup-source="http://61.135.88.136/TVOD/88888892/224/3221225831/10000100000000060000000000025055_0.smil/index.m3u8?fmt=ts2hls&playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",四川卫视
rtp://239.3.1.131:3001
#EXTINF:-1 channel-number="58" tvg-id="58" tvg-name="江西卫视高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/WeiShi/JiangXi.png" group-title="卫视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226136/10000100000000060000000001103865_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",江西卫视
rtp://239.3.1.123:8164
#EXTINF:-1 channel-number="73" tvg-id="73" tvg-name="河北卫视高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/WeiShi/HeBei.png" group-title="卫视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226082/10000100000000060000000000736149_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",河北卫视
rtp://239.3.1.148:8072
#EXTINF:-1 channel-number="59" tvg-id="59" tvg-name="河南卫视高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/WeiShi/HeNan.png" group-title="卫视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226189/382569577.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",河南卫视
rtp://239.3.1.50:8184
#EXTINF:-1 channel-number="48" tvg-id="48" tvg-name="天津卫视高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/WeiShi/TianJin.png" group-title="卫视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221225984/10000100000000060000000000176931_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",天津卫视
rtp://239.3.1.141:1234
#EXTINF:-1 channel-number="52" tvg-id="52" tvg-name="安徽卫视高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/WeiShi/AnHui.png" group-title="卫视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221225999/10000100000000060000000000205203_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",安徽卫视
rtp://239.3.1.211:8064
#EXTINF:-1 channel-number="47" tvg-id="47" tvg-name="湖北卫视高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/WeiShi/HuBei.png" group-title="卫视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221225956/10000100000000060000000000146903_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",湖北卫视
rtp://239.3.1.138:8044
#EXTINF:-1 channel-number="56" tvg-id="56" tvg-name="东南卫视高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/WeiShi/DongNan.png" group-title="卫视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226101/10000100000000060000000000793946_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",东南卫视
rtp://239.3.1.156:8148
#EXTINF:-1 channel-number="55" tvg-id="55" tvg-name="贵州卫视高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/WeiShi/GuiZhou.png" group-title="卫视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226097/10000100000000060000000000736148_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",贵州卫视
rtp://239.3.1.149:8076
#EXTINF:-1 channel-number="51" tvg-id="51" tvg-name="辽宁卫视高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/WeiShi/LiaoNing.png" group-title="卫视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226000/10000100000000060000000000205199_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",辽宁卫视
rtp://239.3.1.210:8056
#EXTINF:-1 channel-number="57" tvg-id="57" tvg-name="重庆卫视高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/WeiShi/ChongQing.png" group-title="卫视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226147/10000100000000060000000001103866_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",重庆卫视
rtp://239.3.1.122:8160
#EXTINF:-1 channel-number="53" tvg-id="53" tvg-name="黑龙江卫视高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/WeiShi/HeiLongJiang.png" group-title="卫视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221225864/10000100000000060000000000025092_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",黑龙江卫视
rtp://239.3.1.133:8016
#EXTINF:-1 channel-number="78" tvg-id="78" tvg-name="吉林卫视高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/WeiShi/JiLin.png" group-title="卫视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226161/10000100000000060000000001233339_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",吉林卫视
rtp://239.3.1.240:8172
#EXTINF:-1 channel-number="62" tvg-id="62" tvg-name="甘肃卫视高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/WeiShi/GanSu.png" group-title="卫视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226197/397709753.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",甘肃卫视
rtp://239.3.1.201:8180
#EXTINF:-1 channel-number="71" tvg-id="71" tvg-name="宁夏卫视高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/WeiShi/NingXia.png" group-title="卫视" zz-definition="HD",宁夏卫视
rtp://239.3.1.169:8212
#EXTINF:-1 channel-number="64" tvg-id="64" tvg-name="新疆卫视高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/WeiShi/XinJiang.png" group-title="卫视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226201/400975092.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",新疆卫视
rtp://239.3.1.167:8192
#EXTINF:-1 channel-number="63" tvg-id="63" tvg-name="西藏卫视高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/WeiShi/XiZang.png" group-title="卫视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226192/400975121.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",西藏卫视
rtp://239.3.1.166:8188
#EXTINF:-1 channel-number="65" tvg-id="65" tvg-name="兵团卫视高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/WeiShi/BingTuan.png" group-title="卫视" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226199/400975082.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",兵团卫视
rtp://239.3.1.168:8196
#EXTINF:-1 channel-number="22" tvg-id="22" tvg-name="BRTV文艺高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/BRTV/BRTV_WenYi.png" group-title="地方" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221225868/10000100000000060000000000025097_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",北京文艺
rtp://239.3.1.242:8000
#EXTINF:-1 channel-number="26" tvg-id="26" tvg-name="BRTV体育休闲高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/BRTV/BRTV_TiYuXiuXian.png" group-title="地方" zz-definition="HD",体育休闲
rtp://239.3.1.120:8000
#EXTINF:-1 channel-number="24" tvg-id="24" tvg-name="BRTV影视高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/BRTV/BRTV_YingShi.png" group-title="地方" zz-definition=HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226094/10000100000000060000000000804258_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",北京影视
rtp://239.3.1.158:8000
#EXTINF:-1 channel-number="29" tvg-id="29" tvg-name="BRTV新闻高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/BRTV/BRTV_XinWen.png" group-title="地方" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226092/10000100000000060000000000804251_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",北京新闻
rtp://239.3.1.159:8000
#EXTINF:-1 channel-number="25" tvg-id="25" tvg-name="BRTV财经高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/BRTV/BRTV_CaiJing.png" group-title="地方" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226134/10000100000000060000000001010593_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",北京财经
rtp://239.3.1.116:8000
#EXTINF:-1 channel-number="27" tvg-id="27" tvg-name="BRTV生活高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/BRTV/BRTV_ShengHuo.png" group-title="地方" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226141/10000100000000060000000001010592_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",北京生活
rtp://239.3.1.117:8000
#EXTINF:-1 channel-number="131" tvg-id="131" tvg-name="BTV国际频道" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/BRTV/BRTV_International.png" group-title="地方" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221225804/10000100000000060000000000025033_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",北京国际
rtp://239.3.1.235:8000
#EXTINF:-1 channel-number="23" tvg-id="23" tvg-name="BRTV纪实科教高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/BRTV/BRTV_JiShiKeJiao.png" group-title="地方" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226143/10000100000000060000000001010594_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",纪实科教
rtp://239.3.1.115:8000
#EXTINF:-1 channel-number="28" tvg-id="28" tvg-name="BRTV卡酷少儿高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/BRTV/BRTV_KakuShaoEr.png" group-title="地方" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226133/10000100000000060000000000943150_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",卡酷少儿
rtp://239.3.1.189:8000
#EXTINF:-1 channel-number="77" tvg-id="77" tvg-name="金鹰纪实高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/WeiShi/HuNan_JingYingJiShi.png" group-title="地方" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226050/10000100000000060000000000384062_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",金鹰纪实
rtp://239.3.1.58:8156
#EXTINF:-1 channel-number="321" tvg-id="321" tvg-name="茶" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/QiTa/HuNan_ChaPinDao.png" group-title="地方" zz-definition="HD",茶友频道
rtp://239.3.1.165:8001
#EXTINF:-1 channel-number="320" tvg-id="320" tvg-name="快乐垂钓" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/QiTa/HuNan_KuaiLeChuiDiao.png" group-title="地方" zz-definition="HD",快乐垂钓
rtp://239.3.1.164:8001
#EXTINF:-1 channel-number="86" tvg-id="85" tvg-name="CCTV4K超高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/CCTV/CCTV-4K.png" group-title="数字" zz-definition="UHD",CCTV-4K
rtp://239.3.1.245:2000
#EXTINF:-1 channel-number="76" tvg-id="76" tvg-name="CETV1高清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/QiTa/CETV-1.png" group-title="数字" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226065/10000100000000060000000000384776_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",中国教育
rtp://239.3.1.57:8152
#EXTINF:-1 channel-number="79" tvg-id="79" tvg-name="中国交通" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/QiTa/ZhongGuoJiaoTong.png" group-title="数字" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226173/10000100000000060000000001341044_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",中国交通
rtp://239.3.1.188:8001
#EXTINF:-1 channel-number="80" tvg-id="80" tvg-name="证券服务" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/QiTa/GanSu_ZhengQuanFuWu.png" group-title="数字" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226210/406742223.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",证券服务
rtp://239.3.1.24:8001
#EXTINF:-1 channel-number="66" tvg-id="66" tvg-name="睛彩竞技" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/QiTa/CBC_JingCaiJingJi.png" group-title="数字" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226160/10000100000000060000000001464323_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",睛彩竞技
rtp://239.3.1.125:8001
#EXTINF:-1 channel-number="67" tvg-id="67" tvg-name="睛彩篮球" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/QiTa/CBC_JingCaiLanQiu.png" group-title="数字" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226162/10000100000000060000000001464344_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",睛彩篮球
rtp://239.3.1.126:8001
#EXTINF:-1 channel-number="68" tvg-id="68" tvg-name="睛彩青少" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/QiTa/CBC_JingCaiQingShao.png" group-title="数字" zz-definition="HD" zz-raw-name="睛彩羽毛球" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226175/10000100000000060000000001464349_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",睛彩青少
rtp://239.3.1.127:8001
#EXTINF:-1 channel-number="69" tvg-id="69" tvg-name="睛彩广场舞" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/QiTa/CBC_JingCaiGuangChangWu.png" group-title="数字" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226164/10000100000000060000000001464351_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",睛彩广场舞
rtp://239.3.1.128:8001
#EXTINF:-1 channel-number="401" tvg-id="401" tvg-name="爱上4K" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/QiTa/AiShang4K.png" group-title="数字" zz-definition="UHD",爱上4K
rtp://239.3.1.236:2000
#EXTINF:-1 channel-number="40" tvg-id="40" tvg-name="4K超清" tvg-logo="https://raw.githubusercontent.com/zzzz0317/beijing-unicom-iptv-playlist/refs/heads/main/img/QiTa/BJIPTV_4KChaoQing.png" group-title="数字" zz-definition="UHD",4K超清
rtp://239.3.1.249:8001
#EXTINF:-1 channel-number="351" tvg-id="351" tvg-name="城市剧场" group-title="数字" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226037/10000100000000060000000000285839_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",城市剧场
rtp://239.3.1.67:4120
#EXTINF:-1 channel-number="352" tvg-id="352" tvg-name="军旅剧场" group-title="数字" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226007/10000100000000060000000000285639_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",军旅剧场
rtp://239.3.1.68:4120
#EXTINF:-1 channel-number="353" tvg-id="353" tvg-name="古装剧场" group-title="数字" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226012/10000100000000060000000000285640_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",古装剧场
rtp://239.3.1.69:4120
#EXTINF:-1 channel-number="358" tvg-id="358" tvg-name="精选" group-title="数字" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226018/10000100000000060000000000285646_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",精选高清
rtp://239.3.1.74:4120
#EXTINF:-1 channel-number="374" tvg-id="374" tvg-name="武侠剧场" group-title="数字" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226041/10000100000000060000000000286228_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",武侠剧场
rtp://239.3.1.90:4120
#EXTINF:-1 channel-number="375" tvg-id="375" tvg-name="喜剧影院" group-title="数字" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226044/10000100000000060000000000286227_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",喜剧影院
rtp://239.3.1.91:4120
#EXTINF:-1 channel-number="376" tvg-id="376" tvg-name="动作影院" group-title="数字" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226039/10000100000000060000000000286226_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",,动作影院
rtp://239.3.1.92:4120
#EXTINF:-1 channel-number="377" tvg-id="377" tvg-name="家庭影院" group-title="数字" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226034/10000100000000060000000000285685_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",家庭影院
rtp://239.3.1.93:4120
#EXTINF:-1 channel-number="378" tvg-id="378" tvg-name="星影" group-title="数字" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226029/10000100000000060000000000285686_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",星影高清
rtp://239.3.1.94:4120
#EXTINF:-1 channel-number="368" tvg-id="368" tvg-name="光影" group-title="数字" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226028/10000100000000060000000000285656_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",光影高清
rtp://239.3.1.84:4120
#EXTINF:-1 channel-number="364" tvg-id="364" tvg-name="动画" group-title="数字" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221226024/10000100000000060000000000285652_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",动画高清
rtp://239.3.1.80:4120
#EXTINF:-1 channel-number="203" tvg-id="203" tvg-name="经典电影" group-title="数字" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221225857/10000100000000060000000000025082_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",经典电影
rtp://239.3.1.195:9024
#EXTINF:-1 channel-number="349" tvg-id="349" tvg-name="重温经典",group-title="数字" zz-definition="HD",重温经典
rtp://239.3.1.25:8001
#EXTINF:-1 channel-number="202" tvg-id="202" tvg-name="热播剧场" group-title="数字" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221225855/10000100000000060000000000025078_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",热播剧场
rtp://239.3.1.194:9020
#EXTINF:-1 channel-number="204" tvg-id="204" tvg-name="魅力时尚" group-title="数字" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221225850/10000100000000060000000000025079_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",魅力时尚
rtp://239.3.1.196:9012
#EXTINF:-1 channel-number="207" tvg-id="207" tvg-name="少儿动画" group-title="数字" zz-definition="HD" catchup="default" catchup-source="rtsp://61.135.88.136/TVOD/88888892/224/3221225859/10000100000000060000000000025083_0.smil?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}",少儿动画
rtp://239.3.1.199:9000
""";

    public static String getBuiltinM3u(String url) {
        if (url != null && url.startsWith("builtin://")) {
            String key = url.replace("builtin://", "");
            if ("yuechan_iptv".equals(key)) {
                return M3U;
            }
        }
        return "";
    }

    public static final String VOD_JSON = """
{
  "spider": "./jar/fan.txt;md5;8432d174d72d5b608ae1bcd16d966847",
  "logo": "https://avatars.githubusercontent.com/u/58679624?v=4",
  "lives": [
    {
      "name": "电视直播",
      "type": 0,
      "url": "builtin://yuechan_iptv",
      "playerType": 1,
      "epg": "http://epg.cdn.loc.cc/?ch={name}&date={date}",
      "logo": "https://logo.wyfc.qzz.io/{name}.png"
    },
    {
      "name": "初秋语•ipv4",
      "type": 0,
      "url": "./list.txt",
      "playerType": 2,
      "epg": "http://epg.cdn.loc.cc/?ch={name}&date={date}",
      "logo": "https://live.fanmingming.com/tv/{name}.png"
    },
    {
      "name": "YanG•综合",
      "type": 0,
      "url": "https://tv.iill.top/m3u/Gather",
      "ua": "okhttp/3.15",
      "playerType": 2,
      "epg": "http://epg.cdn.loc.cc/?ch={name}&date={date}"
    },
    {
      "name": "YanG•直播",
      "type": 0,
      "url": "https://tv.iill.top/m3u/Live",
      "ua": "okhttp/3.15",
      "playerType": 2
    },
    {
      "name": "YanG•体育",
      "type": 0,
      "url": "https://tv.iill.top/m3u/Sport",
      "ua": "okhttp/3.15",
      "playerType": 2
    },
    {
      "name": "范明明•ipv6",
      "type": 0,
      "url": "https://live.fanmingming.com/tv/m3u/ipv6.m3u",
      "playerType": 2,
      "epg": "http://epg.cdn.loc.cc/?ch={name}&date={date}",
      "logo": "https://logo.wyfc.qzz.io/{name}.png"
    },
    {
      "name": "Yoursmile•综合",
      "type": 0,
      "url": "https://agit.ai/Yoursmile7/TVBox/raw/branch/master/live.txt",
      "playerType": 1,
      "epg": "http://epg.cdn.loc.cc/?ch={name}&date={date}",
      "logo": "https://logo.wyfc.qzz.io/{name}.png"
    },
    {
      "name": "MemoryC•综合",
      "type": 0,
      "url": "https://ghp.ci/raw.githubusercontent.com/MemoryCollection/IPTV/refs/heads/main/itvlist.m3u",
      "playerType": 1,
      "ua": "okhttp/3.15",
      "epg": "http://epg.cdn.loc.cc/?ch={name}&date={date}",
      "logo": "https://logo.wyfc.qzz.io/{name}.png"
    },
    {
      "name": "肥猫•综合",
      "type": 0,
      "url": "http://我不是.肥猫.live/TV/tvzb.txt",
      "playerType": 1,
      "epg": "http://epg.cdn.loc.cc/?ch={name}&date={date}",
      "logo": "https://logo.wyfc.qzz.io/{name}.png"
    },
    {
      "name": "Ray•综合",
      "type": 0,
      "url": "https://github.moeyy.xyz/https://raw.githubusercontent.com/dxawi/0/main/tvlive.txt",
      "playerType": 1,
      "epg": "http://epg.cdn.loc.cc/?ch={name}&date={date}",
      "logo": "https://logo.wyfc.qzz.io/{name}.png"
    },
    {
      "name": "俊于•综合",
      "type": 0,
      "url": "http://home.jundie.top:81/Cat/tv/live.txt",
      "playerType": 1,
      "epg": "http://epg.cdn.loc.cc/?ch={name}&date={date}",
      "logo": "https://logo.wyfc.qzz.io/{name}.png"
    },
    {
      "name": "初秋语•电台",
      "type": 0,
      "url": "./radio.txt",
      "playerType": 1
    },
    {
      "name": "范明明•电台1",
      "type": 0,
      "url": "https://github.moeyy.xyz/https://raw.githubusercontent.com/fanmingming/live/main/radio/m3u/fm.m3u",
      "playerType": 1
    },
    {
      "name": "范明明•电台2",
      "type": 0,
      "url": "https://github.moeyy.xyz/https://raw.githubusercontent.com/fanmingming/live/main/radio/m3u/index.m3u",
      "playerType": 1
    },
    {
      "name": "16万•MV",
      "type": 0,
      "url": "https://github.moeyy.xyz/https://raw.githubusercontent.com/lystv/short/main/影视/tvb/MTV.txt"
    },
    {
      "name": "YuanHsing•油管",
      "type": 0,
      "url": "https://github.moeyy.xyz/https://raw.githubusercontent.com/YuanHsing/YouTube_to_m3u/main/youtube.m3u"
    }
  ],
  "wallpaper": "https://深色壁纸.xxooo.cf/",
  "sites": [
    {
      "key": "豆",
      "name": "【首页】",
      "type": 3,
      "api": "csp_DouDouGuard",
      "indexs": 1,
      "searchable": 0,
      "quickSearch": 0,
      "changeable": 0
    },
    {
      "key": "玩偶",
      "name": "👽玩偶哥哥┃4K弹幕",
      "type": 3,
      "api": "csp_WoGGGuard",
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 0,
      "ext": {
        "Cloud-drive": "tvfan/Cloud-drive.txt",
        "from": "4k|auto",
        "siteUrl": "https://www.wogg.com/",
        "danMu": "弹"
      }
    },
    {
      "key": "YGP",
      "name": "🚀叨观荐影┃预告片",
      "type": 3,
      "api": "csp_YGPGuard",
      "searchable": 0,
      "quickSearch": 0,
      "changeable": 0
    },
    {
      "key": "alllive",
      "name": "📽️一直播┃直播",
      "type": 3,
      "api": "csp_AllliveGuard",
      "playerType": 2,
      "searchable": 0,
      "quickSearch": 0,
      "changeable": 0
    },
    {
      "key": "抠搜",
      "name": "🍄抠抠┃搜搜",
      "type": 3,
      "api": "csp_KkSsGuard",
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 0,
      "ext": {
        "Cloud-drive": "tvfan/Cloud-drive.txt",
        "from": "4k|auto"
      }
    },
    {
      "key": "UC",
      "name": "🌈优汐┃搜搜",
      "type": 3,
      "api": "csp_UuSsGuard",
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 0,
      "ext": {
        "Cloud-drive": "tvfan/Cloud-drive.txt",
        "from": "4k|auto"
      }
    },
    {
      "key": "原创",
      "name": "☀原创┃不卡",
      "type": 3,
      "api": "csp_YCyzGuard",
      "timeout": 10,
      "playerType": 1,
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 1
    },
    {
      "key": "苹果",
      "name": "🍎苹果┃不卡",
      "type": 3,
      "api": "csp_LiteAppleGuard",
      "timeout": 10,
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 1
    },
    {
      "key": "糯米",
      "name": "🍓糯米┃秒播",
      "type": 3,
      "api": "csp_NmyswvGuard",
      "timeout": 10,
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 1
    },
    {
      "key": "白白",
      "name": "🐟白白┃秒播",
      "type": 3,
      "api": "csp_SbaibaiGuard",
      "timeout": 10,
      "playerType": 2,
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 1
    },
    {
      "key": "文采",
      "name": "💮文采┃秒播",
      "type": 3,
      "api": "csp_JpysGuard",
      "timeout": 10,
      "playerType": 2,
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 1
    },
    {
      "key": "神车",
      "name": "🐻小熊┃秒播",
      "type": 3,
      "api": "csp_AppSKGuard",
      "timeout": 10,
      "searchable": 1,
      "quickSearch": 0,
      "changeable": 0,
      "ext": "rfOX1voDIQhH8epBwtCFsq+8syyZ18T30GkibomjS1xHcE9fpWU7oKJ1cO9K0M5hrnOHZ9dKjiJz5HEyTr57RZHCqunfFT7jH/mEy+uqVyo="
    },
    {
      "key": "Lib",
      "name": "🌟立播┃秒播",
      "type": 3,
      "api": "csp_LibvioGuard",
      "timeout": 10,
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 1,
      "ext": {
        "Cloud-drive": "tvfan/Cloud-drive.txt",
        "from": "4k|auto"
      }
    },
    {
      "key": "zxzj",
      "name": "🍊在线┃秒播",
      "type": 3,
      "api": "csp_ZxzjGuard",
      "timeout": 10,
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 1,
      "ext": "https://www.zxzjhd.com/"
    },
    {
      "key": "厂长",
      "name": "📔厂长┃不卡",
      "type": 3,
      "api": "csp_NewCzGuard",
      "timeout": 10,
      "playerType": 2,
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 1
    },
    {
      "key": "溢彩",
      "name": "💡流光┃秒播",
      "type": 3,
      "api": "csp_AppSxGuard",
      "timeout": 10,
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 1,
      "ext": "rfOb1uAWbkRHp7hdxprG9un3+T/f19e82TUvZMviAElDfhsS/jcju7U0fe1MnN1h63CTaYIPjmEw9C0qVsgBO8um85+eGlSkG/r2q6m0DA=="
    },
    {
      "key": "比特",
      "name": "🍄比特┃手机",
      "type": 3,
      "api": "csp_BttwooGuard",
      "timeout": 10,
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 1
    },
    {
      "key": "低端",
      "name": "⏮️低端┃外剧",
      "type": 3,
      "api": "csp_DdrkGuard",
      "timeout": 10,
      "playerType": "2",
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 1
    },
    {
      "key": "萌米",
      "name": "👀萌米┃多线",
      "type": 3,
      "api": "csp_AppTTGuard",
      "timeout": 10,
      "playerType": 2,
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 1,
      "ext": "uqGL1bNENEIVq+dC1p/Y9uWjuA=="
    },
    {
      "key": "兄弟",
      "name": "🍊水星┃多线",
      "type": 3,
      "api": "csp_AppSxGuard",
      "timeout": 10,
      "searchable": 1,
      "quickSearch": 1,
      "filterable": 1,
      "ext": "rfOb1uAWbkRHp7hdxprG9un3+SLP183q3ik3cJDiAwlFdF8L6SIvrvc9LrpTyIg76T7QJZdEkWNj43wiSaA0TJyQpu2IF2jsSLWFx7WkAmG40hFxJ1tI+Jf+EVG8DtoDRcNi+TtVGULnWrSz3EWnVcxR3EJhXnrwYWe1kJtNW5txuHAO"
    },
    {
      "key": "热播",
      "name": "📺热播┃多线",
      "type": 3,
      "api": "csp_AppTTGuard",
      "timeout": 10,
      "playerType": 2,
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 1,
      "ext": "uqGL1bNENExT7/hGxpSE5qU="
    },
    {
      "key": "欢视",
      "name": "👓欢视┃多线",
      "type": 3,
      "api": "csp_AppTTGuard",
      "timeout": 10,
      "playerType": 2,
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 1,
      "ext": "uqGL1bNENExT9fFAy5mE5qU="
    },
    {
      "key": "奥特",
      "name": "🏝奥特┃多线",
      "type": 3,
      "api": "csp_AueteGuard",
      "timeout": 10,
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 1,
      "ext": "https://auete.com/"
    },
    {
      "key": "贱贱",
      "name": "🐭荐片┃P2P",
      "type": 3,
      "api": "csp_JPJGuard",
      "timeout": 10,
      "playerType": 2,
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 0
    },
    {
      "key": "新6V",
      "name": "🧲新6V┃磁力",
      "type": 3,
      "api": "csp_SixVGuard",
      "timeout": 10,
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 0,
      "ext": "https://www.xb6v.com/"
    },
    {
      "key": "Dm84",
      "name": "🚌巴士┃动漫",
      "type": 3,
      "api": "csp_Dm84Guard",
      "timeout": 10,
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 1
    },
    {
      "key": "Ysj",
      "name": "🎀异界┃动漫",
      "type": 3,
      "api": "csp_YsjGuard",
      "timeout": 10,
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 1
    },
    {
      "key": "Anime1",
      "name": "🐾日本┃动漫",
      "type": 3,
      "api": "csp_Anime1Guard",
      "timeout": 10,
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 1
    },
    {
      "key": "926",
      "name": "⚽926┃看球",
      "type": 3,
      "api": "csp_kanqiu926Guard",
      "timeout": 10,
      "searchable": 0,
      "changeable": 0,
      "style": {
        "type": "list"
      }
    },
    {
      "key": "88",
      "name": "⚽88┃看球",
      "type": 3,
      "api": "csp_Sir88Guard",
      "timeout": 10,
      "searchable": 0,
      "changeable": 0,
      "style": {
        "type": "list"
      }
    },
    {
      "key": "看球",
      "name": "⚽看球┃直播",
      "type": 3,
      "api": "csp_KanqiuGuard",
      "timeout": 10,
      "searchable": 0,
      "changeable": 0,
      "style": {
        "type": "list"
      }
    },
    {
      "key": "Jrsjs",
      "name": "⚽Jrs┃球迷",
      "type": 3,
      "api": "https://gh-proxy.net/https://raw.githubusercontent.com/fantaiying7/EXT/refs/heads/main/drpy2.min.js",
      "ext": "https://gh-proxy.net/https://raw.githubusercontent.com/fantaiying7/EXT/refs/heads/main/jrk.js",
      "style": {
        "type": "list"
      },
      "timeout": 10,
      "searchable": 0,
      "quickSearch": 0,
      "changeable": 0
    },
    {
      "key": "酷奇",
      "name": "🎤酷奇┃MV",
      "type": 3,
      "api": "https://gh-proxy.net/https://raw.githubusercontent.com/fantaiying7/EXT/refs/heads/main/drpy2.min.js",
      "ext": "https://gh-proxy.net/https://raw.githubusercontent.com/fantaiying7/EXT/refs/heads/main/酷奇MV.js",
      "style": {
        "type": "rect",
        "ratio": 1.597
      },
      "timeout": 10,
      "searchable": 0,
      "quickSearch": 0,
      "changeable": 0
    },
    {
      "key": "MTV",
      "name": "🎧明星┃MV",
      "type": 3,
      "api": "csp_BiliGuard",
      "style": {
        "type": "rect",
        "ratio": 1.597
      },
      "searchable": 0,
      "quickSearch": 0,
      "changeable": 0,
      "ext": {
        "json": "https://im.feelec.com.cn/res/file.html?id=2c9a91099584ed490195f7bf50ac60b9"
      }
    },
    {
      "key": "虎牙直播js",
      "name": "🐯虎牙┃直播",
      "type": 3,
      "api": "https://gh-proxy.net/https://raw.githubusercontent.com/fantaiying7/EXT/refs/heads/main/drpy2.js",
      "ext": "https://gh-proxy.net/https://raw.githubusercontent.com/fantaiying7/EXT/refs/heads/main/huya2.js",
      "style": {
        "type": "rect",
        "ratio": 1.755
      },
      "timeout": 10,
      "playerType": "2",
      "searchable": 0,
      "quickSearch": 0,
      "changeable": 0
    },
    {
      "key": "斗鱼js",
      "name": "🐟斗鱼┃直播",
      "type": 3,
      "api": "https://gh-proxy.net/https://raw.githubusercontent.com/fantaiying7/EXT/refs/heads/main/drpy2.min.js",
      "ext": "https://gh-proxy.net/https://raw.githubusercontent.com/fantaiying7/EXT/refs/heads/main/斗鱼直播.js",
      "style": {
        "type": "rect",
        "ratio": 1.755
      },
      "timeout": 10,
      "playerType": "2",
      "searchable": 0,
      "quickSearch": 0,
      "changeable": 0
    },
    {
      "key": "有声小说js",
      "name": "🎧有声┃小说",
      "type": 3,
      "api": "https://gh-proxy.net/https://raw.githubusercontent.com/fantaiying7/EXT/refs/heads/main/drpy2.min.js",
      "ext": "https://gh-proxy.net/https://raw.githubusercontent.com/fantaiying7/EXT/refs/heads/main/有声小说吧.js",
      "style": {
        "type": "rect",
        "ratio": 1
      },
      "timeout": 10,
      "searchable": 0,
      "quickSearch": 0,
      "changeable": 0
    },
    {
      "key": "Aid",
      "name": "🚑急救┃教学",
      "type": 3,
      "api": "csp_FirstAidGuard",
      "searchable": 0,
      "quickSearch": 0,
      "changeable": 0,
      "style": {
        "type": "rect",
        "ratio": 3.8
      }
    },
    {
      "key": "YpanSo",
      "name": "🐟盘她┃三盘",
      "type": 3,
      "api": "csp_YpanSoGuard",
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 0,
      "ext": {
        "Cloud-drive": "tvfan/Cloud-drive.txt",
        "from": "4k|auto"
      }
    },
    {
      "key": "xzso",
      "name": "👻盘它┃三盘",
      "type": 3,
      "api": "csp_XzsoGuard",
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 0,
      "ext": {
        "Cloud-drive": "tvfan/Cloud-drive.txt",
        "from": "4k|auto"
      }
    },
    {
      "key": "米搜",
      "name": "🦋米搜┃夸父",
      "type": 3,
      "api": "csp_MIPanSoGuard",
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 0,
      "ext": {
        "Cloud-drive": "tvfan/Cloud-drive.txt",
        "from": "4k|auto"
      }
    },
    {
      "key": "夸搜",
      "name": "😻夸搜┃夸父",
      "type": 3,
      "api": "csp_PanSearchGuard",
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 0,
      "ext": {
        "pan": "quark",
        "Cloud-drive": "tvfan/Cloud-drive.txt",
        "from": "4k|auto"
      }
    },
    {
      "key": "Aliso",
      "name": "🙀盘搜┃阿狸",
      "type": 3,
      "api": "csp_PanSearchGuard",
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 0,
      "ext": {
        "Cloud-drive": "tvfan/Cloud-drive.txt",
        "from": "4k|auto"
      }
    },
    {
      "key": "YiSo",
      "name": "😹易搜┃阿狸",
      "type": 3,
      "api": "csp_YiSoGuard",
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 0,
      "ext": {
        "Cloud-drive": "tvfan/Cloud-drive.txt",
        "from": "4k|auto"
      }
    },
    {
      "key": "push_agent",
      "name": "🛴手机┃推送",
      "type": 3,
      "api": "csp_PushGuard",
      "searchable": 0,
      "quickSearch": 0,
      "ext": {
        "Cloud-drive": "tvfan/Cloud-drive.txt",
        "from": "4k|auto"
      }
    },
    {
      "key": "csp_AList",
      "name": "AList┃网盘",
      "type": "3",
      "api": "csp_AList",
      "searchable": "0",
      "quickSearch": "0",
      "filterable": "1",
      "changeable": 0,
      "ext": "./json/alist.json"
    },
    {
      "key": "新6V",
      "name": "新6V磁力┃慎用",
      "type": 3,
      "api": "csp_SixV",
      "searchable": 1,
      "quickSearch": 1,
      "changeable": 0,
      "ext": "http://www.xb6v.com/",
      "timeout": 60
    },
    {
      "key": "百度",
      "name": "百度┃采集",
      "type": 1,
      "api": "https://api.apibdzy.com/api.php/provide/vod?ac=list",
      "searchable": 1,
      "filterable": 0,
      "categories": [
        "国产动漫",
        "日韩动漫",
        "大陆剧",
        "欧美剧",
        "韩剧",
        "日剧",
        "动作片",
        "喜剧片",
        "爱情片",
        "科幻片",
        "恐怖片",
        "剧情片",
        "战争片"
      ]
    },
    {
      "key": "量子",
      "name": "量子┃采集",
      "type": 0,
      "api": "https://cj.lziapi.com/api.php/provide/vod/at/xml/",
      "searchable": 1,
      "changeable": 1,
      "categories": [
        "国产动漫",
        "日韩动漫",
        "国产剧",
        "韩国剧",
        "日本剧",
        "电影片",
        "连续剧",
        "综艺片",
        "动漫片",
        "动作片",
        "喜剧片",
        "爱情片",
        "科幻片",
        "恐怖片",
        "剧情片",
        "战争片",
        "台湾剧",
        "香港剧",
        "欧美剧",
        "记录片",
        "海外剧",
        "泰国剧",
        "大陆综艺",
        "港台综艺",
        "日韩综艺",
        "欧美综艺",
        "欧美动漫",
        "港台动漫",
        "海外动漫",
        "体育",
        "足球",
        "篮球",
        "网球",
        "斯诺克"
      ]
    },
    {
      "key": "非凡",
      "name": "非凡┃采集",
      "type": 0,
      "api": "http://cj.ffzyapi.com/api.php/provide/vod/at/xml/",
      "searchable": 1,
      "changeable": 1,
      "categories": [
        "国产动漫",
        "日韩动漫",
        "国产剧",
        "韩国剧",
        "日本剧",
        "电影片",
        "连续剧",
        "综艺片",
        "动漫片",
        "动作片",
        "喜剧片",
        "爱情片",
        "科幻片",
        "恐怖片",
        "剧情片",
        "战争片",
        "香港剧",
        "欧美剧",
        "记录片",
        "台湾剧",
        "海外剧",
        "泰国剧",
        "大陆综艺",
        "港台综艺",
        "日韩综艺",
        "欧美综艺",
        "欧美动漫",
        "港台动漫",
        "海外动漫"
      ]
    },
    {
      "key": "haiwaikan",
      "name": "海外看┃采集",
      "type": 1,
      "api": "https://haiwaikan.com/api.php/provide/vod",
      "searchable": 1,
      "changeable": 1
    },
    {
      "key": "暴風",
      "name": "暴風┃采集",
      "type": 1,
      "api": "https://bfzyapi.com/api.php/provide/vod",
      "searchable": 1,
      "changeable": 1
    },
    {
      "key": "索尼",
      "name": "索尼┃采集",
      "type": 1,
      "api": "https://suoniapi.com/api.php/provide/vod",
      "searchable": 1,
      "changeable": 1
    },
    {
      "key": "快帆",
      "name": "快帆┃采集",
      "type": 1,
      "api": "https://api.kuaifan.tv/api.php/provide/vod",
      "searchable": 1,
      "changeable": 1
    },
    {
      "key": "drpy_js_360影视",
      "name": "官源┃360[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "ext": "./js/360影视.js"
    },
    {
      "key": "drpy_js_奇珍异兽",
      "name": "官源┃爱奇艺[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "ext": "./js/奇珍异兽.js"
    },
    {
      "key": "drpy_js_百忙无果",
      "name": "官源┃芒果[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "ext": "./js/百忙无果.js"
    },
    {
      "key": "drpy_js_腾云驾雾",
      "name": "官源┃腾讯[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "ext": "./js/腾云驾雾.js"
    },
    {
      "key": "drpy_js_菜狗",
      "name": "官源┃搜狗[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "ext": "./js/菜狗.js"
    },
    {
      "key": "drpy_js_优酷",
      "name": "官源┃优酷[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "ext": "./js/优酷.js"
    },
    {
      "key": "drpy_js_我的哔哩",
      "name": "弹幕┃我的哔哩[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "style": {
        "type": "rect",
        "ratio": 1.597
      },
      "changeable": 0,
      "ext": "./js/我的哔哩.js"
    },
    {
      "key": "drpy_js_哔哩直播",
      "name": "弹幕┃哔哩直播[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "style": {
        "type": "rect",
        "ratio": 1.597
      },
      "changeable": 0,
      "ext": "./js/哔哩直播.js"
    },
    {
      "key": "drpy_js_JustLive",
      "name": "直播┃JustLive[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "style": {
        "type": "rect",
        "ratio": 1.597
      },
      "changeable": 0,
      "ext": "./js/JustLive.js"
    },
    {
      "key": "drpy_js_斗鱼直播",
      "name": "直播┃斗鱼[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "style": {
        "type": "rect",
        "ratio": 1.597
      },
      "changeable": 0,
      "ext": "./js/斗鱼直播.js"
    },
    {
      "key": "drpy_js_虎牙直播",
      "name": "直播┃虎牙[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "style": {
        "type": "rect",
        "ratio": 1.597
      },
      "changeable": 0,
      "ext": "./js/虎牙直播.js"
    },
    {
      "key": "drpy_js_童趣",
      "name": "少儿┃童趣[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "changeable": 0,
      "ext": "./js/童趣.js"
    },
    {
      "key": "drpy_js_兔小贝",
      "name": "少儿┃兔小贝[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "changeable": 0,
      "ext": "./js/兔小贝.js"
    },
    {
      "key": "drpy_js_AnFuns",
      "name": "动漫┃AnFuns[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "ext": "./js/AnFuns.js"
    },
    {
      "key": "drpy_js_NT动漫",
      "name": "动漫┃NT动漫[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "ext": "./js/NT动漫.js"
    },
    {
      "key": "drpy_js_NyaFun",
      "name": "动漫┃NyaFun[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "ext": "./js/NyaFun.js"
    },
    {
      "key": "drpy_js_i275听书",
      "name": "听书┃i275听书[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "changeable": 0,
      "style": {
        "type": "rect",
        "ratio": 1
      },
      "ext": "./js/i275听书.js"
    },
    {
      "key": "drpy_js_爱上你听书网",
      "name": "听书┃爱上你听书[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "changeable": 0,
      "style": {
        "type": "rect",
        "ratio": 1
      },
      "ext": "./js/爱上你听书网.js"
    },
    {
      "key": "drpy_js_博看听书",
      "name": "听书┃博看听书[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "changeable": 0,
      "style": {
        "type": "rect",
        "ratio": 1
      },
      "ext": "./js/博看听书.js"
    },
    {
      "key": "有声小说js",
      "name": "听书┃有声小说[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "ext": "./js/有声小说吧.js",
      "style": {
        "type": "rect",
        "ratio": 1
      },
      "searchable": 0,
      "quickSearch": 0,
      "changeable": 0
    },
    {
      "key": "drpy_js_评书随身听",
      "name": "评书┃评书随身听[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "changeable": 0,
      "ext": "./js/评书随身听.js"
    },
    {
      "key": "drpy_js_相声随身听",
      "name": "相声┃相声随身听[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "changeable": 0,
      "ext": "./js/相声随身听.js"
    },
    {
      "key": "drpy_js_好趣网",
      "name": "电视┃好趣网[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "changeable": 0,
      "style": {
        "type": "oval",
        "ratio": 1.1
      },
      "ext": "./js/好趣网.js"
    },
    {
      "key": "drpy_js_广播迷FM",
      "name": "广播┃广播迷FM[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "changeable": 0,
      "ext": "./js/广播迷FM.js"
    },
    {
      "key": "drpy_js_蜻蜓FM",
      "name": "广播┃蜻蜓FM[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "changeable": 0,
      "ext": "./js/蜻蜓FM.js"
    },
    {
      "key": "drpy_js_DJ音乐",
      "name": "音频┃DJ音乐[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "changeable": 0,
      "ext": "./js/DJ音乐.js"
    },
    {
      "key": "drpy_js_短视频",
      "name": "聚合┃短视频[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "changeable": 0,
      "ext": "./js/短视频.js"
    },
    {
      "key": "drpy_js_酷6网",
      "name": "聚合┃酷6网[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "changeable": 0,
      "ext": "./js/酷6网.js"
    },
    {
      "key": "drpy_js_网易公版影像",
      "name": "聚合┃网易公版[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "changeable": 0,
      "ext": "./js/网易公版影像.js"
    },
    {
      "key": "drpy_js_酷奇MV",
      "name": "MV┃酷奇[js]",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "changeable": 0,
      "ext": "./js/酷奇MV.js"
    },
    {
      "key": "bb",
      "name": "配置接口完全免费",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "ext": "./js/drpy.js"
    },
    {
      "key": "cc",
      "name": "请勿相信视频中任何广告",
      "type": 3,
      "api": "./lib/drpy2.min.js",
      "ext": "./js/drpy.js"
    }
  ],
  "parses": [
    {
      "name": "Json聚合",
      "type": 3,
      "url": "Demo"
    },
    {
      "name": "虾米",
      "type": 0,
      "url": "https://jx.xmflv.com/?url=",
      "ext": {
        "flag": [
          "qq",
          "腾讯",
          "qiyi",
          "爱奇艺",
          "奇艺",
          "youku",
          "优酷",
          "sohu",
          "搜狐",
          "letv",
          "乐视",
          "mgtv",
          "芒果",
          "imgo",
          "rx",
          "ltnb",
          "bilibili",
          "1905",
          "xigua"
        ]
      }
    },
    {
      "name": "PM",
      "url": "https://www.playm3u8.cn/jiexi.php?url=",
      "type": 0,
      "ext": {
        "flag": [
          "qiyi",
          "imgo",
          "爱奇艺",
          "奇艺",
          "qq",
          "腾讯",
          "youku",
          "优酷",
          "pptv",
          "PPTV",
          "letv",
          "乐视",
          "leshi",
          "bilibili",
          "哔哩哔哩",
          "哔哩",
          "mgtv",
          "芒果",
          "sohu",
          "xigua",
          "fun",
          "风行"
        ],
        "header": {
          "User-Agent": "Mozilla/5.0"
        }
      },
      "header": {
        "User-Agent": "Mozilla/5.0"
      }
    },
    {
      "name": "m3u8",
      "type": 0,
      "url": "https://jx.m3u8.tv/jiexi/?url="
    },
    {
      "name": "8090",
      "url": "https://www.8090.la/8090/?url=",
      "type": 0,
      "ext": {
        "flag": [
          "qiyi",
          "imgo",
          "爱奇艺",
          "奇艺",
          "qq",
          "腾讯",
          "youku",
          "优酷",
          "pptv",
          "PPTV",
          "letv",
          "乐视",
          "leshi",
          "bilibili",
          "哔哩哔哩",
          "哔哩",
          "mgtv",
          "芒果",
          "sohu",
          "xigua",
          "fun",
          "风行"
        ],
        "header": {
          "User-Agent": "Mozilla/5.0"
        }
      },
      "header": {
        "User-Agent": "Mozilla/5.0"
      }
    },
    {
      "name": "看看",
      "type": 0,
      "url": "https://jx.m3u8.pw/?url="
    },
    {
      "name": "咸鱼",
      "type": 0,
      "url": "https://jx.xyflv.cc/?url=",
      "ext": {
        "header": {
          "user-agent": "Mozilla/5.0(Linux;Android13;V2049ABuild/TP1A.220624.014;wv)AppleWebKit/537.36(KHTML,likeGecko)Version/4.0Chrome/116.0.0.0MobileSafari/537.36",
          "referer": "https://www.xyflv.cc/"
        }
      }
    },
    {
      "name": "云解析",
      "type": 0,
      "url": "https://jx.yparse.com/index.php?url=",
      "ext": {
        "header": {
          "user-agent": "Mozilla/5.0(Linux;Android13;V2049ABuild/TP1A.220624.014;wv)AppleWebKit/537.36(KHTML,likeGecko)Version/4.0Chrome/116.0.0.0MobileSafari/537.36"
        }
      }
    },
    {
      "name": "爱豆",
      "type": 0,
      "url": "https://jx.aidouer.net/?url=",
      "ext": {
        "header": {
          "user-agent": "Mozilla/5.0(Linux;Android13;V2049ABuild/TP1A.220624.014;wv)AppleWebKit/537.36(KHTML,likeGecko)Version/4.0Chrome/116.0.0.0MobileSafari/537.36",
          "referer": "https://jiejie.uk/"
        }
      }
    },
    {
      "name": "巧技",
      "type": 1,
      "url": "http://pan.qiaoji8.com/tvbox/neibu.php?url=",
      "ext": {
        "flag": [
          "qq",
          "腾讯",
          "qiyi",
          "爱奇艺",
          "奇艺",
          "youku",
          "优酷",
          "sohu",
          "搜狐",
          "letv",
          "乐视",
          "mgtv",
          "芒果",
          "tnmb",
          "seven",
          "bilibili",
          "1905"
        ],
        "header": {
          "User-Agent": "okhttp/4.9.1"
        }
      }
    },
    {
      "name": "巧技二",
      "type": 1,
      "url": "http://pan.qiaoji8.com/tvbox/gouzi.php?url=",
      "ext": {
        "flag": [
          "qq",
          "腾讯",
          "qiyi",
          "爱奇艺",
          "奇艺",
          "youku",
          "优酷",
          "sohu",
          "搜狐",
          "letv",
          "乐视",
          "mgtv",
          "芒果",
          "tnmb",
          "seven",
          "bilibili",
          "1905",
          "NetFilx"
        ],
        "header": {
          "User-Agent": "okhttp/4.9.1"
        }
      }
    }
  ],
  "flags": [
    "youku",
    "优酷",
    "优 酷",
    "优酷视频",
    "qq",
    "腾讯",
    "腾 讯",
    "腾讯视频",
    "iqiyi",
    "qiyi",
    "奇艺",
    "爱奇艺",
    "爱 奇 艺",
    "m1905",
    "xigua",
    "letv",
    "leshi",
    "乐视",
    "乐 视",
    "sohu",
    "搜狐",
    "搜 狐",
    "搜狐视频",
    "tudou",
    "mgtv",
    "芒果",
    "imgo",
    "芒果TV",
    "芒 果 T V",
    "bilibili",
    "哔 哩",
    "哔 哩 哔 哩",
    "SPA",
    "YuMi-vip",
    "pptv",
    "PPTV",
    "ltnb",
    "rx",
    "SLYS4k",
    "tucheng",
    "BYGA",
    "luanzi",
    "dxzy",
    "QEYSS",
    "aliyun",
    "AliS",
    "122",
    "chuangying",
    "CL4K",
    "xfyun",
    "wuduzy",
    "wasu",
    "renrenmi",
    "ppayun",
    "haiwaikan",
    "cool",
    "dbm3u8",
    "xmm",
    "funshion",
    "ruyi1080",
    "ruyib1080"
  ],
  "doh": [
    {
      "name": "Google",
      "url": "https://dns.google/dns-query",
      "ips": [
        "8.8.4.4",
        "8.8.8.8"
      ]
    },
    {
      "name": "Cloudflare",
      "url": "https://cloudflare-dns.com/dns-query",
      "ips": [
        "1.1.1.1",
        "1.0.0.1",
        "2606:4700:4700::1111",
        "2606:4700:4700::1001"
      ]
    },
    {
      "name": "AdGuard",
      "url": "https://dns.adguard.com/dns-query",
      "ips": [
        "94.140.14.140",
        "94.140.14.141"
      ]
    },
    {
      "name": "DNSWatch",
      "url": "https://resolver2.dns.watch/dns-query",
      "ips": [
        "84.200.69.80",
        "84.200.70.40"
      ]
    },
    {
      "name": "Quad9",
      "url": "https://dns.quad9.net/dns-quer",
      "ips": [
        "9.9.9.9",
        "149.112.112.112"
      ]
    }
  ],
  "rules": [
    {
      "name": "kk",
      "hosts": [
        "kuaikan"
      ],
      "regex": [
        "5",
        "20.123",
        "20.167",
        "#EXT-X-DISCONTINUITY\\\\r*\\\\n*((?!#EXT-X-DISCONTINUITY)[\\\\s\\\\S])*?#EXT-X-KEY:METHOD((?!#EXT-X-DISCONTINUITY)[\\\\s\\\\S])*?#EXT-X-DISCONTINUITY"
      ]
    },
    {
      "name": "yqk",
      "hosts": [
        "yqk"
      ],
      "regex": [
        "18.4",
        "15.1666",
        "#EXT-X-DISCONTINUITY\\\\r*\\\\n*((?!#EXT-X-DISCONTINUITY)[\\\\s\\\\S])*?#EXT-X-CUE-OUT((?!#EXT-X-DISCONTINUITY)[\\\\s\\\\S])*?#EXT-X-CUE-IN"
      ]
    },
    {
      "name": "sn",
      "hosts": [
        "suonizy"
      ],
      "regex": [
        "#EXTINF.*?\\\\s+.*?original.*?\\\\.ts\\\\s+",
        "15.1666",
        "15.2666",
        "16.3333",
        "15.266667"
      ]
    },
    {
      "name": "bf",
      "hosts": [
        "bfzy"
      ],
      "regex": [
        "#EXT-X-DISCONTINUITY\\\\r*\\\\n*#EXTINF:3,[\\\\s\\\\S]*?#EXT-X-DISCONTINUITY"
      ]
    },
    {
      "name": "xx",
      "hosts": [
        "aws.ulivetv.net"
      ],
      "regex": [
        "#EXT-X-DISCONTINUITY\\\\r*\\\\n*#EXTINF:8,[\\\\s\\\\S]*?#EXT-X-DISCONTINUITY"
      ]
    },
    {
      "name": "lz",
      "hosts": [
        "vip.lz",
        "hd.lz",
        "v.cdnlz",
        "yzzy1.play"
      ],
      "regex": [
        "18.5333",
        "19.52",
        "18.6666"
      ]
    },
    {
      "name": "ff",
      "hosts": [
        "vip.ffzy",
        "hd.ffzy",
        "ffzy"
      ],
      "regex": [
        "25.0666",
        "25.08",
        "20.52",
        "25.1",
        "25.1999"
      ]
    },
    {
      "name": "hs",
      "hosts": [
        "huoshan.com"
      ],
      "regex": [
        "item_id="
      ]
    },
    {
      "name": "dy",
      "hosts": [
        "douyin.com"
      ],
      "regex": [
        "is_play_url="
      ]
    },
    {
      "name": "nm",
      "hosts": [
        "toutiaovod.com"
      ],
      "regex": [
        "video/tos/cn"
      ]
    },
    {
      "name": "cl",
      "hosts": [
        "magnet"
      ],
      "regex": [
        "最 新",
        "直 播",
        "更 新"
      ]
    }
  ]
}
""";
}
