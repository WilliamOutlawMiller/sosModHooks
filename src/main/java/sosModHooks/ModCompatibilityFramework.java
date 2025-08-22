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
        
        private boolean hasReportedConflicts = false;
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
                        System.out.println("sosModHooks: Runtime detection complete, detecting conflicts...");
                        
                        // Detect conflicts using the unified registry system
                        snake2d.util.sets.LIST<ModConflict> conflicts = ModRegistry.getInstance().detectConflicts();
                        System.out.println("sosModHooks: Detected " + conflicts.size() + " conflicts using runtime detection system");
                        
                        // Report active mods
                        Map<String, String> activeMods = ModRegistry.getInstance().getActiveModNames();
                        System.out.println("sosModHooks: Active mods detected: " + activeMods.size());
                        for (Map.Entry<String, String> entry : activeMods.entrySet()) {
                            System.out.println("  - " + entry.getKey() + ": " + entry.getValue());
                        }
                        
                        System.out.println("sosModHooks: Mod registry initialized successfully with runtime detection");
                    } else {
                        System.out.println("sosModHooks: Runtime detection not complete yet, waiting...");
                    }
                    
                } catch (Exception e) {
                    System.err.println("sosModHooks: Failed to initialize mod registry: " + e.getMessage());
                }
            }
            
            // Try to initialize key bindings if not ready yet
            if (tickCounter == 30 && !ModKeyBindings.getInstance().isInitialized()) { // After 0.5 seconds
                try {
                    System.out.println("sosModHooks: Attempting to initialize key bindings at tick " + tickCounter);
                    ModKeyBindings.getInstance().initialize();
                } catch (Exception e) {
                    System.err.println("sosModHooks: Initial key binding initialization failed: " + e.getMessage());
                    // Don't log the full stack trace here to avoid spam
                }
            }
            
            // Retry initialization if still not ready after 2 seconds
            if (tickCounter == 120 && !ModKeyBindings.getInstance().isInitialized()) { // After 2 seconds
                try {
                    System.out.println("sosModHooks: Retrying key binding initialization at tick " + tickCounter);
                    ModKeyBindings.getInstance().initialize();
                } catch (Exception e) {
                    System.err.println("sosModHooks: Retry key binding initialization failed: " + e.getMessage());
                }
            }
            
            // Report conflicts once after game starts
            if (tickCounter == 60 && !hasReportedConflicts) { // After 1 second
                reportCompatibilityStatus();
                hasReportedConflicts = true;
                System.out.println("sosModHooks: Initial compatibility report sent");
            }
            
            // Check for mod compatibility overlay key press
            if (ModKeyBindings.getInstance().isModCompatibilityOverlayPressed()) {
                System.out.println("sosModHooks: Mod compatibility overlay key pressed - toggling comprehensive overlay");
                comprehensiveOverlay.toggle();
                // Add a small delay to prevent multiple toggles
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // Ignore interruption
                }
            } else if (tickCounter % 300 == 0) { // Log every 5 seconds
                // Debug key binding status
                Key modKey = ModKeyBindings.getInstance().getModCompatibilityOverlayKey();
                if (modKey != null) {
                    System.out.println("sosModHooks: Mod key exists: " + modKey.name);
                    System.out.println("sosModHooks: Mod key modCode: " + modKey.modCode() + ", keyCode: " + modKey.keyCode());
                    System.out.println("sosModHooks: Mod key rebindable: " + modKey.rebindable);
                    
                    // Check if the key is in the MAIN page
                    KeyPage mainPage = KEYS.MAIN();
                    if (mainPage != null) {
                        boolean foundInPage = false;
                        for (int i = 0; i < mainPage.all().size(); i++) {
                            Key k = mainPage.all().get(i);
                            if (k == modKey) {
                                foundInPage = true;
                                System.out.println("sosModHooks: Mod key found in MAIN page at index " + i);
                                break;
                            }
                        }
                        if (!foundInPage) {
                            System.out.println("sosModHooks: WARNING: Mod key NOT found in MAIN page!");
                        }
                    } else {
                        System.out.println("sosModHooks: MAIN page is still null - KEYS system not ready");
                    }
                } else {
                    System.out.println("sosModHooks: Mod key is null - key binding system not working");
                }
            }
            
            // Log mod activity periodically to confirm it's running
            if (tickCounter % 300 == 0) { // Every 5 seconds
                System.out.println("sosModHooks: Mod is running - tick: " + tickCounter + ", key bindings initialized: " + ModKeyBindings.getInstance().isInitialized());
            }
        }
        
        


        @Override
        public void save(snake2d.util.file.FilePutter file) {
            // TEMPORARILY DISABLED: Save functionality disabled to prevent corruption
            // This will be re-enabled once we resolve the underlying issue
            try {
                // Write safe empty data to prevent any corruption
                file.chars("{}");
                System.out.println("sosModHooks: Save functionality temporarily disabled - safe empty data written");
            } catch (Exception e) {
                System.err.println("sosModHooks: Critical error writing save data: " + e.getMessage());
            }
        }

        @Override
        public void load(snake2d.util.file.FileGetter file) throws IOException {
            // TEMPORARILY DISABLED: Load functionality disabled to prevent corruption
            // This will be re-enabled once we resolve the underlying issue
            System.out.println("sosModHooks: Load functionality temporarily disabled - using default data");
            // Don't read any data from the file to prevent corruption
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
                    System.out.println("sosModHooks: Rendering comprehensive overlay");
                    comprehensiveOverlay.render(r);
                    System.out.println("sosModHooks: Comprehensive overlay rendered successfully");
                } catch (Exception e) {
                    System.err.println("sosModHooks: Error rendering comprehensive overlay: " + e.getMessage());
                    e.printStackTrace();
                }
            } else if (tickCounter % 300 == 0) { // Log every 5 seconds
                System.out.println("sosModHooks: render() called - comprehensive overlay not visible");
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
                    // Mouse coordinates will be handled in the hover method
                    // For now, just pass a default position
                    comprehensiveOverlay.handleMouseClick(0, 0, button.ordinal());
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
            
            renderText(r, "System Health: " + healthStatus + " (" + healthScore + "/100)", panelX + 10, panelY + 100, 12);
            
            // Performance info using normal text color
            util.colors.GCOLOR.T().NORMAL.bind();
            Map<String, Long> metrics = enhancementManager.getPerformanceMetrics();
            if (metrics.containsKey("memory_percentage")) {
                long memoryPercent = metrics.get("memory_percentage");
                renderText(r, "Memory Usage: " + memoryPercent + "%", panelX + 10, panelY + 120, 10);
            }
            
            snake2d.util.color.COLOR.unbind();
        }
        
        // Conflict detection not implemented in current version
        private void renderConflictDetails(snake2d.Renderer r, int screenWidth, int screenHeight) {
            int panelX = screenWidth - 400 - 20;
            int panelY = 20;
            int startY = panelY + 140; // Start below the main status section
            
            ModRegistry registry = ModRegistry.getInstance();
            // Note: Conflict detection not implemented in current version
            
            // Header using game's native heading colors
            util.colors.GCOLOR.T().H2.bind();
            renderText(r, "Conflict Details:", panelX + 10, startY, 14);
            startY += 20;
            
            // Show first few conflicts (limit to avoid overwhelming the UI)
            int maxConflicts = 3; // Reduced to make room for mod details
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
                if (description.length() > 50) {
                    description = description.substring(0, 47) + "...";
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
            
            // Add conflict resolution advice
            if (conflicts.size() > 0) {
                util.colors.GCOLOR.T().INORMAL.bind();
                renderText(r, "Press F10 again to see detailed mod information", panelX + 15, startY, 10);
            }
        }
        
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
                return;
            }
            
            // Header using game's native heading colors
            util.colors.GCOLOR.T().H2.bind();
            renderText(r, "Detected Mods:", panelX + 10, startY, 14);
            startY += 20;
            
            // Show each mod's details
            for (Map.Entry<String, ModRegistry.ActiveModInfo> entry : activeMods.entrySet()) {
                String modId = entry.getKey();
                ModRegistry.ActiveModInfo modInfo = entry.getValue();
                
                // Mod name using normal text color
                util.colors.GCOLOR.T().NORMAL.bind();
                renderText(r, "• " + modInfo.modName + " (v" + modInfo.modVersion + ")", panelX + 15, startY, 12);
                startY += 15;
                
                // Show what this mod is modifying
                showModModifications(r, panelX, startY, modId, registry);
                startY += 25; // Extra space between mods
            }
        }
        
        private void showModModifications(snake2d.Renderer r, int panelX, int startY, String modId, ModRegistry registry) {
            // Show class replacements
            if (registry.getClassReplacements().containsKey(modId)) {
                util.colors.GCOLOR.T().WARNING.bind();
                renderText(r, "  Classes: " + getModificationsString(registry.getClassReplacements().get(modId)), panelX + 20, startY, 10);
                startY += 15;
            }
            
            // Show asset modifications
            if (registry.getAssetModifications().containsKey(modId)) {
                util.colors.GCOLOR.T().WARNING.bind();
                renderText(r, "  Assets: " + getModificationsString(registry.getAssetModifications().get(modId)), panelX + 20, startY, 10);
                startY += 15;
            }
            
            // Show data modifications
            if (registry.getDataModifications().containsKey(modId)) {
                util.colors.GCOLOR.T().WARNING.bind();
                renderText(r, "  Data: " + getModificationsString(registry.getDataModifications().get(modId)), panelX + 20, startY, 10);
                startY += 15;
            }
            
            // Show dependencies
            if (registry.getDependencies().containsKey(modId)) {
                util.colors.GCOLOR.T().INORMAL.bind();
                renderText(r, "  Dependencies: " + getModificationsString(registry.getDependencies().get(modId)), panelX + 20, startY, 10);
                startY += 15;
            }
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
    }
}
