package com.fongmi.android.tv.api.config;

public class BuiltinConfig {
    public static final String URL = "builtin";
    public static final String BASE = "https://raw.githubusercontent.com/qist/tvbox/refs/heads/master/";
    public static final String M3U = """
#EXTM3U
#EXTINF:-1 group-title="国际",CNN
https://jmp2.uk/stvp-GBBD8000016N
#EXTINF:-1 group-title="国际",BBC News
https://jmp2.uk/rok-6183f9f73a64394cf3c55690605af2a7.m3u8
#EXTINF:-1 group-title="国际",ABC
http://mytvstream.net:8080/live/30550113/30550113/120952.m3u8
#EXTINF:-1 group-title="国际",CNA
https://d2e1asnsl7br7b.cloudfront.net/7782e205e72f43aeb4a48ec97f66ebbe/index_5.m3u8
#EXTINF:-1 group-title="国际",CBS
http://mytvstream.net:8080/live/30550113/30550113/120749.m3u8
#EXTINF:-1 group-title="国际",GB News
https://rakutenaa-lightning-gbnews-rakuten-ccoa9.amagi.tv/playlist/rakutenAA-lightning-gbnews-rakuten/playlist.m3u8
#EXTINF:-1 group-title="国际",RT News
https://rt-glb.rttv.com/dvr/rtnews/playlist_4500Kb.m3u8
#EXTINF:-1 group-title="国际",Sky News
https://d39chvnxm26pgp.cloudfront.net/v1/master/72588bff830dec7b26d7cbbf5f3c24928aec5c03/cc-sthen6ms4vxgv-stage/WNSFO/ABR.m3u8
#EXTINF:-1 group-title="国际",Al Jazeera
https://live-hls-aje-ak.getaj.net/AJE/01.m3u8
#EXTINF:-1 group-title="国际",TRT World
https://tv-trtworld.medya.trt.com.tr/master_1080.m3u8
#EXTINF:-1 group-title="国际",NEWSMAX
https://nmx1ota.akamaized.net/hls/live/2107010/Live_1/3.m3u8
#EXTINF:-1 group-title="国际",NEWSMAX2
https://jmp2.uk/rok-28844a800a4e54909eec9695aa3762b2.m3u8
#EXTINF:-1 group-title="国际",Global News
https://live.corusdigitaldev.com/groupb/live/3062d0e3-ed4c-4f47-8482-95648250f4b8/live.isml/.m3u8
#EXTINF:-1 group-title="国际",NBC News Now
https://livehub-voidnet.onrender.com/cluster/streamcore/us/NBC_REDIS.m3u8
#EXTINF:-1 tvg-id="Discovery.Channel.HD.us2" tvg-name="Discovery Channel" tvg-logo="http://schedulesdirect-api20141201-logos.s3.dualstack.us-east-1.amazonaws.com/stationLogos/s11150_dark_360w_270h.png" group-title="国际",Discovery Channel
http://23.237.104.106:8080/USA_DISCOVERY/index.m3u8
#EXTINF:-1 tvg-id="Disney.XD.HD.us2" tvg-name="Disney XD" tvg-logo="http://schedulesdirect-api20141201-logos.s3.dualstack.us-east-1.amazonaws.com/stationLogos/s18279_dark_360w_270h.png" group-title="国际",Disney XD
http://23.237.104.106:8080/USA_DISNEY_XD/index.m3u8
#EXTINF:-1 tvg-id="ESPN.HD.us2" tvg-name="ESPN" tvg-logo="http://schedulesdirect-api20141201-logos.s3.dualstack.us-east-1.amazonaws.com/stationLogos/s10179_dark_360w_270h.png" group-title="国际",ESPN
http://41.205.93.154/ESPN/index.m3u8
#EXTINF:-1 tvg-id="ESPNU.HD.us2" tvg-name="ESPN U" tvg-logo="http://schedulesdirect-api20141201-logos.s3.dualstack.us-east-1.amazonaws.com/stationLogos/s45654_dark_360w_270h.png" group-title="国际",ESPN U
http://23.237.104.106:8080/USA_ESPNU/index.m3u8
#EXTINF:-1 tvg-id="ESPN2.HD.us2" tvg-name="ESPN2" tvg-logo="http://schedulesdirect-api20141201-logos.s3.dualstack.us-east-1.amazonaws.com/stationLogos/s12444_dark_360w_270h.png" group-title="国际",ESPN2
http://mytvstream.net:8080/live/30550113/30550113/2210.m3u8
#EXTINF:-1 tvg-id="Fox.News.Channel.HD.us2" tvg-name="Fox News" tvg-logo="http://schedulesdirect-api20141201-logos.s3.dualstack.us-east-1.amazonaws.com/stationLogos/s16374_dark_360w_270h.png" group-title="国际",Fox News
http://mytvstream.net:8080/live/30550113/30550113/1818.m3u8
#EXTINF:-1 tvg-id="WFLX-DT.us_locals1" tvg-name="Fox" tvg-logo="http://schedulesdirect-api20141201-logos.s3.dualstack.us-east-1.amazonaws.com/stationLogos/s28719_dark_360w_270h.png" group-title="国际",Fox
http://mytvstream.net:8080/live/30550113/30550113/121595.m3u8
#EXTINF:-1 tvg-id="FS1.Fox.Sports.1.HD.us2" tvg-name="Fox Sports 1" tvg-logo="http://schedulesdirect-api20141201-logos.s3.dualstack.us-east-1.amazonaws.com/stationLogos/s82541_dark_360w_270h.png" group-title="国际",Fox Sports 1
http://mytvstream.net:8080/live/30550113/30550113/1846.m3u8
#EXTINF:-1 tvg-id="FS2.Fox.Sports.2.HD.us2" tvg-name="Fox Sports 2" tvg-logo="http://schedulesdirect-api20141201-logos.s3.dualstack.us-east-1.amazonaws.com/stationLogos/s33178_dark_360w_270h.png" group-title="国际",Fox Sports 2
http://mytvstream.net:8080/live/30550113/30550113/1847.m3u8
#EXTINF:-1 tvg-id="HBO.East.us2" tvg-name="HBO" tvg-logo="http://schedulesdirect-api20141201-logos.s3.dualstack.us-east-1.amazonaws.com/stationLogos/s10240_dark_360w_270h.png" group-title="国际",HBO
http://23.237.104.106:8080/USA_HBO/index.m3u8
#EXTINF:-1 tvg-id="HBO2.HD.us2" tvg-name="HBO 2" tvg-logo="http://schedulesdirect-api20141201-logos.s3.dualstack.us-east-1.amazonaws.com/stationLogos/s68140_dark_360w_270h.png" group-title="国际",HBO 2
http://23.237.104.106:8080/USA_HBO2/index.m3u8
#EXTINF:-1 tvg-id="ION.Television.HD.us2" tvg-name="ION TV" tvg-logo="https://schedulesdirect-api20141201-logos.s3.dualstack.us-east-1.amazonaws.com/stationLogos/s18633_dark_360w_270h.png" group-title="国际",ION TV
http://mytvstream.net:8080/live/30550113/30550113/9297.m3u8
#EXTINF:-1 tvg-id="LMN.HD.us2" tvg-name="Lifetime Movie Network" tvg-logo="https://schedulesdirect-api20141201-logos.s3.dualstack.us-east-1.amazonaws.com/stationLogos/s18480_dark_360w_270h.png" group-title="国际",Lifetime Movie Network
http://23.237.104.106:8080/USA_LMN/index.m3u8
#EXTINF:-1 tvg-id="National.Geographic.HD.us2" tvg-name="National Geographic" tvg-logo="http://schedulesdirect-api20141201-logos.s3.dualstack.us-east-1.amazonaws.com/stationLogos/s49438_dark_360w_270h.png" group-title="国际",National Geographic
http://23.237.104.106:8080/USA_NAT_GEO/index.m3u8
#EXTINF:-1 group-title="国际",WION
https://d7x8z4yuq42qn.cloudfront.net/index_7.m3u8
#EXTINF:-1 group-title="国际",Cartoon Network
http://23.237.104.106:8080/USA_CARTOON_NETWORK/index.m3u8
#EXTINF:-1 group-title="国际",C-Span
http://mytvstream.net:8080/live/30550113/30550113/136589.m3u8
#EXTINF:-1 group-title="国际",CMT
http://23.237.104.106:8080/USA_CMT/index.m3u8
#EXTINF:-1 group-title="国际",TalkTV
https://live-talktv-ssai.simplestreamcdn.com/v1/master/774d979dd66704abea7c5b62cb34c6815fda0d35/talktv-live/index.m3u8
#EXTINF:-1 group-title="国际",Arirang
https://amdlive-ch01-ctnd-com.akamaized.net/arirang_1ch/smil:arirang_1ch.smil/chunklist_b3256000_sleng.m3u8
#EXTINF:-1 group-title="国际",RT Documentary
https://rt-rtd.rttv.com/live/rtdoc/playlist_4500Kb.m3u8
#EXTINF:-1 group-title="国际",Newsy
https://547f72e6652371c3.mediapackage.us-east-1.amazonaws.com/out/v1/e3e6e29095844c4ba7d887f01e44a5ef/index_8.m3u8
#EXTINF:-1 group-title="国际",Redbull
http://rbmn-live.akamaized.net/hls/live/590964/BoRB-AT/master_3360.m3u8
#EXTINF:-1 group-title="国际",HollyWire
https://bozztv.com/hwotta/playlist/HD3400/HD3400.m3u8
#EXTINF:-1 group-title="国际",Comedy Central
http://23.237.104.106:8080/USA_COMEDY_CENTRAL/index.m3u8
#EXTINF:-1 group-title="国际",MovieSphere
https://moviesphereuk-samsunguk.amagi.tv/playlist.m3u8
#EXTINF:-1 group-title="国际",Trace Sport Stars
https://lightning-tracesport-samsungau.amagi.tv/playlist.m3u8
#EXTINF:-1 group-title="国际",SportsGrid
https://jmp2.uk/rok-62f1cd0d4c395a2a8ab72cff688666a0.m3u8
#EXTINF:-1 group-title="国际",Eurosport 4K
http://nvsoo4tx.megogo.xyz/iptv/UWA8DBPMZFQ9PX9ZLSBM7FFX/31475/index.m3u8
#EXTINF:-1 group-title="国际",Wild Earth
https://wildearth-plex.amagi.tv/masterR1080p.m3u8
#EXTINF:-1 group-title="国际",Love Nature
http://nvsoo4tx.megogo.xyz/iptv/UWA8DBPMZFQ9PX9ZLSBM7FFX/5001/index.m3u8
#EXTINF:-1 group-title="国际",Fashion Channel
http://fl1001.bozztv.com/ushba19/tracks-v1a1/mono.m3u8
#EXTINF:-1 group-title="国际",Action Hollywood
https://cdn-apse1-prod.tsv2.amagi.tv/linear/amg01076-lightningintern-actionhollywood-samsungau/playlist.m3u8
#EXTINF:-1 group-title="国际",Mytime Movie
https://appletree-mytimeau-samsung.amagi.tv/playlist.m3u8
#EXTINF:-1 group-title="国际",Pulse
https://cdn-apse1-prod.tsv2.amagi.tv/linear/amg01076-lightningintern-pulse-samsungau/playlist.m3u8
#EXTINF:-1 group-title="国际",Tastemade
https://cdn-ue1-prod.tsv2.amagi.tv/linear/tastemade-tastemade-int-aus-samsungau/playlist.m3u8
#EXTINF:-1 group-title="国际",ESPN2
http://1tv41.icu:8080/8434692955/judgen64@yahoo.com/2210
#EXTINF:-1 group-title="国际",BBC Earth
https://aegis-cloudfront-1.tubi.video/bb1fc6ad-9948-42ea-aaf3-20acfcdeecac/playlist.m3u8
#EXTINF:-1 group-title="国际",NHL Network
http://23.237.104.106:8080/USA_NHL_NETWORK/index.m3u8
#EXTINF:-1 group-title="国际",The First
https://thefirst-oando.amagi.tv/playlist.m3u8
#EXTINF:-1 group-title="国际",Travel XP
http://nvsoo4tx.megogo.xyz/iptv/UWA8DBPMZFQ9PX9ZLSBM7FFX/14026/index.m3u8
#EXTINF:-1 group-title="国际",Trace Urban
https://lightning-traceurban-samsungau.amagi.tv/playlist.m3u8
#EXTINF:-1 group-title="国际",Kartoon Channel
https://lightning-fnf-samsungaus.amagi.tv/playlist.m3u8
#EXTINF:-1 group-title="国际",Cooking Channel
http://23.237.104.106:8080/USA_COOKING/index.m3u8
#EXTINF:-1 group-title="国际",ION Plus
https://cdn-uw2-prod.tsv2.amagi.tv/linear/amg01438-ewscrippscompan-ionplus-tablo/playlist.m3u8
#EXTINF:-1 group-title="国际",History Hit
https://cdn-ue1-prod.tsv2.amagi.tv/linear/ldsAAAAAA-timeline-samsungau/playlist.m3u8
#EXTINF:-1 group-title="国际",Outdoor Channel
https://cdn-apse1-prod.tsv2.amagi.tv/linear/amg00718-outdoorchannela-outdoortvnz-samsungnz/playlist.m3u8
#EXTINF:-1 group-title="国际",Sony One Action HITS
https://89514e758f814907be6d14bbc0aa66b7.mediatailor.us-west-2.amazonaws.com/v1/master/ba62fe743df0fe93366eba3a257d792884136c7f/LINEAR-800-UK-SONYONEACTIONHITS-LG_UK/playlist.m3u8
#EXTINF:-1 group-title="国际",Sony One Comedy HITS
https://9f8e2ffcd87f4c469be7194e48f84874.mediatailor.us-west-2.amazonaws.com/v1/master/ba62fe743df0fe93366eba3a257d792884136c7f/LINEAR-802-UK-SONYONECOMEDYHITS-LG_UK/playlist.m3u8
#EXTINF:-1 group-title="国际",Top Movies
https://0145451975a64b35866170fd2e8fa486.mediatailor.eu-west-1.amazonaws.com/v1/master/0547f18649bd788bec7b67b746e47670f558b6b2/production-LiveChannel-5987/master.m3u8
#EXTINF:-1 group-title="国际",Action Movies
https://54045f0c40fd442c8b06df076aaf1e85.mediatailor.eu-west-1.amazonaws.com/v1/master/0547f18649bd788bec7b67b746e47670f558b6b2/production-LiveChannel-6065/master.m3u8
#EXTINF:-1 group-title="国际",Comedy Movies
https://9be783d652cd4b099cf63e1dc134c4a3.mediatailor.eu-west-1.amazonaws.com/v1/master/0547f18649bd788bec7b67b746e47670f558b6b2/production-LiveChannel-6181/master.m3u8
#EXTINF:-1 group-title="国际",Thrillers
https://thriller-rakuten-tv-uk.fast.rakuten.tv/v1/master/0547f18649bd788bec7b67b746e47670f558b6b2/production-LiveChannel-6482/master.m3u8?ads.app_bundle=[APP_BUNDLE]&ads.app_name=LG%20Channel%20Plus&ads.app_store_url=[APP_STOREURL]&ads.app_version=[APP_VERSION]&ads.brand_name=[DEVICE_MAKE]&ads.content_livestream=0&ads.device_lmt=[LMT]&ads.device_model=[DEVICE_MODEL]&ads.did=[IFA]&ads.fck=[FCK]&ads.gdpr_consent=[GDPR_CONSENT]&ads.ifa_type=[IFA_TYPE]&ads.market=uk&ads.platform=lg_channelplus&ads.pod_type=playerpage_midroll&ads.rtv_channel_name=thriller-rakuten-tv_uk&ads.rtv_content_id=6482&ads.rtv_language=en&ads.viewsize=[VIEWSIZE]&channel_id=6482&disable_subtitles=true&publishing_platform_id=2
#EXTINF:-1 group-title="国际",Sci-Fi
https://sci-fi-rakuten-tv-uk.fast.rakuten.tv/v1/master/0547f18649bd788bec7b67b746e47670f558b6b2/production-LiveChannel-6241/master.m3u8?ads.app_bundle=[APP_BUNDLE]&ads.app_name=LG%20Channel%20Plus&ads.app_store_url=[APP_STOREURL]&ads.app_version=[APP_VERSION]&ads.brand_name=[DEVICE_MAKE]&ads.content_livestream=0&ads.device_lmt=[LMT]&ads.device_model=[DEVICE_MODEL]&ads.did=[IFA]&ads.fck=[FCK]&ads.gdpr_consent=[GDPR_CONSENT]&ads.ifa_type=[IFA_TYPE]&ads.market=uk&ads.platform=lg_channelplus&ads.pod_type=playerpage_midroll&ads.rtv_channel_name=sci-fi-rakuten-tv_uk&ads.rtv_content_id=6241&ads.rtv_language=en&ads.viewsize=[VIEWSIZE]&channel_id=6241&disable_subtitles=true&publishing_platform_id=2
#EXTINF:-1 group-title="国际",Romance Movies
https://romance-rakuten-tv-uk.fast.rakuten.tv/v1/master/0547f18649bd788bec7b67b746e47670f558b6b2/production-LiveChannel-6194/master.m3u8?ads.app_bundle=[APP_BUNDLE]&ads.app_name=LG%20Channel%20Plus&ads.app_store_url=[APP_STOREURL]&ads.app_version=[APP_VERSION]&ads.brand_name=[DEVICE_MAKE]&ads.content_livestream=0&ads.device_lmt=[LMT]&ads.device_model=[DEVICE_MODEL]&ads.did=[IFA]&ads.fck=[FCK]&ads.gdpr_consent=[GDPR_CONSENT]&ads.ifa_type=[IFA_TYPE]&ads.market=uk&ads.platform=lg_channelplus&ads.pod_type=playerpage_midroll&ads.rtv_channel_name=romance-rakuten-tv_uk&ads.rtv_content_id=6194&ads.rtv_language=en&ads.viewsize=[VIEWSIZE]&channel_id=6194&disable_subtitles=true&publishing_platform_id=2
#EXTINF:-1 group-title="国际",Drama Movies
https://fee09fd665814f51b939b6d106cf5f66.mediatailor.eu-west-1.amazonaws.com/v1/master/0547f18649bd788bec7b67b746e47670f558b6b2/production-LiveChannel-6093/master.m3u8
#EXTINF:-1 group-title="国际",Thriller TV
https://52aad07d9d4f4d479d9b27e08ddf9e8b.mediatailor.us-west-2.amazonaws.com/v1/master/ba62fe743df0fe93366eba3a257d792884136c7f/LINEAR-808-UK-SONYONETHRILLERTV-LG_UK/playlist.m3u8
#EXTINF:-1 group-title="国际",Comedy TV
https://ec613694ee5049d3b8f30913b619662e.mediatailor.us-west-2.amazonaws.com/v1/master/ba62fe743df0fe93366eba3a257d792884136c7f/LINEAR-804-UK-SONYONECOMEDYTV-LG_UK/playlist.m3u8
#EXTINF:-1 group-title="国际",Sony One FAVES
https://8a4805800dac428ebbf1d3cde0cdcf87.mediatailor.us-west-2.amazonaws.com/v1/master/ba62fe743df0fe93366eba3a257d792884136c7f/LINEAR-806-UK-SONYONEFAVES-LG_UK/playlist.m3u8
#EXTINF:-1 group-title="国际",Sony One Dragons' Den
https://5dae1f2dfec54d8f9992b7d2ac0bc627.mediatailor.us-west-2.amazonaws.com/v1/master/ba62fe743df0fe93366eba3a257d792884136c7f/LINEAR-810-UK-SONYONEDRAGONSDEN-LG_UK/playlist.m3u8
#EXTINF:-1 group-title="国际",Great British Menu
https://7ed93f662af44c2e9bdf93b56464f6a8.mediatailor.us-east-1.amazonaws.com/v1/master/44f73ba4d03e9607dcd9bebdcb8494d86964f1d8/LG-gb_GreatBritishMenu/playlist.m3u8
#EXTINF:-1 group-title="国际",Icon Film Channel
https://09e3020901654256a51f2f2e9213765a.mediatailor.us-east-1.amazonaws.com/v1/master/04fd913bb278d8775298c26fdca9d9841f37601f/LG-gb_IconFilmChannelClassics/playlist.m3u8
#EXTINF:-1 group-title="国际",Popflix
https://2e90a1efd576492a8eaf4364feedf8d5.mediatailor.us-east-1.amazonaws.com/v1/master/04fd913bb278d8775298c26fdca9d9841f37601f/LG-gb_Popflix/playlist.m3u8
#EXTINF:-1 group-title="国际",LG 1 Film
https://stream.ads.ottera.tv/playlist.m3u8?network_id=15465&avod=1&coppa=0&td=6&is_lat=[LMT]&dnt=[DNS]&us_privacy=[US_PRIVACY]&did=[DEVICE_ID]&ip=[IP]&ua=[UA]&ifa_type=[IFA_TYPE]&app_bundle=[APP_BUNDLE]&app_store_url=[APP_STOREURL]&app_name=[APP_NAME]&custom_targeting=html5&custom_4=lg_channels_uk&player_height=720&player_width=1280&content_channel=lg_channels&content_dist_name=lg_channels&device_make=[DEVICE_MAKE]&device_model=[DEVICE_MODEL]&device_os=html5&consent=[GDPR_CONSENT]&gdpr=[GDPR]&gender=not_specified&yob=0&livestream=1&custom_param_0=[DEVICE_MODEL]&custom_param_1=tv&custom_param_2=[DEVICE_MAKE]&custom_param_3=[DEVICE_ID]&custom_param_4=lg_channels&custom_param_5=html5&fck=[FCK]&viewsize=[VIEWSIZE]
#EXTINF:-1 group-title="国际",LG 1 Spotlight
https://stream.ads.ottera.tv/playlist.m3u8?network_id=13959&avod=1&coppa=0&td=6&is_lat=[LMT]&dnt=[DNS]&us_privacy=[US_PRIVACY]&did=[DEVICE_ID]&ip=[IP]&ua=[UA]&ifa_type=[IFA_TYPE]&app_bundle=[APP_BUNDLE]&app_store_url=[APP_STOREURL]&app_name=[APP_NAME]&custom_targeting=html5&custom_4=lg_channels_uk&player_height=720&player_width=1280&content_channel=lg_channels&content_dist_name=lg_channels&device_make=[DEVICE_MAKE]&device_model=[DEVICE_MODEL]&device_os=html5&consent=[GDPR_CONSENT]&gdpr=[GDPR]&gender=not_specified&yob=0&livestream=1&custom_param_0=[DEVICE_MODEL]&custom_param_1=tv&custom_param_2=[DEVICE_MAKE]&custom_param_3=[DEVICE_ID]&custom_param_4=lg_channels&custom_param_5=html5&fck=[FCK]&viewsize=[VIEWSIZE]
#EXTINF:-1 group-title="国际",LG 1
https://stream.ads.ottera.tv/playlist.m3u8?network_id=10957&avod=1&coppa=0&td=6&is_lat=[LMT]&dnt=[DNS]&us_privacy=[US_PRIVACY]&did=[DEVICE_ID]&ip=[IP]&ua=[UA]&ifa_type=[IFA_TYPE]&app_bundle=[APP_BUNDLE]&app_store_url=[APP_STOREURL]&app_name=[APP_NAME]&custom_targeting=html5&custom_4=lg_channels_uk&player_height=720&player_width=1280&content_channel=lg_channels&content_dist_name=lg_channels&device_make=[DEVICE_MAKE]&device_model=[DEVICE_MODEL]&device_os=html5&consent=[GDPR_CONSENT]&gdpr=[GDPR]&gender=not_specified&yob=0&livestream=1&custom_param_0=[DEVICE_MODEL]&custom_param_1=tv&custom_param_2=[DEVICE_MAKE]&custom_param_3=[DEVICE_ID]&custom_param_4=lg_channels&custom_param_5=html5&fck=[FCK]&viewsize=[VIEWSIZE]
#EXTINF:-1 group-title="国际",GoUSA TV
https://cc2b7c60df304389ba60c76790d1a82f.mediatailor.us-east-1.amazonaws.com/v1/master/44f73ba4d03e9607dcd9bebdcb8494d86964f1d8/LG-au_GoUSATV/playlist.m3u8
#EXTINF:-1 group-title="国际",Inside Outside
https://52405cd167dc41c68edcaa842b916ccd.mediatailor.us-east-1.amazonaws.com/v1/master/44f73ba4d03e9607dcd9bebdcb8494d86964f1d8/LG-au_InsideOutside/playlist.m3u8
#EXTINF:-1 group-title="国际",SBS Drama
https://ads.its-newid.net/api/manifest.m3u8?tp=lg_channels&channel_name=sbsk-drama&channel_id=newid_008&mpf=c064f5f4-39af1962-7c36be2c&apikey=48230e6b-1cea0097-15975f93-39af1962&auth=474ab4cb-5d47aa0a-84d0cbca-2a3730a6&ads.live=[CONTENT_LIVE]&ads.deviceid=[DEVICE_ID]&ads.ifa=[IFA]&ads.ifatype=[IFA_TYPE]&ads.lat=[LMT]&ads.donotsell=[DNS]&ads.ua=[UA]&ads.ip=[IP]&ads.gdpr=[GDPR]&ads.gdpr_consent=[GDPR_CONSENT]&ads.country=[COUNTRY]&ads.us_privacy=[US_PRIVACY]&ads.appstoreurl=[APP_STOREURL]&ads.bundleid=[APP_BUNDLE]&ads.appname=[APP_NAME]&ads.appversion=[APP_VERSION]&ads.devicetype=[DEVICE_TYPE]&ads.devicemake=[DEVICE_MAKE]&ads.devicemodel=[DEVICE_MODEL]&ads.targetad=[TARGETAD_ALLOWED]&ads.fck=[FCK]&ads.viewsize=[VIEWSIZE]&ads.givn=[NONCE]
#EXTINF:-1 group-title="国际",New Kmovies
https://ads.its-newid.net/api/manifest.m3u8?tp=lg_channels&channel_name=newkmovies&channel_id=newid_219&mpf=adfc204d-39af1962-13a2f01a&apikey=48230e6b-1cea0097-15975f93-39af1962&auth=b62c2c8b-8316a771-295eba28-f4663ddd&ads.live=[CONTENT_LIVE]&ads.deviceid=[DEVICE_ID]&ads.ifa=[IFA]&ads.ifatype=[IFA_TYPE]&ads.lat=[LMT]&ads.donotsell=[DNS]&ads.ua=[UA]&ads.ip=[IP]&ads.gdpr=[GDPR]&ads.gdpr_consent=[GDPR_CONSENT]&ads.country=[COUNTRY]&ads.us_privacy=[US_PRIVACY]&ads.appstoreurl=[APP_STOREURL]&ads.bundleid=[APP_BUNDLE]&ads.appname=[APP_NAME]&ads.appversion=[APP_VERSION]&ads.devicetype=[DEVICE_TYPE]&ads.devicemake=[DEVICE_MAKE]&ads.devicemodel=[DEVICE_MODEL]&ads.targetad=[TARGETAD_ALLOWED]&ads.fck=[FCK]&ads.viewsize=[VIEWSIZE]&ads.givn=[NONCE]
#EXTINF:-1 group-title="国际",Tennis Channel
https://cdn-uw2-prod.tsv2.amagi.tv/linear/amg01444-tennischannelth-tennischnlintl-lggb/playlist.m3u8?ads.deviceid=[DEVICE_ID]&ads.ifa=[IFA]&ads.ifatype=[IFA_TYPE]&ads.lat=[LMT]&ads.donotsell=[DNS]&ads.ua=[UA]&ads.ip=[IP]&ads.gdpr=[GDPR]&ads.gdprconsent=[GDPR_CONSENT]&ads.country=[COUNTRY]&ads.usprivacy=[US_PRIVACY]&ads.appstoreurl=[APP_STOREURL]&ads.bundleid=[APP_BUNDLE]&ads.appname=[APP_NAME]&ads.appversion=[APP_VERSION]&ads.devicetype=[DEVICE_TYPE]&ads.devicemake=[DEVICE_MAKE]&ads.devicemodel=[DEVICE_MODEL]&ads.coppa=0&ads.targetad=[TARGETAD_ALLOWED]&ads.fck=[FCK]&ads.viewsize=[VIEWSIZE]&ads.givn=[NONCE]
#EXTINF:-1 group-title="国际",Universal Monsters
https://d4whmvwm0rdvi.cloudfront.net/10007/99993044/hls/master.m3u8?ads.xumo_channelId=99993044&ads.asnw=169843&ads.afid=380753606&ads.sfid=17850336&ads.csid=lgchannels_us_nbcuuniversalmonsters_ssai_cro&ads._fw_is_lat=[LMT]&ads._fw_us_privacy=[US_PRIVACY]&ads._fw_coppa=0&ads._fw_did=[IFA]&ads._fw_vcid2=512116:[IFA]&ads._fw_app_bundle=[APP_BUNDLE]&ads._fw_app_store_url=[APP_STOREURL]&ads._fw_content_category=IAB1-7&ads._fw_content_genre=television&ads._fw_content_language=en&ads._fw_content_rating=tv-14&ads._fw_deviceMake=[DEVICE_MAKE]&ads._fw_device_model=[DEVICE_MODEL]&ads._fw_deviceType=3-Connected_TV&ads.appVersion=[APP_VERSION]&ads.appName=lgchannels&ads.xumo_contentId=3940&ads.xumo_contentName=NBCUUniversalMonsters&ads.xumo_providerId=3940&ads.xumo_providerName=NBCUUniversalMonsters&ads.channelId=99993044&ads._fw_ifa_type=dpid&ads.givn=[NONCE]&ads.fck=[FCK]&ads.viewsize=[VIEWSIZE]
#EXTINF:-1 group-title="国际",Billiard TV
https://stream.ads.ottera.tv/playlist.m3u8?network_id=15270&avod=1&coppa=0&td=6&is_lat=[LMT]&dnt=[DNS]&us_privacy=[US_PRIVACY]&did=[DEVICE_ID]&ip=[IP]&ua=[UA]&ifa_type=[IFA_TYPE]&app_bundle=[APP_BUNDLE]&app_store_url=[APP_STOREURL]&app_name=[APP_NAME]&custom_targeting=html5&custom_4=lg_channels_us&player_height=720&player_width=1280&content_channel=lg_channels&content_dist_name=lg_channels&device_make=[DEVICE_MAKE]&device_model=[DEVICE_MODEL]&device_os=html5&consent=[GDPR_CONSENT]&gdpr=[GDPR]&gender=not_specified&yob=0&livestream=1&custom_param_0=[DEVICE_MODEL]&custom_param_1=tv&custom_param_2=[DEVICE_MAKE]&custom_param_3=[DEVICE_ID]&custom_param_4=lg_channels&custom_param_5=html5&fck=[FCK]&viewsize=[VIEWSIZE]&nonce=[NONCE]
#EXTINF:-1 group-title="国际",Bloomberg TV
https://1cd69008d08c486ca5abc0f1c4e2ef63.mediatailor.ap-northeast-1.amazonaws.com/v1/manifest/3722c60a815c199d9c0ef36c5b73da68a62b09d1/Bloomberg-kr-prod/87e22cf4-e302-42f6-a781-1d726fc87835/0.m3u8
#EXTINF:-1 tvg-logo="https://i.imgur.com/skAiUxg.png" group-title="国际",RTM ASEAN
https://d25tgymtnqzu8s.cloudfront.net/event/smil:event1/chunklist_b2596000_slENG.m3u8
#EXTINF:-1 tvg-logo="https://jiotvimages.cdn.jio.com/dare_images/images/Brit_Asia.png" group-title="国际",BritAsiaTV
https://d35j504z0x2vu2.cloudfront.net/v1/master/0bc8e8376bd8417a1b6761138aa41c26c7309312/britasiatv/master.m3u8?ads.vf=65ah6vw-F20
#EXTINF:-1 tvg-logo="https://i.imgur.com/KxHWpQB.png" group-title="国际",TalkTV
https://live-talktv-ssai.simplestreamcdn.com/v1/master/774d979dd66704abea7c5b62cb34c6815fda0d35/talktv-live/index.m3u8
#EXTINF:-1 tvg-logo="https://d2n0069hmnqmmx.cloudfront.net/epgdata/1.0/newchanlogos/512/512/skychb1621.png" group-title="国际",Channel 4
https://viamotionhsi.netplus.ch/live/eds/channel4/browser-HLS8/channel4.m3u8
#EXTINF:-1 tvg-logo="https://d3b6luslimvglo.cloudfront.net/images/79/rlaxximages/channels-rescaled/icon-white/adventureearth_white.png" group-title="国际",Adventure Earth
https://a57e9c69976649b582a8d7604c00e69a.mediatailor.us-east-1.amazonaws.com/v1/master/44f73ba4d03e9607dcd9bebdcb8494d86964f1d8/RlaxxTV-eu_AdventureEarth/playlist.m3u8
#EXTINF:-1 tvg-logo="https://d3b6luslimvglo.cloudfront.net/images/79/rlaxximages/channels-rescaled/icon-white/autentichistory_white.png" group-title="国际",Autentic History
https://9e754fa707344ccca6d84955c8fcaf36.mediatailor.us-east-1.amazonaws.com/v1/master/44f73ba4d03e9607dcd9bebdcb8494d86964f1d8/RlaxxTV-eu_AutenticHistory/playlist.m3u8
#EXTINF:-1 tvg-logo="https://d3b6luslimvglo.cloudfront.net/images/79/rlaxximages/channels-rescaled/icon-white/autentictravel_white.png" group-title="国际",Autentic Travel
https://cb0c87cc605942ff9766a4e6744bbadc.mediatailor.us-east-1.amazonaws.com/v1/master/44f73ba4d03e9607dcd9bebdcb8494d86964f1d8/RlaxxTV-eu_AutenticTravel/playlist.m3u8
#EXTINF:-1 tvg-logo="https://i.imgur.com/Meew6eX.png" group-title="国际",Rakuten Family
https://e3207568b726401995c25670faaf32e4.mediatailor.eu-west-1.amazonaws.com/v1/master/0547f18649bd788bec7b67b746e47670f558b6b2/production-LiveChannel-6203/master.m3u8
#EXTINF:-1 tvg-logo="https://i.imgur.com/Meew6eX.png" group-title="国际",Rakuten Top Movies
https://0145451975a64b35866170fd2e8fa486.mediatailor.eu-west-1.amazonaws.com/v1/master/0547f18649bd788bec7b67b746e47670f558b6b2/production-LiveChannel-5987/master.m3u8
#EXTINF:-1 tvg-logo="https://i.imgur.com/Meew6eX.png" group-title="国际",Rakuten Action
https://54045f0c40fd442c8b06df076aaf1e85.mediatailor.eu-west-1.amazonaws.com/v1/master/0547f18649bd788bec7b67b746e47670f558b6b2/production-LiveChannel-6065/master.m3u8
#EXTINF:-1 tvg-logo="https://i.imgur.com/Meew6eX.png" group-title="国际",Rakuten Comedy
https://9be783d652cd4b099cf63e1dc134c4a3.mediatailor.eu-west-1.amazonaws.com/v1/master/0547f18649bd788bec7b67b746e47670f558b6b2/production-LiveChannel-6181/master.m3u8
#EXTINF:-1 tvg-logo="https://i.imgur.com/Meew6eX.png" group-title="国际",Rakuten Drama
https://fee09fd665814f51b939b6d106cf5f66.mediatailor.eu-west-1.amazonaws.com/v1/master/0547f18649bd788bec7b67b746e47670f558b6b2/production-LiveChannel-6093/master.m3u8
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
