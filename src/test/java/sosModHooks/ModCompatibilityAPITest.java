package sosModHooks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import snake2d.util.sets.LIST;

/**
 * Unit tests for ModCompatibilityAPI public interface
 */
public class ModCompatibilityAPITest {
    
    private ModCompatibilityAPI api;
    
    @BeforeEach
    void setUp() {
        // Clear the registry before each test
        ModRegistry.getInstance().clearAll();
        api = ModCompatibilityAPI.getInstance();
    }
    
    @Test
    @DisplayName("Should register mods through API")
    void testModRegistration() {
        api.registerMod("test_mod", "Test Mod", "1.0");
        
        assertTrue(api.hasConflicts() == false, "New mod should not have conflicts");
        assertEquals(0, api.getConflictCount(), "New mod should have 0 conflicts");
    }
    
    @Test
    @DisplayName("Should detect conflicts through API")
    void testConflictDetection() {
        // Register two conflicting mods
        api.registerMod("mod1", "Mod 1", "1.0");
        api.registerMod("mod2", "Mod 2", "1.0");
        
        api.declareClassReplacement("mod1", "game.GAME");
        api.declareClassReplacement("mod2", "game.GAME");
        
        assertTrue(api.hasConflicts(), "Should detect conflicts");
        assertTrue(api.getConflictCount() > 0, "Should have conflict count > 0");
        
        LIST<ModConflict> conflicts = api.getAllConflicts();
        assertFalse(conflicts.isEmpty(), "Should return conflicts list");
    }
    
    @Test
    @DisplayName("Should check specific mod conflicts")
    void testSpecificModConflicts() {
        api.registerMod("mod1", "Mod 1", "1.0");
        api.registerMod("mod2", "Mod 2", "1.0");
        
        api.declareClassReplacement("mod1", "game.GAME");
        api.declareClassReplacement("mod2", "game.GAME");
        
        LIST<ModConflict> mod1Conflicts = api.checkModConflicts("mod1");
        LIST<ModConflict> mod2Conflicts = api.checkModConflicts("mod2");
        
        assertFalse(mod1Conflicts.isEmpty(), "Mod1 should have conflicts");
        assertFalse(mod2Conflicts.isEmpty(), "Mod2 should have conflicts");
    }
    
    @Test
    @DisplayName("Should handle asset modifications")
    void testAssetModifications() {
        api.registerMod("texture_mod", "Texture Mod", "1.0");
        api.declareAssetModification("texture_mod", "/data/assets/sprite/race");
        
        // This should not create conflicts by itself
        assertFalse(api.hasConflicts(), "Single asset modification should not create conflicts");
    }
    
    @Test
    @DisplayName("Should handle data modifications")
    void testDataModifications() {
        api.registerMod("data_mod", "Data Mod", "1.0");
        api.declareDataModification("data_mod", "RACE", "FACTION");
        
        // This should not create conflicts by itself
        assertFalse(api.hasConflicts(), "Single data modification should not create conflicts");
    }
    
    @Test
    @DisplayName("Should handle dependencies")
    void testDependencies() {
        api.registerMod("dependent_mod", "Dependent Mod", "1.0");
        api.declareDependency("dependent_mod", "required_mod");
        
        // This should create a missing dependency conflict
        assertTrue(api.hasConflicts(), "Missing dependency should create conflict");
        assertTrue(api.getConflictCount() > 0, "Should have conflict count > 0");
    }
    
    @Test
    @DisplayName("Should provide mod summary")
    void testModSummary() {
        api.registerMod("test_mod", "Test Mod", "1.0", "A test mod", "Test Author");
        api.declareClassReplacement("test_mod", "game.GAME");
        
        String summary = api.getModSummary();
        assertNotNull(summary, "Summary should not be null");
        assertTrue(summary.contains("Test Mod"), "Summary should contain mod name");
        assertTrue(summary.contains("test_mod"), "Summary should contain mod ID");
    }
    
    @Test
    @DisplayName("Should handle complex conflict scenarios")
    void testComplexConflictScenarios() {
        // Create a complex scenario with multiple mods and conflict types
        api.registerMod("overhaul", "Overhaul Mod", "1.0");
        api.registerMod("compatibility", "Compatibility Mod", "1.0");
        api.registerMod("texture", "Texture Mod", "1.0");
        
        // Class replacement conflicts
        api.declareClassReplacement("overhaul", "settlement.main.SETT");
        api.declareClassReplacement("compatibility", "settlement.main.SETT");
        
        // Asset modification conflicts
        api.declareAssetModification("overhaul", "/data/assets/sprite/settlement");
        api.declareAssetModification("texture", "/data/assets/sprite/settlement");
        
        // Data modification conflicts
        api.declareDataModification("overhaul", "SETTLEMENT");
        api.declareDataModification("compatibility", "SETTLEMENT");
        
        assertTrue(api.hasConflicts(), "Complex scenario should have conflicts");
        assertTrue(api.getConflictCount() >= 3, "Should detect multiple conflicts");
        
        LIST<ModConflict> allConflicts = api.getAllConflicts();
        assertTrue(allConflicts.size() >= 3, "Should return all conflicts");
    }
}
