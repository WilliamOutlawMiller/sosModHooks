package sosModHooks;

/**
 * Types of conflicts that can occur between mods.
 */
public enum ConflictType {
    
    CLASS_REPLACEMENT("Class Replacement", "Multiple mods replace the same core game class"),
    METHOD_CONFLICT("Method Conflict", "Mods have methods with conflicting signatures"),
    FIELD_CONFLICT("Field Conflict", "Mods have fields with conflicting names"),
    PACKAGE_CONFLICT("Package Conflict", "Mods use packages that conflict with core game packages"),
    ASSET_CONFLICT("Asset Conflict", "Multiple mods modify the same asset files"),
    DATA_CONFLICT("Data Conflict", "Multiple mods modify the same data structures"),
    MISSING_DEPENDENCY("Missing Dependency", "A required mod is not loaded"),
    LOAD_ORDER_CONFLICT("Load Order Conflict", "Mods have conflicting load order requirements");
    
    private final String displayName;
    private final String description;
    
    ConflictType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
