# sosModHooks - Mod Compatibility Framework for Songs of Syx

## Overview

**sosModHooks** is a comprehensive mod compatibility framework that provides automatic conflict detection, runtime compatibility monitoring, and intelligent mod management for Songs of Syx. The framework operates entirely as a standard game mod, ensuring safe integration without modifying core game files.

## Core Functionality

### Primary Capabilities

The framework operates as a comprehensive modding ecosystem that provides:

1. **Automatic Mod Discovery**: Uses reflection to automatically detect and analyze all loaded mods
2. **Real-time Conflict Detection**: Identifies compatibility issues as they occur during gameplay
3. **Performance Monitoring**: Tracks system health, memory usage, and error patterns
4. **Seamless Integration**: Integrates with the game's native key binding system and UI theming
5. **Runtime Compatibility**: Monitors for runtime conflicts and crashes in real-time

### Conflict Detection Types

The framework intelligently detects eight types of compatibility issues:

- **Class Replacement Conflicts**: Multiple mods replacing the same core game class
- **Asset Modification Conflicts**: Multiple mods modifying the same asset files
- **Data Structure Conflicts**: Multiple mods modifying the same game data types
- **Missing Dependencies**: Required mods that are not loaded
- **Load Order Conflicts**: Conflicting mod loading sequences
- **Method Signature Conflicts**: Incompatible method implementations
- **Field Name Conflicts**: Conflicting field names in shared classes
- **Package Conflicts**: Package naming collisions

## Technical Architecture

### Core Components

The framework consists of ten specialized components working together:

| Component | Purpose
|-----------|---------
| **ModCompatibilityFramework** | Main orchestrator
| **ModRegistry** | Central conflict detection 
| **ModCompatibilityAPI** | Public modder interface
| **ModEnhancementManager** | Runtime monitoring
| **ModKeyBindings** | Game integration
| **ModConflict** | Conflict representation
| **ModDeclaration** | Mod metadata
| **ConflictType** | Conflict categorization
| **ModConflictReporter** | UI management
| **MainScript** | Game entry point

### System Flow

```
Game Startup → Mod Selection → Framework Initialization → Mod Discovery → Conflict Analysis → Runtime Monitoring
     ↓              ↓                        ↓              ↓                ↓
MainScript → ModCompatibilityFramework → Reflection → ModRegistry → EnhancementManager
```

## User Interface Features

### Compatibility Overlay

The framework provides a comprehensive overlay interface (toggle with F10) displaying:

- **Real-time Compatibility Status**: Live conflict counts and status indicators
- **System Health Score**: 0-100 score with color-coded status
- **Performance Metrics**: Memory usage, error counts, and system performance
- **Conflict Details**: Comprehensive information about detected issues
- **Resolution Suggestions**: Actionable advice for fixing conflicts

### Key Binding Integration

- **F10**: Toggle compatibility overlay
- **Custom Key Page**: Integrated with game's settings menu
- **Rebindable**: Players can customize the overlay key

## Modder Integration

### Integration Methods

The framework supports two integration approaches:

#### Method 1: Reflection-Based Discovery (Recommended)

No imports required - implement standard methods in your SCRIPT class:

```java
public class YourModScript implements SCRIPT {
    // Basic mod information
    public String getModId() { return "your_mod_id"; }
    public String getModName() { return "Your Mod Name"; }
    public String getModVersion() { return "1.0.0"; }
    
    // Declare your modifications
    public String[] getClassReplacements() {
        return new String[] { "settlement.room.food.farm.FarmInstance" };
    }
    
    public String[] getAssetModifications() {
        return new String[] { "/data/assets/sprite/race/face/addon" };
    }
    
    public String[] getDataModifications() {
        return new String[] { "FACTION", "RACE" };
    }
    
    public String[] getDependencies() {
        return new String[] { "required_mod" };
    }
}
```

#### Method 2: Direct API Integration

Full control with direct framework access:

```java
import sosModHooks.ModCompatibilityAPI;

ModCompatibilityAPI api = ModCompatibilityAPI.getInstance();

// Register your mod
api.registerMod("myMod", "My Awesome Mod", "1.0.0", "Description", "Author");

// Declare modifications
api.declareClassReplacement("myMod", "settlement.room.food.farm.FarmInstance");
api.declareAssetModification("myMod", "/data/assets/sprite/race/face/addon");
api.declareDataModification("myMod", "FACTION", "RACE");
api.declareDependency("myMod", "required_mod");

// Check for conflicts
if (api.hasConflicts()) {
    List<ModConflict> conflicts = api.checkModConflicts("myMod");
    // Handle conflicts appropriately
}
```

### Declaration Types

Mods can declare comprehensive modification information:

| Type | Description | Example |
|------|-------------|---------|
| **Class Replacements** | Core classes completely replaced | `"settlement.room.food.farm.FarmInstance"` |
| **Asset Modifications** | Sprite files, textures, sounds | `"/data/assets/sprite/race/face/addon"` |
| **Data Modifications** | Game data structures | `"FACTION"`, `"RACE"`, `"EVENT"` |
| **Dependencies** | Required mods | `"base_mod"`, `"library_mod"` |

## Installation and Setup

### For Players

1. **Download the mod** from the workshop or releases
2. **Place in mods folder**: Copy to your Songs of Syx mods directory
3. **Enable in game**: Select sosModHooks when starting a new game
4. **Use F10**: Press F10 to access the compatibility overlay
5. **Monitor console**: Check console output for compatibility information

### For Modders

**No dependency required!** sosModHooks will automatically discover your mod if you implement the standard methods:

1. **Implement methods**: Add reflection methods to your SCRIPT class (see examples above)
2. **Test compatibility**: Use the framework to verify your mod works with others
3. **No imports needed**: The framework discovers mods automatically

### Additional Guides

- **[MODDER_INTEGRATION_GUIDE.md](MODDER_INTEGRATION_GUIDE.md)**: Detailed integration instructions
- **[MOD_INTEGRATION_TEMPLATE.java](MOD_INTEGRATION_TEMPLATE.java)**: Copy-paste integration template
- **[INSTALL.md](INSTALL.md)**: Installation and setup instructions
