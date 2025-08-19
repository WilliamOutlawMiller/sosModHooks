# Mod Compatibility Framework - Modder Integration Guide

## Overview

The Mod Compatibility Framework is a powerful system that allows mods to declare their changes and automatically detect conflicts with other mods. This guide explains how to integrate your mod with the framework.

## Quick Start

### 1. Add Dependency

Add `sosModHooks` as a dependency in your mod's documentation. Players need to have this mod enabled for compatibility checking to work.

### 2. Basic Integration

In your mod's initialization code (usually in the `initBeforeGameCreated()` method), add:

```java
import sosModHooks.ModCompatibilityAPI;

// Get the API instance
ModCompatibilityAPI api = ModCompatibilityAPI.getInstance();

// Register your mod
api.registerMod("myModId", "My Mod Name", "1.0.0");

// Declare what you're modifying
api.declareClassReplacement("myModId", "settlement.room.food.farm.FarmInstance");
api.declareAssetModification("myModId", "/data/assets/sprite/race/face/addon");
api.declareDataModification("myModId", "FACTION", "RACE");
```

## API Reference

### Core Methods

#### `registerMod(String modId, String modName, String version)`
Registers your mod with the framework. Call this first before any other declarations.

- **modId**: Unique identifier (e.g., "warhammer_overhaul", "farm_enhancement")
- **modName**: Human-readable name (e.g., "Warhammer Overhaul", "Farm Enhancement")
- **version**: Version string (e.g., "1.0.0", "2.1.3")

#### `declareClassReplacement(String modId, String... classNames)`
Declares that your mod completely replaces core game classes.

```java
api.declareClassReplacement("myModId", 
    "settlement.room.food.farm.FarmInstance",
    "settlement.room.food.farm.ROOM_FARM"
);
```

#### `declareAssetModification(String modId, String... assetPaths)`
Declares that your mod modifies specific asset files.

```java
api.declareAssetModification("myModId",
    "/data/assets/sprite/race/face/addon",
    "/data/assets/text/event",
    "/data/assets/init/race/sprite"
);
```

#### `declareDataModification(String modId, String... dataTypes)`
Declares that your mod modifies specific data structures.

```java
api.declareDataModification("myModId",
    "FACTION",    // Modifies faction data
    "RACE",       // Modifies race data
    "EVENT",      // Modifies event data
    "TECH"        // Modifies technology data
);
```

#### `declareDependency(String modId, String... requiredModIds)`
Declares that your mod requires other mods to function.

```java
api.declareDependency("myModId", "requiredMod", "anotherMod");
```

### Utility Methods

#### `checkModConflicts(String modId)`
Check if there are any conflicts involving your mod.

```java
snake2d.util.sets.LIST<ModConflict> conflicts = api.checkModConflicts("myModId");
if (!conflicts.isEmpty()) {
    System.out.println("My mod has " + conflicts.size() + " conflicts!");
}
```

#### `hasConflicts()`
Check if there are any conflicts at all.

```java
if (api.hasConflicts()) {
    System.out.println("There are compatibility issues!");
}
```

#### `getConflictCount()`
Get the total number of conflicts.

```java
int conflictCount = api.getConflictCount();
System.out.println("Total conflicts: " + conflictCount);
```

## Integration Examples

### Example 1: Simple Class Replacement Mod

```java
import sosModHooks.ModCompatibilityAPI;

public class MyModScript implements SCRIPT {
    
    @Override
    public void initBeforeGameCreated() {
        ModCompatibilityAPI api = ModCompatibilityAPI.getInstance();
        
        // Register the mod
        api.registerMod("simpleFarmMod", "Simple Farm Mod", "1.0.0");
        
        // Declare class replacement
        api.declareClassReplacement("simpleFarmMod", 
            "settlement.room.food.farm.FarmInstance"
        );
    }
}
```

### Example 2: Complex Overhaul Mod

```java
import sosModHooks.ModCompatibilityAPI;

public class WarhammerOverhaulScript implements SCRIPT {
    
    @Override
    public void initBeforeGameCreated() {
        ModCompatibilityAPI api = ModCompatibilityAPI.getInstance();
        
        // Register with full metadata
        api.registerMod("warhammer_overhaul", "Warhammer Overhaul", "2.0.0", 
            "Complete overhaul of the game with Warhammer themes", "YourName");
        
        // Declare class replacements
        api.declareClassReplacement("warhammer_overhaul",
            "world.region.RD",
            "settlement.entity.humanoid.Humanoid",
            "menu.ScMain"
        );
        
        // Declare asset modifications
        api.declareAssetModification("warhammer_overhaul",
            "/data/assets/sprite/race/face/addon",
            "/data/assets/init/race/sprite",
            "/data/assets/text/event",
            "/data/assets/init/race/sprite/Graveguard.txt",
            "/data/assets/init/race/sprite/Mummy.txt",
            "/data/assets/init/race/sprite/Zombie.txt"
        );
        
        // Declare data modifications
        api.declareDataModification("warhammer_overhaul",
            "FACTION",
            "RACE", 
            "EVENT",
            "HUMAN"
        );
        
        // Declare dependencies (if any)
        // api.declareDependency("warhammer_overhaul", "baseMod");
    }
}
```

### Example 3: Technology Enhancement Mod

```java
import sosModHooks.ModCompatibilityAPI;

public class TechEnhancementScript implements SCRIPT {
    
    @Override
    public void initBeforeGameCreated() {
        ModCompatibilityAPI api = ModCompatibilityAPI.getInstance();
        
        api.registerMod("tech_enhancement", "Technology Enhancement", "1.5.0");
        
        // Replace technology system
        api.declareClassReplacement("tech_enhancement",
            "init.tech.TECH",
            "init.tech.Knowledge_Costs"
        );
        
        // Modify tech-related data
        api.declareDataModification("tech_enhancement",
            "TECH",
            "KNOWLEDGE"
        );
    }
}
```

## Conflict Types

The framework detects these types of conflicts:

1. **Class Replacement**: Multiple mods replacing the same core class
2. **Asset Conflict**: Multiple mods modifying the same asset files
3. **Data Conflict**: Multiple mods modifying the same data structures
4. **Missing Dependency**: Required mods not loaded
5. **Load Order Conflict**: Conflicting load order requirements

## Best Practices

### 1. Use Descriptive Mod IDs
```java
// Good
api.registerMod("warhammer_overhaul_v2", "Warhammer Overhaul v2", "2.0.0");

// Avoid
api.registerMod("mod", "Mod", "1.0");
```

### 2. Be Specific About Modifications
```java
// Good - specific class names
api.declareClassReplacement("myModId", "settlement.room.food.farm.FarmInstance");

// Avoid - vague descriptions
api.declareClassReplacement("myModId", "farm");
```

### 3. Declare All Asset Modifications
```java
// Good - list all modified assets
api.declareAssetModification("myModId",
    "/data/assets/sprite/race/face/addon",
    "/data/assets/init/race/sprite/Graveguard.txt",
    "/data/assets/init/race/sprite/Mummy.txt"
);
```

### 4. Check for Conflicts
```java
// Check if your mod has conflicts
snake2d.util.sets.LIST<ModConflict> conflicts = api.checkModConflicts("myModId");
if (!conflicts.isEmpty()) {
    System.out.println("Warning: " + conflicts.size() + " conflicts detected!");
    for (ModConflict conflict : conflicts) {
        System.out.println("Conflict: " + conflict.getSummary());
    }
}
```

## Backward Compatibility

The framework automatically detects and registers existing mods that haven't been updated yet. However, for best results and accurate conflict detection, modders should update their mods to use the new API.

## Testing Your Integration

1. **Enable sosModHooks** in your game
2. **Load your mod** with the new integration code
3. **Check the console** for registration messages
4. **Press F10** to see the compatibility overlay
5. **Verify conflicts** are detected correctly

## Getting Help

If you encounter issues with the integration:

1. Check the console for error messages
2. Verify your mod ID is unique
3. Ensure all method calls use the correct parameters
4. Test with a minimal mod setup first

## Example Integration Checklist

- [ ] Added `sosModHooks` as a dependency requirement
- [ ] Imported `ModCompatibilityAPI` in your script
- [ ] Called `registerMod()` in `initBeforeGameCreated()`
- [ ] Declared all class replacements with `declareClassReplacement()`
- [ ] Declared all asset modifications with `declareAssetModification()`
- [ ] Declared all data modifications with `declareDataModification()`
- [ ] Declared all dependencies with `declareDependency()`
- [ ] Tested with the compatibility overlay (F10)
- [ ] Verified conflicts are detected correctly

By following this guide, your mod will be fully integrated with the compatibility framework, providing players with valuable information about potential conflicts and helping them resolve compatibility issues.
