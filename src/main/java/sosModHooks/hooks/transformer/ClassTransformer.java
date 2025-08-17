package sosModHooks.hooks.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;
import sosModHooks.hooks.HookRegistry;
import java.util.List;

/**
 * Transforms game classes to inject hook calls.
 * This is the core ASM bytecode manipulation class.
 */
public class ClassTransformer {
    
    private final Class<?> targetClass;
    
    public ClassTransformer(Class<?> targetClass) {
        this.targetClass = targetClass;
    }
    
    /**
     * Transform the class bytecode to inject hooks.
     */
    public byte[] transform(byte[] classBytes) {
        try {
            ClassReader reader = new ClassReader(classBytes);
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            
            // Create visitor that will modify the class
            HookInjectingClassVisitor visitor = new HookInjectingClassVisitor(writer);
            reader.accept(visitor, 0);
            
            return writer.toByteArray();
        } catch (Exception e) {
            System.err.println("Error transforming class " + targetClass.getName() + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * ASM visitor that injects hook calls into constructors.
     */
    private class HookInjectingClassVisitor extends ClassVisitor {
        
        public HookInjectingClassVisitor(ClassVisitor cv) {
            super(Opcodes.ASM9, cv);
        }
        
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, 
                                      String signature, String[] exceptions) {
            
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            
            // Only inject into constructors
            if ("<init>".equals(name)) {
                return new HookInjectingMethodVisitor(mv, access, name, descriptor);
            }
            
            return mv;
        }
    }
    
    /**
     * ASM visitor that injects hook calls into methods.
     */
    private class HookInjectingMethodVisitor extends AdviceAdapter {
        
        private final String methodName;
        private final String methodDescriptor;
        
        protected HookInjectingMethodVisitor(MethodVisitor mv, int access, String name, String descriptor) {
            super(Opcodes.ASM9, mv, access, name, descriptor);
            this.methodName = name;
            this.methodDescriptor = descriptor;
        }
        
        @Override
        protected void onMethodEnter() {
            // Inject beforeCreate hook call at start of constructor
            injectHookCall(true);
        }
        
        @Override
        protected void onMethodExit(int opcode) {
            // Inject afterCreate hook call at end of constructor
            injectHookCall(false);
        }
        
        private void injectHookCall(boolean isBeforeCreate) {
            try {
                // Get hooks for this class
                List<sosModHooks.hooks.GameClassHook> hooks = HookRegistry.getHooks(targetClass);
                if (hooks == null || hooks.isEmpty()) return;
                
                // For each hook, call the appropriate method
                for (sosModHooks.hooks.GameClassHook hook : hooks) {
                    // Push the hook instance onto the stack
                    mv.visitLdcInsn(hook);
                    
                    // Push 'this' (the instance being created)
                    mv.visitVarInsn(ALOAD, 0);
                    
                    // Call the hook method
                    if (isBeforeCreate) {
                        mv.visitMethodInsn(INVOKEINTERFACE, 
                            "sosModHooks/hooks/GameClassHook", 
                            "beforeCreate", 
                            "(Ljava/lang/Object;)V", 
                            true);
                    } else {
                        mv.visitMethodInsn(INVOKEINTERFACE, 
                            "sosModHooks/hooks/GameClassHook", 
                            "afterCreate", 
                            "(Ljava/lang/Object;)V", 
                            true);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error injecting hook call: " + e.getMessage());
            }
        }
    }
}
