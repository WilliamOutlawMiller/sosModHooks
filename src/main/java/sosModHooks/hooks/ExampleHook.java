package sosModHooks.hooks;

/**
 * Example implementation of a game class hook.
 * Shows how to hook into game classes when they're created.
 */
public class ExampleHook implements GameClassHook {
    
    private final String hookName;
    
    public ExampleHook(String hookName) {
        this.hookName = hookName;
    }
    
    @Override
    public void beforeCreate(Object instance) {
        // Called BEFORE the constructor executes
        // instance will be null since the object isn't created yet
        System.out.println("[" + hookName + "] About to create game object");
    }
    
    @Override
    public void afterCreate(Object instance) {
        // Called AFTER the constructor executes
        // instance is the newly created game object
        if (instance != null) {
            System.out.println("[" + hookName + "] Created: " + 
                instance.getClass().getSimpleName());
        }
    }
    
    /**
     * Register example hooks for common game classes.
     */
    public static void registerExamples() {
        try {
            // Hook into game classes by name
            // These will be injected when the classes are loaded
            HookRegistry.registerHook("game.GAME", new ExampleHook("GameHook"));
            HookRegistry.registerHook("settlement.main.SETT", new ExampleHook("SettlementHook"));
            
            System.out.println("Example hooks registered successfully!");
        } catch (Exception e) {
            System.err.println("Failed to register example hooks: " + e.getMessage());
        }
    }
}
