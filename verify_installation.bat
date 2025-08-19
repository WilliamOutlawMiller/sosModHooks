@echo off
echo sosModHooks Installation Verification
echo ===================================
echo.
echo This script verifies that the sosModHooks mod has been properly installed.
echo.

echo Checking mod installation directory...
if exist "%USERPROFILE%\AppData\Roaming\songsofsyx\mods\sosModHooks" (
    echo [✓] Mod directory exists
    if exist "%USERPROFILE%\AppData\Roaming\songsofsyx\mods\sosModHooks\_Info.txt" (
        echo [✓] Mod info file exists
        echo.
        echo Mod Info:
        type "%USERPROFILE%\AppData\Roaming\songsofsyx\mods\sosModHooks\_Info.txt"
        echo.
    ) else (
        echo [✗] Mod info file missing
    )
) else (
    echo [✗] Mod directory missing
)

echo Checking game directory...
if exist "C:\Program Files (x86)\Steam\steamapps\common\Songs of Syx\sosModHooks.jar" (
    echo [✓] Mod JAR copied to game directory
    echo [✓] File size: 
    dir "C:\Program Files (x86)\Steam\steamapps\common\Songs of Syx\sosModHooks.jar" | findstr "sosModHooks.jar"
) else (
    echo [✗] Mod JAR not found in game directory
)

echo.
echo Installation verification complete.
echo.
echo To test the mod:
echo 1. Start Songs of Syx normally from Steam or desktop shortcut
echo 2. When starting a new game, select sosModHooks from the mods list
echo 3. The mod will load automatically and provide compatibility information
echo 4. Press F10 to toggle the compatibility overlay
echo.
echo Expected console output:
echo - sosModHooks: ModCompatibilityFramework constructor called
echo - sosModHooks: ModRegistry initialized
echo - sosModHooks: Mod registry initialized successfully
echo.
pause
