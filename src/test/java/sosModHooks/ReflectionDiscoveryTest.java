package sosModHooks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import script.SCRIPT;

/**
 * Tests for reflection-based mod declaration discovery
 */
public class ReflectionDiscoveryTest {
    
    private ModRegistry registry;
    private TestModScript testMod;
    
    @BeforeEach
    void setUp() {
        registry = ModRegistry.getInstance();
        registry.clearAll();
        testMod = new TestModScript();
    }
    
    @Test
    @DisplayName("Should discover mod ID through reflection")
    void testModIdDiscovery() {
        String modId = invokeStringMethod(testMod.getClass(), testMod, "getModId");
        assertEquals("test_mod", modId, "Should discover correct mod ID");
    }
    
    @Test
    @DisplayName("Should discover mod name through reflection")
    void testModNameDiscovery() {
        String modName = invokeStringMethod(testMod.getClass(), testMod, "getModName");
        assertEquals("Test Mod", modName, "Should discover correct mod name");
    }
    
    @Test
    @DisplayName("Should discover mod version through reflection")
    void testModVersionDiscovery() {
        String modVersion = invokeStringMethod(testMod.getClass(), testMod, "getModVersion");
        assertEquals("1.0.0", modVersion, "Should discover correct mod version");
    }
    
    @Test
    @DisplayName("Should discover class replacements through reflection")
    void testClassReplacementsDiscovery() {
        String[] classReplacements = invokeStringArrayMethod(testMod.getClass(), testMod, "getClassReplacements");
        
        assertNotNull(classReplacements, "Class replacements should not be null");
        assertEquals(2, classReplacements.length, "Should discover 2 class replacements");
        assertEquals("game.GAME", classReplacements[0], "Should discover first class replacement");
        assertEquals("settlement.main.SETT", classReplacements[1], "Should discover second class replacement");
    }
    
    @Test
    @DisplayName("Should discover asset modifications through reflection")
    void testAssetModificationsDiscovery() {
        String[] assetModifications = invokeStringArrayMethod(testMod.getClass(), testMod, "getAssetModifications");
        
        assertNotNull(assetModifications, "Asset modifications should not be null");
        assertEquals(2, assetModifications.length, "Should discover 2 asset modifications");
        assertTrue(assetModifications[0].contains("sprite/race"), "Should discover sprite modification");
        assertTrue(assetModifications[1].contains("init/race"), "Should discover init modification");
    }
    
    @Test
    @DisplayName("Should discover data modifications through reflection")
    void testDataModificationsDiscovery() {
        String[] dataModifications = invokeStringArrayMethod(testMod.getClass(), testMod, "getDataModifications");
        
        assertNotNull(dataModifications, "Data modifications should not be null");
        assertEquals(2, dataModifications.length, "Should discover 2 data modifications");
        assertEquals("RACE", dataModifications[0], "Should discover RACE modification");
        assertEquals("FACTION", dataModifications[1], "Should discover FACTION modification");
    }
    
    @Test
    @DisplayName("Should discover dependencies through reflection")
    void testDependenciesDiscovery() {
        String[] dependencies = invokeStringArrayMethod(testMod.getClass(), testMod, "getDependencies");
        
        assertNotNull(dependencies, "Dependencies should not be null");
        assertEquals(1, dependencies.length, "Should discover 1 dependency");
        assertEquals("base_mod", dependencies[0], "Should discover base_mod dependency");
    }
    
    @Test
    @DisplayName("Should handle missing reflection methods gracefully")
    void testMissingReflectionMethods() {
        // Test with a script that doesn't implement the reflection methods
        SCRIPT basicScript = new SCRIPT() {
            @Override
            public CharSequence name() { return "Basic Script"; }
            @Override
            public CharSequence desc() { return "Basic description"; }
            @Override
            public boolean isSelectable() { return false; }
            @Override
            public boolean forceInit() { return false; }
            @Override
            public SCRIPT_INSTANCE createInstance() { return null; }
        };
        
        // These should return null without throwing exceptions
        String modId = invokeStringMethod(basicScript.getClass(), basicScript, "getModId");
        String[] classReplacements = invokeStringArrayMethod(basicScript.getClass(), basicScript, "getClassReplacements");
        
        assertNull(modId, "Should return null for missing method");
        assertNull(classReplacements, "Should return null for missing method");
    }
    
    // Helper methods copied from ModCompatibilityFramework for testing
    private String invokeStringMethod(Class<?> scriptClass, SCRIPT script, String methodName) {
        try {
            java.lang.reflect.Method method = scriptClass.getDeclaredMethod(methodName);
            method.setAccessible(true);
            Object result = method.invoke(script);
            return result != null ? result.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    private String[] invokeStringArrayMethod(Class<?> scriptClass, SCRIPT script, String methodName) {
        try {
            java.lang.reflect.Method method = scriptClass.getDeclaredMethod(methodName);
            method.setAccessible(true);
            Object result = method.invoke(script);
            if (result instanceof String[]) {
                return (String[]) result;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
