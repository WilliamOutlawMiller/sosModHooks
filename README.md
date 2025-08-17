# Songs of Syx Mod Hook System

**A simple, safe way to add mods to Songs of Syx without breaking other mods.**

## What This Is

Instead of replacing game files (which causes conflicts), this system **injects your code** into the game while it's running. Multiple mods can hook the same game classes safely.

## Quick Start

### 1. Download & Install

1. Download the latest release JAR file
2. Start the game with: `java -javaagent:sosModHooks.jar -jar SongsOfSyx.jar`

### 2. Create Your First Mod

```java
public class MyMod implements GameClassHook {
    
    @Override
    public void beforeCreate(Object instance) {
        // Runs BEFORE a game object is created
        System.out.println("About to create something...");
    }
    
    @Override
    public void afterCreate(Object instance) {
        // Runs AFTER a game object is created
        System.out.println("Created: " + instance.getClass().getSimpleName());
    }
}
```

### 3. Register Your Hook

```java
// In your mod's initBeforeGameCreated() method:
HookSystem.initialize();
HookSystem.registerHook("game.GAME", new MyMod());
```

## How It Works

1. **Game starts** → Mod loads automatically
2. **Game loads classes** → Mod intercepts and modifies them  
3. **Your hooks run** → Every time those classes are used
4. **No conflicts** → Multiple mods can hook the same classes

## Why This Approach?

| Traditional Modding | This System |
|---------------------|-------------|
| ❌ Replace game files | ✅ Inject code |
| ❌ Mod conflicts | ✅ Multiple mods work |
| ❌ Game updates break mods | ✅ More resilient |
| ❌ Complex setup | ✅ Simple registration |

## Building From Source

```bash
git clone https://github.com/yourusername/sosModHooks.git
cd sosModHooks
mvn clean package
```

## Documentation

- **[Hook System Guide](doc/HOOK_SYSTEM_README.md)** - Complete modding guide
- **[Examples](src/main/java/sosModHooks/hooks/ExampleHook.java)** - Working examples
- **[API Reference](src/main/java/sosModHooks/HookSystem.java)** - System methods

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

- **Issues**: [GitHub Issues](https://github.com/yourusername/sosModHooks/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/sosModHooks/discussions)
- **Wiki**: [Project Wiki](https://github.com/yourusername/sosModHooks/wiki)

---

**Perfect for:** Mod developers who want to add features without breaking the game or other mods.
