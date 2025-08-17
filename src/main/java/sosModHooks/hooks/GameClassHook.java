package sosModHooks.hooks;

public interface GameClassHook {

    void beforeCreate(Object instance);

    void afterCreate(Object instance);
}