package sosModHooks;

import script.SCRIPT;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.sprite.text.Font;
import snake2d.util.sprite.text.Text;
import util.colors.GCOLOR;
import view.keyboard.KEYS;
import view.keyboard.Key;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive mod overlay system that displays detailed information about each mod
 * with clickable entries and expandable dropdowns showing file modifications, assets, etc.
 */
public class ComprehensiveModOverlay {
    
    private boolean isVisible = false;
    private final Map<String, Boolean> expandedMods = new HashMap<>();
    private final List<ModEntry> modEntries = new ArrayList<>();
    private int scrollOffset = 0;
    private int maxScrollOffset = 0;
    
    // UI state
    private boolean isDragging = false;
    private int dragStartX, dragStartY;
    private int panelX = 100, panelY = 100;
    private final int panelWidth = 800;
    private final int panelHeight = 600;
    
    // Colors and styling
    private static final int HEADER_HEIGHT = 40;
    private static final int MOD_ENTRY_HEIGHT = 60;
    private static final int DROPDOWN_ENTRY_HEIGHT = 25;
    private static final int SCROLL_BAR_WIDTH = 20;
    
    public ComprehensiveModOverlay() {
        System.out.println("sosModHooks: ComprehensiveModOverlay initialized");
    }
    
    public void toggle() {
        isVisible = !isVisible;
        if (isVisible) {
            refreshModEntries();
        }
        System.out.println("sosModHooks: Comprehensive overlay " + (isVisible ? "shown" : "hidden"));
    }
    
    public boolean isVisible() {
        return isVisible;
    }
    
    public void render(Renderer r) {
        if (!isVisible) return;
        
        try {
            // Render background panel
            renderBackgroundPanel(r);
            
            // Render header
            renderHeader(r);
            
            // Render mod entries
            renderModEntries(r);
            
            // Render scroll bar if needed
            if (maxScrollOffset > 0) {
                renderScrollBar(r);
            }
            
            // Render close button
            renderCloseButton(r);
            
        } catch (Exception e) {
            System.err.println("sosModHooks: Error rendering comprehensive overlay: " + e.getMessage());
        }
    }
    
    private void renderBackgroundPanel(Renderer r) {
        // Main panel background
        GCOLOR.UI().panBG.render(r, panelX, panelX + panelWidth, panelY, panelY + panelHeight);
        
        // Panel border
        GCOLOR.UI().border().render(r, panelX, panelX + panelWidth, panelY, panelY + 1);
        GCOLOR.UI().border().render(r, panelX, panelX + panelWidth, panelY + panelHeight - 1, panelY + panelHeight);
        GCOLOR.UI().border().render(r, panelX, panelX + 1, panelY, panelY + panelHeight);
        GCOLOR.UI().border().render(r, panelX + panelWidth - 1, panelX + panelWidth, panelY, panelY + panelHeight);
    }
    
    private void renderHeader(Renderer r) {
        int headerY = panelY + 10;
        
        // Title
        GCOLOR.T().H1.bind();
        renderText(r, "Comprehensive Mod Analysis", panelX + 20, headerY, 18);
        
        // Subtitle with mod count
        GCOLOR.T().NORMAL.bind();
        ModRegistry registry = ModRegistry.getInstance();
        int totalMods = registry.getActiveMods().size();
        renderText(r, "Active Mods: " + totalMods + " | Press F10 to close", panelX + 20, headerY + 25, 12);
        
        COLOR.unbind();
    }
    
    private void renderModEntries(Renderer r) {
        int startY = panelY + HEADER_HEIGHT + 10;
        int currentY = startY - scrollOffset;
        
        for (ModEntry entry : modEntries) {
            if (currentY + MOD_ENTRY_HEIGHT < panelY) {
                currentY += MOD_ENTRY_HEIGHT;
                continue; // Skip if above visible area
            }
            
            if (currentY > panelY + panelHeight) {
                break; // Stop if below visible area
            }
            
            // Render mod entry
            renderModEntry(r, entry, panelX + 10, currentY);
            
            // Render dropdown if expanded
            if (expandedMods.getOrDefault(entry.modId, false)) {
                currentY += MOD_ENTRY_HEIGHT;
                renderModDropdown(r, entry, panelX + 20, currentY);
                currentY += entry.getDropdownHeight();
            }
            
            currentY += MOD_ENTRY_HEIGHT;
        }
    }
    
    private void renderModEntry(Renderer r, ModEntry entry, int x, int y) {
        // Entry background
        if (entry.isHovered) {
            GCOLOR.UI().panBG.render(r, x, x + panelWidth - 20, y, y + MOD_ENTRY_HEIGHT);
        } else {
            GCOLOR.UI().panBG.render(r, x, x + panelWidth - 20, y, y + MOD_ENTRY_HEIGHT);
        }
        
        // Entry border
        GCOLOR.UI().border().render(r, x, x + panelWidth - 20, y, y + 1);
        GCOLOR.UI().border().render(r, x, x + panelWidth - 20, y + MOD_ENTRY_HEIGHT - 1, y + MOD_ENTRY_HEIGHT);
        
        // Mod name and version
        GCOLOR.T().H2.bind();
        renderText(r, entry.modName + " v" + entry.modVersion, x + 10, y + 15, 16);
        
        // Mod ID
        GCOLOR.T().NORMAL.bind();
        renderText(r, "ID: " + entry.modId, x + 10, y + 35, 12);
        
        // Modification count
        GCOLOR.T().INORMAL.bind();
        renderText(r, entry.modificationCount + " modifications", x + panelWidth - 150, y + 20, 12);
        
        // Expand/collapse indicator
        String indicator = expandedMods.getOrDefault(entry.modId, false) ? "▼" : "▶";
        GCOLOR.T().H2.bind();
        renderText(r, indicator, x + panelWidth - 40, y + 20, 16);
        
        COLOR.unbind();
    }
    
    private void renderModDropdown(Renderer r, ModEntry entry, int x, int y) {
        ModRegistry registry = ModRegistry.getInstance();
        ModRegistry.ModAnalysis analysis = registry.getModAnalysis(entry.modId);
        
        if (analysis == null) {
            GCOLOR.T().IBAD.bind();
            renderText(r, "No analysis available for this mod", x, y, 12);
            COLOR.unbind();
            return;
        }
        
        int currentY = y;
        
        // File modifications
        if (!analysis.getFileModifications().isEmpty()) {
            renderSectionHeader(r, "File Modifications", x, currentY);
            currentY += 20;
            
            for (ModRegistry.FileModification fileMod : analysis.getFileModifications().values()) {
                renderModificationEntry(r, fileMod.getFilePath(), fileMod.getModificationType(), x + 10, currentY);
                currentY += DROPDOWN_ENTRY_HEIGHT;
            }
            currentY += 10;
        }
        
        // Asset modifications
        if (!analysis.getAssetModifications().isEmpty()) {
            renderSectionHeader(r, "Asset Modifications", x, currentY);
            currentY += 20;
            
            for (ModRegistry.AssetModification assetMod : analysis.getAssetModifications().values()) {
                String desc = assetMod.getAssetType() + ": " + assetMod.getAssetPath();
                if (assetMod.isCompleteOverride()) {
                    desc += " (COMPLETE OVERRIDE)";
                }
                renderModificationEntry(r, desc, assetMod.getModificationType(), x + 10, currentY);
                currentY += DROPDOWN_ENTRY_HEIGHT;
            }
            currentY += 10;
        }
        
        // Data modifications
        if (!analysis.getDataModifications().isEmpty()) {
            renderSectionHeader(r, "Data Modifications", x, currentY);
            currentY += 20;
            
            for (ModRegistry.DataModification dataMod : analysis.getDataModifications().values()) {
                String desc = dataMod.getDataType() + " (" + dataMod.getRecordCount() + " records)";
                if (dataMod.isCompleteOverride()) {
                    desc += " - COMPLETE OVERRIDE";
                }
                renderModificationEntry(r, desc, dataMod.getModificationType(), x + 10, currentY);
                currentY += DROPDOWN_ENTRY_HEIGHT;
            }
            currentY += 10;
        }
        
        // Script modifications
        if (!analysis.getScriptModifications().isEmpty()) {
            renderSectionHeader(r, "Script Modifications", x, currentY);
            currentY += 20;
            
            for (ModRegistry.ScriptModification scriptMod : analysis.getScriptModifications().values()) {
                String desc = scriptMod.getClassName();
                if (scriptMod.hasCustomMethods()) {
                    desc += " (Custom Methods)";
                }
                renderModificationEntry(r, desc, scriptMod.getModificationType(), x + 10, currentY);
                currentY += DROPDOWN_ENTRY_HEIGHT;
            }
            currentY += 10;
        }
        
        // Runtime modifications
        if (!analysis.getRuntimeModifications().isEmpty()) {
            renderSectionHeader(r, "Runtime Modifications", x, currentY);
            currentY += 20;
            
            for (ModRegistry.RuntimeModification runtimeMod : analysis.getRuntimeModifications().values()) {
                renderModificationEntry(r, runtimeMod.getTarget(), runtimeMod.getModificationType(), x + 10, currentY);
                currentY += DROPDOWN_ENTRY_HEIGHT;
            }
            currentY += 10;
        }
        
        // Conflicts
        if (!analysis.getConflicts().isEmpty()) {
            renderSectionHeader(r, "Conflicts", x, currentY);
            currentY += 20;
            
            for (String conflict : analysis.getConflicts()) {
                GCOLOR.T().WARNING.bind();
                renderText(r, "⚠ " + conflict, x + 10, currentY, 12);
                currentY += DROPDOWN_ENTRY_HEIGHT;
            }
            COLOR.unbind();
        }
    }
    
    private void renderSectionHeader(Renderer r, String title, int x, int y) {
        GCOLOR.T().H2.bind();
        renderText(r, title + ":", x, y, 14);
        COLOR.unbind();
    }
    
    private void renderModificationEntry(Renderer r, String description, String type, int x, int y) {
        // Description
        GCOLOR.T().NORMAL.bind();
        renderText(r, description, x, y, 12);
        
        // Type (right-aligned)
        GCOLOR.T().INORMAL.bind();
        renderText(r, type, x + panelWidth - 100, y, 12);
        COLOR.unbind();
    }
    
    private void renderScrollBar(Renderer r) {
        int scrollBarX = panelX + panelWidth - SCROLL_BAR_WIDTH;
        int scrollBarY = panelY + HEADER_HEIGHT + 10;
        int scrollBarHeight = panelHeight - HEADER_HEIGHT - 20;
        
        // Scroll bar background
        GCOLOR.UI().panBG.render(r, scrollBarX, scrollBarX + SCROLL_BAR_WIDTH, scrollBarY, scrollBarY + scrollBarHeight);
        
        // Scroll bar handle
        int handleHeight = Math.max(20, (scrollBarHeight * panelHeight) / (maxScrollOffset + panelHeight));
        int handleY = scrollBarY + (scrollBarHeight - handleHeight) * scrollOffset / maxScrollOffset;
        
        GCOLOR.UI().panBG.render(r, scrollBarX, scrollBarX + SCROLL_BAR_WIDTH, handleY, handleY + handleHeight);
    }
    
    private void renderCloseButton(Renderer r) {
        int buttonX = panelX + panelWidth - 30;
        int buttonY = panelY + 10;
        
        // Button background
        GCOLOR.UI().panBG.render(r, buttonX, buttonX + 20, buttonY, buttonY + 20);
        
        // X symbol
        GCOLOR.T().H2.bind();
        renderText(r, "×", buttonX + 5, buttonY + 5, 16);
        COLOR.unbind();
    }
    
    private void renderText(Renderer r, String text, int x, int y, int fontSize) {
        try {
            Font font = init.sprite.UI.UI.FONT().M;
            Text textSprite = (Text) font.getText(text);
            textSprite.render(r, x, y);
        } catch (Exception e) {
            // Fallback: just log the text for debugging
            System.err.println("Could not render text: " + text + " at (" + x + "," + y + ")");
        }
    }
    
    public void handleMouseClick(int mouseX, int mouseY, int button) {
        if (!isVisible) return;
        
        // Check close button
        if (mouseX >= panelX + panelWidth - 30 && mouseX <= panelX + panelWidth - 10 &&
            mouseY >= panelY + 10 && mouseY <= panelY + 30) {
            toggle();
            return;
        }
        
        // Check mod entry clicks
        int startY = panelY + HEADER_HEIGHT + 10;
        int currentY = startY - scrollOffset;
        
        for (ModEntry entry : modEntries) {
            if (mouseX >= panelX + 10 && mouseX <= panelX + panelWidth - 10 &&
                mouseY >= currentY && mouseY <= currentY + MOD_ENTRY_HEIGHT) {
                
                // Toggle expansion
                boolean currentlyExpanded = expandedMods.getOrDefault(entry.modId, false);
                expandedMods.put(entry.modId, !currentlyExpanded);
                
                System.out.println("sosModHooks: Toggled mod " + entry.modName + " expansion to " + !currentlyExpanded);
                return;
            }
            
            currentY += MOD_ENTRY_HEIGHT;
            
            // Skip dropdown area if expanded
            if (expandedMods.getOrDefault(entry.modId, false)) {
                currentY += entry.getDropdownHeight();
            }
        }
    }
    
    public void handleMouseMove(int mouseX, int mouseY) {
        if (!isVisible) return;
        
        // Update hover states
        int startY = panelY + HEADER_HEIGHT + 10;
        int currentY = startY - scrollOffset;
        
        for (ModEntry entry : modEntries) {
            entry.isHovered = (mouseX >= panelX + 10 && mouseX <= panelX + panelWidth - 10 &&
                              mouseY >= currentY && mouseY <= currentY + MOD_ENTRY_HEIGHT);
            
            currentY += MOD_ENTRY_HEIGHT;
            
            if (expandedMods.getOrDefault(entry.modId, false)) {
                currentY += entry.getDropdownHeight();
            }
        }
    }
    
    public void handleMouseWheel(int mouseX, int mouseY, int wheelDelta) {
        if (!isVisible) return;
        
        // Check if mouse is over the mod list area
        if (mouseX >= panelX + 10 && mouseX <= panelX + panelWidth - 30 &&
            mouseY >= panelY + HEADER_HEIGHT + 10 && mouseY <= panelY + panelHeight - 10) {
            
            scrollOffset += wheelDelta * 20; // Scroll speed
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
        }
    }
    
    private void refreshModEntries() {
        modEntries.clear();
        ModRegistry registry = ModRegistry.getInstance();
        
        for (Map.Entry<String, ModRegistry.ActiveModInfo> entry : registry.getActiveMods().entrySet()) {
            String modId = entry.getKey();
            ModRegistry.ActiveModInfo modInfo = entry.getValue();
            
            ModRegistry.ModAnalysis analysis = registry.getModAnalysis(modId);
            int modificationCount = analysis != null ? analysis.getTotalModifications() : 0;
            
            ModEntry modEntry = new ModEntry(modId, modInfo.modName, modInfo.modVersion, modificationCount);
            modEntries.add(modEntry);
        }
        
        // Calculate scroll limits
        int totalHeight = modEntries.size() * MOD_ENTRY_HEIGHT;
        for (ModEntry entry : modEntries) {
            if (expandedMods.getOrDefault(entry.modId, false)) {
                totalHeight += entry.getDropdownHeight();
            }
        }
        
        maxScrollOffset = Math.max(0, totalHeight - (panelHeight - HEADER_HEIGHT - 20));
        scrollOffset = Math.min(scrollOffset, maxScrollOffset);
        
        System.out.println("sosModHooks: Refreshed mod entries, found " + modEntries.size() + " mods");
    }
    
    /**
     * Represents a mod entry in the overlay
     */
    private static class ModEntry {
        final String modId;
        final String modName;
        final String modVersion;
        final int modificationCount;
        boolean isHovered = false;
        
        ModEntry(String modId, String modName, String modVersion, int modificationCount) {
            this.modId = modId;
            this.modName = modName;
            this.modVersion = modVersion;
            this.modificationCount = modificationCount;
        }
        
        int getDropdownHeight() {
            // This would be calculated based on the actual content
            // For now, return a reasonable estimate
            return Math.min(400, modificationCount * 25 + 100);
        }
    }
}
