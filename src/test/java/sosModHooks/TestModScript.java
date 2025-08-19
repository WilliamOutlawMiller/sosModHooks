package sosModHooks;

import script.SCRIPT;

/**
 * Test mod script that implements the reflection-based integration methods.
 * This is used to test the framework's ability to discover mod declarations.
 */
public class TestModScript implements SCRIPT {
    
    @Override
    public CharSequence name() {
        return "Test Mod";
    }
    
    @Override
    public CharSequence desc() {
        return "A test mod for testing the compatibility framework";
    }
    
    @Override
    public boolean isSelectable() {
        return false; // Not selectable in game
    }
    
    @Override
    public boolean forceInit() {
        return false;
    }
    
    @Override
    public SCRIPT_INSTANCE createInstance() {
        return null; // Not needed for testing
    }
    
    // Reflection-based integration methods
    
    public String getModId() {
        return "test_mod";
    }
    
    public String getModName() {
        return "Test Mod";
    }
    
    public String getModVersion() {
        return "1.0.0";
    }
    
    public String getModDescription() {
        return "A test mod for testing the compatibility framework";
    }
    
    public String getModAuthor() {
        return "Test Author";
    }
    
    public String[] getClassReplacements() {
        return new String[] {
            "game.GAME",
            "settlement.main.SETT"
        };
    }
    
    public String[] getAssetModifications() {
        return new String[] {
            "/data/assets/sprite/race/face/addon",
            "/data/assets/init/race/sprite"
        };
    }
    
    public String[] getDataModifications() {
        return new String[] {
            "RACE",
            "FACTION"
        };
    }
    
    public String[] getDependencies() {
        return new String[] {
            "base_mod"
        };
    }
}
