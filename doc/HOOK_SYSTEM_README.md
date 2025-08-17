# Songs of Syx Mod Hook System

**A simple way to add mods to Songs of Syx without breaking other mods.**

## What This Does

Instead of replacing game files (which causes conflicts), this system **injects your code** into the game while it's running. Multiple mods can hook the same game classes safely.

## Prerequisites

**⚠️ IMPORTANT: Java 1.8 (Java 8) Required**

This modding system requires **Java 1.8 (Java 8)** to work properly. It is not compatible with Java 9+ or newer versions.

**Check your Java version:**
```bash
java -version
```

**You should see something like:**
```
java version "1.8.0_xxx"
Java(TM) SE Runtime Environment (build 1.8.0_xxx-bxxx)
Java HotSpot(TM) 64-Bit Server VM (build 25.xxx-bxxx, mixed mode)
```

## Installation

### Step 1: Download the Modding Utility

1. **Download** `sosModHooks.jar` from the GitHub releases page
2. **Place it** in your Songs of Syx game folder (same folder as `SongsOfSyx.jar`)

**Your game folder should now look like this:**
```
Songs of Syx/
├── SongsOfSyx.jar          # The game
├── sosModHooks.jar         # The modding utility
├── saves/                  # Game saves
├── campaigns/              # Game campaigns
└── other game files...
```

### Step 2: Run the Game with the Utility

**Windows (Command Prompt):**
```cmd
cd "C:\Program Files (x86)\Steam\steamapps\common\Songs of Syx"
java -javaagent:sosModHooks.jar -jar SongsOfSyx.jar
```

**Linux/Mac (Terminal):**
```bash
cd /home/username/games/songs-of-syx
java -javaagent:sosModHooks.jar -jar SongsOfSyx.jar
```

**Steam Users:**
1. Right-click Songs of Syx in Steam
2. Properties → General → Launch Options
3. Add: `-javaagent:sosModHooks.jar`
4. Launch normally from Steam

### Step 3: Verify Installation

When the utility loads correctly, you'll see these messages in the console:
```
HookAgent: Initializing ASM-based hook system...
HookAgent: Successfully initialized hook system
```

**That's it!** The modding utility is now installed and ready to use.

## Creating Your First Mod

Now that the utility is installed, you can create mods that use it. Here's how:

### 1. Create Your Mod Project Structure

```
YourModName/                    # Create this new folder anywhere you want
├── src/
│   └── main/
│       └── java/
│           └── yourmod/
│               ├── MainScript.java          # REQUIRED: Main entry point
│               ├── InstanceScript.java      # REQUIRED: Game instance script
│               └── hooks/
│                   └── MyCustomHook.java    # Your hook implementation
├── pom.xml                                  # Maven configuration
└── _Info.txt                               # Mod info file
```

### 2. Create Your Main Script (REQUIRED)

**File:** `src/main/java/yourmod/MainScript.java`

```java
package yourmod;

import script.SCRIPT;
import sosModHooks.HookSystem;
import yourmod.hooks.MyCustomHook;

public class MainScript implements SCRIPT {
    
    private final String info = "Your Mod Description";
    
    @Override
    public CharSequence name() {
        return "Your Mod Name";
    }
    
    @Override
    public CharSequence desc() {
        return info;
    }
    
    @Override
    public void initBeforeGameCreated() {
        // STEP 1: Initialize the hook system
        HookSystem.initialize();
        
        // STEP 2: Register your hooks
        HookSystem.registerHook("game.GAME", new MyCustomHook("GameHook"));
        HookSystem.registerHook("settlement.main.SETT", new MyCustomHook("SettlementHook"));
        
        System.out.println("Your Mod initialized successfully!");
    }
    
    @Override
    public boolean isSelectable() {
        return true; // Set to false if you don't want users to select this mod
    }
    
    @Override
    public boolean forceInit() {
        return true; // Always initialize
    }
    
    @Override
    public SCRIPT_INSTANCE createInstance() {
        return new InstanceScript();
    }
}
```

### 3. Create Your Instance Script (REQUIRED)

**File:** `src/main/java/yourmod/InstanceScript.java`

```java
package yourmod;

import java.io.IOException;
import script.SCRIPT;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.*;
import util.gui.misc.GBox;
import view.keyboard.KEYS;

final class InstanceScript implements SCRIPT.SCRIPT_INSTANCE {

    private static boolean initialized = false;

    InstanceScript() {
        if (!initialized) {
            try {
                System.out.println("Your Mod instance created successfully");
                initialized = true;
            } catch (Exception e) {
                System.err.println("Failed to initialize mod instance: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void save(FilePutter file) {
        // Add your save logic here
    }
    
    @Override
    public void load(FileGetter file) throws IOException {
        // Add your load logic here
    }
    
    @Override
    public void update(double ds) {
        // Add your update logic here (runs every frame)
    }
    
    @Override
    public void hoverTimer(double mouseTimer, GBox text) {
        // Add your hover tooltip logic here
    }
    
    @Override
    public void render(Renderer renderer, float ds) {
        // Add your rendering logic here
    }
    
    @Override
    public void keyPush(KEYS key) {
        // Add your keyboard input handling here
    }
    
    @Override
    public void mouseClick(MButt button) {
        // Add your mouse click handling here
    }
    
    @Override
    public void hover(COORDINATE mCoo, boolean mouseHasMoved) {
        // Add your mouse hover handling here
    }
    
    @Override
    public boolean handleBrokenSavedState() {
        return SCRIPT.SCRIPT_INSTANCE.super.handleBrokenSavedState();
    }
}
```

### 4. Create Your Hook Class

**File:** `src/main/java/yourmod/hooks/MyCustomHook.java`

```java
package yourmod.hooks;

import sosModHooks.hooks.GameClassHook;

public class MyCustomHook implements GameClassHook {
    
    private final String hookName;
    
    public MyCustomHook(String hookName) {
        this.hookName = hookName;
    }
    
    @Override
    public void beforeCreate(Object instance) {
        // Runs BEFORE the constructor executes
        // instance will be null since the object isn't created yet
        System.out.println("[" + hookName + "] About to create game object");
        
        // Add your pre-creation logic here
        // Example: Set up global variables, prepare resources
    }
    
    @Override
    public void afterCreate(Object instance) {
        // Runs AFTER the constructor executes
        // instance is the newly created game object
        if (instance != null) {
            System.out.println("[" + hookName + "] Created: " + 
                instance.getClass().getSimpleName());
            
            // Add your post-creation logic here
            // Example: Modify the object, add custom properties
        }
    }
}
```

### 5. Create Your Mod Info File

**File:** `_Info.txt`

```
Name: Your Mod Name
Description: Your mod description here
Version: 1.0.0
Author: Your Name
```

### 6. Build Your Mod

**File:** `pom.xml`

**⚠️ IMPORTANT: This pom.xml MUST use Java 8 settings**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>yourmod</groupId>
    <artifactId>your-mod-name</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    
    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    
    <dependencies>
        <!-- Add any external dependencies your mod needs -->
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>8</source>
                    <target>8</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

Then build:
```bash
mvn clean package
# Creates: target/your-mod-name-1.0.0.jar
```

## How It Works

1. **You install** the modding utility (`sosModHooks.jar`) into your game folder
2. **Game starts** with the utility loaded via `-javaagent`
3. **Your mods load** and register hooks via `MainScript`
4. **Game loads classes** → Utility intercepts and modifies them
5. **Your hooks run** → Every time those classes are instantiated
6. **No conflicts** → Multiple mods can hook the same classes

## What You Can Hook

- **Game classes** like `game.GAME`, `settlement.main.SETT`
- **Any class** that gets instantiated during gameplay
- **Constructors only** (when objects are created)

## Example: Settlement Mod

Here's a complete working example that hooks into settlements:

**File:** `src/main/java/yourmod/hooks/SettlementMod.java`

```java
package yourmod.hooks;

import sosModHooks.hooks.GameClassHook;

public class SettlementMod implements GameClassHook {
    
    @Override
    public void beforeCreate(Object instance) {
        // Setup before settlement is created
        System.out.println("Preparing settlement...");
        
        // Add your pre-creation logic here
        // Example: Initialize settlement-specific variables
    }
    
    @Override
    public void afterCreate(Object instance) {
        // Modify settlement after creation
        if (instance != null) {
            System.out.println("Settlement created: " + 
                instance.getClass().getSimpleName());
            
            // Add your post-creation logic here
            // Example: Modify settlement properties, add custom features
        }
    }
}
```

**Then in your MainScript.java, add this line:**

```java
@Override
public void initBeforeGameCreated() {
    HookSystem.initialize();
    
    // Register your settlement hook
    HookSystem.registerHook("settlement.main.SETT", new SettlementMod());
    
    System.out.println("Settlement Mod initialized successfully!");
}
```

## For Mod Developers

### Required Files (Copy These Exactly)

- **MainScript.java** - Entry point (implements `SCRIPT`) - REQUIRED
- **InstanceScript.java** - Game instance script - REQUIRED
- **Your hook classes** - Implement `GameClassHook` - REQUIRED
- **Registration** - Call `HookSystem.registerHook()` in `initBeforeGameCreated()` - REQUIRED

### Project Structure (Follow This Exactly)

```
YourModName/
├── src/
│   └── main/
│       └── java/
│           └── yourmod/                    # Change 'yourmod' to your package name
│               ├── MainScript.java          # REQUIRED: Copy the template above
│               ├── InstanceScript.java      # REQUIRED: Copy the template above
│               └── hooks/                   # Create this folder
│                   └── MyCustomHook.java    # Your hook implementation
├── pom.xml                                  # Copy the template above
└── _Info.txt                               # Copy the template above
```

### Building

```bash
mvn clean package
# Creates: target/your-mod-name-1.0.0.jar
```

## Why This Approach?

| Traditional Modding | This System |
|---------------------|-------------|
| ❌ Replace game files | ✅ Inject code |
| ❌ Mod conflicts | ✅ Multiple mods work |
| ❌ Game updates break mods | ✅ More resilient |
| ❌ Complex setup | ✅ Simple registration |

## Common Use Cases

- **Add new features** to existing game objects
- **Modify behavior** without changing game code
- **Log game events** for debugging
- **Add custom logic** to game systems

## Troubleshooting

- **"No instrumentation available"** → Make sure you're using `-javaagent:sosModHooks.jar`
- **Hooks not running** → Check class names are correct
- **Game crashes** → Check your hook code for errors
- **Mod not loading** → Verify `MainScript.java` implements `SCRIPT` correctly
- **Utility not found** → Make sure `sosModHooks.jar` is in the same folder as `SongsOfSyx.jar`
- **Java version errors** → **MUST use Java 1.8 (Java 8)** - check with `java -version`
- **"Unsupported class file version"** → You're using Java 9+ - downgrade to Java 8

## Summary

This system lets you **safely extend Songs of Syx** by injecting your code into the game at runtime. It's simple, safe, and compatible with other mods.

**Perfect for:** Mod developers who want to add features without breaking the game or other mods.

**Remember:** 
1. **First** install the modding utility (`sosModHooks.jar`) into your game folder
2. **Then** run the game with `-javaagent:sosModHooks.jar`
3. **Finally** create your mods using the templates above

Copy the template files exactly, change only the package names and class names, and your mod will work!
