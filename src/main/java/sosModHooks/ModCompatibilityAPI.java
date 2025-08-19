package sosModHooks;

/**
 * Public API for the Mod Compatibility Framework.
 * This is the main interface that other mods will use to register themselves
 * and declare their modifications.
 * 
 * Usage Example:
 * <pre>
 * // In your mod's initialization code:
 * ModCompatibilityAPI api = ModCompatibilityAPI.getInstance();
 * 
 * // Register your mod
 * api.registerMod("myMod", "My Awesome Mod", "1.0.0");
 * 
 * // Declare what you're modifying
 * api.declareClassReplacement("myMod", "settlement.room.food.farm.FarmInstance");
 * api.declareAssetModification("myMod", "/data/assets/sprite/race/face/addon");
 * api.declareDataModification("myMod", "FACTION", "HUMAN");
 * 
 * // Declare dependencies
 * api.declareDependency("myMod", "requiredMod");
 * </pre>
 */
public final class ModCompatibilityAPI {
    
    private static ModCompatibilityAPI instance;
    private final ModRegistry registry;
    
    private ModCompatibilityAPI() {
        this.registry = ModRegistry.getInstance();
    }
    
    public static ModCompatibilityAPI getInstance() {
        if (instance == null) {
            instance = new ModCompatibilityAPI();
        }
        return instance;
    }
    
    /**
     * Register a mod with the compatibility framework.
     * This should be called first before any other declarations.
     * 
     * @param modId Unique identifier for your mod (e.g., "warhammer_overhaul")
     * @param modName Human-readable name for your mod (e.g., "Warhammer Overhaul")
     * @param version Version string (e.g., "1.0.0")
     */
    public void registerMod(String modId, String modName, String version) {
        registry.registerMod(modId, modName, version);
    }
    
    /**
     * Register a mod with additional metadata.
     */
    public void registerMod(String modId, String modName, String version, String description, String author) {
        registry.registerMod(modId, modName, version);
        ModDeclaration declaration = registry.getRegisteredMods().get(modId);
        if (declaration != null) {
            declaration.setDescription(description);
            declaration.setAuthor(author);
        }
    }
    
    /**
     * Declare that your mod replaces specific core game classes.
     * Use this when your mod completely replaces a core game class.
     * 
     * @param modId Your mod's ID
     * @param classNames Full class names (e.g., "settlement.room.food.farm.FarmInstance")
     */
    public void declareClassReplacement(String modId, String... classNames) {
        registry.declareClassReplacement(modId, classNames);
    }
    
    /**
     * Declare that your mod modifies specific asset files.
     * Use this for sprite files, textures, sounds, etc.
     * 
     * @param modId Your mod's ID
     * @param assetPaths Asset file paths (e.g., "/data/assets/sprite/race/face/addon")
     */
    public void declareAssetModification(String modId, String... assetPaths) {
        registry.declareAssetModification(modId, assetPaths);
    }
    
    /**
     * Declare that your mod modifies specific data structures.
     * Use this for game data like factions, races, events, etc.
     * 
     * @param modId Your mod's ID
     * @param dataTypes Data type identifiers (e.g., "FACTION", "RACE", "EVENT")
     */
    public void declareDataModification(String modId, String... dataTypes) {
        registry.declareDataModification(modId, dataTypes);
    }
    
    /**
     * Declare dependencies on other mods.
     * Use this when your mod requires other mods to function.
     * 
     * @param modId Your mod's ID
     * @param requiredModIds IDs of required mods
     */
    public void declareDependency(String modId, String... requiredModIds) {
        registry.declareDependency(modId, requiredModIds);
    }
    
    /**
     * Check if there are any conflicts with your mod.
     * This is useful for debugging and validation.
     * 
     * @return List of conflicts involving your mod
     */
    public snake2d.util.sets.LIST<ModConflict> checkModConflicts(String modId) {
        snake2d.util.sets.ArrayList<ModConflict> modConflicts = new snake2d.util.sets.ArrayList<>();
        snake2d.util.sets.LIST<ModConflict> allConflicts = registry.detectConflicts();
        
        for (ModConflict conflict : allConflicts) {
            if (conflict.getConflictingMods().contains(modId)) {
                modConflicts.add(conflict);
            }
        }
        
        return modConflicts;
    }
    
    /**
     * Get a summary of all registered mods and their changes.
     * Useful for debugging and understanding the mod landscape.
     * 
     * @return Human-readable summary of all mods
     */
    public String getModSummary() {
        return registry.getModSummary();
    }
    
    /**
     * Get the total number of conflicts detected.
     * 
     * @return Number of conflicts
     */
    public int getConflictCount() {
        return registry.detectConflicts().size();
    }
    
    /**
     * Check if there are any conflicts at all.
     * 
     * @return true if conflicts exist, false otherwise
     */
    public boolean hasConflicts() {
        return !registry.detectConflicts().isEmpty();
    }
    
    /**
     * Get all detected conflicts.
     * 
     * @return List of all conflicts
     */
    public snake2d.util.sets.LIST<ModConflict> getAllConflicts() {
        return registry.detectConflicts();
    }
}
