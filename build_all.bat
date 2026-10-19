@echo off
setlocal
pushd "%~dp0"
if errorlevel 1 exit /b 1

rem Find Python 3.10 for the Android Python runtime.
py -3.10 --version >nul 2>&1
if not errorlevel 1 goto use_launcher
python -c "import sys; sys.exit(sys.version_info[:2] != (3, 10))" >nul 2>&1
if not errorlevel 1 goto use_python
echo ERROR: Install Python 3.10 and add Python or the py launcher to PATH.
set "BUILD_EXIT=1"
goto finish

:use_launcher
rem Build and verify all four release APKs.
py -3.10 "%~dp0scripts\build_all.py"
set "BUILD_EXIT=%errorlevel%"
goto finish

:use_python
rem Build and verify all four release APKs.
python "%~dp0scripts\build_all.py"
set "BUILD_EXIT=%errorlevel%"

:finish
if not "%BUILD_EXIT%"=="0" echo Build failed. Read the error above.
popd
if /i not "%~1"=="--no-pause" pause
exit /b %BUILD_EXIT%
