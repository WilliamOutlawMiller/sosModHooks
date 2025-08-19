# Installation Guide

## Quick Install

### 1. Download
- Download the latest `sosModHooks.jar` file from releases
- Place it in your Songs of Syx mods folder

### 2. Enable in Game
- Start Songs of Syx
- When starting a new game, select sosModHooks from the mods list
- The mod will load automatically with your game

**That's it!** The mod will work automatically once enabled.

## Detailed Steps

### Windows

1. **Download** the `sosModHooks.jar` file
2. **Copy to mods folder**:
   - **Location:** `%USERPROFILE%\AppData\Roaming\songsofsyx\mods\`
   - **Create folder:** `sosModHooks\V69\script\`
   - **Place JAR:** Copy `sosModHooks.jar` to the `script` folder
3. **Start the game** normally from Steam or desktop shortcut
4. **Enable mod** when starting a new game

### Linux/Mac

1. **Download** the `sosModHooks.jar` file
2. **Copy to mods folder**:
   - **Location:** `~/.local/share/songsofsyx/mods/`
   - **Create folder:** `sosModHooks/V69/script/`
   - **Place JAR:** Copy `sosModHooks.jar` to the `script` folder
3. **Start the game** normally from Steam or desktop shortcut
4. **Enable mod** when starting a new game

### Steam

1. **Install normally** - no special launch options needed
2. **Enable mod** in the mods list when starting a new game
3. **Launch normally** from Steam

## File Structure After Installation

```
Your Songs of Syx Mods Folder/
├── sosModHooks/
│   ├── _Info.txt
│   └── V69/
│       └── script/
│           ├── sosModHooks.jar
│           └── _src/
│               └── sosModHooks-sources.jar
└── other mods...
```

## Troubleshooting

### Mod not appearing in list
- **Problem:** Mod doesn't show up when starting a new game
- **Solution:** Verify the JAR file is in the correct `script` folder
- **Check:** Ensure the folder structure matches exactly: `mods/sosModHooks/V69/script/`

### Game crashes with mod
- **Problem:** Game crashes when mod is enabled
- **Solutions:**
  - Verify Java 8+ is installed: `java -version`
  - Check the JAR file isn't corrupted (try downloading again)
  - Try running without the mod first to isolate the problem
  - Check file permissions (Linux/Mac)

### Mod not working
- **Problem:** Game runs but mod doesn't work
- **Solutions:**
  - Check console output for error messages
  - Verify the mod JAR is in the correct location
  - Make sure you've enabled the mod in the mods list
  - Check that your mod JAR files are properly built

### "Class not found" errors
- **Problem:** Java can't find the mod classes
- **Solution:** Ensure your mod JAR files are in the correct location and properly built

## What Happens During Installation

1. **Mod loads** as a standard Songs of Syx script mod
2. **Framework initializes** during game startup
3. **Mod discovery** automatically detects other loaded mods
4. **Conflict analysis** runs to identify potential issues
5. **Overlay becomes available** via F10 key

## Verifying Installation

When the mod is working correctly, you should see these messages in the console:

```
sosModHooks: ModCompatibilityFramework constructor called
sosModHooks: ModRegistry initialized
sosModHooks: Mod registry initialized successfully
```

## Uninstalling

### Remove Mod System
1. **Disable** the mod in the mods list when starting a new game
2. **Delete** the `sosModHooks` folder from your mods directory
3. **Restart** the game

The mod will no longer load, and the game will run normally.

## Advanced Installation

### Multiple Mods
You can load multiple mods by enabling them all in the mods list:

1. **Enable sosModHooks** in the mods list
2. **Enable your other mods** in the same list
3. **Start the game** - sosModHooks will automatically detect and analyze all mods

### Custom Mod Folders
If you have custom mod locations, ensure sosModHooks is in the same mods directory that the game recognizes.

## Support

If you encounter issues:

1. **Check console output** for error messages
2. **Verify file locations** and permissions
3. **Test without mods** first to isolate the problem
4. **Check Java version** compatibility
5. **Review troubleshooting** section above

**Remember:** sosModHooks is a standard mod that loads with your game - no special launch parameters or Java agents are needed!
