package sosModHooks;

import script.SCRIPT;
import sosModHooks.hooks.ExampleHook;

/**
 * Main entry point for the mod.
 * This class is loaded by the game when the mod starts.
 */
public class MainScript implements SCRIPT {
    
    private final String info = "Songs of Syx Mod Hook System - Enables ASM-based modding";
    
    @Override
    public CharSequence name() {
        return "Mod Hook System";
    }
    
    @Override
    public CharSequence desc() {
        return info;
    }
    
    @Override
    public void initBeforeGameCreated() {
        // Initialize the hook system
        HookSystem.initialize();
        
        // Register example hooks
        ExampleHook.registerExamples();
        
        System.out.println("Mod Hook System initialized successfully!");
    }
    
    @Override
    public boolean isSelectable() {
        return false; // This mod doesn't need to be selectable
    }
    
    @Override
    public boolean forceInit() {
        return true; // Force initialization
    }
    
    @Override
    public SCRIPT_INSTANCE createInstance() {
        return new InstanceScript();
    }
}