@echo off
echo ===================================================
echo  OneDrive-Safe Android Build Script
echo ===================================================
echo This script will build your Android project safely
echo while avoiding common file locking issues that occur
echo when developing on OneDrive-synced folders.
echo.

echo Stopping any existing Gradle daemons...
taskkill /F /IM gradle.exe /T >nul 2>&1

echo Setting up build environment...
set GRADLE_OPTS=-Dorg.gradle.daemon=false -Dorg.gradle.configureondemand=false -Dorg.gradle.parallel=false -Dorg.gradle.caching=false

echo Running build with offline mode and no daemon...
call gradlew.bat --no-daemon --offline clean assembleDebug

echo.
if %ERRORLEVEL% EQU 0 (
    echo ===================================================
    echo BUILD SUCCESSFUL!
    echo ===================================================
    echo.
    echo Your app was built successfully.
) else (
    echo ===================================================
    echo BUILD FAILED - ATTEMPTING RECOVERY
    echo ===================================================
    echo.
    echo Attempting to recover from build failure...
    echo Running force-clean script...
    call force-clean.bat
    
    echo.
    echo Please run this script again after force-clean completes.
)

echo.
echo Press any key to exit...
pause >nul