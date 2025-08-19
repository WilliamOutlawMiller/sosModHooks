package sosModHooks;

import lombok.Getter;
import lombok.Setter;
import script.SCRIPT;
import snake2d.util.file.Json;
import snake2d.util.file.JsonE;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Scans for compatibility conflicts between loaded mods.
 * Analyzes class modifications and detects potential issues.
 */
public class ModCompatibilityScanner {
    
    @Getter
    private CompatibilityReport compatibilityReport;
    
    public ModCompatibilityScanner() {
        this.compatibilityReport = new CompatibilityReport();
    }
    
    /**
     * Scan for conflicts by analyzing loaded mods and their class modifications
     */
    public void scanForConflicts() {
        try {
            // Get all loaded scripts (mods)
            LIST<SCRIPT> loadedScripts = getLoadedScripts();
            
            // Analyze each script for potential conflicts
            for (SCRIPT script : loadedScripts) {
                analyzeScriptForConflicts(script);
            }
            
            // Detect class replacement conflicts
            detectClassReplacementConflicts();
            
            // Generate compatibility report
            generateCompatibilityReport();
            
        } catch (Exception e) {
            // Log error but don't crash
            System.err.println("Error scanning for conflicts: " + e.getMessage());
        }
    }
    
    /**
     * Get all currently loaded scripts/mods
     */
    private LIST<SCRIPT> getLoadedScripts() {
        try {
            // Use reflection to access the game's script loading system
            Class<?> scriptLoadClass = Class.forName("script.ScriptLoad");
            java.lang.reflect.Method getAllMethod = scriptLoadClass.getDeclaredMethod("getAll");
            getAllMethod.setAccessible(true);
            
            Object scriptLoads = getAllMethod.invoke(null);
            if (scriptLoads instanceof LIST) {
                LIST<?> scriptLoadList = (LIST<?>) scriptLoads;
                snake2d.util.sets.ArrayList<SCRIPT> scripts = new snake2d.util.sets.ArrayList<>();
                
                for (Object scriptLoad : scriptLoadList) {
                    try {
                        // Extract the SCRIPT instance from ScriptLoad object
                        java.lang.reflect.Field scriptField = scriptLoad.getClass().getDeclaredField("script");
                        scriptField.setAccessible(true);
                        Object script = scriptField.get(scriptLoad);
                        
                        if (script instanceof SCRIPT) {
                            scripts.add((SCRIPT) script);
                        }
                    } catch (Exception e) {
                        // Skip this script if we can't extract it
                        System.err.println("Could not extract script from ScriptLoad: " + e.getMessage());
                    }
                }
                
                return scripts;
            }
        } catch (Exception e) {
            System.err.println("Error accessing loaded scripts: " + e.getMessage());
        }
        
        // Fallback to empty list if reflection fails
        return new snake2d.util.sets.ArrayList<>();
    }
    
    /**
     * Analyze a single script for potential conflicts
     */
    private void analyzeScriptForConflicts(SCRIPT script) {
        String modName = script.name().toString();
        
        // Use reflection to analyze the actual script class
        analyzeScriptClass(script, modName);
        
        // Check if this mod replaces any core classes
        if (isClassReplacementMod(script)) {
            compatibilityReport.addClassReplacement(modName, getReplacedClasses(script));
        }
        
        // Check for other potential conflict indicators
        checkForOtherConflicts(script);
    }
    
    /**
     * Analyze script class using reflection to detect actual conflicts
     */
    private void analyzeScriptClass(SCRIPT script, String modName) {
        try {
            Class<?> scriptClass = script.getClass();
            
            // Check for method signature conflicts with core game classes
            detectMethodConflicts(scriptClass, modName);
            
            // Check for field conflicts
            detectFieldConflicts(scriptClass, modName);
            
            // Check for package conflicts (mods that might replace core packages)
            detectPackageConflicts(scriptClass, modName);
            
        } catch (Exception e) {
            System.err.println("Error analyzing script class " + script.getClass().getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Detect method signature conflicts with core game classes
     */
    private void detectMethodConflicts(Class<?> scriptClass, String modName) {
        try {
            Method[] methods = scriptClass.getDeclaredMethods();
            
            for (Method method : methods) {
                // Check if this method signature conflicts with core game classes
                String methodSignature = method.getName() + "(" + getMethodSignature(method) + ")";
                
                // Check against common core classes that mods often replace
                String[] coreClasses = {
                    "settlement.room.food.farm.FarmInstance",
                    "init.tech.TECH",
                    "menu.ScMain",
                    "world.region.RD",
                    "settlement.entity.humanoid.Humanoid"
                };
                
                for (String coreClass : coreClasses) {
                    try {
                        Class<?> coreClassObj = Class.forName(coreClass);
                        Method[] coreMethods = coreClassObj.getDeclaredMethods();
                        
                        for (Method coreMethod : coreMethods) {
                            if (coreMethod.getName().equals(method.getName()) && 
                                hasCompatibleSignature(method, coreMethod)) {
                                
                                // Potential method conflict detected
                                compatibilityReport.addConflict(new ClassConflict(
                                    coreClass + "." + methodSignature,
                                    new snake2d.util.sets.ArrayList<>(modName),
                                    ConflictType.METHOD_CONFLICT,
                                    "Mod " + modName + " has method with same signature as core class " + coreClass
                                ));
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        // Core class doesn't exist, skip
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error detecting method conflicts: " + e.getMessage());
        }
    }
    
    /**
     * Get method signature string
     */
    private String getMethodSignature(Method method) {
        Class<?>[] params = method.getParameterTypes();
        StringBuilder signature = new StringBuilder();
        
        for (int i = 0; i < params.length; i++) {
            if (i > 0) signature.append(",");
            signature.append(params[i].getSimpleName());
        }
        
        return signature.toString();
    }
    
    /**
     * Check if two methods have compatible signatures
     */
    private boolean hasCompatibleSignature(Method method1, Method method2) {
        if (!method1.getName().equals(method2.getName())) {
            return false;
        }
        
        Class<?>[] params1 = method1.getParameterTypes();
        Class<?>[] params2 = method2.getParameterTypes();
        
        if (params1.length != params2.length) {
            return false;
        }
        
        for (int i = 0; i < params1.length; i++) {
            if (!params1[i].equals(params2[i])) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Detect field conflicts
     */
    private void detectFieldConflicts(Class<?> scriptClass, String modName) {
        try {
            Field[] fields = scriptClass.getDeclaredFields();
            
            for (Field field : fields) {
                // Check if this field name conflicts with core game classes
                String fieldName = field.getName();
                
                // Check against common core classes
                String[] coreClasses = {
                    "settlement.room.food.farm.FarmInstance",
                    "init.tech.TECH",
                    "menu.ScMain"
                };
                
                for (String coreClass : coreClasses) {
                    try {
                        Class<?> coreClassObj = Class.forName(coreClass);
                        Field[] coreFields = coreClassObj.getDeclaredFields();
                        
                        for (Field coreField : coreFields) {
                            if (coreField.getName().equals(fieldName)) {
                                // Potential field conflict detected
                                compatibilityReport.addConflict(new ClassConflict(
                                    coreClass + "." + fieldName,
                                    new snake2d.util.sets.ArrayList<>(modName),
                                    ConflictType.FIELD_CONFLICT,
                                    "Mod " + modName + " has field with same name as core class " + coreClass
                                ));
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        // Core class doesn't exist, skip
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error detecting field conflicts: " + e.getMessage());
        }
    }
    
    /**
     * Detect package conflicts
     */
    private void detectPackageConflicts(Class<?> scriptClass, String modName) {
        String packageName = scriptClass.getPackage().getName();
        
        // Check if this mod is trying to replace core game packages
        if (packageName.startsWith("settlement.") || 
            packageName.startsWith("init.") || 
            packageName.startsWith("world.") ||
            packageName.startsWith("menu.")) {
            
            compatibilityReport.addConflict(new ClassConflict(
                packageName + ".*",
                new snake2d.util.sets.ArrayList<>(modName),
                ConflictType.PACKAGE_CONFLICT,
                "Mod " + modName + " uses package that might conflict with core game: " + packageName
            ));
        }
    }
    
    /**
     * Detect if a mod replaces core game classes
     */
    private boolean isClassReplacementMod(SCRIPT script) {
        // This is a heuristic - we check if the mod's class name
        // suggests it might be replacing core functionality
        String className = script.getClass().getName();
        return className.contains("farm") || 
               className.contains("tech") || 
               className.contains("ui") ||
               className.contains("world");
    }
    
    /**
     * Get list of classes this mod might be replacing
     */
    private snake2d.util.sets.ArrayList<String> getReplacedClasses(SCRIPT script) {
        snake2d.util.sets.ArrayList<String> replacedClasses = new snake2d.util.sets.ArrayList<>();
        
        // This is a simplified detection - in practice we'd need to
        // analyze the actual class files in the mod's JAR
        String modName = script.name().toString().toLowerCase();
        
        if (modName.contains("farm") || modName.contains("agriculture")) {
            replacedClasses.add("settlement.room.food.farm.FarmInstance");
            replacedClasses.add("settlement.room.food.farm.ROOM_FARM");
            replacedClasses.add("settlement.room.food.farm.Gui");
        }
        
        if (modName.contains("tech") || modName.contains("technology")) {
            replacedClasses.add("init.tech.TECH");
            replacedClasses.add("init.tech.Knowledge_Costs");
        }
        
        if (modName.contains("warhammer") || modName.contains("overhaul")) {
            // Warhammer mod replaces many core systems
            replacedClasses.add("world.region.RD");
            replacedClasses.add("settlement.entity.humanoid.Humanoid");
            replacedClasses.add("menu.ScMain");
        }
        
        return replacedClasses;
    }
    
    /**
     * Check for other types of conflicts
     */
    private void checkForOtherConflicts(SCRIPT script) {
        // Check for resource conflicts, event conflicts, etc.
        // This would be expanded based on the specific mod
    }
    
    /**
     * Detect conflicts from class replacement
     */
    private void detectClassReplacementConflicts() {
        Map<String, LIST<String>> classReplacements = compatibilityReport.getClassReplacements();
        
        for (Map.Entry<String, LIST<String>> entry : classReplacements.entrySet()) {
            String modName = entry.getKey();
            LIST<String> replacedClasses = entry.getValue();
            
            for (String className : replacedClasses) {
                // Check if multiple mods replace the same class
                LIST<String> modsReplacingClass = getModsReplacingClass(className);
                
                if (modsReplacingClass.size() > 1) {
                    compatibilityReport.addConflict(new ClassConflict(
                        className,
                        modsReplacingClass,
                        ConflictType.CLASS_REPLACEMENT,
                        "Multiple mods replace the same class: " + className
                    ));
                }
            }
        }
    }
    
    /**
     * Get all mods that replace a specific class
     */
    private snake2d.util.sets.ArrayList<String> getModsReplacingClass(String className) {
        snake2d.util.sets.ArrayList<String> mods = new snake2d.util.sets.ArrayList<>();
        Map<String, LIST<String>> classReplacements = compatibilityReport.getClassReplacements();
        
        for (Map.Entry<String, LIST<String>> entry : classReplacements.entrySet()) {
            if (entry.getValue().contains(className)) {
                mods.add(entry.getKey());
            }
        }
        
        return mods;
    }
    
    /**
     * Generate the final compatibility report
     */
    private void generateCompatibilityReport() {
        // Calculate overall compatibility status
        if (compatibilityReport.getConflicts().isEmpty()) {
            compatibilityReport.setStatus(CompatibilityStatus.COMPATIBLE);
        } else {
            compatibilityReport.setStatus(CompatibilityStatus.CONFLICTS_DETECTED);
        }
    }
    
    /**
     * Check if there are any compatibility conflicts
     */
    public boolean hasConflicts() {
        return !compatibilityReport.getConflicts().isEmpty();
    }
    
    /**
     * Get a human-readable compatibility status
     */
    public String getCompatibilityStatus() {
        if (hasConflicts()) {
            return "Conflicts detected - " + compatibilityReport.getConflicts().size() + " issues found";
        } else {
            return "All mods appear compatible";
        }
    }
    
    /**
     * Load compatibility report from save file
     */
    public void loadCompatibilityReport(String jsonData) {
        try {
            // Validate the JSON data
            if (jsonData == null || jsonData.isEmpty() || jsonData.equals("{}")) {
                System.out.println("sosModHooks: No saved compatibility data found, using defaults");
                this.compatibilityReport = new CompatibilityReport();
                return;
            }
            
            // Basic JSON validation - check if it starts and ends correctly
            if (!jsonData.trim().startsWith("{") || !jsonData.trim().endsWith("}")) {
                System.out.println("sosModHooks: Invalid JSON format in saved data, using defaults");
                this.compatibilityReport = new CompatibilityReport();
                return;
            }
            
            // For now, just create a new report since the full JSON parsing would be complex
            // In a production version, you would parse the JSON and restore the state
            System.out.println("sosModHooks: Saved compatibility data found, but parsing not yet implemented");
            this.compatibilityReport = new CompatibilityReport();
            
        } catch (Exception e) {
            System.err.println("sosModHooks: Error loading compatibility report: " + e.getMessage());
            // If loading fails, create a new report
            this.compatibilityReport = new CompatibilityReport();
        }
    }
}
