package sosModHooks;

import java.io.IOException;

import script.SCRIPT;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.*;
import util.gui.misc.GBox;
import view.keyboard.KEYS;

final class InstanceScript implements SCRIPT.SCRIPT_INSTANCE {

    private static boolean initialized = false;

    InstanceScript() {
        if (!initialized) {
            try {
                System.out.println("Mod instance created successfully");
                initialized = true;
            } catch (Exception e) {
                System.err.println("Failed to initialize mod instance: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void save(FilePutter file) {
    }
    
    @Override
    public void load(FileGetter file) throws IOException {
    }
    
    @Override
    public void update(double ds) {
    }
    
    @Override
    public void hoverTimer(double mouseTimer, GBox text) {
    }
    
    @Override
    public void render(Renderer renderer, float ds) {
    }
    
    @Override
    public void keyPush(KEYS key) {
    }
    
    @Override
    public void mouseClick(MButt button) {
    }
    
    @Override
    public void hover(COORDINATE mCoo, boolean mouseHasMoved) {
    }
    
    @Override
    public boolean handleBrokenSavedState() {
        return SCRIPT.SCRIPT_INSTANCE.super.handleBrokenSavedState();
    }
}
