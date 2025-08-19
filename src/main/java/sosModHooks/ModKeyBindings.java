package sosModHooks;

import snake2d.KEYCODES;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import view.keyboard.Key;
import view.keyboard.KeyPage;
import view.keyboard.KEYS;
import init.text.D;
import util.dic.Dic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Mod Key Bindings system that integrates with the game's key binding settings
 * This creates a "Mod Keybindings" section in the game's settings menu
 */
public class ModKeyBindings {
    
    private static ModKeyBindings instance;
    private KeyPageMod modKeyPage;
    private boolean initialized = false;
    
    private ModKeyBindings() {
        // Private constructor for singleton
    }
    
    public static ModKeyBindings getInstance() {
        if (instance == null) {
            instance = new ModKeyBindings();
        }
        return instance;
    }
    
    /**
     * Initialize the mod key bindings system
     * This should be called when the game is ready (after KEYS system is initialized)
     */
    public void initialize() {
        if (initialized) {
            System.out.println("sosModHooks: Already initialized, skipping");
            return;
        }
        
        try {
            System.out.println("sosModHooks: Initializing mod key bindings system");
            
            // Wait for the KEYS system to be ready
            System.out.println("sosModHooks: Checking if KEYS system is ready...");
            KeyPage mainPage = KEYS.MAIN();
            if (mainPage == null) {
                System.out.println("sosModHooks: KEYS system not ready yet, MAIN() returned null - deferring initialization");
                return;
            }
            System.out.println("sosModHooks: KEYS system is ready, MAIN() = " + mainPage);
            
            // Create our mod key page
            System.out.println("sosModHooks: Creating KeyPageMod...");
            modKeyPage = new KeyPageMod();
            System.out.println("sosModHooks: KeyPageMod created: " + modKeyPage);
            
            // Add it to the game's key system using reflection
            System.out.println("sosModHooks: Calling addModKeyPageToGame()...");
            addModKeyPageToGame();
            
            initialized = true;
            System.out.println("sosModHooks: Mod key bindings system initialized successfully");
            
        } catch (Exception e) {
            System.err.println("sosModHooks: Failed to initialize mod key bindings: " + e.getMessage());
            // Don't print full stack trace here to avoid spam in the logs
            // The framework will retry initialization later
        }
    }
    
    /**
     * Get the mod compatibility overlay key
     */
    public Key getModCompatibilityOverlayKey() {
        if (modKeyPage != null) {
            return modKeyPage.MOD_COMPATIBILITY_OVERLAY;
        }
        return null;
    }
    
    /**
     * Check if the mod compatibility overlay key was pressed
     */
    public boolean isModCompatibilityOverlayPressed() {
        Key key = getModCompatibilityOverlayKey();
        return key != null && key.consumeClick();
    }
    
    /**
     * Check if the mod key bindings system is initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Add our mod key to the game's key system
     * Try multiple approaches to ensure the key is properly integrated
     */
    private void addModKeyPageToGame() throws Exception {
        System.out.println("sosModHooks: Starting addModKeyPageToGame()");
        
        // Get the MAIN key page
        KeyPage mainPage = KEYS.MAIN();
        if (mainPage == null) {
            throw new RuntimeException("MAIN key page not available");
        }
        System.out.println("sosModHooks: Got MAIN key page: " + mainPage);
        
        // Log current state of the main page
        System.out.println("sosModHooks: MAIN page currently has " + mainPage.all().size() + " keys");
        System.out.println("sosModHooks: MAIN page key: " + mainPage.key);
        System.out.println("sosModHooks: MAIN page name: " + mainPage.name());
        
        // Try approach 1: Use the simpler constructor that doesn't call assign()
        System.out.println("sosModHooks: Trying approach 1: Simple constructor without assign()");
        try {
            java.lang.reflect.Constructor<Key> simpleConstructor = Key.class.getDeclaredConstructor(
                String.class, CharSequence.class, CharSequence.class, KeyPage.class
            );
            simpleConstructor.setAccessible(true);
            
            String keyName = "Mod Compatibility Overlay";
            String keyDesc = "Toggles the mod compatibility overlay display";
            
            System.out.println("sosModHooks: Creating key with simple constructor: '" + keyName + "'");
            
            Key modKey = simpleConstructor.newInstance(
                "MOD_COMPATIBILITY_OVERLAY",
                keyName,
                keyDesc,
                mainPage
            );
            
            System.out.println("sosModHooks: Simple constructor approach successful: " + modKey);
            modKeyPage.MOD_COMPATIBILITY_OVERLAY = modKey;
            
            // Verify the key was added
            System.out.println("sosModHooks: MAIN page now has " + mainPage.all().size() + " keys");
            
            // Try to manually assign the key code if needed
            try {
                System.out.println("sosModHooks: Attempting to assign F10 to the key");
                boolean assigned = modKey.assign(-1, KEYCODES.KEY_F10);
                System.out.println("sosModHooks: Key assignment result: " + assigned);
            } catch (Exception e) {
                System.err.println("sosModHooks: Key assignment failed: " + e.getMessage());
            }
            
            return;
            
        } catch (Exception e) {
            System.err.println("sosModHooks: Simple constructor approach failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Try approach 2: Use the full constructor but with safer parameters
        System.out.println("sosModHooks: Trying approach 2: Full constructor with safer parameters");
        try {
            java.lang.reflect.Constructor<Key> fullConstructor = Key.class.getDeclaredConstructor(
                String.class, CharSequence.class, CharSequence.class, KeyPage.class, int.class, int.class, boolean.class
            );
            fullConstructor.setAccessible(true);
            
            String keyName = "Mod Compatibility Overlay";
            String keyDesc = "Toggles the mod compatibility overlay display";
            
            System.out.println("sosModHooks: Creating key with full constructor: '" + keyName + "'");
            
            // Use -1 for both mod and key code to avoid conflicts, then assign later
            Key modKey = fullConstructor.newInstance(
                "MOD_COMPATIBILITY_OVERLAY",
                keyName,
                keyDesc,
                mainPage,
                -1, // No modifier key initially
                -1, // No key code initially
                true // Rebindable
            );
            
            System.out.println("sosModHooks: Full constructor approach successful: " + modKey);
            modKeyPage.MOD_COMPATIBILITY_OVERLAY = modKey;
            
            // Verify the key was added
            System.out.println("sosModHooks: MAIN page now has " + mainPage.all().size() + " keys");
            
            // Try to assign the key code
            try {
                System.out.println("sosModHooks: Attempting to assign F10 to the key");
                boolean assigned = modKey.assign(-1, KEYCODES.KEY_F10);
                System.out.println("sosModHooks: Key assignment result: " + assigned);
            } catch (Exception e) {
                System.err.println("sosModHooks: Key assignment failed: " + e.getMessage());
            }
            
            return;
            
        } catch (Exception e) {
            System.err.println("sosModHooks: Full constructor approach failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        // If both approaches failed, throw an exception
        throw new RuntimeException("All key creation approaches failed");
    }
    
    /**
     * Custom KeyPage for mod-specific key bindings
     * This is just a wrapper to hold the key reference
     */
    private static class KeyPageMod {
        
        public Key MOD_COMPATIBILITY_OVERLAY; // Will be set after creation
        private final String key;
        private final CharSequence name;
        
        public KeyPageMod() {
            this.key = "MOD";
            this.name = "Mod Keybindings";
            // Key will be created and assigned later in addModKeyPageToGame()
        }
        
        public String getKey() {
            return key;
        }
        
        public CharSequence getName() {
            return name;
        }
    }
}
