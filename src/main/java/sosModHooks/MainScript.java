package sosModHooks;

import java.nio.file.Path;

import lombok.NoArgsConstructor;
import script.SCRIPT;
import settlement.room.main.util.RoomInitData;
import util.info.INFO;

/**
 * Entry point for the mod.
 * Contains some basic information about the mod.
 * Used to set up your mod.
 *
 * See {@link SCRIPT} for some documentation.
 */
@NoArgsConstructor
@SuppressWarnings("unused") // used by the game via reflection
public final class MainScript implements SCRIPT {

	/**
	 * This info will be displayed when starting a new game and choosing a script
	 */
	private final INFO info = new INFO("sosModHooks", "Mod compatibility framework for Songs of Syx");

	@Override
	public CharSequence name() {
		return "sosModHooks";
	}

	@Override
	public CharSequence desc() {
		return info.desc;
	}

	/**
	 * Called before an actual game is started or loaded
	 */
	@Override
	public void initBeforeGameCreated() {
		System.out.println("sosModHooks: initBeforeGameCreated called");
		
		// Phase 1: Detect which mods are actually activated and loaded by the game
		try {
			System.out.println("sosModHooks: Starting runtime active mod detection...");
			ModRegistry.getInstance().detectActiveMods();
			System.out.println("sosModHooks: Runtime active mod detection complete");
		} catch (Exception e) {
			System.err.println("sosModHooks: Error in runtime active mod detection: " + e.getMessage());
		}
		
		// Key bindings will be initialized later when the KEYS system is ready
		// during the update loop in ModCompatibilityFramework
	}

	/**
	 * Called after the game has been created, but before everything has been tightened
	 */
	@Override
	public void initBeforeGameInited() {
		System.out.println("sosModHooks: initBeforeGameInited called");
		
		// Phase 2: Analyze the runtime effects of active mods and detect conflicts
		try {
			System.out.println("sosModHooks: Starting runtime effects analysis...");
			ModRegistry.getInstance().analyzeRuntimeEffects();
			System.out.println("sosModHooks: Runtime effects analysis complete");
		} catch (Exception e) {
			System.err.println("sosModHooks: Error in runtime effects analysis: " + e.getMessage());
		}
	}


	/**
	 * @return whether mod shall be selectable when starting a new game
	 */
	@Override
	public boolean isSelectable() {
		return false; // Make sosModHooks automatically load without requiring selection
	}

	/**
	 * @return whether mod shall be loaded into existing saves or not
	 */
	@Override
	public boolean forceInit() {
		return SCRIPT.super.forceInit();
	}

	/**
	 * This actually creates the "instance" of your script.
	 */
	@Override
	public SCRIPT_INSTANCE createInstance() {
		ModCompatibilityFramework framework = new ModCompatibilityFramework();
		return framework.new CompatibilityFrameworkInstance();
	}
}