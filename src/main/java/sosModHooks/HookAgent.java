package sosModHooks;

import java.lang.instrument.Instrumentation;

/**
 * Java agent that intercepts class loading to enable ASM-based hooking.
 * This agent must be started with the JVM using: -javaagent:sosModHooks.jar
 */
public class HookAgent {
    
    private static Instrumentation instrumentation;
    
    /**
     * Called by the JVM when the agent is loaded.
     * This is the entry point for the Java agent.
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("HookAgent: Initializing ASM-based hook system...");
        
        try {
            // Store the instrumentation instance
            instrumentation = inst;
            
            // Initialize the hook system with instrumentation
            HookSystem.initializeWithInstrumentation(inst);
            
            System.out.println("HookAgent: Successfully initialized hook system");
        } catch (Exception e) {
            System.err.println("HookAgent: Failed to initialize hook system: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Called by the JVM when the agent is loaded dynamically.
     * This allows the agent to be loaded after the JVM has started.
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("HookAgent: Dynamic initialization of ASM-based hook system...");
        premain(agentArgs, inst);
    }
    
    /**
     * Get the instrumentation instance if available.
     */
    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }
    
    /**
     * Check if the agent is loaded and instrumentation is available.
     */
    public static boolean isAgentLoaded() {
        return instrumentation != null;
    }
}
