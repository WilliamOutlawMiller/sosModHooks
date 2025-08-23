# sosModHooks - Runtime Mod Detection & Real Conflict Analysis Framework for Songs of Syx

## Overview

**sosModHooks** is a comprehensive runtime mod detection and real conflict analysis framework for Songs of Syx. The framework operates as a standard game mod and provides real-time monitoring of loaded mods, actual file analysis for conflict detection, system health tracking, and compatibility analysis through the game's internal systems.

## Core Functionality

### Primary Capabilities

The framework provides comprehensive runtime monitoring and real conflict analysis:

1. **Runtime Mod Detection**: Accesses the game's internal PATHS and ScriptEngine systems to detect actively loaded mods
2. **Real File Analysis**: Examines actual mod files to detect what they're modifying
3. **Actual Conflict Detection**: Identifies real conflicts based on file overlaps, not just name patterns
4. **Comprehensive Runtime Monitoring**: Monitors class loading, resource loading, and file system changes in real-time
5. **Real-time System Monitoring**: Tracks system health, memory usage, and error patterns during gameplay
6. **Compatibility Analysis**: Provides detailed analysis of mod compatibility and actual conflicts
7. **Performance Metrics**: Monitors game performance and provides real-time feedback
8. **Seamless Integration**: Integrates with the game's native key binding system and UI theming

### Mod Detection Methods

The framework uses a multi-layered approach to avoid duplicates and ensure comprehensive detection:

- **PATHS System Access**: Direct access to the game's mod loading system (primary method)
- **Classpath Analysis**: Detection of script-based mods through classpaths (secondary method)
- **Class Loading Monitoring**: Real-time monitoring of classes being loaded from mod JARs
- **Resource Loading Monitoring**: Tracks assets, sprites, and data files being loaded
- **File System Monitoring**: Watches for file system changes during runtime
- **Periodic Re-analysis**: Background thread that re-analyzes classpath for late-loading mods
- **Duplicate Prevention**: Tracks detected mods by name to avoid multiple detections
- **Smart ID Generation**: Creates unique IDs combining mod names and Steam Workshop IDs

### Real Conflict Detection System

The framework analyzes actual mod files to detect real conflicts:

- **File System Analysis**: Scans mod directories to see what files are actually being modified
- **Asset File Detection**: Identifies sprite, texture, audio, and configuration file modifications
- **Configuration Analysis**: Reads config files to determine what data types are being modified
- **Class Modification Detection**: Analyzes JAR files and script directories for class changes
- **Real Conflict Identification**: Detects when multiple mods modify the same actual files
- **Steam Workshop Support**: Automatically finds and analyzes Steam Workshop mods
- **Runtime Conflict Detection**: Monitors for conflicts that develop during gameplay

## Technical Architecture

### Core Components

The framework consists of specialized components working together:

| Component | Purpose |
|-----------|---------|
| **ModCompatibilityFramework** | Main orchestrator and UI manager |
| **ModRegistry** | Runtime mod detection and real conflict analysis |
| **ModEnhancementManager** | System monitoring and performance tracking |
| **ModKeyBindings** | Game integration and key binding management |
| **ComprehensiveModOverlay** | F10 overlay interface system |
| **MainScript** | Game entry point and initialization |

### System Flow

```
Game Startup → Framework Initialization → Consolidated Detection → Runtime Monitoring → Real File Analysis → Actual Conflict Detection → Continuous Monitoring
     ↓                    ↓                        ↓                ↓                ↓                ↓                ↓
MainScript → ModCompatibilityFramework → ModRegistry → Class Loading → File Analysis → Conflict System → EnhancementManager
```

### Runtime Monitoring Architecture

The framework implements comprehensive runtime monitoring:

1. **Class Loading Monitoring**: Tracks what classes are loaded and from where
2. **Resource Loading Monitoring**: Monitors asset and data file loading
3. **File System Monitoring**: Watches for file system changes
4. **Periodic Analysis**: Background thread that re-analyzes for new mods
5. **Real-time Conflict Detection**: Continuously monitors for new conflicts

### Real File Analysis Architecture

The framework uses a comprehensive file analysis approach:

1. **Consolidated Detection Phase**: Identifies which mods are loaded without duplicates
2. **Runtime Monitoring Phase**: Monitors class loading and resource loading in real-time
3. **File Discovery Phase**: Finds actual mod directories and file structures
4. **Content Analysis Phase**: Analyzes individual files to determine modifications
5. **Conflict Detection Phase**: Identifies real overlaps between mod modifications
6. **Real-time Updates**: Continuously monitors for new conflicts as mods load

## User Interface Features

### Compatibility Overlay

The framework provides a comprehensive overlay interface (toggle with F10) displaying:

- **Real-time Mod Status**: Live detection of loaded mods and their status
- **Actual File Modifications**: Real files being modified by each mod
- **Real Conflict Analysis**: Actual conflicts based on file overlaps
- **Runtime Monitoring Status**: Shows what monitoring systems are active
- **System Health Score**: 0-100 score with color-coded status indicators
- **Performance Metrics**: Memory usage, error counts, and system performance
- **Mod Information**: Details about detected mods and their actual modifications
- **Conflict Details**: Real conflicts with specific file paths and resolution suggestions
- **System Monitoring**: Continuous health and performance tracking

### Key Binding Integration

- **F10**: Toggle compatibility overlay
- **Custom Key Page**: Integrated with game's settings menu
- **Rebindable**: Players can customize the overlay key

## Installation and Setup

### For Players

1. **Download the mod** from the workshop or releases
2. **Place in mods folder**: Copy to your Songs of Syx mods directory
3. **Enable in game**: sosModHooks loads automatically (no selection required)
4. **Use F10**: Press F10 to access the compatibility overlay
5. **Monitor console**: Check console output for detection and real conflict information
6. **Runtime Monitoring**: The system continuously monitors for new mods and conflicts

### For Modders

**No integration required!** sosModHooks automatically detects your mods through the game's internal systems and analyzes their actual files for conflicts. The framework will:

1. **Automatically detect** your mod when it's loaded by the game
2. **Monitor class loading** to see what classes your mod provides
3. **Analyze actual files** to see what your mod is really modifying
4. **Detect real conflicts** with other loaded mods based on file overlaps
5. **Provide detailed feedback** through the F10 overlay showing actual modifications
6. **Monitor runtime changes** to catch conflicts that develop during gameplay

## Technical Details

### Runtime Detection

The framework operates entirely at runtime and does not require mods to implement specific interfaces. Instead, it:

- Accesses the game's internal mod loading systems
- Monitors class loading in real-time
- Tracks resource loading and file system changes
- Analyzes runtime classpath and JAR loading
- Monitors system behavior and performance
- Provides real-time compatibility analysis

### Real File Analysis

The conflict detection system works by:

1. **File Discovery**: Finding actual mod directories and file structures
2. **Content Analysis**: Examining individual files to determine modifications
3. **Asset Detection**: Identifying sprite, texture, audio, and config file changes
4. **Configuration Analysis**: Reading config files to determine data type modifications
5. **Class Analysis**: Analyzing JAR files for class modifications
6. **Runtime Monitoring**: Continuously monitoring for new modifications
7. **Real Conflict Detection**: Identifying actual file overlaps between mods

### Runtime Monitoring

The framework implements several monitoring systems:

- **Class Loading Interceptor**: Monitors what classes are loaded from mod JARs
- **Resource Loading Monitor**: Tracks asset and data file loading
- **File System Watcher**: Monitors for file system changes
- **Periodic Classpath Analysis**: Re-analyzes classpath for new mods
- **Background Conflict Detection**: Continuously checks for new conflicts

### Performance Impact

- **Minimal overhead**: Designed for minimal performance impact
- **Efficient monitoring**: Uses optimized monitoring techniques
- **Background operation**: Runs in background without affecting gameplay
- **Smart caching**: Caches analysis results to avoid repeated operations
- **Selective monitoring**: Only monitors relevant systems and files

## Current Status

- ✅ **Runtime mod detection** via game internal systems
- ✅ **Real file analysis** for actual conflict detection
- ✅ **Duplicate prevention** in mod detection
- ✅ **Comprehensive runtime monitoring** of class loading and resource loading
- ✅ **F10 overlay UI** with comprehensive real conflict information
- ✅ **System health monitoring** and performance tracking
- ✅ **Automatic loading** without user selection
- ✅ **Real-time compatibility analysis**
- ✅ **Actual conflict resolution suggestions**
- ✅ **Background monitoring** for late-loading mods
- ✅ **Steam Workshop integration** with automatic path detection

## Development

This project is built using Maven and requires Java 8 or higher. The framework is designed to work within the constraints of the Songs of Syx modding system without requiring modifications to core game files.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
