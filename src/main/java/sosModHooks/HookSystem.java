package sosModHooks;

import sosModHooks.hooks.HookRegistry;
import sosModHooks.hooks.transformer.ClassTransformer;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Main entry point for the mod hook system.
 * Handles initialization, hook registration, and class transformation.
 */
public class HookSystem {
    
    private static boolean initialized = false;
    private static final Map<String, Class<?>> transformedClasses = new ConcurrentHashMap<>();
    private static Instrumentation instrumentation;
    
    /**
     * Initialize the hook system.
     * Call this before registering any hooks.
     */
    public static void initialize() {
        if (initialized) {
            System.out.println("Hook system already initialized");
            return;
        }
        
        try {
            // Try to get instrumentation if available
            Instrumentation inst = getInstrumentation();
            if (inst != null) {
                inst.addTransformer(new HookClassFileTransformer());
                System.out.println("Hook system initialized with instrumentation");
            } else {
                System.out.println("Hook system initialized (no instrumentation available)");
                System.out.println("Note: For full hook functionality, start JVM with -javaagent:sosModHooks.jar");
            }
            
            initialized = true;
        } catch (Exception e) {
            System.err.println("Failed to initialize hook system: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Initialize with instrumentation from Java agent.
     */
    public static void initializeWithInstrumentation(Instrumentation inst) {
        if (initialized) {
            System.out.println("Hook system already initialized");
            return;
        }
        
        try {
            instrumentation = inst;
            inst.addTransformer(new HookClassFileTransformer());
            System.out.println("Hook system initialized with instrumentation from agent");
            initialized = true;
        } catch (Exception e) {
            System.err.println("Failed to initialize hook system with instrumentation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Register a hook for a game class.
     */
    public static void registerHook(Class<?> gameClass, sosModHooks.hooks.GameClassHook hook) {
        ensureInitialized();
        HookRegistry.registerHook(gameClass, hook);
    }
    
    /**
     * Register a hook for a game class by name.
     */
    public static void registerHook(String className, sosModHooks.hooks.GameClassHook hook) {
        ensureInitialized();
        HookRegistry.registerHook(className, hook);
    }
    
    /**
     * Get all registered hooks.
     */
    public static java.util.Map<Class<?>, java.util.List<sosModHooks.hooks.GameClassHook>> getAllHooks() {
        ensureInitialized();
        return HookRegistry.getAllHooks();
    }
    
    /**
     * Check if a class has hooks.
     */
    public static boolean hasHook(Class<?> gameClass) {
        ensureInitialized();
        return HookRegistry.hasHook(gameClass);
    }
    
    /**
     * Remove a hook.
     */
    public static boolean removeHook(Class<?> gameClass, sosModHooks.hooks.GameClassHook hook) {
        ensureInitialized();
        return HookRegistry.removeHook(gameClass, hook);
    }
    
    /**
     * Clear all hooks.
     */
    public static void clearHooks() {
        ensureInitialized();
        HookRegistry.clearHooks();
    }
    
    /**
     * Shutdown the hook system.
     */
    public static void shutdown() {
        if (!initialized) return;
        
        try {
            clearHooks();
            transformedClasses.clear();
            instrumentation = null;
            System.out.println("Hook system shut down successfully");
            initialized = false;
        } catch (Exception e) {
            System.err.println("Error during hook system shutdown: " + e.getMessage());
        }
    }
    
    /**
     * Check if system is initialized.
     */
    public static boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Check if a class has been transformed.
     */
    public static boolean isClassTransformed(String className) {
        return transformedClasses.containsKey(className);
    }
    
    private static void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Hook system not initialized. Call HookSystem.initialize() first.");
        }
    }
    
    private static Instrumentation getInstrumentation() {
        try {
            if (HookAgent.isAgentLoaded()) {
                return HookAgent.getInstrumentation();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Transforms classes when they're loaded by the JVM.
     */
    private static class HookClassFileTransformer implements ClassFileTransformer {
        
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                              ProtectionDomain protectionDomain, byte[] classfileBuffer) {
            
            if (className == null || classfileBuffer == null) return classfileBuffer;
            
            // Convert class name format (e.g., "game/GAME" -> "game.GAME")
            String javaClassName = className.replace('/', '.');
            
            try {
                // Check if we have hooks for this class
                if (HookRegistry.hasHook(javaClassName)) {
                    System.out.println("Transforming class: " + javaClassName);
                    
                    // Create transformer and transform the class
                    ClassTransformer transformer = new ClassTransformer(
                        Class.forName(javaClassName, false, loader)
                    );
                    
                    byte[] transformedBytes = transformer.transform(classfileBuffer);
                    if (transformedBytes != null) {
                        transformedClasses.put(javaClassName, null);
                        System.out.println("Successfully transformed class: " + javaClassName);
                        return transformedBytes;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error transforming class " + javaClassName + ": " + e.getMessage());
            }
            
            return classfileBuffer;
        }
    }
}
