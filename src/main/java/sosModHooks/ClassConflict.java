package sosModHooks;

import lombok.Getter;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

/**
 * Represents a specific compatibility conflict between mods
 */
public class ClassConflict {
    
    @Getter
    private final String className;
    
    @Getter
    private final LIST<String> conflictingMods;
    
    @Getter
    private final ConflictType type;
    
    @Getter
    private final String description;
    
    public ClassConflict(String className, LIST<String> conflictingMods, ConflictType type, String description) {
        this.className = className;
        this.conflictingMods = conflictingMods;
        this.type = type;
        this.description = description;
    }
    
    /**
     * Convert conflict to JSON for saving
     */
    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"className\":\"").append(className).append("\",");
        json.append("\"type\":\"").append(type.name()).append("\",");
        json.append("\"description\":\"").append(description).append("\",");
        json.append("\"conflictingMods\":[");
        
        for (int i = 0; i < conflictingMods.size(); i++) {
            if (i > 0) json.append(",");
            json.append("\"").append(conflictingMods.get(i)).append("\"");
        }
        
        json.append("]}");
        return json.toString();
    }
    
    /**
     * Get a human-readable summary of the conflict
     */
    public String getSummary() {
        return String.format("Conflict in %s: %s (%s)", 
            className, description, type.getDescription());
    }
}
