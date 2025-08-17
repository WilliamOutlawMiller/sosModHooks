package sosModHooks.hooks;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Manages registration and retrieval of game class hooks.
 * This is the central registry for all hooks in the system.
 */
public class HookRegistry {
    
    private static final Map<Class<?>, List<GameClassHook>> hooksByClass = new ConcurrentHashMap<>();
    private static final Map<String, List<GameClassHook>> hooksByName = new ConcurrentHashMap<>();
    
    /**
     * Register a hook for a game class.
     */
    public static void registerHook(Class<?> gameClass, GameClassHook hook) {
        if (gameClass == null || hook == null) return;
        
        hooksByClass.computeIfAbsent(gameClass, k -> new ArrayList<>()).add(hook);
        System.out.println("Registered hook for class: " + gameClass.getName());
    }
    
    /**
     * Register a hook for a game class by name.
     */
    public static void registerHook(String className, GameClassHook hook) {
        if (className == null || hook == null) return;
        
        hooksByName.computeIfAbsent(className, k -> new ArrayList<>()).add(hook);
        System.out.println("Registered hook for class name: " + className);
    }
    
    /**
     * Get all hooks for a game class.
     */
    public static List<GameClassHook> getHooks(Class<?> gameClass) {
        if (gameClass == null) return new ArrayList<>();
        
        List<GameClassHook> hooks = hooksByClass.get(gameClass);
        return hooks != null ? new ArrayList<>(hooks) : new ArrayList<>();
    }
    
    /**
     * Get all hooks for a game class by name.
     */
    public static List<GameClassHook> getHooks(String className) {
        if (className == null) return new ArrayList<>();
        
        List<GameClassHook> hooks = hooksByName.get(className);
        return hooks != null ? new ArrayList<>(hooks) : new ArrayList<>();
    }
    
    /**
     * Check if a class has hooks.
     */
    public static boolean hasHook(Class<?> gameClass) {
        if (gameClass == null) return false;
        
        List<GameClassHook> hooks = hooksByClass.get(gameClass);
        return hooks != null && !hooks.isEmpty();
    }
    
    /**
     * Check if a class name has hooks.
     */
    public static boolean hasHook(String className) {
        if (className == null) return false;
        
        List<GameClassHook> hooks = hooksByName.get(className);
        return hooks != null && !hooks.isEmpty();
    }
    
    /**
     * Remove a hook from a game class.
     */
    public static boolean removeHook(Class<?> gameClass, GameClassHook hook) {
        if (gameClass == null || hook == null) return false;
        
        List<GameClassHook> hooks = hooksByClass.get(gameClass);
        if (hooks != null) {
            boolean removed = hooks.remove(hook);
            if (hooks.isEmpty()) {
                hooksByClass.remove(gameClass);
            }
            return removed;
        }
        return false;
    }
    
    /**
     * Get all registered hooks.
     */
    public static Map<Class<?>, List<GameClassHook>> getAllHooks() {
        return new ConcurrentHashMap<>(hooksByClass);
    }
    
    /**
     * Clear all hooks.
     */
    public static void clearHooks() {
        hooksByClass.clear();
        hooksByName.clear();
        System.out.println("All hooks cleared");
    }
}
