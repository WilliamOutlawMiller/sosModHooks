# sosModHooks - Runtime Mod Detection Framework for Songs of Syx

## Overview

**sosModHooks** is a runtime mod detection and compatibility monitoring framework for Songs of Syx. The framework operates as a standard game mod and provides real-time monitoring of loaded mods, system health tracking, and compatibility analysis through the game's internal systems.

## Core Functionality

### Primary Capabilities

The framework provides comprehensive runtime monitoring and analysis:

1. **Runtime Mod Detection**: Accesses the game's internal PATHS and ScriptEngine systems to detect actively loaded mods
2. **Real-time System Monitoring**: Tracks system health, memory usage, and error patterns during gameplay
3. **Compatibility Analysis**: Analyzes mod interactions and identifies potential conflicts based on runtime behavior
4. **Performance Metrics**: Monitors game performance and provides real-time feedback
5. **Seamless Integration**: Integrates with the game's native key binding system and UI theming

### Mod Detection Methods

The framework uses multiple detection approaches to identify loaded mods:

- **PATHS System Access**: Direct access to the game's mod loading system
- **ScriptEngine Integration**: Detection of script-based mods through the game's script engine
- **Classpath Analysis**: Runtime analysis of loaded JAR files and classpaths
- **Launcher Settings**: Access to user-activated mods through launcher configuration

## Technical Architecture

### Core Components

The framework consists of specialized components working together:

| Component | Purpose |
|-----------|---------|
| **ModCompatibilityFramework** | Main orchestrator and UI manager |
| **ModRegistry** | Runtime mod detection and analysis |
| **ModEnhancementManager** | System monitoring and performance tracking |
| **ModKeyBindings** | Game integration and key binding management |
| **ComprehensiveModOverlay** | F10 overlay interface system |
| **MainScript** | Game entry point and initialization |

### System Flow

```
Game Startup → Framework Initialization → Runtime Detection → Mod Analysis → Continuous Monitoring
     ↓                    ↓                    ↓                ↓                ↓
MainScript → ModCompatibilityFramework → ModRegistry → Runtime Analysis → EnhancementManager
```

## User Interface Features

### Compatibility Overlay

The framework provides a comprehensive overlay interface (toggle with F10) displaying:

- **Real-time Mod Status**: Live detection of loaded mods and their status
- **System Health Score**: 0-100 score with color-coded status indicators
- **Performance Metrics**: Memory usage, error counts, and system performance
- **Mod Information**: Details about detected mods and their runtime behavior
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
5. **Monitor console**: Check console output for detection information

### For Modders

**No integration required!** sosModHooks automatically detects your mods through the game's internal systems. The framework will:

1. **Automatically detect** your mod when it's loaded by the game
2. **Monitor runtime behavior** to understand what your mod is doing
3. **Analyze compatibility** with other loaded mods
4. **Provide feedback** through the F10 overlay

## Technical Details

### Runtime Detection

The framework operates entirely at runtime and does not require mods to implement specific interfaces. Instead, it:

- Accesses the game's internal mod loading systems
- Analyzes runtime classpath and JAR loading
- Monitors system behavior and performance
- Provides real-time compatibility analysis

### Performance Impact

- **Minimal overhead**: Designed for minimal performance impact
- **Efficient monitoring**: Uses efficient detection methods
- **Background operation**: Runs in background without affecting gameplay
- **Smart caching**: Caches detection results to avoid repeated analysis

## Current Status

- ✅ **Runtime mod detection** via game internal systems
- ✅ **F10 overlay UI** with comprehensive information
- ✅ **System health monitoring** and performance tracking
- ✅ **Automatic loading** without user selection
- ✅ **Real-time compatibility analysis**

## Development

This project is built using Maven and requires Java 8 or higher. The framework is designed to work within the constraints of the Songs of Syx modding system without requiring modifications to core game files.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
