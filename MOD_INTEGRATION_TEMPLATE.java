/**
 * MOD INTEGRATION TEMPLATE
 * 
 * This template shows how to integrate your mod with the Mod Compatibility Framework
 * using the reflection-based approach. This method requires NO imports of framework
 * classes and works automatically.
 * 
 * To use this template:
 * 1. Copy these methods into your mod's SCRIPT class
 * 2. Customize the return values for your specific mod
 * 3. The framework will automatically detect and register your mod
 * 4. Your mod will appear in the F10 compatibility overlay
 */

public class YourModScript implements SCRIPT {
    
    // Your existing mod code here...
    
    // ========================================
    // COMPATIBILITY FRAMEWORK INTEGRATION
    // ========================================
    
    /**
     * Returns your mod's unique identifier.
     * Use lowercase letters, numbers, and underscores only.
     * Example: "warhammer_overhaul", "farm_enhancement", "tech_mod"
     */
    public String getModId() {
        return "your_mod_id";
    }
    
    /**
     * Returns your mod's display name.
     * This is what players will see in the compatibility overlay.
     */
    public String getModName() {
        return "Your Mod Name";
    }
    
    /**
     * Returns your mod's version.
     * Use semantic versioning: "1.0.0", "2.1.3", etc.
     */
    public String getModVersion() {
        return "1.0.0";
    }
    
    /**
     * Returns array of core game classes your mod replaces.
     * Only include classes you completely replace, not just modify.
     * Use full class names with package paths.
     */
    public String[] getClassReplacements() {
        return new String[] {
            // Example: "settlement.room.food.farm.FarmInstance",
            // Example: "world.region.RD",
            // Add your actual class replacements here
        };
    }
    
    /**
     * Returns array of asset files your mod modifies.
     * Include sprite files, textures, sounds, etc.
     * Use the same paths that appear in your mod's asset folder.
     */
    public String[] getAssetModifications() {
        return new String[] {
            // Example: "/data/assets/sprite/race/face/addon",
            // Example: "/data/assets/text/event",
            // Example: "/data/assets/init/race/sprite",
            // Add your actual asset modifications here
        };
    }
    
    /**
     * Returns array of data types your mod modifies.
     * Include game data like factions, races, events, etc.
     * Use the same identifiers that appear in your mod's data files.
     */
    public String[] getDataModifications() {
        return new String[] {
            // Example: "FACTION",
            // Example: "RACE", 
            // Example: "EVENT",
            // Example: "TECH",
            // Add your actual data modifications here
        };
    }
    
    /**
     * Returns array of mod IDs your mod requires to function.
     * Only include mods that are absolutely necessary.
     * Use the same mod IDs that other mods return in getModId().
     */
    public String[] getDependencies() {
        return new String[] {
            // Example: "base_mod",
            // Example: "required_framework",
            // Add your actual dependencies here
        };
    }
    
    // ========================================
    // REAL-WORLD EXAMPLES
    // ========================================
    
    /**
     * EXAMPLE: Warhammer Overhaul Mod
     */
    /*
    public String getModId() {
        return "warhammer_overhaul";
    }
    
    public String getModName() {
        return "Warhammer Overhaul";
    }
    
    public String getModVersion() {
        return "2.0.0";
    }
    
    public String[] getClassReplacements() {
        return new String[] {
            "world.region.RD",
            "settlement.entity.humanoid.Humanoid"
        };
    }
    
    public String[] getAssetModifications() {
        return new String[] {
            "/data/assets/sprite/race/face/addon",
            "/data/assets/init/race/sprite",
            "/data/assets/text/event"
        };
    }
    
    public String[] getDataModifications() {
        return new String[] {
            "FACTION", "RACE", "EVENT", "HUMAN"
        };
    }
    
    public String[] getDependencies() {
        return new String[] {
            // No dependencies for this mod
        };
    }
    */
    
    /**
     * EXAMPLE: Farm Enhancement Mod
     */
    /*
    public String getModId() {
        return "farm_enhancement";
    }
    
    public String getModName() {
        return "Farm Enhancement";
    }
    
    public String getModVersion() {
        return "1.5.0";
    }
    
    public String[] getClassReplacements() {
        return new String[] {
            "settlement.room.food.farm.FarmInstance",
            "settlement.room.food.farm.ROOM_FARM"
        };
    }
    
    public String[] getAssetModifications() {
        return new String[] {
            "/data/assets/sprite/room/farm",
            "/data/assets/text/farm_events"
        };
    }
    
    public String[] getDataModifications() {
        return new String[] {
            "FARM", "CROP", "SEASON"
        };
    }
    
    public String[] getDependencies() {
        return new String[] {
            // No dependencies for this mod
        };
    }
    */
    
    /**
     * EXAMPLE: Technology Mod with Dependencies
     */
    /*
    public String getModId() {
        return "advanced_technology";
    }
    
    public String getModName() {
        return "Advanced Technology";
    }
    
    public String getModVersion() {
        return "1.0.0";
    }
    
    public String[] getClassReplacements() {
        return new String[] {
            "init.tech.TECH",
            "init.tech.Knowledge_Costs"
        };
    }
    
    public String[] getAssetModifications() {
        return new String[] {
            "/data/assets/sprite/tech",
            "/data/assets/text/tech_events"
        };
    }
    
    public String[] getDataModifications() {
        return new String[] {
            "TECH", "KNOWLEDGE", "RESEARCH"
        };
    }
    
    public String[] getDependencies() {
        return new String[] {
            "base_tech_framework",  // Requires another mod
            "ui_enhancement"        // Requires UI improvements
        };
    }
    */
    
    // ========================================
    // BENEFITS OF INTEGRATING
    // ========================================
    
    /**
     * By implementing these methods, your mod will:
     * 
     * 1. AUTOMATICALLY REGISTER with the compatibility framework
     * 2. DECLARE ITS CHANGES so other mods can avoid conflicts
     * 3. PROVIDE CLEAR INFORMATION to players about compatibility
     * 4. HELP RESOLVE CONFLICTS before they cause crashes
     * 5. BUILD TRUST with players who know your mod is well-tested
     * 
     * Players will see your mod in the F10 compatibility overlay
     * and get clear information about any potential conflicts.
     */
}
