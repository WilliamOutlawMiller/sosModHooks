package sosModHooks;

import lombok.Getter;
import snake2d.util.sets.LIST;

/**
 * Represents a conflict between mods detected by the compatibility framework.
 */
public final class ModConflict {
    
    @Getter
    private final String conflictTarget;
    
    @Getter
    private final LIST<String> conflictingMods;
    
    @Getter
    private final ConflictType type;
    
    @Getter
    private final String description;
    
    @Getter
    private final long timestamp;
    
    public ModConflict(String conflictTarget, LIST<String> conflictingMods, ConflictType type, String description) {
        this.conflictTarget = conflictTarget;
        this.conflictingMods = conflictingMods;
        this.type = type;
        this.description = description;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Get a human-readable summary of the conflict.
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.getDisplayName()).append(": ").append(conflictTarget);
        
        if (conflictingMods.size() > 1) {
            sb.append(" (affected by ").append(conflictingMods.size()).append(" mods)");
        }
        
        return sb.toString();
    }
    
    /**
     * Get detailed information about the conflict.
     */
    public String getDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append("Conflict Type: ").append(type.getDisplayName()).append("\n");
        sb.append("Target: ").append(conflictTarget).append("\n");
        sb.append("Description: ").append(description).append("\n");
        sb.append("Affected Mods: ");
        
        for (int i = 0; i < conflictingMods.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(conflictingMods.get(i));
        }
        
        sb.append("\n");
        sb.append("Detected: ").append(new java.util.Date(timestamp));
        
        return sb.toString();
    }
    
    /**
     * Get suggestions for resolving the conflict.
     */
    public String getResolutionSuggestions() {
        switch (type) {
            case CLASS_REPLACEMENT:
                return "Only one mod can replace a core class. Consider:\n" +
                       "1. Using only one of the conflicting mods\n" +
                       "2. Checking if the mods have compatibility patches\n" +
                       "3. Contacting the mod authors for guidance";
                
            case ASSET_CONFLICT:
                return "Multiple mods are modifying the same assets. Consider:\n" +
                       "1. Checking mod load order (last loaded wins)\n" +
                       "2. Using asset merging tools if available\n" +
                       "3. Choosing one mod over the other";
                
            case DATA_CONFLICT:
                return "Multiple mods are modifying the same data structures. Consider:\n" +
                       "1. Checking if the mods are designed to work together\n" +
                       "2. Using compatibility patches if available\n" +
                       "3. Loading mods in a different order";
                
            case MISSING_DEPENDENCY:
                return "A required mod is not loaded. Consider:\n" +
                       "1. Installing the missing dependency mod\n" +
                       "2. Checking if the dependency is optional\n" +
                       "3. Contacting the mod author for clarification";
                
            case LOAD_ORDER_CONFLICT:
                return "Mods have conflicting load order requirements. Consider:\n" +
                       "1. Loading mods in the order specified by their authors\n" +
                       "2. Using a mod manager to handle load order\n" +
                       "3. Checking for compatibility patches";
                
            default:
                return "General conflict detected. Consider:\n" +
                       "1. Checking mod compatibility documentation\n" +
                       "2. Using compatibility patches if available\n" +
                       "3. Contacting mod authors for support";
        }
    }
    
    @Override
    public String toString() {
        return getSummary();
    }
}
