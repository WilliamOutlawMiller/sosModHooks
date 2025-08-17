# Songs of Syx Mod Hook System

**Advanced ASM-based runtime code injection for conflict-free modding architecture.**

## Overview

This system implements a sophisticated **bytecode manipulation framework** that enables runtime modification of Songs of Syx without file replacement or conflicts. By leveraging Java's Instrumentation API and ASM bytecode engineering, multiple mods can safely intercept and modify game behavior simultaneously.

## Technical Architecture

Instead of traditional file replacement approaches that cause conflicts, this system **injects custom bytecode** into the JVM during class loading. The ASM framework transforms game classes at runtime, inserting hook calls that execute your custom logic without modifying the original game files.

## Prerequisites

**⚠️ CRITICAL: Java 1.8 (Java 8) Runtime Required**

This modding system requires **Java 1.8 (Java 8)** for compatibility. It is not compatible with Java 9+ due to module system changes and bytecode version differences.

**Verify your Java version:**
```bash
java -version
```

**Expected output:**
```
java version "1.8.0_xxx"
Java(TM) SE Runtime Environment (build 1.8.0_xxx-bxxx)
Java HotSpot(TM) 64-Bit Server VM (build 25.xxx-bxxx, mixed mode)
```

## Installation

### Step 1: Deploy the Modding Framework

1. **Download** `sosModHooks.jar` from the GitHub releases page
2. **Deploy** to your Songs of Syx game directory (same location as `SongsOfSyx.jar`)

**Target directory structure:**
```
Songs of Syx/
├── SongsOfSyx.jar          # Primary game executable
├── sosModHooks.jar         # Modding framework (deployed)
├── saves/                  # Game save data
├── campaigns/              # Campaign definitions
└── other game files...
```

### Step 2: Initialize the Framework

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

**Steam Integration:**
1. Right-click Songs of Syx in Steam
2. Properties → General → Launch Options
3. Add: `-javaagent:sosModHooks.jar`
4. Launch normally from Steam

### Step 3: Verify Framework Initialization

When the framework loads successfully, you'll observe these diagnostic messages:
```
HookAgent: Initializing ASM-based hook system...
Hook system initialized with instrumentation from agent
```

**Framework deployment complete.** The bytecode transformation system is now operational.

## Mod Development Framework

With the framework deployed, you can now develop mods that leverage the runtime injection capabilities. Here's the development workflow:

### 1. Establish Mod Project Architecture

**Create a new mod project with this structure:**

```
YourModName/                    # Project root directory
├── src/
│   └── main/
│       └── java/
│           └── yourmod/       # Package namespace
│               ├── MainScript.java          # Primary entry point
│               ├── InstanceScript.java      # Runtime instance management
│               └── hooks/                   # Hook implementations
│                   └── MyCustomHook.java    # Custom hook logic
├── pom.xml                                  # Maven build configuration
└── _Info.txt                               # Mod metadata
```

**Critical:** 
- **DO NOT copy files from the sosModHooks directory** - those are framework components
- **Create NEW files** using the provided templates
- **Your mod is architecturally separate** from the modding framework
- **⚠️ IMPORTANT:** The sosModHooks classes are NOT available at compile time - use reflection as shown in the examples below

**Why Reflection is Required:**
- **sosModHooks.jar** is loaded at **runtime** via the `-javaagent` parameter
- **Your mod** is compiled **separately** in its own project
- **At compile time**, the sosModHooks classes don't exist in your project's classpath
- **Reflection** allows your mod to access the framework classes at runtime

### 2. Implement the Primary Entry Point

**File:** `src/main/java/yourmod/MainScript.java`

```java
package yourmod;

import script.SCRIPT;

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
        try {
            // Initialize the hook framework using reflection
            Class<?> hookSystemClass = Class.forName("sosModHooks.HookSystem");
            Object hookSystem = hookSystemClass.getMethod("initialize").invoke(null);
            
            // Register hook interceptors using reflection
            Class<?> gameClassHookClass = Class.forName("sosModHooks.hooks.GameClassHook");
            Object myHook = new MyCustomHook("GameHook");
            
            hookSystemClass.getMethod("registerHook", String.class, gameClassHookClass)
                .invoke(null, "game.GAME", myHook);
            hookSystemClass.getMethod("registerHook", String.class, gameClassHookClass)
                .invoke(null, "settlement.main.SETT", myHook);
            
            System.out.println("Your Mod initialized successfully!");
        } catch (Exception e) {
            System.err.println("Failed to initialize mod: " + e.getMessage());
            e.printStackTrace();
        }
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

### 3. Implement Runtime Instance Management

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

### 4. Implement Hook Interceptors

**File:** `src/main/java/yourmod/hooks/MyCustomHook.java`

```java
package yourmod.hooks;

public class MyCustomHook {
    
    private final String hookName;
    
    public MyCustomHook(String hookName) {
        this.hookName = hookName;
    }
    
    // These methods will be called by the hook system at runtime
    // The sosModHooks framework will handle the interface implementation
    
    public void beforeCreate(Object instance) {
        // Executes BEFORE constructor execution
        // instance will be null since the object isn't instantiated yet
        System.out.println("[" + hookName + "] Pre-instantiation hook triggered");
        
        // Add your pre-instantiation logic here
        // Example: Initialize global state, prepare resources
    }
    
    public void afterCreate(Object instance) {
        // Executes AFTER constructor execution
        // instance is the newly instantiated game object
        if (instance != null) {
            System.out.println("[" + hookName + "] Post-instantiation hook triggered for: " + 
                instance.getClass().getSimpleName());
            
            // Add your post-instantiation logic here
            // Example: Modify object state, inject custom properties
        }
    }
}
```

### 5. Define Mod Metadata

**File:** `_Info.txt`

```
Name: Your Mod Name
Description: Your mod description here
Version: 1.0.0
Author: Your Name
```

### 6. Configure Build System

**File:** `pom.xml`

**⚠️ CRITICAL: Maven configuration MUST use Java 8 settings**

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

**Build the mod:**
```bash
mvn clean package
# Generates: target/your-mod-name-1.0.0.jar
```

## Technical Implementation Details

### Execution Flow

1. **Framework Deployment** - `sosModHooks.jar` deploys to game directory
2. **JVM Initialization** - Framework loads via `-javaagent` parameter
3. **Mod Loading** - Your mod registers hooks via `MainScript`
4. **Class Interception** - Framework intercepts class loading events
5. **Bytecode Transformation** - ASM transforms classes, injecting hook calls
6. **Hook Execution** - Your custom logic executes at runtime

### Hook Interception Points

- **Game classes** like `game.GAME`, `settlement.main.SETT`
- **Any instantiated class** during gameplay
- **Constructor execution** (object instantiation lifecycle)

## Advanced Implementation Example

Here's a sophisticated example that demonstrates settlement modification:

**File:** `src/main/java/yourmod/hooks/SettlementMod.java`

```java
package yourmod.hooks;

import sosModHooks.hooks.GameClassHook;

public class SettlementMod implements GameClassHook {
    
    @Override
    public void beforeCreate(Object instance) {
        // Pre-instantiation logic
        System.out.println("Preparing settlement instantiation...");
        
        // Add your pre-instantiation logic here
        // Example: Initialize settlement-specific state
    }
    
    @Override
    public void afterCreate(Object instance) {
        // Post-instantiation logic
        if (instance != null) {
            System.out.println("Settlement instantiated: " + 
                instance.getClass().getSimpleName());
            
            // Add your post-instantiation logic here
            // Example: Modify settlement properties, inject custom features
        }
    }
}
```

**Hook registration in MainScript.java:**

```java
@Override
public void initBeforeGameCreated() {
    HookSystem.initialize();
    
    // Register settlement modification hook
    HookSystem.registerHook("settlement.main.SETT", new SettlementMod());
    
    System.out.println("Settlement Mod initialized successfully!");
}
```

## Development Requirements

### Mandatory Components

- **MainScript.java** - Primary entry point (implements `SCRIPT`) - REQUIRED
- **InstanceScript.java** - Runtime instance management - REQUIRED
- **Hook implementations** - Implement `GameClassHook` interface - REQUIRED
- **Hook registration** - Call `HookSystem.registerHook()` in `initBeforeGameCreated()` - REQUIRED

### Project Architecture

**Follow this structure exactly:**

```
YourModName/
├── src/
│   └── main/
│       └── java/
│           └── yourmod/                    # Change 'yourmod' to your package namespace
│               ├── MainScript.java          # REQUIRED: Use template above
│               ├── InstanceScript.java      # REQUIRED: Use template above
│               └── hooks/                   # Create this directory
│                   └── MyCustomHook.java    # Your hook implementation
├── pom.xml                                  # Use template above
└── _Info.txt                               # Use template above
```

### Build Process

```bash
mvn clean package
# Generates: target/your-mod-name-1.0.0.jar
```

## Architectural Advantages

| Traditional Modding | This Framework |
|---------------------|----------------|
| ❌ File replacement conflicts | ✅ Runtime bytecode injection |
| ❌ Mod incompatibilities | ✅ Simultaneous mod execution |
| ❌ Update vulnerability | ✅ Framework resilience |
| ❌ Complex integration | ✅ Clean hook registration |

## Common Use Cases

- **Runtime behavior modification** of existing game objects
- **State injection** without altering game code
- **Event logging and debugging** capabilities
- **Custom logic integration** into game systems

## Troubleshooting

- **"No instrumentation available"** → Verify `-javaagent:sosModHooks.jar` parameter
- **Hooks not executing** → Validate class names and hook registration
- **Game crashes** → Review hook implementation for errors
- **Mod not loading** → Verify `MainScript.java` implements `SCRIPT` correctly
- **Framework not found** → Confirm `sosModHooks.jar` is in game directory
- **Java version errors** → **MUST use Java 1.8 (Java 8)** - verify with `java -version`
- **"Unsupported class file version"** → Downgrade from Java 9+ to Java 8

## Summary

This framework provides **enterprise-grade modding capabilities** through sophisticated bytecode manipulation, enabling safe and conflict-free extension of Songs of Syx. The ASM-based approach ensures runtime stability while maintaining full compatibility with existing mods.

**Ideal for:** Developers requiring robust, scalable modding solutions that integrate seamlessly with existing game architecture.

**Implementation workflow:** 
1. **Deploy** the modding framework (`sosModHooks.jar`) to your game directory
2. **Initialize** the framework with `-javaagent:sosModHooks.jar`
3. **Develop** your mods using the provided templates and hook system

**Copy the template implementations exactly, modify only package names and class names, and your mod will integrate seamlessly with the framework.**
