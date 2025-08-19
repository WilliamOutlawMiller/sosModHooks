package sosModHooks;

import lombok.Getter;
import lombok.Setter;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the results of compatibility analysis between mods
 */
public class CompatibilityReport {
    
    @Getter @Setter
    private CompatibilityStatus status = CompatibilityStatus.UNKNOWN;
    
    @Getter
    private final Map<String, LIST<String>> classReplacements = new HashMap<>();
    
    @Getter
    private final snake2d.util.sets.ArrayList<ClassConflict> conflicts = new snake2d.util.sets.ArrayList<>(64);
    
    /**
     * Add a class replacement detected for a mod
     */
    public void addClassReplacement(String modName, LIST<String> replacedClasses) {
        classReplacements.put(modName, replacedClasses);
    }
    
    /**
     * Add a detected conflict
     */
    public void addConflict(ClassConflict conflict) {
        conflicts.add(conflict);
    }
    
    /**
     * Convert report to JSON for saving
     */
    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"status\":\"").append(status.name()).append("\",");
        json.append("\"conflicts\":[");
        
        for (int i = 0; i < conflicts.size(); i++) {
            if (i > 0) json.append(",");
            json.append(conflicts.get(i).toJson());
        }
        
        json.append("]}");
        return json.toString();
    }
    
    /**
     * Check if there are any conflicts
     */
    public boolean hasConflicts() {
        return !conflicts.isEmpty();
    }
}
