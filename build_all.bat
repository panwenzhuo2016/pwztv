@echo off
setlocal
pushd "%~dp0"
if errorlevel 1 exit /b 1
set "BUILD_PAUSE=1"
for %%A in (%*) do if /i "%%~A"=="--no-pause" set "BUILD_PAUSE=0"
for %%A in (%*) do if /i "%%~A"=="--help" set "BUILD_PAUSE=0"
for %%A in (%*) do if /i "%%~A"=="-h" set "BUILD_PAUSE=0"

rem Locate Python 3.10 to run the shared build script.
py -3.10 --version >nul 2>&1
if not errorlevel 1 goto use_launcher
python -c "import sys; sys.exit(sys.version_info[:2] != (3, 10))" >nul 2>&1
if not errorlevel 1 goto use_python
echo ERROR: Install Python 3.10 and add Python or the py launcher to PATH.
set "BUILD_EXIT=1"
goto finish

:use_launcher
rem Build both device types and both ARM architectures together.
py -3.10 "%~dp0scripts\build_all.py" %* --include-32bit
set "BUILD_EXIT=%errorlevel%"
goto finish

:use_python
rem Build both device types and both ARM architectures together.
python "%~dp0scripts\build_all.py" %* --include-32bit
set "BUILD_EXIT=%errorlevel%"

:finish
if not "%BUILD_EXIT%"=="0" echo Build failed. Read the error above.
popd
if "%BUILD_PAUSE%"=="1" pause
exit /b %BUILD_EXIT%
