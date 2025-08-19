package sosModHooks;

/**
 * Represents the overall compatibility status of loaded mods
 */
public enum CompatibilityStatus {
    UNKNOWN("Unknown compatibility status"),
    COMPATIBLE("All mods appear compatible"),
    CONFLICTS_DETECTED("Compatibility conflicts detected"),
    PARTIALLY_COMPATIBLE("Some conflicts detected but mods may work together"),
    INCOMPATIBLE("Mods are incompatible and will cause issues");
    
    private final String description;
    
    CompatibilityStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
