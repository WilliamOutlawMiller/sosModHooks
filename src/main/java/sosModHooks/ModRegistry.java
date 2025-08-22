package sosModHooks;

import lombok.Getter;
import lombok.Setter;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.List;

/**
 * Runtime mod detection and conflict analysis system.
 * This system detects only activated mods and analyzes their actual effects on the gamestate.
 */
public final class ModRegistry {
    
    private static ModRegistry instance;
    
    @Getter
    private final Map<String, ModDeclaration> registeredMods;
    
    @Getter
    private final Map<String, LIST<String>> classReplacements;
    
    @Getter
    private final Map<String, LIST<String>> assetModifications;
    
    @Getter
    private final Map<String, LIST<String>> dataModifications;
    
    @Getter
    private final Map<String, LIST<String>> dependencies;
    
    // Runtime detection state
    private boolean hasDetectedActiveMods = false;
    private boolean hasAnalyzedRuntimeEffects = false;
    
    // Active mod tracking
    private final Map<String, ActiveModInfo> activeMods;
    
    // Comprehensive mod analyses
    private final Map<String, ModAnalysis> modAnalyses;
    
    private ModRegistry() {
        this.registeredMods = new HashMap<>();
        this.classReplacements = new HashMap<>();
        this.assetModifications = new HashMap<>();
        this.dataModifications = new HashMap<>();
        this.dependencies = new HashMap<>();
        this.activeMods = new HashMap<>();
        this.modAnalyses = new HashMap<>();
        
        System.out.println("sosModHooks: Runtime ModRegistry initialized");
    }
    
    public static ModRegistry getInstance() {
        if (instance == null) {
            instance = new ModRegistry();
        }
        return instance;
    }
    
    // ========================================
    // RUNTIME MOD DETECTION SYSTEM
    // ========================================
    
    /**
     * Phase 1: Detect which mods are actually activated and loaded by the game.
     * This is called during initBeforeGameCreated() to identify active mods.
     */
    public void detectActiveMods() {
        if (hasDetectedActiveMods) {
            System.out.println("sosModHooks: Active mod detection already completed, skipping...");
            return;
        }
        
        System.out.println("sosModHooks: Starting runtime active mod detection...");
        
        try {
            // Method 1: Access launcher settings to see which mods user activated
            detectModsFromLauncherSettings();
            
            // Method 2: Access game's PATHS system to see which mods are actually loaded
            detectModsFromGamePATHS();
            
            // Method 3: Access ScriptEngine to see which script mods are loaded
            detectModsFromScriptEngine();
            
            // Method 4: Detect mods from script classpaths (most reliable)
            detectModsFromScriptClasspaths();
            
            // Method 5: Analyze runtime classpath to see what's actually loaded
            analyzeRuntimeClasspath();
            
            System.out.println("sosModHooks: Active mod detection complete. Found " + activeMods.size() + " active mods");
            
            // Log all found mods
            if (activeMods.size() > 0) {
                System.out.println("sosModHooks: Detected mods:");
                for (Map.Entry<String, ActiveModInfo> entry : activeMods.entrySet()) {
                    ActiveModInfo modInfo = entry.getValue();
                    System.out.println("  - " + modInfo.modName + " (" + modInfo.modId + ") v" + modInfo.modVersion);
                }
            } else {
                System.out.println("sosModHooks: WARNING: No mods were detected!");
            }
            
        } catch (Exception e) {
            System.err.println("sosModHooks: Error during active mod detection: " + e.getMessage());
            e.printStackTrace();
        }
        
        hasDetectedActiveMods = true;
    }
    
    /**
     * Access the launcher settings to see which mods the user actually activated.
     */
    private void detectModsFromLauncherSettings() {
        System.out.println("sosModHooks: --- Attempting launcher settings detection ---");
        try {
            // The game creates LSettings in Main.java and passes it to PATHS.init()
            // We need to find the actual instance that was used, not create a new one
            
            // Method 1: Try to find the existing LSettings instance
            Class<?> lSettingsClass = Class.forName("launcher.LSettings");
            System.out.println("sosModHooks: Found LSettings class: " + lSettingsClass.getName());
            
            // Look for a static instance or try to access the one used by PATHS
            // Since PATHS.i is static, let's try to access it first
            try {
                Class<?> pathsClass = Class.forName("init.paths.PATHS");
                System.out.println("sosModHooks: Found PATHS class: " + pathsClass.getName());
                
                // Check if PATHS has been initialized
                Field instanceField = pathsClass.getDeclaredField("i");
                instanceField.setAccessible(true);
                Object pathsInstance = instanceField.get(null);
                
                if (pathsInstance != null) {
                    System.out.println("sosModHooks: PATHS system is initialized, accessing mods directly");
                    
                    // Get the mods list directly from PATHS
                    Field modsField = pathsInstance.getClass().getDeclaredField("mods");
                    modsField.setAccessible(true);
                    Object modsList = modsField.get(pathsInstance);
                    
                    if (modsList != null) {
                        System.out.println("sosModHooks: Found mods list: " + modsList.getClass().getName());
                        
                        // Get the size and iterate through mods
                        Method sizeMethod = modsList.getClass().getMethod("size");
                        int modCount = (Integer) sizeMethod.invoke(modsList);
                        
                        System.out.println("sosModHooks: Found " + modCount + " mods in PATHS system");
                        
                        for (int i = 0; i < modCount; i++) {
                            try {
                                Method getMethod = modsList.getClass().getMethod("get", int.class);
                                Object modInfo = getMethod.invoke(modsList, i);
                                
                                if (modInfo != null) {
                                    // Extract mod information using reflection
                                    String modName = getModInfoField(modInfo, "name");
                                    String modVersion = getModInfoField(modInfo, "version");
                                    String modPath = getModInfoField(modInfo, "path");
                                    
                                    System.out.println("sosModHooks: Mod " + i + " - Name: '" + modName + "', Version: '" + modVersion + "', Path: '" + modPath + "'");
                                    
                                    if (modName != null && !modName.equals("???")) {
                                        // Use the mod path as the ID since that's what the game uses
                                        registerActiveMod(modPath, modName, modVersion);
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("sosModHooks: Error processing mod " + i + ": " + e.getMessage());
                            }
                        }
                    } else {
                        System.out.println("sosModHooks: Mods list is null in PATHS");
                    }
                } else {
                    System.out.println("sosModHooks: PATHS system not yet initialized, trying alternative methods");
                    detectModsFromLauncherSettingsAlternative();
                }
                
            } catch (Exception e) {
                System.err.println("sosModHooks: Error accessing PATHS system: " + e.getMessage());
                detectModsFromLauncherSettingsAlternative();
            }
            
        } catch (Exception e) {
            System.err.println("sosModHooks: Launcher settings detection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Alternative method to detect mods when PATHS system isn't ready.
     */
    private void detectModsFromLauncherSettingsAlternative() {
        System.out.println("sosModHooks: --- Attempting alternative launcher settings detection ---");
        try {
            // Try to create a new LSettings instance and read from the config file
            Class<?> lSettingsClass = Class.forName("launcher.LSettings");
            Object lSettingsInstance = lSettingsClass.getConstructor().newInstance();
            System.out.println("sosModHooks: Created LSettings instance: " + lSettingsInstance.getClass().getName());
            
            // Get the mods field
            Field modsField = lSettingsClass.getDeclaredField("mods");
            modsField.setAccessible(true);
            Object modsFieldInstance = modsField.get(lSettingsInstance);
            System.out.println("sosModHooks: Got mods field instance: " + (modsFieldInstance != null ? modsFieldInstance.getClass().getName() : "NULL"));
            
            // Get the current mods array
            Method getMethod = modsFieldInstance.getClass().getMethod("get");
            System.out.println("sosModHooks: Found get method: " + getMethod.getName());
            
            String[] activatedMods = (String[]) getMethod.invoke(modsFieldInstance);
            System.out.println("sosModHooks: Invoked get method, got: " + (activatedMods != null ? activatedMods.length + " mods" : "NULL"));
            
            if (activatedMods != null && activatedMods.length > 0) {
                System.out.println("sosModHooks: Found " + activatedMods.length + " activated mods in launcher settings:");
                for (String modId : activatedMods) {
                    System.out.println("  - " + modId);
                    // Register these mods with basic info
                    registerActiveMod(modId, modId, "1.0.0");
                }
            } else {
                System.out.println("sosModHooks: No mods activated in launcher settings");
            }
            
        } catch (Exception e) {
            System.err.println("sosModHooks: Alternative launcher settings detection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Access the game's PATHS system to see which mods are actually loaded.
     */
    private void detectModsFromGamePATHS() {
        System.out.println("sosModHooks: --- Attempting PATHS system detection ---");
        try {
            // Try to access the game's PATHS class
            Class<?> pathsClass = Class.forName("init.paths.PATHS");
            System.out.println("sosModHooks: Successfully found PATHS class: " + pathsClass.getName());
            
            // Get the current mods list
            Method currentModsMethod = pathsClass.getMethod("currentMods");
            System.out.println("sosModHooks: Found currentMods method: " + currentModsMethod.getName());
            
            Object modsList = currentModsMethod.invoke(null);
            System.out.println("sosModHooks: Invoked currentMods, got: " + (modsList != null ? modsList.getClass().getName() : "NULL"));
            
            if (modsList != null) {
                // Get the size of the mods list
                Method sizeMethod = modsList.getClass().getMethod("size");
                int modCount = (Integer) sizeMethod.invoke(modsList);
                
                System.out.println("sosModHooks: Found " + modCount + " mods loaded in PATHS system");
                
                // Iterate through each mod
                for (int i = 0; i < modCount; i++) {
                    try {
                        Method getMethod = modsList.getClass().getMethod("get", int.class);
                        Object modInfo = getMethod.invoke(modsList, i);
                        System.out.println("sosModHooks: Got modInfo[" + i + "]: " + (modInfo != null ? modInfo.getClass().getName() : "NULL"));
                        
                        // Extract mod information using reflection
                        String modName = getModInfoField(modInfo, "name");
                        String modVersion = getModInfoField(modInfo, "version");
                        String modPath = getModInfoField(modInfo, "path");
                        
                        System.out.println("sosModHooks: Mod " + i + " - Name: '" + modName + "', Version: '" + modVersion + "', Path: '" + modPath + "'");
                        
                        if (modName != null && !modName.equals("???")) {
                            System.out.println("sosModHooks: PATHS mod " + i + ": " + modName + " v" + modVersion + " at " + modPath);
                            
                            // Extract the actual mod folder name from the path
                            String modFolderName = extractModFolderNameFromPath(modPath);
                            if (modFolderName != null) {
                                System.out.println("sosModHooks: Extracted mod folder name: " + modFolderName);
                                registerActiveMod(modFolderName, modName, modVersion);
                            } else {
                                System.out.println("sosModHooks: Could not extract mod folder name from path: " + modPath);
                                // Fallback: try to use the mod name as folder name
                                registerActiveMod(modName, modName, modVersion);
                            }
                        } else {
                            System.out.println("sosModHooks: Skipping mod " + i + " - invalid name: " + modName);
                        }
                    } catch (Exception e) {
                        System.err.println("sosModHooks: Error processing PATHS mod " + i + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("sosModHooks: currentMods() returned null");
            }
            
        } catch (Exception e) {
            System.err.println("sosModHooks: PATHS system detection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Extract the actual mod folder name from a PATHS mod path.
     * PATHS returns paths like "mods/Extra Info Compatible/V69/script/Extra Info Compatible.jar"
     * We need to extract "Extra Info Compatible" as the folder name.
     */
    private String extractModFolderNameFromPath(String modPath) {
        try {
            if (modPath == null || modPath.isEmpty()) {
                return null;
            }
            
            // Split the path by directory separators
            String[] pathParts = modPath.split("[\\\\/]");
            
            // Look for the "mods" directory and get the next part
            for (int i = 0; i < pathParts.length - 1; i++) {
                if ("mods".equals(pathParts[i])) {
                    // The next part should be the mod folder name
                    if (i + 1 < pathParts.length) {
                        String modFolderName = pathParts[i + 1];
                        System.out.println("sosModHooks: Extracted mod folder name '" + modFolderName + "' from path: " + modPath);
                        return modFolderName;
                    }
                }
            }
            
            // Fallback: try to extract from the end of the path
            if (modPath.contains("mods")) {
                String afterMods = modPath.substring(modPath.indexOf("mods") + 4);
                String[] remainingParts = afterMods.split("[\\\\/]");
                if (remainingParts.length > 0) {
                    String modFolderName = remainingParts[0];
                    System.out.println("sosModHooks: Fallback extracted mod folder name '" + modFolderName + "' from path: " + modPath);
                    return modFolderName;
                }
            }
            
        } catch (Exception e) {
            System.err.println("sosModHooks: Error extracting mod folder name from path: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Access the ScriptEngine to see which script mods are loaded.
     */
    private void detectModsFromScriptEngine() {
        System.out.println("sosModHooks: --- Attempting ScriptEngine detection ---");
        try {
            // Try to access the ScriptEngine
            Class<?> scriptEngineClass = Class.forName("script.ScriptEngine");
            Object scriptEngine = scriptEngineClass.getMethod("script").invoke(null);
            
            if (scriptEngine != null) {
                System.out.println("sosModHooks: Successfully accessed ScriptEngine");
                
                // Try multiple possible methods to get current scripts
                String[] possibleMethods = {"currentScripts", "getCurrentScripts", "scripts", "getScripts"};
                Object currentScripts = null;
                
                for (String methodName : possibleMethods) {
                    try {
                        currentScripts = scriptEngineClass.getMethod(methodName).invoke(scriptEngine);
                        if (currentScripts != null) {
                            System.out.println("sosModHooks: Found current scripts using method: " + methodName);
                            break;
                        }
                    } catch (Exception e) {
                        // Try next method
                    }
                }
                
                if (currentScripts != null) {
                    parseScriptsFromEngine(currentScripts);
                } else {
                    System.out.println("sosModHooks: Could not find method to get current scripts from ScriptEngine");
                }
            }
        } catch (Exception e) {
            System.out.println("sosModHooks: Could not access game ScriptEngine: " + e.getMessage());
        }
    }
    
    /**
     * Detect mods from the game's script classpaths.
     * This is the most reliable method since it shows what's actually loaded.
     */
    private void detectModsFromScriptClasspaths() {
        System.out.println("sosModHooks: --- Attempting script classpath detection ---");
        try {
            // Access the PATHS system to get script classpaths
            Class<?> pathsClass = Class.forName("init.paths.PATHS");
            System.out.println("sosModHooks: Found PATHS class: " + pathsClass.getName());
            
            // Check if PATHS has been initialized
            Field instanceField = pathsClass.getDeclaredField("i");
            instanceField.setAccessible(true);
            Object pathsInstance = instanceField.get(null);
            
            if (pathsInstance != null) {
                System.out.println("sosModHooks: PATHS system is initialized, accessing script classpaths");
                
                // Get the SCRIPT field from PATHS
                Field scriptField = pathsInstance.getClass().getDeclaredField("SCRIPT");
                scriptField.setAccessible(true);
                Object scriptInstance = scriptField.get(pathsInstance);
                
                if (scriptInstance != null) {
                    System.out.println("sosModHooks: Found SCRIPT instance: " + scriptInstance.getClass().getName());
                    
                    // Call modClasspaths() method
                    Method modClasspathsMethod = scriptInstance.getClass().getMethod("modClasspaths");
                    Object classpathsList = modClasspathsMethod.invoke(scriptInstance);
                    
                    if (classpathsList != null) {
                        System.out.println("sosModHooks: Got classpaths list: " + classpathsList.getClass().getName());
                        
                        // Get the size and iterate through classpaths
                        Method sizeMethod = classpathsList.getClass().getMethod("size");
                        int classpathCount = (Integer) sizeMethod.invoke(classpathsList);
                        
                        System.out.println("sosModHooks: Found " + classpathCount + " script classpaths");
                        
                        for (int i = 0; i < classpathCount; i++) {
                            try {
                                Method getMethod = classpathsList.getClass().getMethod("get", int.class);
                                String classpath = (String) getMethod.invoke(classpathsList, i);
                                
                                if (classpath != null && classpath.contains("mods")) {
                                    System.out.println("sosModHooks: Script classpath " + i + ": " + classpath);
                                    
                                    // Extract mod name from classpath
                                    String modName = extractModNameFromClasspath(classpath);
                                    if (modName != null) {
                                        System.out.println("sosModHooks: Extracted mod name: " + modName);
                                        registerActiveMod(modName, modName, "1.0.0");
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("sosModHooks: Error processing classpath " + i + ": " + e.getMessage());
                            }
                        }
                    } else {
                        System.out.println("sosModHooks: Classpaths list is null");
                    }
                } else {
                    System.out.println("sosModHooks: SCRIPT instance is null");
                }
            } else {
                System.out.println("sosModHooks: PATHS system not yet initialized");
            }
            
        } catch (Exception e) {
            System.err.println("sosModHooks: Script classpath detection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Extract mod name from a script classpath.
     * Classpaths look like: "C:/path/to/game/mods/Extra Info Compatible/V69/script/Extra Info Compatible.jar"
     */
    private String extractModNameFromClasspath(String classpath) {
        try {
            if (classpath == null || classpath.isEmpty()) {
                return null;
            }
            
            // Split the path by directory separators
            String[] pathParts = classpath.split("[\\\\/]");
            
            // Look for the "mods" directory and get the next part
            for (int i = 0; i < pathParts.length - 1; i++) {
                if ("mods".equals(pathParts[i])) {
                    // The next part should be the mod folder name
                    if (i + 1 < pathParts.length) {
                        String modFolderName = pathParts[i + 1];
                        System.out.println("sosModHooks: Extracted mod folder name '" + modFolderName + "' from classpath: " + classpath);
                        return modFolderName;
                    }
                }
            }
            
            // Fallback: try to extract from the end of the path
            if (classpath.contains("mods")) {
                String afterMods = classpath.substring(classpath.indexOf("mods") + 4);
                String[] remainingParts = afterMods.split("[\\\\/]");
                if (remainingParts.length > 0) {
                    String modFolderName = remainingParts[0];
                    System.out.println("sosModHooks: Fallback extracted mod folder name '" + modFolderName + "' from classpath: " + classpath);
                    return modFolderName;
                }
            }
            
        } catch (Exception e) {
            System.err.println("sosModHooks: Error extracting mod name from classpath: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Analyze the runtime classpath to see what's actually loaded.
     */
    private void analyzeRuntimeClasspath() {
        System.out.println("sosModHooks: --- Analyzing runtime classpath ---");
        try {
            // Get the current class loader
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            System.out.println("sosModHooks: Context class loader: " + (classLoader != null ? classLoader.getClass().getName() : "NULL"));
            
            if (classLoader == null) {
                classLoader = ClassLoader.getSystemClassLoader();
                System.out.println("sosModHooks: Using system class loader: " + classLoader.getClass().getName());
            }
            
            // Check if we can access the class loader's URLs (for system class loader)
            if (classLoader instanceof java.net.URLClassLoader) {
                java.net.URLClassLoader urlClassLoader = (java.net.URLClassLoader) classLoader;
                System.out.println("sosModHooks: Class loader is URLClassLoader: " + urlClassLoader.getClass().getName());
                
                try {
                    java.net.URL[] urls = urlClassLoader.getURLs();
                    
                    System.out.println("sosModHooks: Found " + urls.length + " classpath entries");
                    
                    // Look for mod JARs in the classpath
                    for (int i = 0; i < urls.length; i++) {
                        java.net.URL url = urls[i];
                        String path = url.getPath();
                        System.out.println("sosModHooks: Classpath entry " + i + ": " + path);
                        
                        if (path.contains("mods") && path.endsWith(".jar")) {
                            System.out.println("sosModHooks: Found mod JAR in classpath: " + path);
                            
                            // Extract mod name from path
                            String modName = extractModNameFromPath(path);
                            if (modName != null) {
                                System.out.println("sosModHooks: Extracted mod name: " + modName);
                                registerActiveMod(modName, modName, "1.0.0");
                            } else {
                                System.out.println("sosModHooks: Could not extract mod name from path: " + path);
                            }
                        }
                    }
                } finally {
                    // Don't close the system classloader - it's managed by the JVM
                    // Only close if it's a custom classloader we created
                    if (classLoader != Thread.currentThread().getContextClassLoader() && 
                        classLoader != ClassLoader.getSystemClassLoader()) {
                        try {
                            urlClassLoader.close();
                        } catch (Exception e) {
                            // Ignore close errors
                        }
                    }
                }
            } else {
                System.out.println("sosModHooks: Class loader is not URLClassLoader: " + classLoader.getClass().getName());
            }
            
        } catch (Exception e) {
            System.err.println("sosModHooks: Error analyzing runtime classpath: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Phase 2: Analyze the runtime effects of active mods.
     * This analyzes what the active mods are actually doing to the game.
     */
    public void analyzeRuntimeEffects() {
        if (hasAnalyzedRuntimeEffects) {
            System.out.println("sosModHooks: Runtime effects analysis already completed, skipping...");
            return;
        }
        
        System.out.println("sosModHooks: Starting runtime effects analysis...");
        
        try {
            // Analyze each active mod's runtime effects
            for (Map.Entry<String, ActiveModInfo> entry : activeMods.entrySet()) {
                String modId = entry.getKey();
                ActiveModInfo modInfo = entry.getValue();
                
                System.out.println("sosModHooks: Analyzing runtime effects of: " + modInfo.modName);
                
                // Analyze the mod's actual file structure
                analyzeModRuntimeStructure(modId, modInfo);
                
                // Check for runtime conflicts with other mods
                detectRuntimeConflicts(modId, modInfo);
            }
            
            System.out.println("sosModHooks: Runtime effects analysis complete");
            
        } catch (Exception e) {
            System.err.println("sosModHooks: Error during runtime effects analysis: " + e.getMessage());
            e.printStackTrace();
        }
        
        hasAnalyzedRuntimeEffects = true;
    }
    
    /**
     * Analyze the runtime structure of an active mod.
     */
    private void analyzeModRuntimeStructure(String modId, ActiveModInfo modInfo) {
        try {
            System.out.println("sosModHooks: Analyzing runtime structure for: " + modInfo.modName);
            
            // Create a comprehensive mod analysis based on what we know
            ModAnalysis analysis = new ModAnalysis(modId, modInfo.modName, modInfo.modVersion);
            
            // Since we can't access file system (mods may be from Steam Workshop),
            // create intelligent analysis based on mod detection method and runtime analysis
            if (modInfo.modName != null && !modInfo.modName.equals("???")) {
                // This mod was detected by the game's PATHS system, so it's actively loaded
                System.out.println("sosModHooks: Mod " + modInfo.modName + " is actively loaded by the game");
                
                // Analyze what this mod is likely doing based on its name and detection method
                analyzeModByDetectionMethod(modId, modInfo, analysis);
                
                // Analyze runtime class loading patterns
                analyzeRuntimeClassPatterns(modId, modInfo, analysis);
                
                // Analyze mod JAR contents if accessible
                analyzeModJarContents(modId, modInfo, analysis);
            }
            
            // Store the comprehensive analysis
            modAnalyses.put(modId, analysis);
            
            System.out.println("sosModHooks: Completed intelligent analysis for " + modInfo.modName + 
                             " - Found " + analysis.getTotalModifications() + " modifications");
            
        } catch (Exception e) {
            System.err.println("sosModHooks: Error analyzing runtime structure for " + modId + ": " + e.getMessage());
        }
    }
    
    /**
     * Analyze a mod based on how it was detected.
     */
    private void analyzeModByDetectionMethod(String modId, ActiveModInfo modInfo, ModAnalysis analysis) {
        try {
            // Check if this mod was detected via PATHS (script mod)
            if (modInfo.modName.contains("Script") || modInfo.modName.contains("Mod")) {
                // Likely a script mod
                ModRegistry.ScriptModification scriptMod = new ModRegistry.ScriptModification(
                    modInfo.modName, 
                    "ADDED", 
                    "script", 
                    true
                );
                analysis.addScriptModification(modInfo.modName, scriptMod);
                
                // Add as data modification
                ModRegistry.DataModification dataMod = new ModRegistry.DataModification(
                    "SCRIPT_MOD", 
                    "ADDED", 
                    1, 
                    false
                );
                analysis.addDataModification("SCRIPT_MOD", dataMod);
            }
            
            // Check if this mod was detected via classpath (JAR mod)
            if (modInfo.modName.endsWith(".jar") || modInfo.modName.contains("JAR")) {
                // Likely a JAR-based mod
                ModRegistry.ScriptModification jarMod = new ModRegistry.ScriptModification(
                    modInfo.modName, 
                    "ADDED", 
                    "jar", 
                    true
                );
                analysis.addScriptModification(modInfo.modName, jarMod);
                
                // Add as asset modification
                ModRegistry.AssetModification assetMod = new ModRegistry.AssetModification(
                    "JAR_CONTENT", 
                    modInfo.modName, 
                    "ADDED", 
                    false
                );
                analysis.addAssetModification("JAR_" + modInfo.modName, assetMod);
            }
            
            // Check if this mod was detected via ScriptEngine
            if (modInfo.modName.contains("Script") || modInfo.modName.contains("Engine")) {
                // Likely a script engine mod
                ModRegistry.RuntimeModification runtimeMod = new ModRegistry.RuntimeModification(
                    "SCRIPT_ENGINE", 
                    "CLASS_LOADING", 
                    "Mod provides script engine functionality"
                );
                analysis.addRuntimeModification("SCRIPT_ENGINE", runtimeMod);
            }
            
        } catch (Exception e) {
            System.err.println("sosModHooks: Error analyzing mod by detection method: " + e.getMessage());
        }
    }
    
    /**
     * Analyze runtime class loading patterns to detect mod behavior.
     */
    private void analyzeRuntimeClassPatterns(String modId, ActiveModInfo modInfo, ModAnalysis analysis) {
        try {
            // Get the current class loader
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = ClassLoader.getSystemClassLoader();
            }
            
            // Check if we can access the class loader's URLs
            if (classLoader instanceof java.net.URLClassLoader) {
                java.net.URLClassLoader urlClassLoader = (java.net.URLClassLoader) classLoader;
                try {
                    java.net.URL[] urls = urlClassLoader.getURLs();
                    
                    // Look for mod-specific JARs or classes
                    for (java.net.URL url : urls) {
                        String path = url.getPath();
                        if (path.contains(modId) || path.contains(modInfo.modName.replace(" ", ""))) {
                            System.out.println("sosModHooks: Found mod-specific classpath entry: " + path);
                            
                            // This mod is providing classes
                            ModRegistry.ScriptModification classMod = new ModRegistry.ScriptModification(
                                "CLASSES", 
                                "ADDED", 
                                "runtime", 
                                true
                            );
                            analysis.addScriptModification("CLASSES", classMod);
                            
                            // Add as runtime modification
                            ModRegistry.RuntimeModification runtimeMod = new ModRegistry.RuntimeModification(
                                "CLASS_LOADING", 
                                "CLASS_LOADING", 
                                "Mod provides custom classes via classpath: " + path
                            );
                            analysis.addRuntimeModification("CLASS_LOADING", runtimeMod);
                            break;
                        }
                    }
                } finally {
                    // Don't close system classloaders
                    if (classLoader != Thread.currentThread().getContextClassLoader() && 
                        classLoader != ClassLoader.getSystemClassLoader()) {
                        try {
                            urlClassLoader.close();
                        } catch (Exception e) {
                            // Ignore close errors
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("sosModHooks: Error analyzing runtime class patterns: " + e.getMessage());
        }
    }
    
    /**
     * Analyze mod JAR contents if accessible.
     */
    private void analyzeModJarContents(String modId, ActiveModInfo modInfo, ModAnalysis analysis) {
        try {
            // Try to find the mod's JAR file in the classpath
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = ClassLoader.getSystemClassLoader();
            }
            
            if (classLoader instanceof java.net.URLClassLoader) {
                java.net.URLClassLoader urlClassLoader = (java.net.URLClassLoader) classLoader;
                try {
                    java.net.URL[] urls = urlClassLoader.getURLs();
                    
                    for (java.net.URL url : urls) {
                        String path = url.getPath();
                        if (path.contains(modId) || path.contains(modInfo.modName.replace(" ", ""))) {
                            if (path.endsWith(".jar")) {
                                System.out.println("sosModHooks: Analyzing JAR contents: " + path);
                                analyzeJarContents(path, modId, modInfo, analysis);
                                break;
                            }
                        }
                    }
                } finally {
                    // Don't close system classloaders
                    if (classLoader != Thread.currentThread().getContextClassLoader() && 
                        classLoader != ClassLoader.getSystemClassLoader()) {
                        try {
                            urlClassLoader.close();
                        } catch (Exception e) {
                            // Ignore close errors
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("sosModHooks: Error analyzing mod JAR contents: " + e.getMessage());
        }
    }
    
    /**
     * Analyze the contents of a JAR file to detect mod behavior.
     */
    private void analyzeJarContents(String jarPath, String modId, ActiveModInfo modInfo, ModAnalysis analysis) {
        try {
            java.io.File jarFile = new java.io.File(jarPath);
            if (jarFile.exists()) {
                try (JarFile jar = new JarFile(jarFile)) {
                    int classCount = 0;
                    int assetCount = 0;
                    int configCount = 0;
                    
                    for (java.util.jar.JarEntry entry : jar.stream().toArray(java.util.jar.JarEntry[]::new)) {
                        String name = entry.getName();
                        
                        if (name.endsWith(".class")) {
                            classCount++;
                        } else if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".gif")) {
                            assetCount++;
                        } else if (name.endsWith(".txt") || name.endsWith(".json") || name.endsWith(".xml")) {
                            configCount++;
                        }
                    }
                    
                    // Add findings to analysis
                    if (classCount > 0) {
                        ModRegistry.ScriptModification scriptMod = new ModRegistry.ScriptModification(
                            "JAR_CLASSES", 
                            "ADDED", 
                            "jar", 
                            classCount > 5
                        );
                        analysis.addScriptModification("JAR_CLASSES", scriptMod);
                        
                        // Add as data modification with count
                        ModRegistry.DataModification dataMod = new ModRegistry.DataModification(
                            "CLASSES", 
                            "ADDED", 
                            classCount, 
                            false
                        );
                        analysis.addDataModification("CLASSES", dataMod);
                    }
                    
                    if (assetCount > 0) {
                        ModRegistry.AssetModification assetMod = new ModRegistry.AssetModification(
                            "VISUAL_ASSETS", 
                            "JAR", 
                            "ADDED", 
                            false
                        );
                        analysis.addAssetModification("VISUAL_ASSETS", assetMod);
                        
                        // Add as data modification with count
                        ModRegistry.DataModification dataMod = new ModRegistry.DataModification(
                            "ASSETS", 
                            "ADDED", 
                            assetCount, 
                            false
                        );
                        analysis.addDataModification("ASSETS", dataMod);
                    }
                    
                    if (configCount > 0) {
                        ModRegistry.DataModification configMod = new ModRegistry.DataModification(
                            "CONFIG_FILES", 
                            "ADDED", 
                            configCount, 
                            false
                        );
                        analysis.addDataModification("CONFIG_FILES", configMod);
                    }
                    
                    System.out.println("sosModHooks: JAR analysis complete - Classes: " + classCount + 
                                     ", Assets: " + assetCount + ", Config: " + configCount);
                }
            }
            
        } catch (Exception e) {
            System.err.println("sosModHooks: Error analyzing JAR contents: " + e.getMessage());
        }
    }
    
    /**
     * Find a mod folder in the game's mods directory.
     */
    private File findModFolderInGame(String modId) {
        // Don't try to find hardcoded file paths - mods can come from Steam Workshop
        // Instead, rely on the game's built-in mod detection systems
        System.out.println("sosModHooks: Skipping file system search for mod: " + modId + " (may be from Steam Workshop)");
        return null;
    }
    
    /**
     * Analyze what a mod is actually modifying.
     * Note: This method is no longer used since we can't access file system
     * for Steam Workshop mods. Analysis is now done in analyzeModRuntimeStructure.
     */
    private void analyzeModModifications(File modFolder, String modId, String modName, ModAnalysis analysis) {
        // File system analysis removed - mods may come from Steam Workshop
        // Analysis is now done in analyzeModRuntimeStructure using game's built-in systems
    }
    
    /**
     * Analyze modifications in a version folder.
     * Note: This method is no longer used since we can't access file system.
     */
    private void analyzeVersionFolderModifications(File versionFolder, String modId, String modName, ModAnalysis analysis) {
        // File system analysis removed - mods may come from Steam Workshop
    }
    
    /**
     * Analyze a generic folder that might contain mod files.
     * Note: This method is no longer used since we can't access file system.
     */
    private void analyzeGenericFolder(File folder, String modId, String modName, ModAnalysis analysis) {
        // File system analysis removed - mods may come from Steam Workshop
    }
    
    /**
     * Count all files recursively in a directory.
     * Note: This method is no longer used since we can't access file system.
     */
    private int countFilesRecursively(File directory) {
        return 0; // File system analysis removed
    }
    
    /**
     * Add file modifications recursively for all files in a directory.
     * Note: This method is no longer used since we can't access file system.
     */
    private void addFileModificationsRecursively(File directory, String basePath, ModAnalysis analysis) {
        // File system analysis removed - mods may come from Steam Workshop
    }
    
    /**
     * Analyze asset modifications.
     * Note: This method is no longer used since we can't access file system.
     */
    private void analyzeAssetsModifications(File assetsFolder, String modId, String modName, ModAnalysis analysis) {
        // File system analysis removed - mods may come from Steam Workshop
    }
    
    /**
     * Analyze a generic asset subfolder.
     * Note: This method is no longer used since we can't access file system.
     */
    private void analyzeAssetSubFolder(File subFolder, String modId, String modName, ModAnalysis analysis) {
        // File system analysis removed - mods may come from Steam Workshop
    }
    
    /**
     * Analyze init data modifications.
     * Note: This method is no longer used since we can't access file system.
     */
    private void analyzeInitModifications(File initFolder, String modId, String modName, ModAnalysis analysis) {
        // File system analysis removed - mods may come from Steam Workshop
    }
    
    /**
     * Analyze sprite modifications.
     * Note: This method is no longer used since we can't access file system.
     */
    private void analyzeSpriteModifications(File spriteFolder, String modId, String modName, ModAnalysis analysis) {
        // File system analysis removed - mods may come from Steam Workshop
    }
    
    /**
     * Analyze script modifications.
     * Note: This method is no longer used since we can't access file system.
     */
    private void analyzeScriptModifications(File scriptFolder, String modId, String modName, ModAnalysis analysis) {
        // File system analysis removed - mods may come from Steam Workshop
    }
    
    /**
     * Analyze the src subfolder in script folders.
     * Note: This method is no longer used since we can't access file system.
     */
    private void analyzeScriptSrcFolder(File srcFolder, String modId, String modName, ModAnalysis analysis) {
        // File system analysis removed - mods may come from Steam Workshop
    }
    
    /**
     * Detect runtime conflicts between active mods.
     */
    private void detectRuntimeConflicts(String modId, ActiveModInfo modInfo) {
        try {
            // Check for conflicts with other active mods
            for (Map.Entry<String, ActiveModInfo> otherEntry : activeMods.entrySet()) {
                String otherModId = otherEntry.getKey();
                ActiveModInfo otherModInfo = otherEntry.getValue();
                
                if (!modId.equals(otherModId)) {
                    // Check for data type conflicts
                    detectDataTypeConflicts(modId, modInfo, otherModId, otherModInfo);
                    
                    // Check for asset conflicts
                    detectAssetConflicts(modId, modInfo, otherModId, otherModInfo);
                }
            }
            
        } catch (Exception e) {
            System.err.println("sosModHooks: Error detecting runtime conflicts for " + modId + ": " + e.getMessage());
        }
    }
    
    /**
     * Detect data type conflicts between two mods.
     */
    private void detectDataTypeConflicts(String modId1, ActiveModInfo modInfo1, String modId2, ActiveModInfo modInfo2) {
        try {
            LIST<String> dataTypes1 = dataModifications.get(modId1);
            LIST<String> dataTypes2 = dataModifications.get(modId2);
            
            if (dataTypes1 != null && dataTypes2 != null) {
                for (int i = 0; i < dataTypes1.size(); i++) {
                    String dataType1 = dataTypes1.get(i);
                    for (int j = 0; j < dataTypes2.size(); j++) {
                        String dataType2 = dataTypes2.get(j);
                        
                        if (dataType1.equals(dataType2)) {
                            System.out.println("sosModHooks: CONFLICT DETECTED: " + modInfo1.modName + " and " + modInfo2.modName + " both modify " + dataType1);
                            
                            // Register the conflict
                            registerModConflict(modId1, modId2, dataType1, "DATA_MODIFICATION");
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("sosModHooks: Error detecting data type conflicts: " + e.getMessage());
        }
    }
    
    /**
     * Detect asset conflicts between two mods.
     */
    private void detectAssetConflicts(String modId1, ActiveModInfo modInfo1, String modId2, ActiveModInfo modInfo2) {
        try {
            LIST<String> assets1 = assetModifications.get(modId1);
            LIST<String> assets2 = assetModifications.get(modId2);
            
            if (assets1 != null && assets2 != null) {
                for (int i = 0; i < assets1.size(); i++) {
                    String asset1 = assets1.get(i);
                    for (int j = 0; j < assets2.size(); j++) {
                        String asset2 = assets2.get(j);
                        
                        if (asset1.equals(asset2)) {
                            System.out.println("sosModHooks: CONFLICT DETECTED: " + modInfo1.modName + " and " + modInfo2.modName + " both modify asset: " + asset1);
                            
                            // Register the conflict
                            registerModConflict(modId1, modId2, asset1, "ASSET_MODIFICATION");
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("sosModHooks: Error detecting asset conflicts: " + e.getMessage());
        }
    }
    
    // ========================================
    // UTILITY METHODS
    // ========================================
    
    /**
     * Register an active mod.
     */
    private void registerActiveMod(String modId, String modName, String version) {
        System.out.println("sosModHooks: Attempting to register mod - ID: '" + modId + "', Name: '" + modName + "', Version: '" + version + "'");
        
        if (!activeMods.containsKey(modId)) {
            ActiveModInfo modInfo = new ActiveModInfo(modId, modName, version);
            activeMods.put(modId, modInfo);
            
            // Also register in the main registry
            ModDeclaration declaration = new ModDeclaration(modId, modName, version);
            registeredMods.put(modId, declaration);
            
            System.out.println("sosModHooks: Successfully registered active mod: " + modName + " (" + modId + ") v" + version);
            System.out.println("sosModHooks: Total active mods now: " + activeMods.size());
        } else {
            System.out.println("sosModHooks: Mod " + modId + " already registered, skipping duplicate");
        }
    }
    
    /**
     * Register a mod conflict.
     */
    private void registerModConflict(String modId1, String modId2, String conflictType, String conflictCategory) {
        // This will be used by the conflict reporting system
        System.out.println("sosModHooks: Registered conflict: " + conflictType + " between " + modId1 + " and " + modId2);
    }
    
    /**
     * Extract mod name from a file path.
     */
    private String extractModNameFromPath(String path) {
        try {
            File file = new File(path);
            String fileName = file.getName();
            if (fileName.endsWith(".jar")) {
                return fileName.substring(0, fileName.length() - 4);
            }
        } catch (Exception e) {
            // Ignore errors
        }
        return null;
    }
    
    /**
     * Get a field value from a ModInfo object using reflection.
     */
    private String getModInfoField(Object modInfo, String fieldName) {
        try {
            Field field = modInfo.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(modInfo);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Parse scripts from the ScriptEngine.
     */
    private void parseScriptsFromEngine(Object currentScripts) {
        try {
            if (currentScripts instanceof String[]) {
                String[] scripts = (String[]) currentScripts;
                System.out.println("sosModHooks: Found " + scripts.length + " scripts from ScriptEngine");
                
                for (String script : scripts) {
                    System.out.println("sosModHooks: Script: " + script);
                    // Create a basic mod info entry for each script
                    ModDeclaration modDecl = new ModDeclaration(script, script, "1.0.0", "Script detected from ScriptEngine", "Unknown");
                    registerMod(script, script, "1.0.0");
                }
            }
        } catch (Exception e) {
            System.err.println("sosModHooks: Error parsing ScriptEngine scripts: " + e.getMessage());
        }
    }
    
    /**
     * Analyze a JAR file structure to detect what the mod is modifying.
     * Note: This method is no longer used since we can't access file system.
     */
    private void analyzeModJarStructure(File jarFile, String modId, String modName, ModAnalysis analysis) {
        // File system analysis removed - mods may come from Steam Workshop
    }
    
    /**
     * Check if file is an asset file.
     * Note: This method is no longer used since we can't access file system.
     */
    private boolean isAssetFile(String name) {
        return false; // File system analysis removed
    }
    
    // ========================================
    // LEGACY API SUPPORT (for backward compatibility)
    // ========================================
    
    /**
     * Register a mod with the compatibility framework.
     * This is the main API that other mods will use.
     */
    public void registerMod(String modId, String modName, String version) {
        if (registeredMods.containsKey(modId)) {
            System.out.println("sosModHooks: Mod " + modId + " already registered, updating...");
        }
        
        ModDeclaration declaration = new ModDeclaration(modId, modName, version);
        registeredMods.put(modId, declaration);
        
        System.out.println("sosModHooks: Mod registered: " + modId + " (" + modName + ") v" + version);
    }
    
    /**
     * Declare that a mod replaces specific core game classes.
     */
    public void declareClassReplacement(String modId, String... classNames) {
        if (!registeredMods.containsKey(modId)) {
            System.err.println("sosModHooks: Cannot declare class replacement for unregistered mod: " + modId);
            return;
        }
        
        ArrayList<String> classes = new ArrayList<>();
        for (String className : classNames) {
            classes.add(className);
        }
        
        classReplacements.put(modId, classes);
        
        System.out.println("sosModHooks: Mod " + modId + " declares replacement of " + classes.size() + " classes");
        for (String className : classes) {
            System.out.println("  - " + className);
        }
    }
    
    /**
     * Declare that a mod modifies specific asset files.
     */
    public void declareAssetModification(String modId, String... assetPaths) {
        if (!registeredMods.containsKey(modId)) {
            System.err.println("sosModHooks: Cannot declare asset modification for unregistered mod: " + modId);
            return;
        }
        
        ArrayList<String> assets = new ArrayList<>();
        for (String assetPath : assetPaths) {
            assets.add(assetPath);
        }
        
        assetModifications.put(modId, assets);
        
        System.out.println("sosModHooks: Mod " + modId + " declares modification of " + assets.size() + " assets");
        for (String assetPath : assets) {
            System.out.println("  - " + assetPath);
        }
    }
    
    /**
     * Declare that a mod modifies specific data structures.
     */
    public void declareDataModification(String modId, String... dataTypes) {
        if (!registeredMods.containsKey(modId)) {
            System.err.println("sosModHooks: Cannot declare data modification for unregistered mod: " + modId);
            return;
        }
        
        ArrayList<String> dataTypesList = new ArrayList<>();
        for (String dataType : dataTypes) {
            dataTypesList.add(dataType);
        }
        
        dataModifications.put(modId, dataTypesList);
        
        System.out.println("sosModHooks: Mod " + modId + " declares modification of " + dataTypesList.size() + " data types");
        for (String dataType : dataTypes) {
            System.out.println("  - " + dataType);
        }
    }
    
    /**
     * Declare dependencies on other mods.
     */
    public void declareDependency(String modId, String... dependencyIds) {
        if (!registeredMods.containsKey(modId)) {
            System.err.println("sosModHooks: Cannot declare dependency for unregistered mod: " + modId);
            return;
        }
        
        ArrayList<String> deps = new ArrayList<>();
        for (String depId : dependencyIds) {
            deps.add(depId);
        }
        
        dependencies.put(modId, deps);
        
        System.out.println("sosModHooks: Mod " + modId + " declares " + deps.size() + " dependencies");
        for (String depId : dependencyIds) {
            System.out.println("  - " + depId);
        }
    }
    
    /**
     * Check for conflicts between registered mods.
     * This is the single source of truth for conflict detection.
     */
    public LIST<ModConflict> detectConflicts() {
        ArrayList<ModConflict> conflicts = new ArrayList<>();
        
        // Check for class replacement conflicts
        detectClassReplacementConflicts(conflicts);
        
        // Check for asset modification conflicts
        detectAssetModificationConflicts(conflicts);
        
        // Check for data modification conflicts
        detectDataModificationConflicts(conflicts);
        
        // Check for missing dependencies
        detectMissingDependencies(conflicts);
        
        return conflicts;
    }
    
    private void detectClassReplacementConflicts(ArrayList<ModConflict> conflicts) {
        Map<String, ArrayList<String>> classToMods = new HashMap<>();
        
        // Build reverse mapping: class -> list of mods that replace it
        for (Map.Entry<String, LIST<String>> entry : classReplacements.entrySet()) {
            String modId = entry.getKey();
            LIST<String> classes = entry.getValue();
            
            for (String className : classes) {
                classToMods.computeIfAbsent(className, k -> new ArrayList<>()).add(modId);
            }
        }
        
        // Check for conflicts
        for (Map.Entry<String, ArrayList<String>> entry : classToMods.entrySet()) {
            String className = entry.getKey();
            ArrayList<String> mods = entry.getValue();
            
            if (mods.size() > 1) {
                conflicts.add(new ModConflict(
                    className,
                    mods,
                    ConflictType.CLASS_REPLACEMENT,
                    "Multiple mods replace the same class: " + className
                ));
            }
        }
    }
    
    private void detectAssetModificationConflicts(ArrayList<ModConflict> conflicts) {
        Map<String, ArrayList<String>> assetToMods = new HashMap<>();
        
        for (Map.Entry<String, LIST<String>> entry : assetModifications.entrySet()) {
            String modId = entry.getKey();
            LIST<String> assets = entry.getValue();
            
            for (String assetPath : assets) {
                assetToMods.computeIfAbsent(assetPath, k -> new ArrayList<>()).add(modId);
            }
        }
        
        for (Map.Entry<String, ArrayList<String>> entry : assetToMods.entrySet()) {
            String assetPath = entry.getKey();
            ArrayList<String> mods = entry.getValue();
            
            if (mods.size() > 1) {
                conflicts.add(new ModConflict(
                    assetPath,
                    mods,
                    ConflictType.ASSET_CONFLICT,
                    "Multiple mods modify the same asset: " + assetPath
                ));
            }
        }
    }
    
    private void detectDataModificationConflicts(ArrayList<ModConflict> conflicts) {
        Map<String, ArrayList<String>> dataTypeToMods = new HashMap<>();
        
        for (Map.Entry<String, LIST<String>> entry : dataModifications.entrySet()) {
            String modId = entry.getKey();
            LIST<String> dataTypes = entry.getValue();
            
            for (String dataType : dataTypes) {
                dataTypeToMods.computeIfAbsent(dataType, k -> new ArrayList<>()).add(modId);
            }
        }
        
        for (Map.Entry<String, ArrayList<String>> entry : dataTypeToMods.entrySet()) {
            String dataType = entry.getKey();
            ArrayList<String> mods = entry.getValue();
            
            if (mods.size() > 1) {
                conflicts.add(new ModConflict(
                    dataType,
                    mods,
                    ConflictType.DATA_CONFLICT,
                    "Multiple mods modify the same data type: " + dataType
                ));
            }
        }
    }
    
    private void detectMissingDependencies(ArrayList<ModConflict> conflicts) {
        for (Map.Entry<String, LIST<String>> entry : dependencies.entrySet()) {
            String modId = entry.getKey();
            LIST<String> requiredDeps = entry.getValue();
            
            for (String depId : requiredDeps) {
                if (!registeredMods.containsKey(depId)) {
                    conflicts.add(new ModConflict(
                        depId,
                        new ArrayList<>(modId),
                        ConflictType.MISSING_DEPENDENCY,
                        "Mod " + modId + " requires " + depId + " but it's not loaded"
                    ));
                }
            }
        }
    }
    
    // ========================================
    // UTILITY METHODS
    // ========================================
    
    /**
     * Get the total number of registered mods
     */
    public int getModCount() {
        return registeredMods.size();
    }
    
    /**
     * Check if a specific mod is registered
     */
    public boolean isModRegistered(String modId) {
        return registeredMods.containsKey(modId);
    }
    
    /**
     * Get mod declaration by ID
     */
    public ModDeclaration getMod(String modId) {
        return registeredMods.get(modId);
    }
    
    /**
     * Get all registered mods
     */
    public Map<String, ModDeclaration> getAllMods() {
        return new HashMap<>(registeredMods);
    }
    
    /**
     * Get all active mods as a map of mod ID to mod name
     */
    public Map<String, String> getActiveModNames() {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, ActiveModInfo> entry : activeMods.entrySet()) {
            result.put(entry.getKey(), entry.getValue().modName);
        }
        return result;
    }
    
    /**
     * Get all active mods
     */
    public Map<String, ActiveModInfo> getActiveMods() {
        return new HashMap<>(activeMods);
    }
    
    /**
     * Get all mod analyses for comprehensive overlay display
     */
    public Map<String, ModAnalysis> getAllModAnalyses() {
        return new HashMap<>(modAnalyses);
    }
    
    /**
     * Get a specific mod analysis by ID
     */
    public ModAnalysis getModAnalysis(String modId) {
        return modAnalyses.get(modId);
    }
    
    /**
     * Check if runtime detection has been completed
     */
    public boolean isRuntimeDetectionComplete() {
        return hasDetectedActiveMods && hasAnalyzedRuntimeEffects;
    }
    
    /**
     * Helper method to add data modifications to the registry.
     * This handles the LIST type properly by creating a new ArrayList if needed.
     */
    private void addDataModification(String modId, String dataType) {
        if (!this.dataModifications.containsKey(modId)) {
            this.dataModifications.put(modId, new ArrayList<>());
        }
        // Get the existing list and add to it
        LIST<String> existingList = this.dataModifications.get(modId);
        if (existingList instanceof ArrayList) {
            ((ArrayList<String>) existingList).add(dataType);
        } else {
            // If it's not an ArrayList, create a new one with the existing data
            ArrayList<String> newList = new ArrayList<>();
            for (int i = 0; i < existingList.size(); i++) {
                newList.add(existingList.get(i));
            }
            newList.add(dataType);
            this.dataModifications.put(modId, newList);
        }
    }
    
    // ========================================
    // INTERNAL DATA CLASSES
    // ========================================
    
    /**
     * Internal class to hold active mod information during runtime detection
     */
    public static class ActiveModInfo {
        final String modId;
        final String modName;
        final String modVersion;
        
        ActiveModInfo(String modId, String modName, String modVersion) {
            this.modId = modId;
            this.modName = modName;
            this.modVersion = modVersion;
        }
    }
    
    /**
     * Comprehensive mod analysis containing all detected modifications and file information
     */
    public static class ModAnalysis {
        private final String modId;
        private final String modName;
        private final String modVersion;
        
        // File system analysis
        private final Map<String, FileModification> fileModifications;
        private final Map<String, AssetModification> assetModifications;
        private final Map<String, DataModification> dataModifications;
        private final Map<String, ScriptModification> scriptModifications;
        
        // Runtime analysis
        private final Map<String, RuntimeModification> runtimeModifications;
        
        // Conflict information
        private final ArrayList<String> conflicts;
        
        public ModAnalysis(String modId, String modName, String modVersion) {
            this.modId = modId;
            this.modName = modName;
            this.modVersion = modVersion;
            this.fileModifications = new HashMap<>();
            this.assetModifications = new HashMap<>();
            this.dataModifications = new HashMap<>();
            this.scriptModifications = new HashMap<>();
            this.runtimeModifications = new HashMap<>();
            this.conflicts = new ArrayList<String>();
        }
        
        // Getters
        public String getModId() { return modId; }
        public String getModName() { return modName; }
        public String getModVersion() { return modVersion; }
        public Map<String, FileModification> getFileModifications() { return fileModifications; }
        public Map<String, AssetModification> getAssetModifications() { return assetModifications; }
        public Map<String, DataModification> getDataModifications() { return dataModifications; }
        public Map<String, ScriptModification> getScriptModifications() { return scriptModifications; }
        public Map<String, RuntimeModification> getRuntimeModifications() { return runtimeModifications; }
        public ArrayList<String> getConflicts() { return conflicts; }
        
        // Add modification methods
        public void addFileModification(String path, FileModification mod) {
            fileModifications.put(path, mod);
        }
        
        public void addAssetModification(String type, AssetModification mod) {
            assetModifications.put(type, mod);
        }
        
        public void addDataModification(String type, DataModification mod) {
            dataModifications.put(type, mod);
        }
        
        public void addScriptModification(String className, ScriptModification mod) {
            scriptModifications.put(className, mod);
        }
        
        public void addRuntimeModification(String target, RuntimeModification mod) {
            runtimeModifications.put(target, mod);
        }
        
        public void addConflict(String conflict) {
            conflicts.add(conflict);
        }
        
        public int getTotalModifications() {
            return fileModifications.size() + assetModifications.size() + 
                   dataModifications.size() + scriptModifications.size() + 
                   runtimeModifications.size();
        }
    }
    
    /**
     * Represents a file modification by a mod
     */
    public static class FileModification {
        private final String filePath;
        private final String modificationType; // "ADDED", "MODIFIED", "REPLACED"
        private final long fileSize;
        private final String fileHash;
        
        public FileModification(String filePath, String modificationType, long fileSize) {
            this.filePath = filePath;
            this.modificationType = modificationType;
            this.fileSize = fileSize;
            this.fileHash = ""; // Could be implemented later for integrity checking
        }
        
        public String getFilePath() { return filePath; }
        public String getModificationType() { return modificationType; }
        public long getFileSize() { return fileSize; }
        public String getFileHash() { return fileHash; }
    }
    
    /**
     * Represents an asset modification by a mod
     */
    public static class AssetModification {
        private final String assetType; // "SPRITE", "TEXTURE", "SOUND", "MUSIC"
        private final String assetPath;
        private final String modificationType;
        private final boolean isCompleteOverride;
        
        public AssetModification(String assetType, String assetPath, String modificationType, boolean isCompleteOverride) {
            this.assetType = assetType;
            this.assetPath = assetPath;
            this.modificationType = modificationType;
            this.isCompleteOverride = isCompleteOverride;
        }
        
        public String getAssetType() { return assetType; }
        public String getAssetPath() { return assetPath; }
        public String getModificationType() { return modificationType; }
        public boolean isCompleteOverride() { return isCompleteOverride; }
    }
    
    /**
     * Represents a data modification by a mod
     */
    public static class DataModification {
        private final String dataType; // "RACE", "TECH", "ROOM", "RESOURCE", etc.
        private final String modificationType; // "ADDED", "MODIFIED", "REPLACED"
        private final int recordCount;
        private final boolean isCompleteOverride;
        
        public DataModification(String dataType, String modificationType, int recordCount, boolean isCompleteOverride) {
            this.dataType = dataType;
            this.modificationType = modificationType;
            this.recordCount = recordCount;
            this.isCompleteOverride = isCompleteOverride;
        }
        
        public String getDataType() { return dataType; }
        public String getModificationType() { return modificationType; }
        public int getRecordCount() { return recordCount; }
        public boolean isCompleteOverride() { return isCompleteOverride; }
    }
    
    /**
     * Represents a script modification by a mod
     */
    public static class ScriptModification {
        private final String className;
        private final String modificationType; // "ADDED", "MODIFIED", "REPLACED"
        private final String packageName;
        private final boolean hasCustomMethods;
        
        public ScriptModification(String className, String modificationType, String packageName, boolean hasCustomMethods) {
            this.className = className;
            this.modificationType = modificationType;
            this.packageName = packageName;
            this.hasCustomMethods = hasCustomMethods;
        }
        
        public String getClassName() { return className; }
        public String getModificationType() { return modificationType; }
        public String getPackageName() { return packageName; }
        public boolean hasCustomMethods() { return hasCustomMethods; }
    }
    
    /**
     * Represents a runtime modification detected during gameplay
     */
    public static class RuntimeModification {
        private final String target; // What is being modified
        private final String modificationType; // "METHOD_CALL", "FIELD_ACCESS", "CLASS_LOADING"
        private final String description;
        private final long timestamp;
        
        public RuntimeModification(String target, String modificationType, String description) {
            this.target = target;
            this.modificationType = modificationType;
            this.description = description;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getTarget() { return target; }
        public String getModificationType() { return modificationType; }
        public String getDescription() { return description; }
        public long getTimestamp() { return timestamp; }
    }
}

