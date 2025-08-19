# sosModHooks - Mod Compatibility Framework for Songs of Syx

## Overview

sosModHooks is a comprehensive mod compatibility framework for Songs of Syx that provides real-time analysis, conflict detection, and compatibility monitoring for modded games. The framework operates entirely through script hooks, ensuring safe integration without modifying core game files.

## What This Framework Does

### Core Functionality

The framework provides a comprehensive compatibility analysis system that:

1. **Registers Mod Declarations**: Allows mods to declare their changes and modifications through a standardized interface
2. **Detects Compatibility Issues**: Identifies conflicts between mods through declared modifications and runtime analysis
3. **Monitors Runtime Performance**: Tracks system health, memory usage, and error counts during gameplay
4. **Provides User Interface**: Offers an in-game overlay (toggleable with F10) showing compatibility status and conflict details
5. **Integrates with Game Systems**: Seamlessly integrates with the game's key binding settings and UI theming

### Conflict Detection Types

The framework detects several types of compatibility issues:

- **Class Replacement Conflicts**: Multiple mods replacing the same game class
- **Asset Modification Conflicts**: Multiple mods modifying the same asset files
- **Data Structure Conflicts**: Multiple mods modifying the same game data types
- **Missing Dependencies**: Required mods that are not loaded
- **Load Order Conflicts**: Conflicting mod loading sequences

### Technical Architecture

The framework consists of several key components:

- **ModRegistry**: Central system for mod declarations and conflict detection
- **ModCompatibilityAPI**: Public interface for modders to integrate with the framework
- **ModCompatibilityFramework**: Main framework that coordinates all functionality
- **ModCompatibilityScanner**: Analyzes loaded mods for conflicts using reflection
- **ModConflictReporter**: Manages user interface and overlay display
- **ModEnhancementManager**: Monitors runtime performance and system health
- **ModKeyBindings**: Integrates with game's native key binding system

### User Interface Features

The framework provides a professional overlay interface that displays:

- Overall compatibility status with conflict counts
- System health score (0-100) with status indicators
- Performance metrics including memory usage and error counts
- Detailed conflict information for detected issues
- Real-time monitoring data during gameplay

## How Modders Use It

### Integration Methods

The framework supports two integration approaches:

#### Method 1: Reflection-Based Discovery (Recommended)
Mods can implement standard methods that the framework automatically discovers:

```java
public class YourModScript implements SCRIPT {
    public String getModId() { return "your_mod_id"; }
    public String getModName() { return "Your Mod Name"; }
    public String getModVersion() { return "1.0.0"; }
    
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
Mods can directly use the framework's API (requires importing framework classes):

```java
import sosModHooks.ModCompatibilityAPI;

ModCompatibilityAPI api = ModCompatibilityAPI.getInstance();
api.registerMod("myMod", "My Awesome Mod", "1.0.0");
api.declareClassReplacement("myMod", "settlement.room.food.farm.FarmInstance");
```

### Declaration Types

Mods can declare various types of modifications:

- **Class Replacements**: Core game classes that are completely replaced
- **Asset Modifications**: Sprite files, textures, sounds, and other assets
- **Data Modifications**: Game data structures like factions, races, events
- **Dependencies**: Other mods that are required for functionality

## Installation

### Prerequisites

- Songs of Syx (tested with V69.38)
- Java 8 or higher
- Maven (for building from source)

### Build and Install

```bash
cd sosModHooks
mvn clean package
mvn install
```

The framework will be automatically installed to your game's mod directory and will appear in the Scripts tab when starting a new game.

## Usage

### Basic Operation

1. **Enable the Framework**: Select sosModHooks in the Scripts tab when starting a new game
2. **Access Overlay**: Press F10 to toggle the compatibility overlay
3. **View Status**: The overlay shows real-time compatibility information
4. **Configure Keys**: The F10 key binding can be reconfigured in the game's key binding settings

### Key Bindings

- **F10**: Toggle compatibility overlay visibility
- **Customizable**: Key binding can be changed through the game's settings menu

### Overlay Information

The overlay displays:

- **Compatibility Status**: Overall assessment of mod compatibility
- **System Health**: Performance score and status indicators
- **Conflict Details**: Specific information about detected compatibility issues
- **Performance Metrics**: Memory usage and system stress indicators
- **Active Mod Count**: Number of loaded mods and their status

## Technical Details

### Integration Method

The framework uses script hooks rather than class replacement, ensuring:

- Safe operation without modifying core game files
- Compatibility with game updates
- No risk of save file corruption
- Clean integration with existing mod systems

### Reflection Usage

The framework uses Java reflection to:

- Access the game's loaded mod list
- Discover mod declarations through method calls
- Analyze mod class structures
- Detect method and field conflicts
- Monitor runtime behavior

### Performance Impact

The framework is designed for minimal performance impact:

- Efficient conflict detection algorithms
- Minimal memory footprint
- Optimized rendering system
- Background monitoring with configurable intervals

## Compatibility

### Game Versions

- **Primary**: Songs of Syx V69.38
- **Tested**: V69.x series
- **Expected**: Compatible with V68+ (may require minor adjustments)

### Mod Compatibility

The framework is designed to work with:

- Vanilla game installations
- Other script-based mods
- Class replacement mods
- Asset modification mods
- Mods that implement the declaration interface

### Known Limitations

- Cannot detect conflicts in mods that use advanced obfuscation
- Limited to Java reflection capabilities
- May not detect all resource file conflicts
- Performance monitoring requires mod cooperation

## Development

### Source Code Structure

```
src/main/java/sosModHooks/
├── MainScript.java              # Entry point and SCRIPT interface
├── ModCompatibilityFramework.java  # Main framework coordination
├── ModRegistry.java             # Central mod registration system
├── ModCompatibilityAPI.java     # Public API for modders
├── ModDeclaration.java          # Mod metadata representation
├── ModConflict.java             # Conflict representation
├── ConflictType.java            # Conflict categorization
├── ModCompatibilityScanner.java    # Conflict detection engine
├── ModConflictReporter.java        # User interface management
├── ModEnhancementManager.java      # Runtime monitoring
└── ModKeyBindings.java             # Key binding integration
```

### Building from Source

1. Clone the repository
2. Install Maven dependencies: `mvn validate`
3. Build the framework: `mvn clean package`
4. Install to game: `mvn install`

### Extending the Framework

The framework is designed to be extensible:

- Add new conflict detection types in ConflictType enum
- Implement custom scanners by extending ModCompatibilityScanner
- Create additional UI elements in ModConflictReporter
- Add new monitoring capabilities in ModEnhancementManager

## Troubleshooting

### Common Issues

- **Framework Not Appearing**: Ensure the framework is properly installed in the game's mod directory
- **Overlay Not Showing**: Check that F10 key binding is working and not conflicting with other mods
- **Performance Issues**: The framework has minimal impact, but can be disabled if needed
- **Conflict Detection**: Some conflicts may not be detected if mods use advanced techniques

### Debug Information

The framework provides comprehensive console logging:

- Initialization status and timing
- Mod registration confirmations
- Conflict detection results
- Performance monitoring data
- Error conditions and recovery attempts

### Support

For issues or questions:

1. Check the console output for error messages
2. Verify framework installation in the correct directory
3. Test with minimal mod loadout to isolate conflicts
4. Review the game's error logs for additional information

## License

This framework is provided as-is for educational and compatibility purposes. Use at your own risk.

## Acknowledgments

- Songs of Syx development team for the excellent modding framework
- Community modders for testing and feedback
- Java reflection API for enabling safe mod analysis

---

**Note**: This framework is designed to help identify compatibility issues but cannot guarantee that all detected conflicts will cause problems. Some conflicts may be harmless or even intentional. Always test your mod combinations thoroughly before using them in important games.
