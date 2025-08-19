package sosModHooks;

import lombok.Getter;
import lombok.Setter;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

import java.util.HashMap;
import java.util.Map;

/**
 * Central registry system for mods to declare their changes and conflicts.
 * This is the core of the modding framework that other mods will use.
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
    
    private ModRegistry() {
        this.registeredMods = new HashMap<>();
        this.classReplacements = new HashMap<>();
        this.assetModifications = new HashMap<>();
        this.dataModifications = new HashMap<>();
        this.dependencies = new HashMap<>();
        
        System.out.println("sosModHooks: ModRegistry initialized");
    }
    
    public static ModRegistry getInstance() {
        if (instance == null) {
            instance = new ModRegistry();
        }
        return instance;
    }
    
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
        for (String dataType : dataTypesList) {
            System.out.println("  - " + dataType);
        }
    }
    
    /**
     * Declare dependencies on other mods.
     */
    public void declareDependency(String modId, String... requiredModIds) {
        if (!registeredMods.containsKey(modId)) {
            System.err.println("sosModHooks: Cannot declare dependency for unregistered mod: " + modId);
            return;
        }
        
        ArrayList<String> deps = new ArrayList<>();
        for (String depId : requiredModIds) {
            deps.add(depId);
        }
        
        dependencies.put(modId, deps);
        
        System.out.println("sosModHooks: Mod " + modId + " declares dependencies on " + deps.size() + " mods");
        for (String depId : deps) {
            System.out.println("  - " + depId);
        }
    }
    
    /**
     * Check for conflicts between registered mods.
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
            LIST<String> requiredMods = entry.getValue();
            
            for (String requiredModId : requiredMods) {
                if (!registeredMods.containsKey(requiredModId)) {
                    conflicts.add(new ModConflict(
                        modId,
                        new ArrayList<>(requiredModId),
                        ConflictType.MISSING_DEPENDENCY,
                        "Mod " + modId + " requires " + requiredModId + " but it's not loaded"
                    ));
                }
            }
        }
    }
    
    /**
     * Clear all registered mods and their declarations.
     * This is useful for testing and resetting the registry.
     */
    public void clearAll() {
        registeredMods.clear();
        classReplacements.clear();
        assetModifications.clear();
        dataModifications.clear();
        dependencies.clear();
        System.out.println("sosModHooks: ModRegistry cleared all data");
    }
    
    /**
     * Get a summary of all registered mods and their changes.
     */
    public String getModSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Mod Registry Summary ===\n");
        sb.append("Total mods registered: ").append(registeredMods.size()).append("\n\n");
        
        for (ModDeclaration mod : registeredMods.values()) {
            sb.append("Mod: ").append(mod.getName()).append(" (v").append(mod.getVersion()).append(")\n");
            sb.append("ID: ").append(mod.getId()).append("\n");
            
            if (classReplacements.containsKey(mod.getId())) {
                sb.append("Class Replacements: ").append(classReplacements.get(mod.getId()).size()).append("\n");
            }
            
            if (assetModifications.containsKey(mod.getId())) {
                sb.append("Asset Modifications: ").append(assetModifications.get(mod.getId()).size()).append("\n");
            }
            
            if (dataModifications.containsKey(mod.getId())) {
                sb.append("Data Modifications: ").append(dataModifications.get(mod.getId()).size()).append("\n");
            }
            
            if (dependencies.containsKey(mod.getId())) {
                sb.append("Dependencies: ").append(dependencies.get(mod.getId()).size()).append("\n");
            }
            
            sb.append("\n");
        }
        
        return sb.toString();
    }
}
