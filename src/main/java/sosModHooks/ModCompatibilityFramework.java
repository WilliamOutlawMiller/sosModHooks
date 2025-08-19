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

    private final ModCompatibilityScanner scanner;
    private final ModConflictReporter reporter;
    private final ModEnhancementManager enhancementManager;

    public ModCompatibilityFramework() {
        this.scanner = new ModCompatibilityScanner();
        this.reporter = new ModConflictReporter();
        this.enhancementManager = new ModEnhancementManager();
        
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
                    
                    // Auto-register detected mods from the old scanner for backward compatibility
                    autoRegisterExistingMods();
                    
                    // Detect conflicts using the new registry system
                    snake2d.util.sets.LIST<ModConflict> conflicts = ModRegistry.getInstance().detectConflicts();
                    System.out.println("sosModHooks: Detected " + conflicts.size() + " conflicts using new registry system");
                    
                    System.out.println("sosModHooks: Mod registry initialized successfully");
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
                System.out.println("sosModHooks: Mod compatibility overlay key pressed - toggling overlay");
                reporter.toggleOverlay();
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
        
        /**
         * Auto-register existing mods for backward compatibility.
         * This allows the new system to work with mods that haven't been updated yet.
         * 
         * NEW APPROACH: Instead of requiring mods to import our classes,
         * we'll detect them automatically and provide a way for them to
         * declare their changes through reflection or simple methods.
         */
        private void autoRegisterExistingMods() {
            try {
                // Use the old scanner to detect mods and register them automatically
                scanner.scanForConflicts();
                
                // Get loaded scripts and register them with basic info
                LIST<SCRIPT> loadedScripts = getLoadedScripts();
                for (SCRIPT script : loadedScripts) {
                    String modName = script.name().toString();
                    String modId = modName.toLowerCase().replaceAll("[^a-z0-9_]", "_");
                    
                    // Auto-register with basic info
                    ModRegistry.getInstance().registerMod(modId, modName, "unknown");
                    
                    // Try to detect what this mod modifies based on its name
                    if (modName.toLowerCase().contains("warhammer")) {
                        ModRegistry.getInstance().declareAssetModification(modId, 
                            "/data/assets/sprite/race/face/addon",
                            "/data/assets/init/race/sprite",
                            "/data/assets/text/event"
                        );
                        ModRegistry.getInstance().declareDataModification(modId, "FACTION", "RACE", "EVENT");
                    }
                    
                    if (modName.toLowerCase().contains("farm") || modName.toLowerCase().contains("agriculture")) {
                        ModRegistry.getInstance().declareClassReplacement(modId, 
                            "settlement.room.food.farm.FarmInstance",
                            "settlement.room.food.farm.ROOM_FARM"
                        );
                    }
                    
                    if (modName.toLowerCase().contains("tech") || modName.toLowerCase().contains("technology")) {
                        ModRegistry.getInstance().declareClassReplacement(modId, 
                            "init.tech.TECH",
                            "init.tech.Knowledge_Costs"
                        );
                    }
                    
                    // NEW: Try to discover mod declarations through reflection
                    discoverModDeclarations(script, modId);
                }
                
                System.out.println("sosModHooks: Auto-registered " + loadedScripts.size() + " existing mods");
                
            } catch (Exception e) {
                System.err.println("sosModHooks: Failed to auto-register existing mods: " + e.getMessage());
            }
        }
        
        /**
         * NEW APPROACH: Discover mod declarations through reflection.
         * This allows mods to declare their changes without importing our classes.
         * 
         * Mods can implement these methods to declare their changes:
         * - getModId() - returns the mod's unique identifier
         * - getModName() - returns the mod's display name
         * - getModVersion() - returns the mod's version
         * - getClassReplacements() - returns array of replaced class names
         * - getAssetModifications() - returns array of modified asset paths
         * - getDataModifications() - returns array of modified data types
         * - getDependencies() - returns array of required mod IDs
         */
        private void discoverModDeclarations(SCRIPT script, String defaultModId) {
            try {
                Class<?> scriptClass = script.getClass();
                
                // Try to get mod ID
                String modId = invokeStringMethod(scriptClass, script, "getModId");
                if (modId == null) modId = defaultModId;
                
                // Try to get mod name
                String modName = invokeStringMethod(scriptClass, script, "getModName");
                if (modName == null) modName = script.name().toString();
                
                // Try to get mod version
                String modVersion = invokeStringMethod(scriptClass, script, "getModVersion");
                if (modVersion == null) modVersion = "unknown";
                
                // Update registration with discovered info
                ModRegistry.getInstance().registerMod(modId, modName, modVersion);
                
                // Try to discover class replacements
                String[] classReplacements = invokeStringArrayMethod(scriptClass, script, "getClassReplacements");
                if (classReplacements != null && classReplacements.length > 0) {
                    ModRegistry.getInstance().declareClassReplacement(modId, classReplacements);
                }
                
                // Try to discover asset modifications
                String[] assetModifications = invokeStringArrayMethod(scriptClass, script, "getAssetModifications");
                if (assetModifications != null && assetModifications.length > 0) {
                    ModRegistry.getInstance().declareAssetModification(modId, assetModifications);
                }
                
                // Try to discover data modifications
                String[] dataModifications = invokeStringArrayMethod(scriptClass, script, "getDataModifications");
                if (dataModifications != null && dataModifications.length > 0) {
                    ModRegistry.getInstance().declareDataModification(modId, dataModifications);
                }
                
                // Try to discover dependencies
                String[] dependencies = invokeStringArrayMethod(scriptClass, script, "getDependencies");
                if (dependencies != null && dependencies.length > 0) {
                    ModRegistry.getInstance().declareDependency(modId, dependencies);
                }
                
                System.out.println("sosModHooks: Discovered declarations for mod: " + modId);
                
            } catch (Exception e) {
                System.err.println("sosModHooks: Failed to discover declarations for mod " + defaultModId + ": " + e.getMessage());
            }
        }
        
        /**
         * Helper method to invoke a method that returns a String.
         */
        private String invokeStringMethod(Class<?> scriptClass, SCRIPT script, String methodName) {
            try {
                java.lang.reflect.Method method = scriptClass.getDeclaredMethod(methodName);
                method.setAccessible(true);
                Object result = method.invoke(script);
                return result != null ? result.toString() : null;
            } catch (Exception e) {
                // Method doesn't exist or failed - that's okay
                return null;
            }
        }
        
        /**
         * Helper method to invoke a method that returns a String array.
         */
        private String[] invokeStringArrayMethod(Class<?> scriptClass, SCRIPT script, String methodName) {
            try {
                java.lang.reflect.Method method = scriptClass.getDeclaredMethod(methodName);
                method.setAccessible(true);
                Object result = method.invoke(script);
                if (result instanceof String[]) {
                    return (String[]) result;
                }
                return null;
            } catch (Exception e) {
                // Method doesn't exist or failed - that's okay
                return null;
            }
        }
        
        /**
         * Get loaded scripts using the old scanner method for backward compatibility.
         */
        private LIST<SCRIPT> getLoadedScripts() {
            try {
                // Use reflection to access the game's script loading system
                Class<?> scriptLoadClass = Class.forName("script.ScriptLoad");
                java.lang.reflect.Method getAllMethod = scriptLoadClass.getDeclaredMethod("getAll");
                getAllMethod.setAccessible(true);
                
                Object scriptLoads = getAllMethod.invoke(null);
                if (scriptLoads instanceof LIST) {
                    LIST<?> scriptLoadList = (LIST<?>) scriptLoads;
                    snake2d.util.sets.ArrayList<SCRIPT> scripts = new snake2d.util.sets.ArrayList<>();
                    
                    for (Object scriptLoad : scriptLoadList) {
                        try {
                            // Extract the SCRIPT instance from ScriptLoad object
                            java.lang.reflect.Field scriptField = scriptLoad.getClass().getDeclaredField("script");
                            scriptField.setAccessible(true);
                            Object script = scriptField.get(scriptLoad);
                            
                            if (script instanceof SCRIPT) {
                                scripts.add((SCRIPT) script);
                            }
                        } catch (Exception e) {
                            // Skip this script if we can't extract it
                        }
                    }
                    
                    return scripts;
                }
            } catch (Exception e) {
                System.err.println("sosModHooks: Error accessing loaded scripts: " + e.getMessage());
            }
            
            // Fallback to empty list if reflection fails
            return new snake2d.util.sets.ArrayList<>();
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
                ModCompatibilityAPI api = ModCompatibilityAPI.getInstance();
                if (api.hasConflicts()) {
                    text.add(new util.info.INFO("Mod Compatibility", api.getConflictCount() + " conflicts detected"));
                } else {
                    text.add(new util.info.INFO("Mod Compatibility", "All mods compatible"));
                }
            }
        }

        @Override
        public void render(snake2d.Renderer r, float ds) {
            // Render the compatibility overlay if it's visible
            if (reporter.isOverlayVisible()) {
                try {
                    System.out.println("sosModHooks: Rendering overlay");
                    renderCompatibilityOverlay(r);
                    System.out.println("sosModHooks: Overlay rendered successfully");
                } catch (Exception e) {
                    System.err.println("sosModHooks: Error rendering overlay: " + e.getMessage());
                    e.printStackTrace();
                }
            } else if (tickCounter % 300 == 0) { // Log every 5 seconds
                System.out.println("sosModHooks: render() called - overlay not visible");
            }
        }

        @Override
        public void keyPush(KEYS key) {
            // This method is called when a key is pressed
            // We'll handle key detection in the update method instead
        }

        @Override
        public void mouseClick(snake2d.MButt button) {
            // Handle compatibility UI interactions
        }

        @Override
        public void hover(snake2d.util.datatypes.COORDINATE mCoo, boolean mouseHasMoved) {
            // Handle hover interactions
        }

        @Override
        public boolean handleBrokenSavedState() {
            return false;
        }

        private void reportCompatibilityStatus() {
            ModCompatibilityAPI api = ModCompatibilityAPI.getInstance();
            if (api.hasConflicts()) {
                // Removed annoying MessageText alert - keeping only console logging
                System.out.println("sosModHooks: Mod compatibility issues detected - Press F10 for details");
                System.out.println("sosModHooks: Total conflicts: " + api.getConflictCount());
            } else {
                // Removed annoying MessageText alert - keeping only console logging
                System.out.println("sosModHooks: All mods appear compatible!");
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
            
            // Render conflict details if any
            if (scanner.hasConflicts()) {
                renderConflictDetails(r, screenWidth, screenHeight);
            }
            
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
            
            ModCompatibilityAPI api = ModCompatibilityAPI.getInstance();
            
            // Title using game's native heading colors
            util.colors.GCOLOR.T().H1.bind();
            renderText(r, "Mod Compatibility Status", panelX + 10, panelY + 20, 16);
            
            // Status indicator using game's native text colors
            if (api.hasConflicts()) {
                util.colors.GCOLOR.T().WARNING.bind();
                renderText(r, "⚠️ Conflicts Detected", panelX + 10, panelY + 45, 14);
            } else {
                util.colors.GCOLOR.T().IGOOD.bind();
                renderText(r, "✓ All Mods Compatible", panelX + 10, panelY + 45, 14);
            }
            
            // Summary using game's native normal text color
            util.colors.GCOLOR.T().NORMAL.bind();
            renderText(r, "Total Conflicts: " + api.getConflictCount(), panelX + 10, panelY + 65, 12);
            
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
            
            renderText(r, "System Health: " + healthStatus + " (" + healthScore + "/100)", panelX + 10, panelY + 85, 12);
            
            // Performance info using normal text color
            util.colors.GCOLOR.T().NORMAL.bind();
            Map<String, Long> metrics = enhancementManager.getPerformanceMetrics();
            if (metrics.containsKey("memory_percentage")) {
                long memoryPercent = metrics.get("memory_percentage");
                renderText(r, "Memory Usage: " + memoryPercent + "%", panelX + 10, panelY + 105, 10);
            }
            
            snake2d.util.color.COLOR.unbind();
        }
        
        private void renderConflictDetails(snake2d.Renderer r, int screenWidth, int screenHeight) {
            int panelX = screenWidth - 400 - 20;
            int panelY = 20;
            int startY = panelY + 90;
            
            ModCompatibilityAPI api = ModCompatibilityAPI.getInstance();
            snake2d.util.sets.LIST<ModConflict> conflicts = api.getAllConflicts();
            
            // Header using game's native heading colors
            util.colors.GCOLOR.T().H2.bind();
            renderText(r, "Conflict Details:", panelX + 10, startY, 14);
            
            startY += 20;
            
            // Show first few conflicts (limit to avoid overwhelming the UI)
            int maxConflicts = 5;
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
                
                startY += 20;
                conflictCount++;
            }
            
            if (conflicts.size() > maxConflicts) {
                // Additional conflicts info using normal text color
                util.colors.GCOLOR.T().NORMAL.bind();
                renderText(r, "... and " + (conflicts.size() - maxConflicts) + " more", 
                           panelX + 15, startY, 10);
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
    }
}
