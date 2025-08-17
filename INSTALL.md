# Installation Guide

## Quick Install

### 1. Download
- Download the latest `sosModHooks.jar` file from releases
- Place it in the **same folder** as your `SongsOfSyx.jar` file

### 2. Start the Game
```bash
java -javaagent:sosModHooks.jar -jar SongsOfSyx.jar
```

**That's it!** The mod will load automatically.

## Detailed Steps

### Windows

1. **Download** the `sosModHooks.jar` file to your Songs of Syx folder
   - **Location:** `C:\Games\Songs of Syx\` (or wherever your game is installed)
   - **Files in folder:** `SongsOfSyx.jar`, `sosModHooks.jar`

2. **Open Command Prompt** in the Songs of Syx folder
   - Press `Win + R`, type `cmd`, press Enter
   - Navigate to your game folder: `cd "C:\Games\Songs of Syx"`

3. **Run the game with the mod:**
   ```cmd
   java -javaagent:sosModHooks.jar -jar SongsOfSyx.jar
   ```

### Linux/Mac

1. **Download** the `sosModHooks.jar` file to your Songs of Syx folder
   - **Location:** `/home/username/games/songs-of-syx/` (or wherever your game is installed)
   - **Files in folder:** `SongsOfSyx.jar`, `sosModHooks.jar`

2. **Open Terminal** in the Songs of Syx folder
   - Open Terminal application
   - Navigate to your game folder: `cd /home/username/games/songs-of-syx/`

3. **Run the game with the mod:**
   ```bash
   java -javaagent:sosModHooks.jar -jar SongsOfSyx.jar
   ```

### Steam

1. **Right-click** Songs of Syx in Steam
2. **Properties** → **General** → **Launch Options**
3. **Add this exact line:**
   ```
   -javaagent:sosModHooks.jar
   ```
4. **Launch normally** from Steam

**Important:** Make sure `sosModHooks.jar` is in the same folder as `SongsOfSyx.jar`!

### Creating a Desktop Shortcut (Windows)

1. **Right-click** on your desktop
2. **New** → **Shortcut**
3. **Target location:**
   ```
   "C:\Program Files\Java\bin\java.exe" -javaagent:sosModHooks.jar -jar "C:\Games\Songs of Syx\SongsOfSyx.jar"
   ```
4. **Name:** Songs of Syx (with Mods)
5. **Double-click** to launch with mods

### Creating a Desktop Shortcut (Linux/Mac)

1. **Create a shell script** named `launch-sos-with-mods.sh`:
   ```bash
   #!/bin/bash
   cd "/home/username/games/songs-of-syx"
   java -javaagent:sosModHooks.jar -jar SongsOfSyx.jar
   ```

2. **Make it executable:**
   ```bash
   chmod +x launch-sos-with-mods.sh
   ```

3. **Create desktop entry** or add to applications menu

## File Structure After Installation

```
Your Songs of Syx Folder/
├── SongsOfSyx.jar          # Original game file
├── sosModHooks.jar         # Mod system (you added this)
├── saves/                  # Game saves
├── campaigns/              # Game campaigns
└── other game files...
```

## Troubleshooting

### "No instrumentation available"
- **Problem:** Mod system isn't loading properly
- **Solution:** Make sure you're using the `-javaagent:sosModHooks.jar` parameter
- **Check:** Verify the JAR file path is correct and in the same folder

### Game won't start
- **Problem:** Game crashes or won't launch
- **Solutions:**
  - Verify Java 8+ is installed: `java -version`
  - Check the JAR file isn't corrupted (try downloading again)
  - Try running without the mod first: `java -jar SongsOfSyx.jar`
  - Check file permissions (Linux/Mac)

### Mod not working
- **Problem:** Game runs but mod doesn't work
- **Solutions:**
  - Check console output for error messages
  - Verify the mod JAR is in the same folder as SongsOfSyx.jar
  - Make sure you're using the exact command: `-javaagent:sosModHooks.jar`
  - Check that your mod JAR files are properly built

### "Class not found" errors
- **Problem:** Java can't find the mod classes
- **Solution:** Ensure your mod JAR files are in the correct location and properly built

## What Happens During Installation

1. **Game starts** with the mod loaded via `-javaagent`
2. **Mod intercepts** class loading automatically
3. **Hooks are injected** into game classes
4. **Your mods work** without any additional setup

## Verifying Installation

When the mod is working correctly, you should see these messages in the console:

```
HookAgent: Initializing ASM-based hook system...
HookAgent: Successfully initialized hook system
Hook system initialized with instrumentation from agent
```

## Uninstalling

### Remove Mod System
1. **Remove** the `-javaagent:sosModHooks.jar` parameter from launch options
2. **Delete** the `sosModHooks.jar` file
3. **Restart** the game

### Remove Your Mods
1. **Delete** your mod JAR files from the mods folder
2. **Restart** the game

The mod will no longer load, and the game will run normally.

## Advanced Installation

### Multiple Mods
You can load multiple mods by adding multiple `-javaagent` parameters:

```bash
java -javaagent:sosModHooks.jar -javaagent:yourmod.jar -jar SongsOfSyx.jar
```

### Custom Java Options
You can combine with other Java options:

```bash
java -Xmx4g -javaagent:sosModHooks.jar -jar SongsOfSyx.jar
```

### Environment Variables
Set `JAVA_OPTS` environment variable (Linux/Mac):

```bash
export JAVA_OPTS="-javaagent:sosModHooks.jar"
java $JAVA_OPTS -jar SongsOfSyx.jar
```

## Support

If you encounter issues:

1. **Check console output** for error messages
2. **Verify file locations** and permissions
3. **Test without mods** first to isolate the problem
4. **Check Java version** compatibility
5. **Review troubleshooting** section above

**Remember:** The mod system must be loaded BEFORE the game starts, which is why the `-javaagent` parameter is essential!
