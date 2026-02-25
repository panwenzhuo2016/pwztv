@echo off
chcp 65001 >nul
set "ROOT=%~dp0"

echo [7a] 复制并重命名 armeabi_v7a 的 apk，打包为 tvvold.zip
set "TEMP7=%ROOT%_temp_7a"
if not exist "%TEMP7%" mkdir "%TEMP7%"
copy /Y "%ROOT%mobileArmeabi_v7a\debug\mobile-armeabi_v7a.apk" "%TEMP7%\mobileold.apk"
copy /Y "%ROOT%leanbackArmeabi_v7a\debug\leanback-armeabi_v7a.apk" "%TEMP7%\tvold.apk"
powershell -NoProfile -Command "Compress-Archive -Path '%TEMP7%\*' -DestinationPath '%ROOT%tvvold.zip' -Force"
rd /s /q "%TEMP7%"
echo 已生成: tvvold.zip

echo.
echo [8a] 复制并重命名 arm64_v8a 的 apk，打包为 tvv.zip
set "TEMP8=%ROOT%_temp_8a"
if not exist "%TEMP8%" mkdir "%TEMP8%"
copy /Y "%ROOT%mobileArm64_v8a\debug\mobile-arm64_v8a.apk" "%TEMP8%\mobile.apk"
copy /Y "%ROOT%leanbackArm64_v8a\debug\leanback-arm64_v8a.apk" "%TEMP8%\tv.apk"
powershell -NoProfile -Command "Compress-Archive -Path '%TEMP8%\*' -DestinationPath '%ROOT%tvv.zip' -Force"
rd /s /q "%TEMP8%"
echo 已生成: tvv.zip

echo.
echo 全部完成。
pause
