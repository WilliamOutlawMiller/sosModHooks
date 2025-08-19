package sosModHooks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

/**
 * Unit tests for ModRegistry conflict detection functionality
 */
public class ModRegistryTest {
    
    private ModRegistry registry;
    
    @BeforeEach
    void setUp() {
        registry = ModRegistry.getInstance();
        registry.clearAll(); // Reset for each test
    }
    
    @Test
    @DisplayName("Should detect class replacement conflicts")
    void testClassReplacementConflicts() {
        // Register two mods that replace the same class
        registry.registerMod("warhammer", "Warhammer Mod", "1.0");
        registry.registerMod("fantasy", "Fantasy Mod", "2.0");
        
        registry.declareClassReplacement("warhammer", "settlement.room.main.ROOM");
        registry.declareClassReplacement("fantasy", "settlement.room.main.ROOM");
        
        LIST<ModConflict> conflicts = registry.detectConflicts();
        
        assertFalse(conflicts.isEmpty(), "Should detect class replacement conflicts");
        
        boolean foundConflict = false;
        for (ModConflict conflict : conflicts) {
            if (conflict.getType() == ConflictType.CLASS_REPLACEMENT && 
                conflict.getConflictTarget().equals("settlement.room.main.ROOM")) {
                foundConflict = true;
                assertEquals(2, conflict.getConflictingMods().size(), "Should have 2 conflicting mods");
                assertTrue(conflict.getConflictingMods().contains("warhammer"), "Should include warhammer mod");
                assertTrue(conflict.getConflictingMods().contains("fantasy"), "Should include fantasy mod");
                break;
            }
        }
        
        assertTrue(foundConflict, "Should find class replacement conflict");
    }
    
    @Test
    @DisplayName("Should detect asset modification conflicts")
    void testAssetModificationConflicts() {
        // Register two mods that modify the same asset
        registry.registerMod("texture_pack_1", "Texture Pack 1", "1.0");
        registry.registerMod("texture_pack_2", "Texture Pack 2", "1.0");
        
        registry.declareAssetModification("texture_pack_1", "/data/assets/sprite/race/face/addon");
        registry.declareAssetModification("texture_pack_2", "/data/assets/sprite/race/face/addon");
        
        LIST<ModConflict> conflicts = registry.detectConflicts();
        
        assertFalse(conflicts.isEmpty(), "Should detect asset modification conflicts");
        
        boolean foundConflict = false;
        for (ModConflict conflict : conflicts) {
            if (conflict.getType() == ConflictType.ASSET_CONFLICT && 
                conflict.getConflictTarget().equals("/data/assets/sprite/race/face/addon")) {
                foundConflict = true;
                assertEquals(2, conflict.getConflictingMods().size(), "Should have 2 conflicting mods");
                break;
            }
        }
        
        assertTrue(foundConflict, "Should find asset modification conflict");
    }
    
    @Test
    @DisplayName("Should detect data modification conflicts")
    void testDataModificationConflicts() {
        // Register two mods that modify the same data type
        registry.registerMod("race_expansion", "Race Expansion", "1.0");
        registry.registerMod("faction_overhaul", "Faction Overhaul", "1.0");
        
        registry.declareDataModification("race_expansion", "RACE", "FACTION");
        registry.declareDataModification("faction_overhaul", "FACTION", "EVENT");
        
        LIST<ModConflict> conflicts = registry.detectConflicts();
        
        assertFalse(conflicts.isEmpty(), "Should detect data modification conflicts");
        
        boolean foundFactionConflict = false;
        for (ModConflict conflict : conflicts) {
            if (conflict.getType() == ConflictType.DATA_CONFLICT && 
                conflict.getConflictTarget().equals("FACTION")) {
                foundFactionConflict = true;
                assertEquals(2, conflict.getConflictingMods().size(), "Should have 2 conflicting mods");
                break;
            }
        }
        
        assertTrue(foundFactionConflict, "Should find FACTION data conflict");
    }
    
    @Test
    @DisplayName("Should detect missing dependencies")
    void testMissingDependencies() {
        // Register a mod that depends on another mod that isn't loaded
        registry.registerMod("dependent_mod", "Dependent Mod", "1.0");
        registry.declareDependency("dependent_mod", "required_mod");
        
        LIST<ModConflict> conflicts = registry.detectConflicts();
        
        assertFalse(conflicts.isEmpty(), "Should detect missing dependency conflicts");
        
        boolean foundDependencyConflict = false;
        for (ModConflict conflict : conflicts) {
            if (conflict.getType() == ConflictType.MISSING_DEPENDENCY && 
                conflict.getConflictTarget().equals("required_mod")) {
                foundDependencyConflict = true;
                assertEquals(1, conflict.getConflictingMods().size(), "Should have 1 dependent mod");
                assertTrue(conflict.getConflictingMods().contains("dependent_mod"), "Should include dependent mod");
                break;
            }
        }
        
        assertTrue(foundDependencyConflict, "Should find missing dependency conflict");
    }
    
    @Test
    @DisplayName("Should not detect conflicts when mods are compatible")
    void testNoConflictsWhenCompatible() {
        // Register two mods that modify different things
        registry.registerMod("ui_mod", "UI Mod", "1.0");
        registry.registerMod("sound_mod", "Sound Mod", "1.0");
        
        registry.declareClassReplacement("ui_mod", "view.ui.UI");
        registry.declareAssetModification("sound_mod", "/data/assets/sound/music");
        
        LIST<ModConflict> conflicts = registry.detectConflicts();
        
        assertTrue(conflicts.isEmpty(), "Should not detect conflicts when mods are compatible");
    }
    
    @Test
    @DisplayName("Should provide meaningful conflict descriptions")
    void testConflictDescriptions() {
        registry.registerMod("mod1", "Mod 1", "1.0");
        registry.registerMod("mod2", "Mod 2", "1.0");
        
        registry.declareClassReplacement("mod1", "game.GAME");
        registry.declareClassReplacement("mod2", "game.GAME");
        
        LIST<ModConflict> conflicts = registry.detectConflicts();
        
        assertFalse(conflicts.isEmpty(), "Should detect conflicts");
        
        ModConflict conflict = conflicts.get(0);
        assertNotNull(conflict.getDescription(), "Conflict should have description");
        assertTrue(conflict.getDescription().contains("game.GAME"), "Description should mention conflicting target");
        assertNotNull(conflict.getResolutionSuggestions(), "Conflict should have resolution suggestions");
    }
    
    @Test
    @DisplayName("Should handle multiple conflict types simultaneously")
    void testMultipleConflictTypes() {
        // Create a complex scenario with multiple conflict types
        registry.registerMod("overhaul_mod", "Overhaul Mod", "1.0");
        registry.registerMod("compatibility_mod", "Compatibility Mod", "1.0");
        
        // Class replacement conflict
        registry.declareClassReplacement("overhaul_mod", "settlement.main.SETT");
        registry.declareClassReplacement("compatibility_mod", "settlement.main.SETT");
        
        // Asset modification conflict
        registry.declareAssetModification("overhaul_mod", "/data/assets/sprite/settlement");
        registry.declareAssetModification("compatibility_mod", "/data/assets/sprite/settlement");
        
        // Data modification conflict
        registry.declareDataModification("overhaul_mod", "SETTLEMENT");
        registry.declareDataModification("compatibility_mod", "SETTLEMENT");
        
        LIST<ModConflict> conflicts = registry.detectConflicts();
        
        assertTrue(conflicts.size() >= 3, "Should detect at least 3 different types of conflicts");
        
        // Verify we have all three conflict types
        boolean hasClassConflict = false;
        boolean hasAssetConflict = false;
        boolean hasDataConflict = false;
        
        for (ModConflict conflict : conflicts) {
            switch (conflict.getType()) {
                case CLASS_REPLACEMENT:
                    hasClassConflict = true;
                    break;
                case ASSET_CONFLICT:
                    hasAssetConflict = true;
                    break;
                case DATA_CONFLICT:
                    hasDataConflict = true;
                    break;
            }
        }
        
        assertTrue(hasClassConflict, "Should have class replacement conflict");
        assertTrue(hasAssetConflict, "Should have asset modification conflict");
        assertTrue(hasDataConflict, "Should have data modification conflict");
    }
    
    @Test
    @DisplayName("Should provide accurate conflict count")
    void testConflictCount() {
        registry.registerMod("mod1", "Mod 1", "1.0");
        registry.registerMod("mod2", "Mod 2", "1.0");
        registry.registerMod("mod3", "Mod 3", "1.0");
        
        // Create 3 conflicts
        registry.declareClassReplacement("mod1", "class.A");
        registry.declareClassReplacement("mod2", "class.A");
        
        registry.declareAssetModification("mod2", "asset.B");
        registry.declareAssetModification("mod3", "asset.B");
        
        registry.declareDataModification("mod1", "DATA.C");
        registry.declareDataModification("mod3", "DATA.C");
        
        LIST<ModConflict> conflicts = registry.detectConflicts();
        
        assertEquals(3, conflicts.size(), "Should detect exactly 3 conflicts");
    }
}
