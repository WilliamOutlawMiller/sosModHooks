package sosModHooks;

import lombok.Getter;
import lombok.Setter;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
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
    }
    
    public static ModRegistry getInstance() {
        if (instance == null) {
            instance = new ModRegistry();
        }
        return instance;
    }
    
    /**
     * Write debug information to a log file.
     */
    private void writeLog(String message) {
        try {
            File logFile = new File("sosModHooks_debug.log");
            try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
                writer.println(System.currentTimeMillis() + " - " + message);
            }
        } catch (Exception e) {
            // Ignore logging errors
        }
    }
    
    /**
     * Get the current game version from the VERSION class.
     */
    private String getCurrentGameVersion() {
        try {
            // Try to access the game's VERSION class
            Class<?> versionClass = Class.forName("game.VERSION");
            int major = (Integer) versionClass.getField("VERSION_MAJOR").get(null);
            String versionString = "V" + major;
            return versionString;
        } catch (Exception e) {
            // Fallback to V69 if detection fails
            return "V69";
        }
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
            return;
        }
        
        try {
            // Use a consolidated approach to avoid duplicates
            detectModsConsolidated();
            
            // Phase 2: Runtime monitoring for actual modifications
            setupRuntimeMonitoring();
            
        } catch (Exception e) {
            // Silently fail - will retry later
        }
        
        hasDetectedActiveMods = true;
    }
    
    /**
     * Phase 2: Runtime monitoring for actual modifications.
     * This monitors what's actually happening during gameplay.
     */
    private void setupRuntimeMonitoring() {
        try {
            // Analyze actual mod files on disk for real modifications
            analyzeModFilesOnDisk();
            
            // Set up periodic re-analysis for late-loading mods
            setupPeriodicAnalysis();
            
        } catch (Exception e) {
            // Silently fail - will retry later
        }
    }
    
    /**
     * Analyze actual mod files on disk to detect real modifications.
     * This is the primary method for detecting actual conflicts.
     */
    private void analyzeModFilesOnDisk() {
        try {
            // Limit the number of mods processed to prevent hitting game limits
            int maxModsToProcess = Math.min(activeMods.size(), 5); // Process max 5 mods at once
            
            int processedCount = 0;
            // For each detected mod, analyze its actual files (limited)
            for (Map.Entry<String, ActiveModInfo> entry : activeMods.entrySet()) {
                if (processedCount >= maxModsToProcess) {
                    break;
                }
                
                String modId = entry.getKey();
                ActiveModInfo modInfo = entry.getValue();
                
                // Find the actual mod directory on disk
                String modPath = findModPathOnDisk(modInfo);
                if (modPath != null) {
                    analyzeModDirectory(modId, modInfo.modName, modPath);
                }
                
                processedCount++;
            }
            
        } catch (Exception e) {
            // Silently fail - will retry later
        }
    }
    
    /**
     * Find the actual mod path on disk.
     */
    private String findModPathOnDisk(ActiveModInfo modInfo) {
        try {
            // Use the game's PATHS system instead of hardcoded paths
            Class<?> pathsClass = Class.forName("init.paths.PATHS");
            Field instanceField = pathsClass.getDeclaredField("i");
            instanceField.setAccessible(true);
            Object pathsInstance = instanceField.get(null);
            
            if (pathsInstance != null) {
                // Get the mods list directly from PATHS
                Field modsField = pathsInstance.getClass().getDeclaredField("mods");
                modsField.setAccessible(true);
                Object modsList = modsField.get(pathsInstance);
                
                if (modsList != null) {
                    Method sizeMethod = modsList.getClass().getMethod("size");
                    int modCount = (Integer) sizeMethod.invoke(modsList);
                    
                    for (int i = 0; i < modCount; i++) {
                        Method getMethod = modsList.getClass().getMethod("get", int.class);
                        Object modInfoFromPaths = getMethod.invoke(modsList, i);
                        
                        if (modInfoFromPaths != null) {
                            String modName = getModInfoField(modInfoFromPaths, "name");
                            String modPath = getModInfoField(modInfoFromPaths, "path");
                            
                            // Match by name (case-insensitive)
                            if (modName != null && modName.toLowerCase().equals(modInfo.modName.toLowerCase())) {
                                return modPath;
                            }
                        }
                    }
                }
            }
            
            return null;
            
        } catch (Exception e) {
            // Silently fail - will retry later
            return null;
        }
    }
    
    /**
     * Find a mod in Steam Workshop directory.
     */
    private String findModInSteamWorkshop(File baseDir, ActiveModInfo modInfo) {
        try {
            System.out.println("sosModHooks: Searching Steam Workshop for mod: " + modInfo.modName + " (ID: " + modInfo.modId + ")");
            writeLog("Searching Steam Workshop for mod: " + modInfo.modName + " (ID: " + modInfo.modId + ")");
            
            // This method is deprecated - use findModPathOnDisk instead
            return null;
        } catch (Exception e) {
            // Silently fail - will retry later
            return null;
        }
    }
    
    /**
     * Find a mod in local directory (deprecated - use findModPathOnDisk instead).
     */
    private String findModInLocalDirectory(File baseDir, ActiveModInfo modInfo) {
        // This method is deprecated - use findModPathOnDisk instead
        return null;
    }
    
    /**
     * Check if a directory contains the specified mod.
     */
    private boolean isModDirectory(File dir, ActiveModInfo modInfo) {
        try {
            System.out.println("sosModHooks: Checking if directory is mod: " + dir.getPath());
            
            // FIRST: Check if the directory name matches the mod we're looking for
            String dirName = dir.getName().toLowerCase();
            String modName = modInfo.modName.toLowerCase();
            
            // Check for exact name match first (highest priority)
            if (dirName.equals(modName)) {
                System.out.println("sosModHooks: Found exact name match: " + dir.getName());
                return true;
            }
            
            // Check for partial name match (moderate priority)
            if (dirName.contains(modName) || modName.contains(dirName)) {
                System.out.println("sosModHooks: Found partial name match: " + dir.getName() + " for mod: " + modInfo.modName);
                return true;
            }
            
            // Check for version directories (V69, V68, etc.) - only if name matches
            File[] versionDirs = dir.listFiles((d, name) -> d.isDirectory() && name.startsWith("V"));
            if (versionDirs != null && versionDirs.length > 0) {
                System.out.println("sosModHooks: Found version directory: " + versionDirs[0].getName());
                
                // Check if this version directory contains script or assets
                File versionDir = versionDirs[0];
                if (new File(versionDir, "script").exists() || new File(versionDir, "assets").exists()) {
                    System.out.println("sosModHooks: Directory contains script or assets - likely a mod");
                    return true;
                }
            }
            
            // Check for script or assets directories directly
            if (new File(dir, "script").exists() || new File(dir, "assets").exists()) {
                System.out.println("sosModHooks: Directory contains script or assets directly - likely a mod");
                return true;
            }
            
            // Check for mod.json or similar config files
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().toLowerCase().contains("mod") || 
                        file.getName().toLowerCase().contains("config") ||
                        file.getName().toLowerCase().contains("info")) {
                        System.out.println("sosModHooks: Directory contains config file: " + file.getName());
                        return true;
                    }
                }
            }
            
            System.out.println("sosModHooks: Directory does not appear to be a mod");
            return false;
            
        } catch (Exception e) {
            System.err.println("sosModHooks: Error checking if directory is mod: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Analyze a mod directory to detect actual modifications.
     */
    private void analyzeModDirectory(String modId, String modName, String modPath) {
        try {
            File modDir = new File(modPath);
            if (!modDir.exists() || !modDir.isDirectory()) {
                return;
            }
            
            // Look for version-specific directories
            File[] versionDirs = modDir.listFiles((dir, name) -> dir.isDirectory() && name.startsWith("V"));
            if (versionDirs != null && versionDirs.length > 0) {
                // Get the current game version
                String currentVersion = getCurrentGameVersion();
                
                // First, try to find the exact current game version
                File targetVersionDir = null;
                for (File versionDir : versionDirs) {
                    if (versionDir.getName().equals(currentVersion)) {
                        targetVersionDir = versionDir;
                        break;
                    }
                }
                
                // If no exact match, find the closest version (highest version number)
                if (targetVersionDir == null) {
                    int currentMajor = Integer.parseInt(currentVersion.substring(1));
                    int bestVersion = -1;
                    
                    for (File versionDir : versionDirs) {
                        try {
                            int versionMajor = Integer.parseInt(versionDir.getName().substring(1));
                            if (versionMajor <= currentMajor && versionMajor > bestVersion) {
                                bestVersion = versionMajor;
                                targetVersionDir = versionDir;
                            }
                        } catch (NumberFormatException e) {
                            // Skip non-numeric version directories
                            continue;
                        }
                    }
                }
                
                // If still no match, use the first version directory as fallback
                if (targetVersionDir == null) {
                    targetVersionDir = versionDirs[0];
                }
                
                // Analyze the selected version directory
                analyzeVersionDirectory(modId, modName, targetVersionDir);
            } else {
                // Analyze the root directory directly
                analyzeModRootDirectory(modId, modName, modDir);
            }
            
        } catch (Exception e) {
            // Silently fail - will retry later
        }
    }
    
    /**
     * Analyze a version-specific directory (V69, V68, etc.).
     */
    private void analyzeVersionDirectory(String modId, String modName, File versionDir) {
        try {
            // Analyze script directory for class modifications
            File scriptDir = new File(versionDir, "script");
            if (scriptDir.exists() && scriptDir.isDirectory()) {
                analyzeScriptDirectory(modId, modName, scriptDir);
            }
            
            // Analyze assets directory for file modifications
            File assetsDir = new File(versionDir, "assets");
            if (assetsDir.exists() && assetsDir.isDirectory()) {
                analyzeAssetsDirectory(modId, modName, assetsDir);
            }
            
            // Analyze data directory for configuration changes
            File dataDir = new File(versionDir, "data");
            if (dataDir.exists() && dataDir.isDirectory()) {
                analyzeDataDirectory(modId, modName, dataDir);
            }
            
            // Also check for Java source files in the version directory
            File[] javaFiles = versionDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".java"));
            if (javaFiles != null && javaFiles.length > 0) {
                for (File javaFile : javaFiles) {
                    declareDataModification(modId, "JAVA_SOURCE");
                }
            }
            
        } catch (Exception e) {
            // Silently fail - will retry later
        }
    }
    
    /**
     * Analyze the root mod directory for modifications.
     */
    private void analyzeModRootDirectory(String modId, String modName, File modDir) {
        try {
            System.out.println("sosModHooks: Analyzing root mod directory: " + modDir.getName());
            
            // Look for JAR files
            File[] jarFiles = modDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
            if (jarFiles != null) {
                for (File jarFile : jarFiles) {
                    analyzeJarFile(modId, modName, jarFile);
                }
            }
            
            // Look for script files
            File[] scriptFiles = modDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".js") || 
                                                                 name.toLowerCase().endsWith(".py") ||
                                                                 name.toLowerCase().endsWith(".lua"));
            if (scriptFiles != null && scriptFiles.length > 0) {
                declareDataModification(modId, "SCRIPT");
                System.out.println("sosModHooks: Detected script files in root directory");
            }
            
        } catch (Exception e) {
            System.err.println("sosModHooks: Error analyzing root mod directory: " + e.getMessage());
        }
    }
    
    /**
     * Analyze the script directory for class modifications.
     */
    private void analyzeScriptDirectory(String modId, String modName, File scriptDir) {
        try {
            System.out.println("sosModHooks: Analyzing script directory: " + scriptDir.getPath());
            writeLog("Analyzing script directory: " + scriptDir.getPath());
            
            // Look for JAR files
            File[] jarFiles = scriptDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
            if (jarFiles != null) {
                System.out.println("sosModHooks: Found " + jarFiles.length + " JAR files in script directory");
                writeLog("Found " + jarFiles.length + " JAR files in script directory");
                for (File jarFile : jarFiles) {
                    System.out.println("sosModHooks: Analyzing JAR file: " + jarFile.getName());
                    writeLog("Analyzing JAR file: " + jarFile.getName());
                    analyzeJarFile(modId, modName, jarFile);
                }
            } else {
                System.out.println("sosModHooks: No JAR files found in script directory");
                writeLog("No JAR files found in script directory");
            }
            
            // Look for script files
            File[] scriptFiles = scriptDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".js") || 
                                                                   name.toLowerCase().endsWith(".py") ||
                                                                   name.toLowerCase().endsWith(".lua"));
            if (scriptFiles != null && scriptFiles.length > 0) {
                System.out.println("sosModHooks: Detected " + scriptFiles.length + " script files");
                writeLog("Detected " + scriptFiles.length + " script files");
                declareDataModification(modId, "SCRIPT");
                System.out.println("sosModHooks: Declared script modification for mod: " + modId);
                writeLog("Declared script modification for mod: " + modId);
            } else {
                System.out.println("sosModHooks: No script files found in script directory");
                writeLog("No script files found in script directory");
            }
            
            System.out.println("sosModHooks: Completed script directory analysis for mod: " + modId);
            writeLog("Completed script directory analysis for mod: " + modId);
            
        } catch (Exception e) {
            System.err.println("sosModHooks: Error analyzing script directory: " + e.getMessage());
            writeLog("Error analyzing script directory: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Analyze the assets directory for file modifications.
     */
    private void analyzeAssetsDirectory(String modId, String modName, File assetsDir) {
        try {
            System.out.println("sosModHooks: Analyzing assets directory: " + assetsDir.getPath());
            writeLog("Analyzing assets directory: " + assetsDir.getPath());
            
            // Recursively scan for asset files
            System.out.println("sosModHooks: Starting recursive asset scan for mod: " + modId);
            writeLog("Starting recursive asset scan for mod: " + modId);
            scanAssetsRecursively(modId, modName, assetsDir, "");
            System.out.println("sosModHooks: Completed recursive asset scan for mod: " + modId);
            writeLog("Completed recursive asset scan for mod: " + modId);
            
        } catch (Exception e) {
            System.err.println("sosModHooks: Error analyzing assets directory: " + e.getMessage());
            writeLog("Error analyzing assets directory: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Recursively scan assets directory for modifications.
     * Uses batched processing to avoid hitting game limits.
     */
    private void scanAssetsRecursively(String modId, String modName, File dir, String relativePath) {
        try {
            // Limit recursion depth to prevent stack overflow and game limits
            if (relativePath.split("/").length > 5) {
                System.out.println("sosModHooks: Skipping deep directory: " + relativePath);
                return;
            }
            
            File[] files = dir.listFiles();
            if (files != null) {
                // Process files in batches to avoid hitting game limits
                int processedFiles = 0;
                int maxFilesPerBatch = 100; // Limit files per batch
                
                for (File file : files) {
                    if (processedFiles >= maxFilesPerBatch) {
                        System.out.println("sosModHooks: Reached file limit for batch, declaring batch modification");
                        declareAssetModification(modId, "/data/assets/" + relativePath + "/*");
                        break;
                    }
                    
                    String currentPath = relativePath.isEmpty() ? file.getName() : relativePath + "/" + file.getName();
                    
                    if (file.isDirectory()) {
                        // Recursively scan subdirectories
                        scanAssetsRecursively(modId, modName, file, currentPath);
                    } else {
                        // Analyze individual files
                        analyzeAssetFile(modId, modName, file, currentPath);
                        processedFiles++;
                    }
                }
                
                // If we processed a significant number of files, declare a batch modification
                if (processedFiles >= 50) {
                    System.out.println("sosModHooks: Processed " + processedFiles + " files, declaring batch modification");
                    declareAssetModification(modId, "/data/assets/" + relativePath + "/*");
                }
            }
        } catch (Exception e) {
            System.err.println("sosModHooks: Error scanning assets recursively: " + e.getMessage());
        }
    }
    
    /**
     * Analyze an individual asset file.
     */
    private void analyzeAssetFile(String modId, String modName, File file, String relativePath) {
        try {
            System.out.println("sosModHooks: Analyzing asset file: " + file.getName() + " for mod: " + modId);
            
            String fileName = file.getName().toLowerCase();
            String fullPath = "/data/assets/" + relativePath;
            
            if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                // Image file
                System.out.println("sosModHooks: Calling declareAssetModification for image: " + fullPath);
                declareAssetModification(modId, fullPath);
                System.out.println("sosModHooks: Detected image asset: " + fullPath);
            } else if (fileName.endsWith(".wav") || fileName.endsWith(".mp3") || fileName.endsWith(".ogg")) {
                // Audio file
                System.out.println("sosModHooks: Calling declareAssetModification for audio: " + fullPath);
                declareAssetModification(modId, fullPath);
                System.out.println("sosModHooks: Detected audio asset: " + fullPath);
            } else if (fileName.endsWith(".txt") || fileName.endsWith(".json") || fileName.endsWith(".xml")) {
                // Configuration file
                String dataType = determineDataTypeFromPath(relativePath);
                if (dataType != null) {
                    System.out.println("sosModHooks: Calling declareDataModification for data type: " + dataType);
                    declareDataModification(modId, dataType);
                    System.out.println("sosModHooks: Detected data modification: " + dataType + " in " + fullPath);
                }
                // Also mark as asset modification
                System.out.println("sosModHooks: Calling declareAssetModification for config file: " + fullPath);
                declareAssetModification(modId, fullPath);
            }
            
        } catch (Exception e) {
            System.err.println("sosModHooks: Error analyzing asset file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Analyze the data directory for configuration changes.
     */
    private void analyzeDataDirectory(String modId, String modName, File dataDir) {
        try {
            System.out.println("sosModHooks: Analyzing data directory: " + dataDir.getPath());
            
            // Look for configuration files
            File[] configFiles = dataDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt") || 
                                                                   name.toLowerCase().endsWith(".json") ||
                                                                   name.toLowerCase().endsWith(".xml"));
            if (configFiles != null && configFiles.length > 0) {
                for (File configFile : configFiles) {
                    String dataType = determineDataTypeFromPath(configFile.getName());
                    if (dataType != null) {
                        declareDataModification(modId, dataType);
                        System.out.println("sosModHooks: Detected data modification: " + dataType + " in " + configFile.getName());
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("sosModHooks: Error analyzing data directory: " + e.getMessage());
        }
    }
    
    /**
     * Analyze a JAR file for class modifications.
     */
    private void analyzeJarFile(String modId, String modName, File jarFile) {
        try {
            System.out.println("sosModHooks: Analyzing JAR file: " + jarFile.getName());
            
            java.util.jar.JarFile jar = new java.util.jar.JarFile(jarFile);
            java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries();
            
            int classCount = 0;
            int assetCount = 0;
            int dataCount = 0;
            
            while (entries.hasMoreElements()) {
                java.util.jar.JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                
                if (entryName.endsWith(".class")) {
                    // Class file - check if it's replacing a base game class
                    String className = entryName.replace("/", ".").replace(".class", "");
                    if (isReplacingBaseGameClass(className)) {
                        declareClassReplacement(modId, className);
                        classCount++;
                        System.out.println("sosModHooks: Detected class replacement: " + className);
                    }
                } else if (entryName.startsWith("data/assets/")) {
                    // Asset file
                    declareAssetModification(modId, "/" + entryName);
                    assetCount++;
                    System.out.println("sosModHooks: Detected asset modification: " + entryName);
                } else if (entryName.startsWith("data/") && entryName.endsWith(".txt")) {
                    // Data file
                    String dataType = determineDataTypeFromPath(entryName);
                    if (dataType != null) {
                        declareDataModification(modId, dataType);
                        dataCount++;
                        System.out.println("sosModHooks: Detected data modification: " + dataType + " in " + entryName);
                    }
                }
            }
            
            jar.close();
            
            System.out.println("sosModHooks: JAR analysis complete - Classes: " + classCount + ", Assets: " + assetCount + ", Data: " + dataCount);
            
        } catch (Exception e) {
            System.err.println("sosModHooks: Error analyzing JAR file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Check if a class is replacing a base game class.
     * Uses lightweight pattern matching instead of expensive class loading.
     */
    private boolean isReplacingBaseGameClass(String className) {
        try {
            // Use lightweight pattern matching instead of expensive class loading
            // Look for common base game class patterns
            String lowerClassName = className.toLowerCase();
            
            // Common base game packages
            if (lowerClassName.startsWith("game.") || 
                lowerClassName.startsWith("world.") || 
                lowerClassName.startsWith("menu.") || 
                lowerClassName.startsWith("util.") || 
                lowerClassName.startsWith("script.") ||
                lowerClassName.startsWith("init.")) {
                
                // This is likely a base game class being replaced
                System.out.println("sosModHooks: Detected potential base game class replacement: " + className);
                return true;
            }
            
            // Check for specific common base game classes
            if (lowerClassName.contains("instance") || 
                lowerClassName.contains("manager") || 
                lowerClassName.contains("controller") ||
                lowerClassName.contains("handler") ||
                lowerClassName.contains("system")) {
                
                // Could be replacing base game functionality
                System.out.println("sosModHooks: Detected potential system class replacement: " + className);
                return false;
            }
            
            // Default: assume it's new functionality, not replacement
            return false;
            
        } catch (Exception e) {
            // Error in pattern matching - assume it's not replacing base game
            System.err.println("sosModHooks: Error in lightweight class replacement check: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Determine data type from file path.
     */
    private String determineDataTypeFromPath(String filePath) {
        String lowerPath = filePath.toLowerCase();
        
        if (lowerPath.contains("race") || lowerPath.contains("faction")) {
            return "RACE";
        } else if (lowerPath.contains("event") || lowerPath.contains("story")) {
            return "EVENT";
        } else if (lowerPath.contains("tech") || lowerPath.contains("research")) {
            return "TECH";
        } else if (lowerPath.contains("resource") || lowerPath.contains("item")) {
            return "RESOURCE";
        } else if (lowerPath.contains("room") || lowerPath.contains("building")) {
            return "ROOM";
        } else if (lowerPath.contains("config")) {
            return "CONFIG";
        }
        
        return null;
    }
    
    /**
     * Set up class loading interceptor to monitor new class loads.
     */
    private void setupClassLoadingInterceptor() {
        // This would intercept class loading to detect new mod classes
        // For now, we'll use periodic analysis
        System.out.println("sosModHooks: Class loading interceptor setup (using periodic analysis)");
    }
    
    /**
     * Set up fallback class monitoring for non-URLClassLoader scenarios.
     */
    private void setupFallbackClassMonitoring() {
        System.out.println("sosModHooks: Using fallback class monitoring");
        // Implement alternative monitoring approach
    }
    
    /**
     * Set up resource loading monitoring.
     */
    private void setupResourceLoadingMonitoring() {
        System.out.println("sosModHooks: Resource loading monitoring setup (stub)");
        // TODO: Implement resource loading monitoring
    }
    
    /**
     * Set up file system monitoring.
     */
    private void setupFileSystemMonitoring() {
        System.out.println("sosModHooks: File system monitoring setup (stub)");
        // TODO: Implement file system change monitoring
    }
    
    /**
     * Set up periodic analysis for late-loading mods.
     */
    private void setupPeriodicAnalysis() {
        Thread analysisThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(10000); // Check every 10 seconds
                    reanalyzeClasspath();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    // Silently fail - will retry later
                }
            }
        });
        
        analysisThread.setDaemon(true);
        analysisThread.start();
    }
    
    /**
     * Re-analyze the classpath for newly loaded mods.
     */
    private void reanalyzeClasspath() {
        try {
            ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
            if (currentLoader instanceof java.net.URLClassLoader) {
                java.net.URLClassLoader urlLoader = (java.net.URLClassLoader) currentLoader;
                // Classpath analysis removed - using file system analysis instead
            }
        } catch (Exception e) {
            // Silently fail - will retry later
        }
    }
    
    /**
     * Consolidated mod detection that avoids duplicates and provides real analysis.
     */
    private void detectModsConsolidated() {
        // Track detected mods by name to avoid duplicates
        Map<String, String> detectedModNames = new HashMap<>();
        
        try {
            // Method 1: Get mods from PATHS system (most reliable)
            detectModsFromPATHS(detectedModNames);
            
            // Method 2: Analyze classpaths for additional mods
            detectModsFromClasspaths(detectedModNames);
            
            // Method 3: Fallback to directory scanning if needed
            if (activeMods.isEmpty()) {
                detectModsFromDirectoryScanning(detectedModNames);
            }
            
        } catch (Exception e) {
            // Silently fail - will retry later
        }
    }
    
    /**
     * Detect mods from the game's PATHS system (primary method).
     */
    private void detectModsFromPATHS(Map<String, String> detectedModNames) {
        try {
            Class<?> pathsClass = Class.forName("init.paths.PATHS");
            
            Field instanceField = pathsClass.getDeclaredField("i");
            instanceField.setAccessible(true);
            Object pathsInstance = instanceField.get(null);
            
            if (pathsInstance != null) {
                // Get the mods list directly from PATHS
                Field modsField = pathsInstance.getClass().getDeclaredField("mods");
                modsField.setAccessible(true);
                Object modsList = modsField.get(pathsInstance);
                
                if (modsList != null) {
                    Method sizeMethod = modsList.getClass().getMethod("size");
                    int modCount = (Integer) sizeMethod.invoke(modsList);
                    
                    for (int i = 0; i < modCount; i++) {
                        try {
                            Method getMethod = modsList.getClass().getMethod("get", int.class);
                            Object modInfo = getMethod.invoke(modsList, i);
                            
                            if (modInfo != null) {
                                String modName = getModInfoField(modInfo, "name");
                                String modVersion = getModInfoField(modInfo, "version");
                                String modPath = getModInfoField(modInfo, "path");
                                
                                if (modName != null && !modName.equals("???")) {
                                    // Use mod name as the primary identifier, path as secondary
                                    String modId = generateModId(modName, modPath);
                                    
                                    if (!detectedModNames.containsKey(modName.toLowerCase())) {
                                        detectedModNames.put(modName.toLowerCase(), modId);
                                        registerActiveMod(modId, modName, modVersion);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // Silently fail - will retry later
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            // Silently fail - will retry later
        }
    }
    
    /**
     * Detect mods from classpaths (secondary method).
     */
    private void detectModsFromClasspaths(Map<String, String> detectedModNames) {
        try {
            Class<?> pathsClass = Class.forName("init.paths.PATHS");
            Field instanceField = pathsClass.getDeclaredField("i");
            instanceField.setAccessible(true);
            Object pathsInstance = instanceField.get(null);
            
            if (pathsInstance != null) {
                Field scriptField = pathsInstance.getClass().getDeclaredField("SCRIPT");
                scriptField.setAccessible(true);
                Object scriptInstance = scriptField.get(pathsInstance);
                
                if (scriptInstance != null) {
                    Method modClasspathsMethod = scriptInstance.getClass().getMethod("modClasspaths");
                    Object classpathsList = modClasspathsMethod.invoke(scriptInstance);
                    
                    if (classpathsList != null) {
                        Method sizeMethod = classpathsList.getClass().getMethod("size");
                        int classpathCount = (Integer) sizeMethod.invoke(classpathsList);
                        
                        for (int i = 0; i < classpathCount; i++) {
                            try {
                                Method getMethod = classpathsList.getClass().getMethod("get", int.class);
                                String classpath = (String) getMethod.invoke(classpathsList, i);
                                
                                if (classpath != null && classpath.contains("mods")) {
                                    String modName = extractModNameFromClasspath(classpath);
                                    if (modName != null && !detectedModNames.containsKey(modName.toLowerCase())) {
                                        String modId = generateModId(modName, classpath);
                                        detectedModNames.put(modName.toLowerCase(), modId);
                                        registerActiveMod(modId, modName, "1.0.0");
                                    }
                                }
                            } catch (Exception e) {
                                // Silently fail - will retry later
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Silently fail - will retry later
        }
    }
    
    /**
     * Generate a unique mod ID from name and path.
     */
    private String generateModId(String modName, String path) {
        // Check if the path is a Steam Workshop ID (just numbers)
        if (path != null && path.matches("\\d+")) {
            return "workshop_" + path;
        }
        
        // Check if path contains workshop
        if (path != null && path.contains("workshop")) {
            // Extract Steam Workshop ID if available
            String[] pathParts = path.split("[\\\\/]");
            for (String part : pathParts) {
                if (part.matches("\\d+") && part.length() > 8) {
                    // Likely a Steam Workshop ID
                    return "workshop_" + part;
                }
            }
        }
        
        // Use mod name as ID, sanitized
        return modName.toLowerCase().replaceAll("[^a-z0-9_]", "_");
    }
    
    /**
     * Analyze actual mod files to detect what the mod is modifying.
     * This is the core of real conflict detection.
     */
    // Old file analysis method removed - replaced with runtime monitoring
    
    /**
     * Extract the mod directory from a mod path.
     */
    private String extractModDirectory(String modPath) {
        try {
            if (modPath == null || modPath.isEmpty()) {
                return null;
            }
            
            // Handle different path formats
            if (modPath.contains("workshop")) {
                // Steam Workshop path
                String[] parts = modPath.split("[\\\\/]");
                for (int i = 0; i < parts.length - 1; i++) {
                    if ("workshop".equals(parts[i]) && i + 2 < parts.length) {
                        // workshop/[id]/[modname]
                        return parts[i + 2];
                    }
                }
            } else if (modPath.contains("mods")) {
                // Local mods path
                String[] parts = modPath.split("[\\\\/]");
                for (int i = 0; i < parts.length - 1; i++) {
                    if ("mods".equals(parts[i]) && i + 1 < parts.length) {
                        return parts[i + 1];
                    }
                }
            }
            
            return null;
        } catch (Exception e) {
            // Silently fail - will retry later
            return null;
        }
    }
    
         /**
      * Analyze the actual file structure of a mod to detect modifications.
      */
     private void analyzeModFileStructure(String modId, String modName, String modDirectory) {
         System.out.println("sosModHooks: --- Analyzing file structure for: " + modName + " ---");
         
         try {
             // Analyze the actual mod files for real conflict detection
             analyzeModFilesByDirectory(modId, modName, modDirectory);
             
             // Fallback to name-based analysis if file analysis fails
             if (!hasModifications(modId)) {
                 System.out.println("sosModHooks: File analysis found no modifications, using name-based analysis");
                 // Name-based analysis removed - using file system analysis instead
             }
             
         } catch (Exception e) {
             System.err.println("sosModHooks: Error analyzing file structure: " + e.getMessage());
             e.printStackTrace();
         }
     }
     
     /**
      * Check if a mod has any modifications declared.
      */
     private boolean hasModifications(String modId) {
         return (classReplacements.containsKey(modId) && !classReplacements.get(modId).isEmpty()) ||
                (assetModifications.containsKey(modId) && !assetModifications.get(modId).isEmpty()) ||
                (dataModifications.containsKey(modId) && !dataModifications.get(modId).isEmpty());
     }
     
     /**
      * Analyze mod files by examining the actual directory structure.
      * This is the core of real conflict detection.
      */
     private void analyzeModFilesByDirectory(String modId, String modName, String modDirectory) {
         System.out.println("sosModHooks: --- Analyzing actual files for: " + modName + " ---");
         
         try {
             // Try to find the actual mod directory path
             String fullModPath = findModDirectoryPath(modDirectory);
             if (fullModPath == null) {
                 System.out.println("sosModHooks: Could not find mod directory path for: " + modDirectory);
                 return;
             }
             
             System.out.println("sosModHooks: Found mod directory: " + fullModPath);
             
             // Analyze the mod's file structure
             analyzeModDirectoryContents(modId, modName, fullModPath);
             
         } catch (Exception e) {
             System.err.println("sosModHooks: Error in file analysis: " + e.getMessage());
             e.printStackTrace();
         }
     }
     
     /**
      * Find the actual mod directory path from the mod directory name.
      */
     private String findModDirectoryPath(String modDirectory) {
         try {
             // Try multiple possible locations
             String[] possiblePaths = {
                 System.getProperty("user.home") + "/AppData/Roaming/songsofsyx/mods/" + modDirectory,
                 System.getProperty("user.home") + "/.local/share/songsofsyx/mods/" + modDirectory,
                 "mods/" + modDirectory,
                 "../mods/" + modDirectory,
                 "../../mods/" + modDirectory
             };
             
             for (String path : possiblePaths) {
                 File dir = new File(path);
                 if (dir.exists() && dir.isDirectory()) {
                     System.out.println("sosModHooks: Found mod directory at: " + path);
                     return path;
                 }
             }
             
             // Try Steam Workshop paths
             String steamPath = findSteamWorkshopPath(modDirectory);
             if (steamPath != null) {
                 return steamPath;
             }
             
             return null;
         } catch (Exception e) {
             System.err.println("sosModHooks: Error finding mod directory path: " + e.getMessage());
             return null;
         }
     }
     
     /**
      * Find Steam Workshop mod path.
      */
     private String findSteamWorkshopPath(String modDirectory) {
         try {
             // Common Steam Workshop paths
             String[] steamPaths = {
                 "C:/Program Files (x86)/Steam/steamapps/workshop/content/",
                 "C:/Program Files/Steam/steamapps/workshop/content/",
                 System.getProperty("user.home") + "/Steam/steamapps/workshop/content/"
             };
             
             for (String steamPath : steamPaths) {
                 File steamDir = new File(steamPath);
                 if (steamDir.exists()) {
                     // Look for mod directories
                     File[] contentDirs = steamDir.listFiles();
                     if (contentDirs != null) {
                         for (File contentDir : contentDirs) {
                             if (contentDir.isDirectory()) {
                                 File modDir = new File(contentDir, modDirectory);
                                 if (modDir.exists() && modDir.isDirectory()) {
                                     System.out.println("sosModHooks: Found Steam Workshop mod at: " + modDir.getPath());
                                     return modDir.getPath();
                                 }
                             }
                         }
                     }
                 }
             }
             
             return null;
         } catch (Exception e) {
             System.err.println("sosModHooks: Error finding Steam Workshop path: " + e.getMessage());
             return null;
         }
     }
     
     /**
      * Analyze the contents of a mod directory to detect actual modifications.
      */
     private void analyzeModDirectoryContents(String modId, String modName, String modPath) {
         try {
             File modDir = new File(modPath);
             if (!modDir.exists() || !modDir.isDirectory()) {
                 System.out.println("sosModHooks: Mod directory does not exist: " + modPath);
                 return;
             }
             
             System.out.println("sosModHooks: Analyzing directory contents: " + modPath);
             
             // Analyze the directory structure
             analyzeModStructure(modId, modName, modDir);
             
         } catch (Exception e) {
             System.err.println("sosModHooks: Error analyzing directory contents: " + e.getMessage());
             e.printStackTrace();
         }
     }
     
     /**
      * Analyze the structure of a mod directory to detect modifications.
      */
     private void analyzeModStructure(String modId, String modName, File modDir) {
         try {
             // Look for V69 directory (game version)
             File v69Dir = new File(modDir, "V69");
             if (v69Dir.exists() && v69Dir.isDirectory()) {
                 analyzeVersionDirectory(modId, modName, v69Dir);
             } else {
                 // Try other version directories
                 File[] versionDirs = modDir.listFiles((dir, name) -> dir.isDirectory() && name.startsWith("V"));
                 if (versionDirs != null && versionDirs.length > 0) {
                     // Use the first version directory found
                     analyzeVersionDirectory(modId, modName, versionDirs[0]);
                 }
             }
             
         } catch (Exception e) {
             System.err.println("sosModHooks: Error analyzing mod structure: " + e.getMessage());
         }
     }
     
     /**
      * Analyze a version-specific directory (V69, V68, etc.).
      */
    // Old duplicate method removed
     
     /**
      * Analyze the assets directory to detect file modifications.
      */
    // Old duplicate method removed
     
     /**
      * Recursively scan assets directory to detect modifications.
      */
    // Old duplicate method removed
     

     
     /**
      * Analyze a configuration file to determine what data type it's modifying.
      */
     private void analyzeConfigFile(String modId, String modName, File file, String fullPath) {
         try {
             // Read the first few lines to determine the file type
             java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file));
             String firstLine = reader.readLine();
             reader.close();
             
             if (firstLine != null) {
                 String line = firstLine.toLowerCase();
                 
                 // Detect data type based on file content
                 if (line.contains("race") || line.contains("faction")) {
                     declareDataModification(modId, "RACE", "FACTION");
                     System.out.println("sosModHooks: Detected race/faction data modification: " + fullPath);
                 } else if (line.contains("event") || line.contains("story")) {
                     declareDataModification(modId, "EVENT", "STORY");
                     System.out.println("sosModHooks: Detected event/story data modification: " + fullPath);
                 } else if (line.contains("tech") || line.contains("research")) {
                     declareDataModification(modId, "TECH", "RESEARCH");
                     System.out.println("sosModHooks: Detected tech/research data modification: " + fullPath);
                 } else if (line.contains("resource") || line.contains("item")) {
                     declareDataModification(modId, "RESOURCE", "ITEM");
                     System.out.println("sosModHooks: Detected resource/item data modification: " + fullPath);
                 } else if (line.contains("room") || line.contains("building")) {
                     declareDataModification(modId, "ROOM", "BUILDING");
                     System.out.println("sosModHooks: Detected room/building data modification: " + fullPath);
                 } else {
                     // Generic data modification
                     declareDataModification(modId, "CONFIG");
                     System.out.println("sosModHooks: Detected generic config modification: " + fullPath);
                 }
                 
                 // Also mark as asset modification since it's in assets
                 declareAssetModification(modId, fullPath);
             }
             
         } catch (Exception e) {
             System.err.println("sosModHooks: Error analyzing config file: " + e.getMessage());
         }
     }
     

     
    // Old analysis methods removed - replaced with runtime monitoring
     
     /**
      * Fallback method to detect mods from directory scanning.
      */
         private void detectModsFromDirectoryScanning(Map<String, String> detectedModNames) {
        // This would scan the mods directory if other methods fail
        // For now, just a placeholder
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
                        return pathParts[i + 1];
                    }
                }
            }
            
            // Fallback: try to extract from the end of the path
            if (classpath.contains("mods")) {
                String afterMods = classpath.substring(classpath.indexOf("mods") + 4);
                String[] remainingParts = afterMods.split("[\\\\/]");
                if (remainingParts.length > 0) {
                    return remainingParts[0];
                }
            }
            
        } catch (Exception e) {
            // Silently fail - will retry later
        }
        
        return null;
    }
    
    /**
     * Phase 2: Analyze the runtime effects of active mods.
     * This analyzes what the active mods are actually doing to the game.
     */
    public void analyzeRuntimeEffects() {
        if (hasAnalyzedRuntimeEffects) {
            return;
        }
        
        try {
            // Analyze each active mod's runtime effects
            for (Map.Entry<String, ActiveModInfo> entry : activeMods.entrySet()) {
                String modId = entry.getKey();
                ActiveModInfo modInfo = entry.getValue();
                
                // Analyze the mod's actual file structure
                analyzeModRuntimeStructure(modId, modInfo);
                
                // Check for runtime conflicts with other mods
                detectRuntimeConflicts(modId, modInfo);
            }
            
        } catch (Exception e) {
            // Silently fail - will retry later
        }
        
        hasAnalyzedRuntimeEffects = true;
    }
    
    /**
     * Analyze the runtime structure of an active mod.
     */
    private void analyzeModRuntimeStructure(String modId, ActiveModInfo modInfo) {
        try {
            // Create a comprehensive mod analysis based on what we know
            ModAnalysis analysis = new ModAnalysis(modId, modInfo.modName, modInfo.modVersion);
            
            // Since we can't access file system (mods may be from Steam Workshop),
            // create intelligent analysis based on mod detection method and runtime analysis
            if (modInfo.modName != null && !modInfo.modName.equals("???")) {
                // This mod was detected by the game's PATHS system, so it's actively loaded
                // File system analysis already handled the modifications
            }
            
            // Store the comprehensive analysis
            modAnalyses.put(modId, analysis);
            
        } catch (Exception e) {
            // Silently fail - will retry later
        }
    }
    
    /**
     * Bridge method removed - real file analysis now handles conflict detection directly.
     * The file analysis system populates the conflict maps automatically by examining
     * actual mod files rather than making assumptions based on mod names.
     */
    
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
            // Silently fail - will retry later
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
            // Silently fail - will retry later
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
                            // Register the conflict
                            registerModConflict(modId1, modId2, dataType1, "DATA_MODIFICATION");
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            // Silently fail - will retry later
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
                            // Register the conflict
                            registerModConflict(modId1, modId2, asset1, "ASSET_MODIFICATION");
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            // Silently fail - will retry later
        }
    }
    
    // ========================================
    // UTILITY METHODS
    // ========================================
    
    /**
     * Register an active mod.
     */
    private void registerActiveMod(String modId, String modName, String version) {      
        if (!activeMods.containsKey(modId)) {
            ActiveModInfo modInfo = new ActiveModInfo(modId, modName, version);
            activeMods.put(modId, modInfo);
            
            // Also register in the main registry so declare* methods can work
            ModDeclaration declaration = new ModDeclaration(modId, modName, version);
            registeredMods.put(modId, declaration);
            
            System.out.println("sosModHooks: Successfully registered active mod: " + modName + " (" + modId + ") v" + version);
            System.out.println("sosModHooks: Successfully registered in main registry: " + modId);
            System.out.println("sosModHooks: Total active mods now: " + activeMods.size());
            System.out.println("sosModHooks: Total registered mods now: " + registeredMods.size());
        } else {
            System.out.println("sosModHooks: Mod " + modId + " already registered, skipping duplicate");
        }
    }
    
    /**
     * Register a mod conflict.
     */
    private void registerModConflict(String modId1, String modId2, String conflictType, String conflictCategory) {
        // This will be used by the conflict reporting system
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
                
                for (String script : scripts) {
                    // Create a basic mod info entry for each script
                    ModDeclaration modDecl = new ModDeclaration(script, script, "1.0.0", "Script detected from ScriptEngine", "Unknown");
                    registerMod(script, script, "1.0.0");
                }
            }
        } catch (Exception e) {
            // Silently fail - will retry later
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
        ModDeclaration declaration = new ModDeclaration(modId, modName, version);
        registeredMods.put(modId, declaration);
    }
    
    /**
     * Declare that a mod replaces specific core game classes.
     */
    public void declareClassReplacement(String modId, String... classNames) {
        if (!registeredMods.containsKey(modId)) {
            return;
        }
        
        ArrayListGrower<String> classes = new ArrayListGrower<>();
        for (String className : classNames) {
            classes.add(className);
        }
        
        classReplacements.put(modId, classes);
    }
    
    /**
     * Declare that a mod modifies specific asset files.
     */
    public void declareAssetModification(String modId, String... assetPaths) {
        if (!registeredMods.containsKey(modId)) {
            return;
        }
        
        ArrayListGrower<String> assets = new ArrayListGrower<>();
        for (String assetPath : assetPaths) {
            assets.add(assetPath);
        }
        
        assetModifications.put(modId, assets);
    }
    
    /**
     * Declare that a mod modifies specific data structures.
     */
    public void declareDataModification(String modId, String... dataTypes) {
        if (!registeredMods.containsKey(modId)) {
            return;
        }
        
        ArrayListGrower<String> dataTypesList = new ArrayListGrower<>();
        for (String dataType : dataTypes) {
            dataTypesList.add(dataType);
        }
        
        dataModifications.put(modId, dataTypesList);
    }
    
    /**
     * Get the total number of modifications for a specific mod.
     */
    public int getModificationCount(String modId) {
        if (!activeMods.containsKey(modId)) {
            return 0;
        }
        
        int total = 0;
        
        // Count class replacements
        if (classReplacements.containsKey(modId)) {
            total += classReplacements.get(modId).size();
        }
        
        // Count asset modifications
        if (assetModifications.containsKey(modId)) {
            total += assetModifications.get(modId).size();
        }
        
        // Count data modifications
        if (dataModifications.containsKey(modId)) {
            total += dataModifications.get(modId).size();
        }
        
        return total;
    }
    
    /**
     * Declare dependencies on other mods.
     */
    public void declareDependency(String modId, String... dependencyIds) {
        if (!registeredMods.containsKey(modId)) {
            System.err.println("sosModHooks: Cannot declare dependency for unregistered mod: " + modId);
            return;
        }
        
        ArrayListGrower<String> deps = new ArrayListGrower<>();
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
        ArrayListGrower<ModConflict> conflicts = new ArrayListGrower<>();
        
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
    
    private void detectClassReplacementConflicts(ArrayListGrower<ModConflict> conflicts) {
        Map<String, ArrayListGrower<String>> classToMods = new HashMap<>();
        
        // Build reverse mapping: class -> list of mods that replace it
        for (Map.Entry<String, LIST<String>> entry : classReplacements.entrySet()) {
            String modId = entry.getKey();
            LIST<String> classes = entry.getValue();
            
            for (String className : classes) {
                classToMods.computeIfAbsent(className, k -> new ArrayListGrower<String>()).add(modId);
            }
        }
        
        // Check for conflicts
        for (Map.Entry<String, ArrayListGrower<String>> entry : classToMods.entrySet()) {
            String className = entry.getKey();
            ArrayListGrower<String> mods = entry.getValue();
            
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
    
    private void detectAssetModificationConflicts(ArrayListGrower<ModConflict> conflicts) {
        Map<String, ArrayListGrower<String>> assetToMods = new HashMap<>();
        
        for (Map.Entry<String, LIST<String>> entry : assetModifications.entrySet()) {
            String modId = entry.getKey();
            LIST<String> assets = entry.getValue();
            
            for (String assetPath : assets) {
                assetToMods.computeIfAbsent(assetPath, k -> new ArrayListGrower<String>()).add(modId);
            }
        }
        
        for (Map.Entry<String, ArrayListGrower<String>> entry : assetToMods.entrySet()) {
            String assetPath = entry.getKey();
            ArrayListGrower<String> mods = entry.getValue();
            
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
    
    private void detectDataModificationConflicts(ArrayListGrower<ModConflict> conflicts) {
        Map<String, ArrayListGrower<String>> dataTypeToMods = new HashMap<>();
        
        for (Map.Entry<String, LIST<String>> entry : dataModifications.entrySet()) {
            String modId = entry.getKey();
            LIST<String> dataTypes = entry.getValue();
            
            for (String dataType : dataTypes) {
                dataTypeToMods.computeIfAbsent(dataType, k -> new ArrayListGrower<String>()).add(modId);
            }
        }
        
        for (Map.Entry<String, ArrayListGrower<String>> entry : dataTypeToMods.entrySet()) {
            String dataType = entry.getKey();
            ArrayListGrower<String> mods = entry.getValue();
            
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
    
    private void detectMissingDependencies(ArrayListGrower<ModConflict> conflicts) {
        for (Map.Entry<String, LIST<String>> entry : dependencies.entrySet()) {
            String modId = entry.getKey();
            LIST<String> requiredDeps = entry.getValue();
            
            for (String depId : requiredDeps) {
                if (!registeredMods.containsKey(depId)) {
                    ArrayListGrower<String> modList = new ArrayListGrower<>();
                    modList.add(modId);
                    conflicts.add(new ModConflict(
                        depId,
                        modList,
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
        // Prevent infinite recursion - if we're already populating this mod, return what we have
        if (modAnalyses.containsKey(modId)) {
            return modAnalyses.get(modId);
        }
        
        // Create a new analysis if it doesn't exist
        ActiveModInfo modInfo = activeMods.get(modId);
        if (modInfo != null) {
            ModAnalysis analysis = new ModAnalysis(modId, modInfo.modName, modInfo.modVersion);
            modAnalyses.put(modId, analysis);
            
            // Populate the analysis with data from the main registry maps
            populateModAnalysisFromRegistry(modId, analysis);
            
            return analysis;
        }
        
        return null;
    }
    
    /**
     * Check if runtime detection has been completed
     */
    public boolean isRuntimeDetectionComplete() {
        return hasDetectedActiveMods && hasAnalyzedRuntimeEffects;
    }
    
    /**
     * Populate a ModAnalysis object with data from the main registry maps.
     */
    private void populateModAnalysisFromRegistry(String modId, ModAnalysis analysis) {
        try {
            int totalAdded = 0;
            
            // Populate data modifications (limit to prevent memory issues)
            if (dataModifications.containsKey(modId)) {
                LIST<String> dataTypes = dataModifications.get(modId);
                int count = Math.min(dataTypes.size(), 100); // Limit to 100 items
                for (int i = 0; i < count; i++) {
                    String dataType = dataTypes.get(i);
                    DataModification dataMod = new DataModification(dataType, "ADDED", 1, false);
                    analysis.addDataModification(dataType, dataMod);
                    totalAdded++;
                }
            }
            
            // Populate asset modifications (limit to prevent memory issues)
            if (assetModifications.containsKey(modId)) {
                LIST<String> assetPaths = assetModifications.get(modId);
                int count = Math.min(assetPaths.size(), 100); // Limit to 100 items
                for (int i = 0; i < count; i++) {
                    String assetPath = assetPaths.get(i);
                    AssetModification assetMod = new AssetModification("ASSET", assetPath, "ADDED", false);
                    analysis.addAssetModification(assetPath, assetMod);
                    totalAdded++;
                }
            }
            
            // Populate class replacements (limit to prevent memory issues)
            if (classReplacements.containsKey(modId)) {
                LIST<String> classes = classReplacements.get(modId);
                int count = Math.min(classes.size(), 100); // Limit to 100 items
                for (int i = 0; i < count; i++) {
                    String className = classes.get(i);
                    ScriptModification scriptMod = new ScriptModification(className, "REPLACED", "class", true);
                    analysis.addScriptModification(className, scriptMod);
                    totalAdded++;
                }
            }
            
            // Simple debug output that won't spam
            if (totalAdded > 0) {
                System.out.println("sosModHooks: Added " + totalAdded + " modifications to " + modId);
            }
            
        } catch (Exception e) {
            // Silent fail - don't crash the game
        }
    }
    
    /**
     * Helper method to add data modifications to the registry.
     * This handles the LIST type properly by creating a new ArrayList if needed.
     */
    private void addDataModification(String modId, String dataType) {
        if (!this.dataModifications.containsKey(modId)) {
            this.dataModifications.put(modId, new ArrayListGrower<>());
        }
        // Get the existing list and add to it
        LIST<String> existingList = this.dataModifications.get(modId);
        if (existingList instanceof ArrayList) {
            ((ArrayList<String>) existingList).add(dataType);
        } else {
            // If it's not an ArrayList, create a new one with the existing data
            ArrayListGrower<String> newList = new ArrayListGrower<>();
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
        private final ArrayListGrower<String> conflicts;
        
        public ModAnalysis(String modId, String modName, String modVersion) {
            this.modId = modId;
            this.modName = modName;
            this.modVersion = modVersion;
            this.fileModifications = new HashMap<>();
            this.assetModifications = new HashMap<>();
            this.dataModifications = new HashMap<>();
            this.scriptModifications = new HashMap<>();
            this.runtimeModifications = new HashMap<>();
            this.conflicts = new ArrayListGrower<String>();
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
        public ArrayListGrower<String> getConflicts() { return conflicts; }
        
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
        
        /**
         * Get total modifications from the main registry maps.
         */
        public int getTotalModificationsFromRegistry(String modId) {
            int total = 0;
            
            // Check class replacements
            if (ModRegistry.getInstance().classReplacements.containsKey(modId)) {
                total += ModRegistry.getInstance().classReplacements.get(modId).size();
            }
            
            // Check asset modifications
            if (ModRegistry.getInstance().assetModifications.containsKey(modId)) {
                total += ModRegistry.getInstance().assetModifications.get(modId).size();
            }
            
            // Check data modifications
            if (ModRegistry.getInstance().dataModifications.containsKey(modId)) {
                total += ModRegistry.getInstance().dataModifications.get(modId).size();
            }
            
            return total;
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

