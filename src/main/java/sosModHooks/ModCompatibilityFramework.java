package sosModHooks;

import lombok.Getter;
import lombok.Setter;
import script.SCRIPT;
import settlement.main.SETT;
import snake2d.util.file.Json;
import snake2d.util.file.JsonE;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.gui.misc.GBox;
import view.keyboard.KEYS;
import view.keyboard.Key;
import view.keyboard.KeyPage;
import view.ui.message.MessageText;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Main compatibility framework that detects and manages mod conflicts.
 * This mod works within the existing game's constraints to provide
 * compatibility analysis and conflict resolution.
 */
public final class ModCompatibilityFramework {

    private final ModConflictReporter reporter;
    private final ModEnhancementManager enhancementManager;
    private final ComprehensiveModOverlay comprehensiveOverlay;

    public ModCompatibilityFramework() {
        this.reporter = new ModConflictReporter();
        this.enhancementManager = new ModEnhancementManager();
        this.comprehensiveOverlay = new ComprehensiveModOverlay();
        
        System.out.println("sosModHooks: ModCompatibilityFramework constructor called");
    }

    /**
     * Instance that runs during gameplay to monitor and manage compatibility
     */
    public class CompatibilityFrameworkInstance implements SCRIPT.SCRIPT_INSTANCE {
        
        private boolean hasReportedStatus = false;
        private int tickCounter = 0;

        @Override
        public void update(double ds) {
            tickCounter++;
            
            // Initialize registry and scan for conflicts on first tick
            if (tickCounter == 1) {
                try {
                    System.out.println("sosModHooks: Initializing mod registry at tick " + tickCounter);
                    
                    // Check if runtime detection is complete
                    if (ModRegistry.getInstance().isRuntimeDetectionComplete()) {
                        System.out.println("sosModHooks: Runtime detection complete");
                        System.out.println("sosModHooks: Mod registry initialized successfully with runtime detection");
                    } else {
                        System.out.println("sosModHooks: Runtime detection not complete yet, waiting...");
                    }
                    
                } catch (Exception e) {
                    System.err.println("sosModHooks: Failed to initialize mod registry: " + e.getMessage());
                }
            }
            
            // Initialize key bindings when ready (after 0.5 seconds)
            if (tickCounter == 30 && !ModKeyBindings.getInstance().isInitialized()) {
                try {
                    System.out.println("sosModHooks: Initializing key bindings at tick " + tickCounter);
                    ModKeyBindings.getInstance().initialize();
                } catch (Exception e) {
                    System.err.println("sosModHooks: Key binding initialization failed: " + e.getMessage());
                }
            }
            
            // Report compatibility status once after game starts
            if (tickCounter == 60 && !hasReportedStatus) { // After 1 second
                reportCompatibilityStatus();
                hasReportedStatus = true;
                System.out.println("sosModHooks: Initial compatibility report sent");
            }
            
            // Check for mod compatibility overlay key press
            if (ModKeyBindings.getInstance().isModCompatibilityOverlayPressed()) {
                System.out.println("sosModHooks: Mod compatibility overlay key pressed - toggling comprehensive overlay");
                comprehensiveOverlay.toggle();
            }
            
            // Log mod activity periodically (reduced frequency to avoid spam)
            if (tickCounter % 1800 == 0) { // Every 30 seconds instead of 5
                System.out.println("sosModHooks: Mod is running - tick: " + tickCounter + ", key bindings initialized: " + ModKeyBindings.getInstance().isInitialized());
            }
        }
        
        


        @Override
        public void save(snake2d.util.file.FilePutter file) {
            // No save data needed for this mod
        }

        @Override
        public void load(snake2d.util.file.FileGetter file) throws IOException {
            // No load data needed for this mod
        }

        @Override
        public void hoverTimer(double mouseTimer, GBox text) {
            // Add compatibility info to tooltips
            if (mouseTimer > 1000) { // After 1 second hover
                // Get compatibility status from registry
                ModRegistry registry = ModRegistry.getInstance();
                if (registry.isRuntimeDetectionComplete()) {
                    text.add(new util.info.INFO("Mod Compatibility", "Mod detection complete"));
                } else {
                    text.add(new util.info.INFO("Mod Compatibility", "Mod detection in progress"));
                }
            }
        }

        @Override
        public void render(snake2d.Renderer r, float ds) {
            // Render the comprehensive mod overlay if it's visible
            if (comprehensiveOverlay.isVisible()) {
                try {
                    comprehensiveOverlay.render(r);
                } catch (Exception e) {
                    System.err.println("sosModHooks: Error rendering comprehensive overlay: " + e.getMessage());
                }
            }
        }

        @Override
        public void keyPush(KEYS key) {
            // This method is called when a key is pressed
            // We'll handle key detection in the update method instead
        }

        @Override
        public void mouseClick(snake2d.MButt button) {
            // Handle comprehensive overlay interactions
            if (comprehensiveOverlay.isVisible()) {
                try {
                    // Get current mouse position from the game's input system
                    int mouseX = snake2d.CORE.getInput().getMouse().getCoo().x();
                    int mouseY = snake2d.CORE.getInput().getMouse().getCoo().y();
                    comprehensiveOverlay.handleMouseClick(mouseX, mouseY, button.ordinal());
                } catch (Exception e) {
                    System.err.println("sosModHooks: Error handling mouse click for comprehensive overlay: " + e.getMessage());
                }
            }
        }

        @Override
        public void hover(snake2d.util.datatypes.COORDINATE mCoo, boolean mouseHasMoved) {
            // Handle comprehensive overlay hover interactions
            if (comprehensiveOverlay.isVisible() && mouseHasMoved) {
                try {
                    // Get mouse coordinates from the coordinate object
                    int mouseX = mCoo.x();
                    int mouseY = mCoo.y();
                    
                    comprehensiveOverlay.handleMouseMove(mouseX, mouseY);
                } catch (Exception e) {
                    System.err.println("sosModHooks: Error handling mouse hover for comprehensive overlay: " + e.getMessage());
                }
            }
        }

        @Override
        public boolean handleBrokenSavedState() {
            return false;
        }

        private void reportCompatibilityStatus() {
            ModRegistry registry = ModRegistry.getInstance();
            if (registry.isRuntimeDetectionComplete()) {
                System.out.println("sosModHooks: Mod detection complete - Press F10 for details");
            } else {
                System.out.println("sosModHooks: Mod detection in progress...");
            }
        }

        private void renderCompatibilityOverlay(snake2d.Renderer r) {
            if (!reporter.isOverlayVisible()) {
                return;
            }
            
            // Get screen dimensions from CORE
            int screenWidth = snake2d.CORE.getGraphics().nativeWidth;
            int screenHeight = snake2d.CORE.getGraphics().nativeHeight;
            
            // Render background panel
            renderBackgroundPanel(r, screenWidth, screenHeight);
            
            // Render compatibility status
            renderCompatibilityStatus(r, screenWidth, screenHeight);
            
            // Render mod details to show what each mod is doing
            renderModDetails(r, screenWidth, screenHeight);
            
            // Always render mod details to show what each mod is doing
            renderModDetails(r, screenWidth, screenHeight);
            
            // Render instructions
            renderInstructions(r, screenWidth, screenHeight);
        }
        
        private void renderBackgroundPanel(snake2d.Renderer r, int screenWidth, int screenHeight) {
            // Semi-transparent background panel using game's native UI theming
            int panelWidth = 400;
            int panelHeight = 300;
            int panelX = screenWidth - panelWidth - 20;
            int panelY = 20;
            
            // Use game's native UI background colors
            util.colors.GCOLOR.UI().panBG.render(r, panelX, panelX + panelWidth, panelY, panelY + panelHeight);
            
            // Use game's native border colors
            util.colors.GCOLOR.UI().border().render(r, panelX, panelX + panelWidth, panelY, panelY + 1);
            util.colors.GCOLOR.UI().border().render(r, panelX, panelX + panelWidth, panelY + panelHeight - 1, panelY + panelHeight);
            util.colors.GCOLOR.UI().border().render(r, panelX, panelX + 1, panelY, panelY + panelHeight);
            util.colors.GCOLOR.UI().border().render(r, panelX + panelWidth - 1, panelX + panelWidth, panelY, panelY + panelHeight);
        }
        
        private void renderCompatibilityStatus(snake2d.Renderer r, int screenWidth, int screenHeight) {
            int panelX = screenWidth - 400 - 20;
            int panelY = 20;
            
            ModRegistry registry = ModRegistry.getInstance();
            
            // Title using game's native heading colors
            util.colors.GCOLOR.T().H1.bind();
            renderText(r, "Mod Detection Status", panelX + 10, panelY + 20, 16);
            
            // Status indicator using game's native text colors
            if (registry.isRuntimeDetectionComplete()) {
                util.colors.GCOLOR.T().IGOOD.bind();
                renderText(r, "✓ Mod Detection Complete", panelX + 10, panelY + 45, 14);
            } else {
                util.colors.GCOLOR.T().WARNING.bind();
                renderText(r, "⏳ Detection In Progress", panelX + 10, panelY + 45, 14);
            }
            
            // Summary using game's native normal text color
            util.colors.GCOLOR.T().NORMAL.bind();
            renderText(r, "Detection Status: " + (registry.isRuntimeDetectionComplete() ? "Complete" : "In Progress"), panelX + 10, panelY + 65, 12);
            
            // Show total mods detected
            renderText(r, "Total Mods Detected: " + registry.getActiveMods().size(), panelX + 10, panelY + 80, 12);
            
            // Show debug info about modification maps
            renderText(r, "Debug - Maps: Class(" + registry.getClassReplacements().size() + 
                       ") Asset(" + registry.getAssetModifications().size() + 
                       ") Data(" + registry.getDataModifications().size() + ")", panelX + 10, panelY + 95, 10);
            
            // System Health using game's native text colors
            int healthScore = enhancementManager.getSystemHealthScore();
            String healthStatus = enhancementManager.getSystemHealthStatus();
            
            // Color code health status based on score
            if (healthScore >= 80) {
                util.colors.GCOLOR.T().IGOOD.bind();
            } else if (healthScore >= 60) {
                util.colors.GCOLOR.T().INORMAL.bind();
            } else if (healthScore >= 40) {
                util.colors.GCOLOR.T().IBAD.bind();
            } else {
                util.colors.GCOLOR.T().IWORST.bind();
            }
            
            renderText(r, "System Health: " + healthStatus + " (" + healthScore + "/100)", panelX + 10, panelY + 110, 12);
            
            // Performance info using normal text color
            util.colors.GCOLOR.T().NORMAL.bind();
            Map<String, Long> metrics = enhancementManager.getPerformanceMetrics();
            if (metrics.containsKey("memory_percentage")) {
                long memoryPercent = metrics.get("memory_percentage");
                renderText(r, "Memory Usage: " + memoryPercent + "%", panelX + 10, panelY + 130, 10);
            }
            
            snake2d.util.color.COLOR.unbind();
        }
        
        /**
         * Render conflict details in the overlay.
         */
        private void renderConflictDetails(snake2d.Renderer r, int screenWidth, int screenHeight) {
            int panelX = screenWidth - 400 - 20;
            int panelY = 20;
            int startY = panelY + 140; // Start below the main status section
            
            ModRegistry registry = ModRegistry.getInstance();
            snake2d.util.sets.LIST<ModConflict> conflicts = registry.detectConflicts();
            
            if (conflicts.isEmpty()) {
                // No conflicts detected
                util.colors.GCOLOR.T().NORMAL.bind();
                renderText(r, "No conflicts detected - all mods are compatible", panelX + 10, startY, 10);
                startY += 20;
                renderText(r, "System is monitoring for runtime conflicts...", panelX + 10, startY, 10);
                return;
            }
            
            // Header using game's native heading colors
            util.colors.GCOLOR.T().H2.bind();
            renderText(r, "Runtime Conflict Detection:", panelX + 10, startY, 14);
            startY += 20;
            
            // Show conflict summary
            util.colors.GCOLOR.T().WARNING.bind();
            renderText(r, "Found " + conflicts.size() + " conflicts based on runtime analysis", panelX + 10, startY, 12);
            startY += 20;
            
            // Show first few conflicts with enhanced details
            int maxConflicts = 4; // Increased to show more detail
            int conflictCount = 0;
            
            for (ModConflict conflict : conflicts) {
                if (conflictCount >= maxConflicts) {
                    break;
                }
                
                // Conflict target using normal text color
                util.colors.GCOLOR.T().NORMAL.bind();
                renderText(r, "• " + conflict.getConflictTarget(), panelX + 15, startY, 12);
                
                startY += 15;
                // Conflict type using warning color
                util.colors.GCOLOR.T().WARNING.bind();
                renderText(r, "  Type: " + conflict.getType().getDisplayName(), panelX + 20, startY, 10);
                
                startY += 15;
                // Affected mods using normal text color
                util.colors.GCOLOR.T().NORMAL.bind();
                renderText(r, "  Mods: " + getModsString(conflict.getConflictingMods()), panelX + 20, startY, 10);
                
                startY += 15;
                // Conflict description using normal text color
                util.colors.GCOLOR.T().NORMAL.bind();
                String description = conflict.getSummary();
                if (description.length() > 60) {
                    description = description.substring(0, 57) + "...";
                }
                renderText(r, "  Issue: " + description, panelX + 20, startY, 10);
                
                startY += 20;
                conflictCount++;
            }
            
            if (conflicts.size() > maxConflicts) {
                // Additional conflicts info using normal text color
                util.colors.GCOLOR.T().NORMAL.bind();
                renderText(r, "... and " + (conflicts.size() - maxConflicts) + " more conflicts", 
                           panelX + 15, startY, 10);
                startY += 15;
            }
            
            // Add enhanced conflict resolution advice
            if (conflicts.size() > 0) {
                startY += 10;
                util.colors.GCOLOR.T().INORMAL.bind();
                renderText(r, "Press F10 again to see detailed mod information", panelX + 15, startY, 10);
                startY += 15;
                renderText(r, "Conflicts are based on runtime class loading analysis", panelX + 15, startY, 10);
                startY += 15;
                renderText(r, "Check console for detailed modification logs", panelX + 15, startY, 10);
            }
        }
        
        /**
         * Render detailed information about detected mods and their actual modifications.
         * Shows what each mod is really modifying based on file analysis.
         */
        private void renderModDetails(snake2d.Renderer r, int screenWidth, int screenHeight) {
            int panelX = screenWidth - 400 - 20;
            int panelY = 20;
            int startY = panelY + 140; // Start below the main status section
            
            ModRegistry registry = ModRegistry.getInstance();
            Map<String, ModRegistry.ActiveModInfo> activeMods = registry.getActiveMods();
            
            if (activeMods.isEmpty()) {
                // No mods detected
                util.colors.GCOLOR.T().NORMAL.bind();
                renderText(r, "No mods detected - scanning may not be complete", panelX + 10, startY, 10);
                startY += 20;
                renderText(r, "Check console for detection logs", panelX + 10, startY, 10);
                return;
            }
            
            // Header using game's native heading colors
            util.colors.GCOLOR.T().H2.bind();
            renderText(r, "Real Mod Analysis:", panelX + 10, startY, 14);
            startY += 20;
            
            // Show summary
            util.colors.GCOLOR.T().INORMAL.bind();
            renderText(r, "Found " + activeMods.size() + " active mods with file analysis", panelX + 10, startY, 10);
            startY += 20;
            
            // Show each mod's details
            for (Map.Entry<String, ModRegistry.ActiveModInfo> entry : activeMods.entrySet()) {
                String modId = entry.getKey();
                ModRegistry.ActiveModInfo modInfo = entry.getValue();
                
                // Mod name using normal text color
                util.colors.GCOLOR.T().NORMAL.bind();
                renderText(r, "• " + modInfo.modName + " (v" + modInfo.modVersion + ") [" + modId + "]", panelX + 15, startY, 12);
                startY += 15;
                
                // Show modification count
                int modCount = registry.getModificationCount(modId);
                if (modCount > 0) {
                    util.colors.GCOLOR.T().WARNING.bind();
                    renderText(r, "  " + modCount + " modifications detected", panelX + 20, startY, 10);
                } else {
                    util.colors.GCOLOR.T().INORMAL.bind();
                    renderText(r, "  0 modifications detected", panelX + 20, startY, 10);
                }
                startY += 15;
                
                // Show what this mod is modifying
                startY = showModModifications(r, panelX, startY, modId, registry);
                startY += 25; // Extra space between mods
            }
        }
        
        private int showModModifications(snake2d.Renderer r, int panelX, int startY, String modId, ModRegistry registry) {
            boolean hasModifications = false;
            
            // Show class replacements
            if (registry.getClassReplacements().containsKey(modId)) {
                util.colors.GCOLOR.T().WARNING.bind();
                renderText(r, "  Classes: " + getModificationsString(registry.getClassReplacements().get(modId)), panelX + 20, startY, 10);
                startY += 15;
                hasModifications = true;
            }
            
            // Show asset modifications
            if (registry.getAssetModifications().containsKey(modId)) {
                util.colors.GCOLOR.T().WARNING.bind();
                renderText(r, "  Assets: " + getModificationsString(registry.getAssetModifications().get(modId)), panelX + 20, startY, 10);
                startY += 15;
                hasModifications = true;
            }
            
            // Show data modifications
            if (registry.getDataModifications().containsKey(modId)) {
                util.colors.GCOLOR.T().WARNING.bind();
                renderText(r, "  Data: " + getModificationsString(registry.getDataModifications().get(modId)), panelX + 20, startY, 10);
                startY += 15;
                hasModifications = true;
            }
            
            // Show dependencies
            if (registry.getDependencies().containsKey(modId)) {
                util.colors.GCOLOR.T().INORMAL.bind();
                renderText(r, "  Dependencies: " + getModificationsString(registry.getDependencies().get(modId)), panelX + 20, startY, 10);
                startY += 15;
                hasModifications = true;
            }
            
            // If no modifications found, show debug info
            if (!hasModifications) {
                util.colors.GCOLOR.T().INORMAL.bind();
                renderText(r, "  No modifications detected - check console logs", panelX + 20, startY, 10);
                startY += 15;
                
                // Show debug info about what maps contain
                renderText(r, "  Debug: Class maps: " + registry.getClassReplacements().size() + 
                           ", Asset maps: " + registry.getAssetModifications().size() + 
                           ", Data maps: " + registry.getDataModifications().size(), panelX + 20, startY, 10);
                startY += 15;
            }
            
            return startY;
        }
        
        private void renderInstructions(snake2d.Renderer r, int screenWidth, int screenHeight) {
            int panelX = screenWidth - 400 - 20;
            int panelY = 20;
            int startY = panelY + 250;
            
            // Instructions using game's native normal text color
            util.colors.GCOLOR.T().NORMAL.bind();
            renderText(r, "Press F10 to toggle overlay", panelX + 10, startY, 10);
            renderText(r, "Hover over UI elements for details", panelX + 10, startY + 15, 10);
            snake2d.util.color.COLOR.unbind();
        }
        
        private void renderText(snake2d.Renderer r, String text, int x, int y, int fontSize) {
            // Use the game's font system to render text
            try {
                System.out.println("sosModHooks: Attempting to render text: '" + text + "' at (" + x + "," + y + ")");
                
                // Get the default UI font
                snake2d.util.sprite.text.Font font = init.sprite.UI.UI.FONT().M;
                System.out.println("sosModHooks: Got font: " + font);
                
                snake2d.util.sprite.text.Text textSprite = (snake2d.util.sprite.text.Text) font.getText(text);
                System.out.println("sosModHooks: Created text sprite: " + textSprite);
                
                textSprite.render(r, x, y);
                System.out.println("sosModHooks: Text rendered successfully");
            } catch (Exception e) {
                // Fallback: just log the text position for debugging
                System.err.println("Could not render text: " + text + " at (" + x + "," + y + ")");
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        private String getModsString(snake2d.util.sets.LIST<String> mods) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mods.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(mods.get(i));
            }
            return sb.toString();
        }

        private String getModificationsString(snake2d.util.sets.LIST<String> modifications) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < modifications.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(modifications.get(i));
            }
            return sb.toString();
        }

        // Duplicate method and helper methods removed
    }
}
