package sosModHooks;

import snake2d.KEYCODES;
import view.keyboard.KEYS;

/**
 * Simple mod key bindings system
 */
public class ModKeyBindings {
    
    private static ModKeyBindings instance;
    private boolean initialized = false;
    private int f10KeyCode = KEYCODES.KEY_F10;
    
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
     */
    public void initialize() {
        if (initialized) return;
        
        try {
            // Wait for the KEYS system to be ready
            if (KEYS.MAIN() == null) return;
            
            initialized = true;
            
        } catch (Exception e) {
            // Silently fail - will retry later
        }
    }
    
    /**
     * Check if F10 was pressed (simple key check)
     */
    public boolean isModCompatibilityOverlayPressed() {
        if (!initialized) return false;
        
        try {
            // Use the game's keyboard input system to check for F10
            return snake2d.CORE.getInput().getKeyboard().isPressed(f10KeyCode);
        } catch (Exception e) {
            // Silently fail - will retry later
            return false;
        }
    }
    
    /**
     * Check if the mod key bindings system is initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
}
