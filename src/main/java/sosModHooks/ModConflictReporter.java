package sosModHooks;

import lombok.Getter;
import lombok.Setter;
import view.ui.message.MessageText;

/**
 * Handles reporting compatibility conflicts to the user
 */
public class ModConflictReporter {
    
    @Getter @Setter
    private boolean overlayVisible = false;
    
    /**
     * Toggle the compatibility overlay visibility
     */
    public void toggleOverlay() {
        overlayVisible = !overlayVisible;
        System.out.println("sosModHooks: Overlay toggled. New state: " + overlayVisible);
        
        // Removed annoying MessageText alerts - keeping only console logging
        // if (overlayVisible) {
        //     new MessageText("Compatibility overlay enabled - Press F10 to hide").send();
        // } else {
        //     new MessageText("Compatibility overlay disabled - Press F10 to show").send();
        // }
    }
    
    /**
     * Show a compatibility warning message
     */
    public void showWarning(String message) {
        new MessageText("Compatibility Warning: " + message).send();
    }
    
    /**
     * Show a compatibility error message
     */
    public void showError(String message) {
        new MessageText("Compatibility Error: " + message).send();
    }
    
    /**
     * Show detailed conflict information
     */
    public void showConflictDetails(ClassConflict conflict) {
        StringBuilder message = new StringBuilder();
        message.append("Conflict Details:\n");
        message.append(conflict.getSummary()).append("\n");
        message.append("Affected mods: ");
        
        for (int i = 0; i < conflict.getConflictingMods().size(); i++) {
            if (i > 0) message.append(", ");
            message.append(conflict.getConflictingMods().get(i));
        }
        
        new MessageText(message.toString()).send();
    }
}
