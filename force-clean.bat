@echo off
echo ===================================================
echo  Android Project Force Clean Script
echo ===================================================
echo This script will clean your Android project by 
echo removing build directories that might be locked.
echo.

echo Stopping all Gradle processes...
taskkill /F /IM java.exe /T >nul 2>&1
taskkill /F /IM gradle.exe /T >nul 2>&1
taskkill /F /IM javaw.exe /T >nul 2>&1

timeout /t 2 >nul

echo Removing build directories...
rmdir /S /Q app\build 2>nul
rmdir /S /Q build 2>nul
rmdir /S /Q .gradle 2>nul

echo Creating empty directories...
mkdir app\build 2>nul
mkdir build 2>nul
mkdir .gradle 2>nul

echo Force cleaning completed.
echo You can now run safe-build.bat again to rebuild your project.
echo.
echo Press any key to exit...
pause >nul