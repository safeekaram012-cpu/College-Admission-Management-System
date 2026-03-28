@echo off
:: ============================================================
::  CAMS Build & Run Script  (Windows)
:: ============================================================
setlocal EnableDelayedExpansion

set PROJECT_ROOT=%~dp0
set SRC_DIR=%PROJECT_ROOT%src\main\java
set OUT_DIR=%PROJECT_ROOT%out
set LIB_DIR=%PROJECT_ROOT%lib
set MAIN_CLASS=com.cams.CAMSApplication

echo.
echo   +--------------------------------------------------+
echo   ^|        CAMS Build Script  v1.0  (Windows)       ^|
echo   +--------------------------------------------------+
echo.

:: Check Java
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java not found. Install JDK 11+ and add to PATH.
    pause & exit /b 1
)
for /f "tokens=*" %%i in ('java -version 2^>^&1') do (
    echo [OK]   Java: %%i
    goto :javaok
)
:javaok

:: Check MySQL connector
set CONNECTOR=
for %%f in ("%LIB_DIR%\mysql-connector*.jar") do set CONNECTOR=%%f
if "!CONNECTOR!"=="" (
    echo [ERROR] MySQL connector JAR not found in lib\
    echo         Download: https://dev.mysql.com/downloads/connector/j/
    echo         Copy the .jar into the lib\ directory.
    pause & exit /b 1
)
echo [OK]   MySQL connector: !CONNECTOR!

:: Create output directory
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

:: Collect all .java files
echo [--]  Collecting source files...
dir /s /b "%SRC_DIR%\*.java" > "%TEMP%\cams_sources.txt"

:: Compile
echo [--]  Compiling...
javac -cp "%LIB_DIR%\*" -d "%OUT_DIR%" @"%TEMP%\cams_sources.txt"
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed.
    pause & exit /b 1
)
echo [OK]   Compilation successful.

:: Run
echo [--]  Starting CAMS...
echo.
java -cp "%OUT_DIR%;%LIB_DIR%\*" %MAIN_CLASS%

pause
