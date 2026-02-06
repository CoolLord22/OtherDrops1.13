// OtherDrops - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant, Zarius Tularial, Celtic Minstrel
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.	 If not, see <http://www.gnu.org/licenses/>.

package main.java.com.gmail.zariust.otherdrops;

import main.java.com.gmail.zariust.common.CommonItemstack;
import main.java.com.gmail.zariust.common.CommonMaterial;
import main.java.com.gmail.zariust.common.MaterialGroup;
import main.java.com.gmail.zariust.common.Verbosity;
import main.java.com.gmail.zariust.otherdrops.data.Data;
import main.java.com.gmail.zariust.otherdrops.data.SimpleData;
import main.java.com.gmail.zariust.otherdrops.drop.*;
import main.java.com.gmail.zariust.otherdrops.event.CustomDrop;
import main.java.com.gmail.zariust.otherdrops.event.DropsMap;
import main.java.com.gmail.zariust.otherdrops.event.GroupDropEvent;
import main.java.com.gmail.zariust.otherdrops.event.SimpleDrop;
import main.java.com.gmail.zariust.otherdrops.metrics.BStats;
import main.java.com.gmail.zariust.otherdrops.options.*;
import main.java.com.gmail.zariust.otherdrops.parameters.Trigger;
import main.java.com.gmail.zariust.otherdrops.parameters.conditions.MoonPhaseCheck;
import main.java.com.gmail.zariust.otherdrops.special.SpecialResult;
import main.java.com.gmail.zariust.otherdrops.special.SpecialResultHandler;
import main.java.com.gmail.zariust.otherdrops.special.SpecialResultLoader;
import main.java.com.gmail.zariust.otherdrops.subject.*;
import main.java.com.gmail.zariust.otherdrops.subject.Subject.ItemCategory;
import main.java.com.gmail.zariust.otherdrops.things.ODItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.*;
import java.util.*;

import static main.java.com.gmail.zariust.common.CommonPlugin.getConfigVerbosity;
import static main.java.com.gmail.zariust.common.Verbosity.*;

public class OtherDropsConfig {

    private final OtherDrops parent;

    // Our main list of drops
    protected final DropsMap blocksHash;

    // Name of drops file
    private String mainDropsName;

    // Track loaded files so we don't get into an infinite loop
    final Set<String> loadedDropFiles = new HashSet<>();

    // Constants
    public static final String CreatureDataSeparator = "!!";

    // A place for special events to stash options
    private ConfigurationNode events;

    // Triggers configured - these enable the appropriate listeners if a drop config is found using them.
    public static boolean dropForBlocks;            // target type BLOCK or ANY
    public static boolean dropForCreatures;         // target type CREATURE, PLAYER, or ANY
    public static boolean dropForExplosions;        // target type EXPLOSION
    public static boolean dropForInteract;          // LEFT or RIGHT CLICK or PHYSICAL interact
    public static boolean dropForFishing;           // FISH_CAUGHT or FAILED
    public static boolean dropForSpawned;           // config uses spawned:
    public static boolean dropForSpawnTrigger;      // config uses trigger: CREATURESPAWN
    public static boolean dropForRedstoneTrigger;   // POWERUP or POWERDOWN
    public static boolean dropForPlayerJoin;        // PLAYERJOIN
    public static boolean dropForPlayerRespawn;     // PLAYERRESPAWN
    public static boolean dropForPlayerConsume;
    public static boolean dropForPlayerMove;
    public static boolean dropForJobsLevelUp;
    public static boolean dropForJobsPayment;
    public static boolean dropForJobsExpGain;
    public static boolean dropForItemDrop;
    public static boolean dropForBlockGrow;
    public static boolean dropForProjectileHit;

    // Defaults
    protected static Map<Biome, Boolean> defaultBiomes;
    public static Map<MoonPhaseCheck.MoonPhase, Boolean> defaultMoonPhaseLevels;
    protected static Map<String, Boolean> defaultWorlds;
    protected static Map<String, Boolean> defaultRegions;
    protected static Set<String> defaultStructures;
    public static Map<Weather, Boolean> defaultWeather;
    public static Map<Time, Boolean> defaultTime;
    protected static Map<String, Boolean> defaultPermissionGroups;
    protected static Map<String, Boolean> defaultPermissions;
    public static Comparative defaultHeight;
    public static Comparative defaultAttackRange;
    public static Comparative defaultLightLevel;
    public static Boolean defaultFortuneEnhance;
    protected List<Trigger> defaultTrigger;

    // Variables for settings from config.yml
    protected static Verbosity verbosity = Verbosity.NORMAL;
    protected boolean disableEntityDrops;
    public boolean customDropsForExplosions;
    public boolean defaultDropSpread; // determines if dropspread defaults to true or false
    public static boolean enableBlockTo;
    public static boolean disableXpOnNonDefault; // if drops are configured for mobs - disable the xp unless there is a default drop
    public static int moneyPrecision;
    public static boolean enchantmentsUseUnsafe;
    public static boolean enchantmentsIgnoreLevel;
    public static boolean enchantmentsRestrictMatching = true;
    public static boolean spawnTriggerIgnoreOtherDropsSpawn = true;

    public static boolean globalenablewgmatching = false;
    public static boolean globalRedstonewireTriggersSurrounding = true;
    public static boolean globalUpdateChecking = true;
    public static boolean globalFallToGround = true;
    public static boolean globalOverrideExplosionCap = false;
    public static int globalCustomSpawnLimit;
	public static boolean globalCustomBlockBreakToMcmmo;
    public static String gTimeFormat = "HH:mm:ss";
    public static String gDateFormat = "yyyy/MM/dd";
    public static boolean gColorLogMessages = true;
    public static double gActionRadius = 10;
    private boolean globalLootOverridesDefault;
    private boolean globalMoneyOverridesDefault;
    private boolean globalXpOverridesDefault;
    private boolean moneyOverridesDefault;
    private boolean xpOverridesDefault;
    private boolean lootOverridesDefault;
    private boolean globalAllowAnyReplacementBlock;

	public static boolean primedTNTEnabled = false;

    private int dropSections; // for summary after loading config
    private int dropTargets;  // for summary after loading config
    private int dropFailed;   // for summary after loading config

    public static boolean actionParameterFound; // for summary after loading config

    public static boolean dropForBlockPlace;

    public static boolean exportEnumLists;

    // mobs usually compared via ENUMs however some custom mods add multiple ENUMs with same name that fails this match,
    // config option match_mob_by_name_only allows for a string comparison to work around this issue.
    public static boolean matchMobByNameOnly;

    // option to turn off new "get keys deep" option (defaults to on) - getting keys deep allows mod names like
    // "MyMod.Mobname" to work (otherwise OtherDrops sees only "MyMod").
    private boolean configKeysGetDeep;

    // New file to serialize itemstacks
    public static CommonItemstack commonItemstack;

    public OtherDropsConfig(OtherDrops instance) {
        parent = instance;
        blocksHash = new DropsMap();

        dropForBlocks = false;
        dropForCreatures = false;
        dropForInteract = false;
        dropForFishing = false;

        defaultDropSpread = true;

        commonItemstack = new CommonItemstack(parent);
    }

    private void clearDefaults() {
        defaultTrigger = Collections.singletonList(Trigger.BREAK);
        defaultWorlds = null;
        defaultRegions = null;
        defaultStructures = null;
        defaultWeather = null;
        defaultBiomes = null;
        defaultMoonPhaseLevels = null;
        defaultTime = null;
        defaultPermissionGroups = null;
        defaultPermissions = null;
        defaultHeight = null;
        defaultAttackRange = null;
        defaultLightLevel = null;
        defaultFortuneEnhance = null;
    }

    private void clearDropFor() {
        // reset "dropFor" variables before reading config
        dropForBlocks = false;
        dropForCreatures = false;
        dropForInteract = false;
        dropForFishing = false;
        dropForExplosions = false;
        dropForSpawned = false;
        dropForSpawnTrigger = false;
        dropForRedstoneTrigger = false;
        dropForPlayerJoin = false;
        dropForPlayerRespawn = false;
        dropForPlayerConsume = false;
        dropForPlayerMove = false;
        dropForBlockGrow = false;
        dropForBlockPlace = false;
        actionParameterFound = false;
        dropForJobsLevelUp = false;
        dropForJobsPayment = false;
        dropForJobsExpGain = false;
        dropForItemDrop = false;
    }

    // load
    public void load(CommandSender sender) {
        List<String> result = new ArrayList<>();

        try {
            // make sure all files exist, if not export from jar file
            firstRun();
            clearDropFor();
            // load initial config settings, verbosity, etc., this needs to be before dependencies & drops files
            loadConfig();
            // initialize dependencies
            Dependencies.init();
            loadDropsFile(mainDropsName);
            blocksHash.applySorting();

            if (actionParameterFound) result.add("Note - 'action:' parameter is outdated (but still supported) - please use 'trigger:'");
            result.add("Config loaded - total targets: " + this.dropTargets + " sections: " + this.dropSections + " failed: " + this.dropFailed);
            sendMessage(sender, result);
        } catch (FileNotFoundException e) {
            if (verbosity.exceeds(HIGH)) Log.logError("Config file not found error trace:", e);
            result.add("Config file not found!");
            result.add("The error was:\n" + e);
            result.add("You can fix the error and reload with /odr.");
            sendMessage(sender, result);
        } catch (IOException e) {
            if (verbosity.exceeds(HIGH)) Log.logError("IO Error trace:", e);
            result.add("There was an IO error which has forced OtherDrops to abort loading!");
            result.add("The error was:\n" + e);
            result.add("You can fix the error and reload with /odr.");
            sendMessage(sender, result);
        } catch (InvalidConfigurationException e) {
            if (verbosity.exceeds(HIGH)) Log.logError("Invalid configuration exception:", e);
            result.add("Config is invalid!");
            result.add("The error was:\n" + e);
            result.add("You can fix the error and reload with /odr.");
            sendMessage(sender, result);
        } catch (NullPointerException e) {
            if (verbosity.exceeds(HIGH)) Log.logError("Config load error:", e);
            result.add("Config load failed!");
            result.add("The error was:\n" + e);
            result.add("Please try the latest version & report this issue to the developer if the problem remains.");
            sendMessage(sender, result);
        } catch (Exception e) {
            if (verbosity.exceeds(HIGH)) Log.logError("Config load error:", e);
            result.add("Config load failed! Something went wrong.");
            result.add("The error was:\n" + e);
            result.add("If you can fix the error, reload with /odr.");
            sendMessage(sender, result);
        }
        OtherDrops.disableOtherDrops(); // deregister all listeners
        OtherDrops.enableOtherDrops(); // register only needed listeners
    }

    private void sendMessage(CommandSender sender, List<String> result) {
        if (sender != null) {
            sender.sendMessage(result.toArray(new String[0]));
        }

        // Yes, we want to log to console even when /odr is issued by player
        Log.logInfo(result);
    }

    private void firstRun() throws Exception {
        if (!checkIfAllowedToRefreshFiles()) return;

        List<String> files = new ArrayList<>();
        files.add("otherdrops-config.yml");
        files.add("otherdrops-drops.yml");

        files.add("includes/od-dyewool.yml");
        files.add("includes/od-fix_undroppables.yml");
        files.add("includes/od-goldtools-basic.yml");
        files.add("includes/od-goldtools-smelt.yml");
        files.add("includes/od-leaf_overhaul.yml");
        files.add("includes/od-ore_extraction.yml");
        files.add("includes/od-playerdeath_zombie.yml");
        files.add("includes/od-random_examples.yml");
        files.add("includes/od-stop_mob_farms.yml");
        files.add("includes/od-undead_chaos.yml");
        files.add("includes/od-unit_testing.yml");
        files.add("includes/overhaul-catballs.yml");
        files.add("includes/overhaul-zarius.yml");

        files.add("events/Explosions.jar");
        files.add("events/Sheep.jar");
        files.add("events/Trees.jar");
        files.add("events/Weather.jar");

        for (String filename : files) {
            File file = new File(parent.getDataFolder(), filename);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                copy(parent.getResource(filename), file);
            }
        }
    }

    private boolean checkIfAllowedToRefreshFiles() throws IOException, InvalidConfigurationException {
        File file = new File(parent.getDataFolder(), "otherdrops-config.yml");
        if (file.exists()) {
            YamlConfiguration globalConfig = YamlConfiguration.loadConfiguration(file);
            globalConfig.load(file);
            return globalConfig.getBoolean("restore_deleted_config_files", true);
        }
        return true;
    }

    private void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            if (verbosity.exceeds(HIGH)) Log.logError("Encountered an error while copying files:", e);
        }
    }

    public void loadConfig() throws IOException, InvalidConfigurationException {
        this.dropSections = 0;
        this.dropTargets = 0;
        this.dropFailed = 0; // initialise counts
        blocksHash.clear(); // clear here to avoid issues on /obr reloading
        loadedDropFiles.clear();
        clearDefaults();
        OtherDrops.loadedItems.clear(); // Clear loaded items
        commonItemstack.loadItemStacks(); // Load in ODItems

        String filename = "otherdrops-config.yml";
        if (!(new File(parent.getDataFolder(), filename).exists())) filename = "otherblocks-globalconfig.yml"; // Compatibility with old filename
        if (!(new File(parent.getDataFolder(), filename).exists())) filename = "otherdrops-config.yml"; // If old file not found, go back to new name

        File global = new File(parent.getDataFolder(), filename);
        YamlConfiguration globalConfig = YamlConfiguration.loadConfiguration(global);
        // Make sure config file exists (even for reloads - it's possible this did not create successfully or was deleted before reload)
        if (!global.exists()) {
            try {
                global.createNewFile();
                Log.logInfo("Created an empty file " + parent.getDataFolder() + "/" + filename + ", please edit it!");
                globalConfig.set("verbosity", "normal");
                globalConfig.set("priority", "high");
                globalConfig.save(global);
            } catch (IOException ex) {
                Log.logWarning(parent.getDescription().getName() + ": could not generate " + filename + ". Are the file permissions OK?");
            }
        }

        // Load in the values from the configuration file
        globalConfig.load(global);
        String configKeys = globalConfig.getKeys(false).toString();

        verbosity = getConfigVerbosity(globalConfig);
        enableBlockTo = globalConfig.getBoolean("enableblockto", false);
        moneyPrecision = globalConfig.getInt("money-precision", 2);
        customDropsForExplosions = globalConfig.getBoolean("customdropsforexplosions", false);
        defaultDropSpread = globalConfig.getBoolean("default_dropspread", true);
        disableXpOnNonDefault = globalConfig.getBoolean("disable_xp_on_non_default", true);
        enchantmentsIgnoreLevel = globalConfig.getBoolean("enchantments_ignore_level", false);
        enchantmentsUseUnsafe = globalConfig.getBoolean("enchantments_use_unsafe", false);
        enchantmentsRestrictMatching = globalConfig.getBoolean("enchantments_restrict_matching", true);

        configKeysGetDeep = globalConfig.getBoolean("config_keys_get_deep", true);

        spawnTriggerIgnoreOtherDropsSpawn = globalConfig.getBoolean("spawntrigger_ignores_otherdrops_spawn", true);
        globalenablewgmatching = globalConfig.getBoolean("enable_wg_matching", false);
        globalLootOverridesDefault = globalConfig.getBoolean("loot_overrides_default", true);
        globalMoneyOverridesDefault = globalConfig.getBoolean("money_overrides_default", false);
        globalXpOverridesDefault = globalConfig.getBoolean("xp_overrides_default", false);

        matchMobByNameOnly = globalConfig.getBoolean("match_mob_by_name_only", true);

        exportEnumLists = globalConfig.getBoolean("export_enum_lists", true);
        globalFallToGround = globalConfig.getBoolean("item_falls_on_ground", true);
        globalAllowAnyReplacementBlock = globalConfig.getBoolean("allow_any_replacementblock", false);
        globalRedstonewireTriggersSurrounding = globalConfig.getBoolean("redstonewire_triggers_surrounding", true);
        globalUpdateChecking = globalConfig.getBoolean("update_checker", true);
        primedTNTEnabled = globalConfig.getBoolean("primed_tnt", false);
        globalOverrideExplosionCap = globalConfig.getBoolean("override_explosion_cap", false);
        globalCustomSpawnLimit = globalConfig.getInt("custom_spawn_limit", 150);

        gTimeFormat = globalConfig.getString("time_format", "HH:mm:ss");
        gDateFormat = globalConfig.getString("date_format", "yyyy/MM/dd");

        gColorLogMessages = globalConfig.getBoolean("color_log_messages", true);
        gActionRadius = globalConfig.getInt("action_radius", 10);
        globalCustomBlockBreakToMcmmo = globalConfig.getBoolean("send_customblockbreak_to_mcmmo", true);

        mainDropsName = globalConfig.getString("rootconfig", "otherdrops-drops.yml");
        if (!(new File(parent.getDataFolder(), mainDropsName).exists()) && new File(parent.getDataFolder(), "otherblocks-globalconfig.yml").exists())
            mainDropsName = "otherblocks-globalconfig.yml"; // Compatibility with old filename

        events = new ConfigurationNode(globalConfig.getConfigurationSection("events"));

        // Warn if DAMAGE_WATER is enabled
        if (enableBlockTo) Log.logWarning("blockto/damage_water enabled - BE CAREFUL");

        try {
            SpecialResultLoader.loadEvents();
        } catch (Exception except) {
            Log.logWarning("Event files failed to load - this shouldn't happen, please inform developer.");
            if (verbosity.exceeds(HIGH)) Log.logError("Event files failed to load:", except);
        }

        Log.logInfo("Loaded global config (" + global + "), keys found: " + configKeys + " (verbosity=" + verbosity + ")", Verbosity.HIGHEST);
    }

    private void loadDropsFile(String filename) throws Exception {
        // Check for infinite include loops
        if (loadedDropFiles.contains(filename)) {
            Log.logWarning("Infinite include loop detected at " + filename);
            return;
        } else loadedDropFiles.add(filename);

        Log.logInfo("Loading file: " + filename, NORMAL);

        File yml = new File(parent.getDataFolder(), filename);
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(yml);
        } catch (IOException | InvalidConfigurationException e) {
            throw e;
        }

        // Make sure config file exists (even for reloads - it's possible this did not create successfully or was deleted before reload)
        if (!yml.exists()) {
            try {
                yml.createNewFile();
                Log.logInfo("Created an empty file " + parent.getDataFolder() + "/" + filename + ", please edit it!");
                config.set("otherdrops", null);
                config.set("include-files", null);
                config.set("defaults", null);
                config.set("aliases", null);
                config.set("configversion", 3);
                config.save(yml);
            } catch (IOException ex) {
                Log.logWarning(parent.getDescription().getName() + ": could not generate " + filename + ". Are the file permissions OK?");
            }
            // Nothing to load in this case, so exit now
            return;
        }

        // Warn if wrong version
        int configVersion = config.getInt("configversion", 3);
        if (configVersion < 3) Log.logWarning("config file appears to be in older format; some things may not work");
        else if (configVersion > 3)
            Log.logWarning("config file appears to be in newer format; some things may not work");

        // Load defaults; each of these functions returns null if the value isn't found
        Map<String, Object> map = new HashMap<>();
        ConfigurationNode defaultsNode = null;
        if (config.getConfigurationSection("defaults") == null) {
            config.getMapList("defaults");
            if (!config.getMapList("defaults").isEmpty())
                defaultsNode = ConfigurationNode.parse(config.getMapList("defaults")).get(0);
        } else {
            Log.logInfo("list: " + config.getConfigurationSection("defaults").getKeys(true), Verbosity.NORMAL);
            ConfigurationSection defaultsSection = config.getConfigurationSection("defaults");
            for (String key : config.getConfigurationSection("defaults").getKeys(true)) {
                map.put(key, defaultsSection.get(key));
            }
            defaultsNode = new ConfigurationNode(map);
        }

        clearDefaults();
        loadModuleDefaults(defaultsNode);
        // else Log.logInfo("Loading defaults: none found.", Verbosity.HIGH);

        // Load the drops
        ConfigurationSection node = config.getConfigurationSection("otherdrops");
        Set<String> blocks = null;
        if (node != null) blocks = node.getKeys(configKeysGetDeep);

        if (node == null) { // Compatibility
            node = config.getConfigurationSection("otherblocks");
            if (node != null) blocks = node.getKeys(configKeysGetDeep);
        }
        if (node != null) {
            Log.logInfo("Loading keys: " + blocks, HIGHEST);

            for (Object blockNameObj : blocks.toArray()) {
                String blockName;
                blockName = blockNameObj.toString();

                if (blockNameObj instanceof Integer) {
                    Log.logWarning("Integer target: " + blockName + " (cannot process - please enclose in quotation marks eg. \"" + blockName + "\")");
                    this.dropFailed++;
                    continue;
                }

                Log.logInfo("Loading drop: " + blockName, HIGH);

                // convert spaces and dashes to underscore before parsing to allow more flexible matching
                Target target = parseTarget(blockName); //TODO: See why replaceAll("[ -]", "_") was needed here
                if (target == null) {
                    Log.logWarning("Unrecognized target (skipping): " + blockName, Verbosity.NORMAL);
                    this.dropFailed++;
                    continue;
                }
                switch (target.getType()) {
                    case BLOCK:
                        dropForBlocks = true;
                        break;
                    case PLAYER, CREATURE:
                        dropForCreatures = true;
                        break;
                    case EXPLOSION:
                        dropForExplosions = true;
                        break;
                    case SPECIAL: // used for "ANY" drops - TODO: add specific categories for ANY_CREATURE and ANY_BLOCK
                        dropForBlocks = true;
                        dropForCreatures = true;
                        break;
                    default:
                        // If you want to have other similar flags, add them above the default Possibilities are DAMAGE,
                        // PROJECTILE, SPECIAL (but special isn't used for anything) (The default is here so I don't get
                        // an "incomplete switch" warning.)
                }

                List<ConfigurationNode> drops = ConfigurationNode.parse(node.getMapList(blockName));

                // Check if drop contains actual mappings or just a string
                Object nodeValue = node.get(blockName);
                if (drops.isEmpty() && nodeValue != null) {
                    // This section supports "TARGET: [drops]" short-format by grabbing the string/list/map and stashing it in a mapping to the drop parameter
                    String parameterName = "drop";
                    if (nodeValue instanceof String stringNodeVal) {
                        if (stringNodeVal.matches("[0-9~.-]+")) {
                            parameterName = "money";
                        }
                    } else if (nodeValue instanceof Integer || nodeValue instanceof Float || nodeValue instanceof Double) {
                        parameterName = "money";
                    }

                    Map<String, Object> dropMap = new HashMap<>();
                    dropMap.put(parameterName, nodeValue);
                    drops = new ArrayList<>();
                    drops.add(new ConfigurationNode(dropMap));
                }

                loadBlockDrops(drops, blockName, target);

                this.dropTargets++;
                // Future modulized parameters parsing
                /*
                 * for (Map<String, Object> drop : blockNode) { for (String
                 * parameter : drop.keySet()) { String parameterKey =
                 * parameter.split(".")[0]; Parameters.parse(parameterKey); } }
                 */
                // OtherDrops.logInfo("Loading config... blocknode:"+blockNode.toString()
                // +" for blockname: "+originalBlockName);
                // Set<String> drops = null;
                // if (blockNode != null) drops = blockNode.getKeys(false);
                // loadBlockDrops(blockNode, blockName, target, node);
            }
        }

        // Load the include files
        List<String> includeFiles = config.getStringList("include-files");
        for (String include : includeFiles)
            loadDropsFile(include);
    }

    protected void loadModuleDefaults(ConfigurationNode defaults) {
        // Check for null - it's possible that the defaults key doesn't exist or is empty
        defaultTrigger = Collections.singletonList(Trigger.BREAK);
        lootOverridesDefault = globalLootOverridesDefault;
        xpOverridesDefault = globalXpOverridesDefault;
        moneyOverridesDefault = globalMoneyOverridesDefault;

        if (defaults != null) {
            Log.logInfo("Loading defaults... nodemap=" + defaults, HIGH);
            defaultWorlds = parseWorldsFrom(defaults);
            defaultRegions = parseRegionsFrom(defaults);
            defaultStructures = parseStructuresFrom(defaults);
            defaultWeather = Weather.parseFrom(defaults, null);
            defaultBiomes = parseBiomesFrom(defaults);
            defaultMoonPhaseLevels = parseMoonPhaseFrom(defaults);
            defaultTime = Time.parseFrom(defaults, null);
            defaultPermissionGroups = parseGroupsFrom(defaults);
            defaultPermissions = parsePermissionsFrom(defaults);
            defaultHeight = Comparative.parseFrom(defaults, "height", null);
            defaultAttackRange = Comparative.parseFrom(defaults, "attackrange", null);
            defaultLightLevel = Comparative.parseFrom(defaults, "lightlevel", null);
            defaultFortuneEnhance = defaults.getBoolean("fortune_enhance", null);
            defaultTrigger = Trigger.parseFrom(defaults, defaultTrigger);

            lootOverridesDefault = defaults.getBoolean("loot_overrides_default", globalLootOverridesDefault);
            moneyOverridesDefault = defaults.getBoolean("money_overrides_default", globalMoneyOverridesDefault);
            xpOverridesDefault = defaults.getBoolean("xp_overrides_default", globalXpOverridesDefault);
        } else Log.logInfo("No defaults set.", HIGHEST);
    }

    private void loadBlockDrops(List<ConfigurationNode> drops, String blockName, Target target) {
        for (ConfigurationNode dropNode : drops) {
            boolean isGroup = dropNode.getKeys().contains("dropgroup");
            List<Trigger> triggers;
            List<Trigger> leafdecayTrigger = new ArrayList<>();
            leafdecayTrigger.add(Trigger.LEAF_DECAY);
            if (blockName.equalsIgnoreCase("SPECIAL_LEAFDECAY")) {
                triggers = Trigger.parseFrom(dropNode, leafdecayTrigger);
            } else {
                triggers = Trigger.parseFrom(dropNode, defaultTrigger);
            }

            if (triggers.isEmpty()) {
                // FIXME: Find a way to say which trigger was invalid
                Log.logWarning("No recognized trigger for block " + blockName + "; skipping (known triggers: " + Trigger.getValidActions() + ")", NORMAL);
                continue;
            }
            for (Trigger trigger : triggers) {
                if (trigger.equals(Trigger.HIT) && target.getType() == ItemCategory.CREATURE)
                    BStats.incrementTriggerCounts("HIT_MOB");
                else if (trigger.equals(Trigger.HIT) && target.getType() == ItemCategory.BLOCK)
                    BStats.incrementTriggerCounts("HIT_BLOCK");

                // show difference between mob death and block break for Metrics
                if (trigger.equals(Trigger.BREAK) && target.getType() == ItemCategory.CREATURE)
                    BStats.incrementTriggerCounts("MOB_DEATH");
                else if (trigger.equals(Trigger.BREAK) && target.getType() == ItemCategory.BLOCK)
                    BStats.incrementTriggerCounts("BLOCK_BREAK");
                else BStats.incrementTriggerCounts(trigger.toString());

                // Register "dropForInteract"
                if (trigger.equals(Trigger.HIT) || trigger.equals(Trigger.RIGHT_CLICK) || trigger.equals(Trigger.PHYSICAL)) {
                    dropForInteract = true;
                } else if (trigger.equals(Trigger.FISH_CAUGHT) || trigger.equals(Trigger.FISH_FAILED)) {
                    dropForFishing = true;
                } else if (trigger.equals(Trigger.MOB_SPAWN)) {
                    dropForSpawned = true; // sets the spawnevent to be listened
                    dropForSpawnTrigger = true; // allows spawnevents to launch a drop
                } else if (trigger.equals(Trigger.POWER_UP) || trigger.equals(Trigger.POWER_DOWN)) {
                    dropForRedstoneTrigger = true; // allows redstone power events to launch a drop
                } else if (trigger.equals(Trigger.PLAYER_JOIN)) {
                    dropForPlayerJoin = true; // allows this event to launch a drop
                } else if (trigger.equals(Trigger.PLAYER_RESPAWN)) {
                    dropForPlayerRespawn = true; // allows this event to launch a drop
                } else if (trigger.equals(Trigger.CONSUME_ITEM)) {
                    dropForPlayerConsume = true; // allows this event to launch a drop
                } else if (trigger.equals(Trigger.PLAYER_MOVE)) {
                    dropForPlayerMove = true;
                } else if (trigger.equals(Trigger.BLOCK_GROW)) {
                    dropForBlockGrow = true;
                } else if (trigger.equals(Trigger.PROJECTILE_HIT_BLOCK)) {
                    dropForProjectileHit = true;
                } else if (trigger.equals(Trigger.BLOCK_PLACE)) {
                    dropForBlockPlace = true;
                } else if (trigger.equals(Trigger.JOBS_LEVEL_UP)) {
                    dropForJobsLevelUp = true;
                } else if (trigger.equals(Trigger.JOBS_EXP_GAIN)) {
                    dropForJobsExpGain = true;
                } else if (trigger.equals(Trigger.JOBS_PAYMENT)) {
                    dropForJobsPayment = true;
                } else if (trigger.equals(Trigger.ITEM_DROP)) {
                    dropForItemDrop = true;
                }
                // TODO: This reparses the same drop once for each listed trigger; a way that involves parsing only
                // once? Would require having the drop class implement clone().
                CustomDrop drop = loadDrop(dropNode, target, trigger, isGroup);
                if (drop.getTool() == null || drop.getTool().isEmpty()) {
                    // FIXME: Should find a way to report the actual invalid tool as well
                    // FIXME: Also should find a way to report when some tools are valid and some are not
                    Log.logWarning("Unrecognized tool for block " + blockName + "; skipping.", NORMAL);
                    continue;
                }
                blocksHash.addDrop(drop);
            }
        }
    }

    private CustomDrop loadDrop(ConfigurationNode dropNode, Target target, Trigger trigger, boolean isGroup) {
        CustomDrop drop = isGroup ? new GroupDropEvent(target, trigger) : new SimpleDrop(target, trigger);
        loadConditions(dropNode, drop);
        if (isGroup) loadDropGroup(dropNode, (GroupDropEvent) drop, target, trigger);
        else loadSimpleDrop(dropNode, (SimpleDrop) drop);

        // Only allow PrimedTNT from mobs - very DANGEROUS for blocks (chain reactions)
        // This has to be set here rather than in loadsimpledrop as we need to know the target
        if (drop instanceof SimpleDrop) {
            if (((SimpleDrop) drop).getDropped() instanceof CreatureDrop cDrop) {
                if (cDrop.getCreature() == EntityType.PRIMED_TNT) {
                    if (!(target instanceof CreatureSubject) && !(primedTNTEnabled)) {
                        ((SimpleDrop) drop).setDropped(null);
                        Log.logWarning("DANGER: primedtnt not allowed to drop from blocks (a chain reaction can kill your server), drop removed. To enable these, check the config!", Verbosity.LOW);
                    }
                }
            }
        }
        return drop;
    }

    private void loadConditions(ConfigurationNode node, CustomDrop drop) {
        drop.addActions(main.java.com.gmail.zariust.otherdrops.parameters.Action.parseNodes(node));
        drop.addConditions(main.java.com.gmail.zariust.otherdrops.parameters.Condition.parseNodes(node));

        // Read tool
        drop.setTool(parseAgentFrom(node));
        setDropFilter(node, drop);

        // Now read the stuff that might have a default; if null is returned, use the default
        drop.setFlags(Flag.parseFrom(node));

        // Check if drop has fortune enhancer enabled
        drop.setFortuneEnhance(node.getBoolean("fortune_enhance", defaultFortuneEnhance));

        // Read chance, delay, etc
        drop.setChance(parseChanceFrom(node, "chance"));
        drop.setWeight(parseWeightFrom(node, drop.toString()));

        if (drop.getWeight() > 0) {
            drop.setWeighted(true);
        }

        Object exclusive = node.get("exclusive");
        if (exclusive != null) drop.setExclusiveKey(exclusive.toString());

        // Note: playerrespawn requires minimum delay of 1
        if (drop.getTrigger() == Trigger.PLAYER_RESPAWN) {
            drop.setDelay(IntRange.parse(node.getString("delay", "1")));
        } else {
            drop.setDelay(IntRange.parse(node.getString("delay", "0")));
        }
    }

    public static double parseChanceFrom(ConfigurationNode node, String key) {
        String chanceString = node.getString(key, null);
        double chance;
        if (chanceString == null) {
            chance = 100;
        } else {
            try {
                chance = Double.parseDouble(chanceString.replaceAll("%$", ""));
            } catch (NumberFormatException ex) {
                chance = 100;
            }
        }
        return chance;
    }

    public static double parseWeightFrom(ConfigurationNode node, String dropName) {
        String weightString = node.getString("weight", null);
        double weight;
        if (weightString == null) {
            weight = -1;
        } else {
            try {
                weight = Double.parseDouble(weightString);
                if (weight < 0) Log.logWarning("Negative weight specified for " + dropName + ", this will NOT be weighted...");
            } catch (NumberFormatException ex) {
                weight = -1;
            }
        }
        return weight;
    }

    private Location parseLocationFrom(ConfigurationNode node, String type, double d, double defY, double e) {
        String loc = getStringFrom(node, "loc-" + type, type + "loc");
        if (loc == null) return new Location(null, d, defY, e);
        double x = 0, y = 0, z = 0;
        String[] split = loc.split("/");
        if (split.length == 3) {
            try {
                x = Double.parseDouble(split[0]);
                y = Double.parseDouble(split[1]);
                z = Double.parseDouble(split[2]);
            } catch (NumberFormatException ex) {
                x = y = z = 0;
            }
        }
        return new Location(null, x, y, z);
    }

    private void loadSimpleDrop(ConfigurationNode node, SimpleDrop drop) {
        this.dropSections++;

        // Read drop
        @SuppressWarnings("unused") boolean deny = false;
        String dropStr = node.getString("drop", "UNSPECIFIED"); // default value
        // should be NOTHING (DEFAULT will break some configs) FIXME: it should really be a third option - NOTAPPLICABLE, ie. doesn't change the drop
        dropStr = dropStr.replaceAll("[ -]", "_");
        if (dropStr.equalsIgnoreCase("DENY")) { // TODO: allow DENY to be
            // detected in a list (eg. [DENY, SHEEP])
            drop.setDenied(true);
            // deny = true; // set to DENY (used later to set replacement block to null)
			// drop.setDropped(new ItemDrop(Material.AIR)); set the drop to NOTHING
        } else drop.setDropped(DropType.parseFrom(node));

        setDefaultOverride(drop.getDropped());

        if (drop.getDropped() != null)
            Log.logInfo(drop.getTrigger() + " " + drop.getTarget() + " w/ " + drop.getTool() + " -> " + drop.getDropped().toString(), HIGH);
        else
            Log.logInfo("Loading drop (null: failed or default drop): " + drop.getTrigger() + " with " + drop.getTool() + " on " + drop.getTarget() + " -> \"" + dropStr + "\"", HIGHEST);

        String quantityStr = node.getString("quantity");
        if (quantityStr == null) drop.setQuantity(1);
        else drop.setQuantity(DoubleRange.parse(quantityStr));
        // Damage
        drop.setToolDamage(ToolDamage.parseFrom(node));

        // to avoid replacement tools triggering immediately on right click....
        if (drop.getTrigger() == Trigger.RIGHT_CLICK) {
            if (drop.getToolDamage() != null && drop.getToolDamage().isReplacement()) {
                if (drop.getDelay().getMax() == 0) {
                    drop.setDelay(1);
                    Log.logInfo("...replacetool & rightclick found, adding 'delay:1' to avoid triggering with replaced item.", Verbosity.HIGHEST);
                }
            }
        }

        drop.setDropSpread(node, "dropspread", defaultDropSpread);// Spread chance
        drop.setReplacement(parseReplacement(node)); // Replacement block
        drop.setRandomLocMult(parseLocationFrom(node, "randomise", 0, 0, 0)); // Random location multiplier
        drop.setLocationOffset(parseLocationFrom(node, "offset", 0, 0, 0)); // Location offset
		// Commands, messages, sound effects
        drop.setCommands(getMaybeList(node, "command", "commands"));
        drop.setMessages(getMaybeList(node, "message", "messages"));
        drop.setEffects(SoundEffect.parseFrom(node));
        // Events
        List<SpecialResult> dropEvents = SpecialResult.parseFrom(node);
        if (dropEvents == null) return; // We're done! Note, this means any new options must go above events!
        dropEvents.removeIf(event -> !event.canRunFor(drop));
        drop.setEvents(dropEvents);
    }

    private void setDefaultOverride(DropType dropped) {
        if (dropped == null) return;

        if (dropped instanceof MoneyDrop) {
            dropped.overrideDefault = moneyOverridesDefault;
        } else if (dropped instanceof ExperienceDrop) {
            dropped.overrideDefault = xpOverridesDefault;
        } else if (dropped instanceof DropListExclusive) {
			((DropListExclusive) dropped).getGroup().forEach(this::setDefaultOverride);
        } else if (dropped instanceof DropListInclusive) {
			((DropListInclusive) dropped).getGroup().forEach(this::setDefaultOverride);
        } else {
            dropped.overrideDefault = lootOverridesDefault;
        }
    }

    private void loadDropGroup(ConfigurationNode node, GroupDropEvent group, Target target, Trigger trigger) {
        group.setName(node.getString("dropgroup", ""));
        if (!node.getKeys().contains("drops")) {
            Log.logWarning("Empty drop group " + group.getName() + "; will have no effect!");
            return;
        }
        Log.logInfo("Loading drop group: " + group.getTrigger() + " with " + group.getTool() + " on " + group.getTarget() + " -> " + group.getName(), HIGHEST);
        group.setMessages(getMaybeList(node, "message", "messages"));

        List<ConfigurationNode> drops = node.getNodeList("drops", null);
		drops.forEach(dropNode -> {
			boolean isGroup = dropNode.getKeys().contains("dropgroup");
			CustomDrop drop = loadDrop(dropNode, target, trigger, isGroup);
			group.add(drop);
		});
        group.sort();
    }

    public static List<String> getMaybeList(ConfigurationNode node, String... keys) {
        if (node == null) return new ArrayList<>();
        Object prop = null;
        String key = null;
        for (String s : keys) {
            key = s;
            prop = node.get(key);
            if (prop != null) break;
        }
        List<String> list;
        if (prop == null) return new ArrayList<>();
        else if (prop instanceof List) list = node.getStringList(key);
        else list = Collections.singletonList(prop.toString());
        return list;
    }

    public static String getStringFrom(ConfigurationNode node, String... keys) {
        String prop = null;
        for (String key : keys) {
            prop = node.getString(key);
            if (prop != null) break;
        }
        return prop;
    }

    private BlockTarget parseReplacement(ConfigurationNode node) {
        String blockName = getStringFrom(node, "replacementblock", "replaceblock", "replace");
        if (blockName == null) return null;
        String[] split = blockName.split("@");
        String name = split[0];
        String dataStr = split.length > 1 ? split[1] : "";
        Material mat;
        if (name.matches("[0-9]+")) Log.logWarning("Error while parsing: " + name + ". Support for numerical IDs has been dropped!");

        mat = Material.getMaterial(name.toUpperCase());
        if (mat == null) return null;

        if (!mat.isBlock() && !(this.globalAllowAnyReplacementBlock)) {
            Log.logWarning("Error in 'replacementblock' - " + mat + " is not a block-type.");
            return null;
        }

        if (dataStr.isEmpty()) return new BlockTarget(mat);
        Data data;
        try {
            int intData = Integer.parseInt(dataStr);
            return new BlockTarget(mat, intData);
        } catch (NumberFormatException e) {
            try {
                data = SimpleData.parse(mat, dataStr);
            } catch (IllegalArgumentException ex) {
                Log.logWarning(ex.getMessage());
                return null;
            }
        }
        if (data == null) return new BlockTarget(mat);
        return new BlockTarget(mat, data);
    }

    public static Map<String, Boolean> parseWorldsFrom(ConfigurationNode node) {
        List<String> worlds = getMaybeList(node, "world", "worlds");
        List<String> worldsExcept = getMaybeList(node, "worldexcept", "worldsexcept");
        if (worlds.isEmpty() && worldsExcept.isEmpty()) return defaultWorlds;
        Map<String, Boolean> result = new HashMap<>();
        result.put(null, containsAll(worlds));
        for (String name : worlds) {
            if (name.equalsIgnoreCase("ALL") || name.equalsIgnoreCase("ANY")) {
                result.put(null, true);
                continue;
            }
            if (name.startsWith("-")) {
                String actualName = name.substring(1);
                result.put(null, true); // Indicates "all worlds except..."
                result.put(actualName, false);

                if (Bukkit.getServer().getWorld(actualName) == null) {
                    Log.logWarning("Invalid world " + name + "; may not function as expected...");
                }
            } else {
                result.put(name, true);

                if (Bukkit.getServer().getWorld(name) == null) {
                    Log.logWarning("Invalid world " + name + "; may not function as expected...");
                }
            }
        }
        for (String name : worldsExcept) {
            if (Bukkit.getServer().getWorld(name) == null) {
                Log.logWarning("Invalid world " + name + "; may not function as expected...");
            }
            result.put(null, true);
            result.put(name, false);
        }
        return result;
    }

    public static Map<Biome, Boolean> parseBiomesFrom(ConfigurationNode node) {
        return parseMaybeBiomesFrom(node, "biome", "biomes");
    }

    public static Map<Biome, Boolean> parseFishingBiomesFrom(ConfigurationNode node) {
        return parseMaybeBiomesFrom(node, "hookbiome", "hookbiomes", "fishhookbiome", "fishhookbiomes");
    }

    public static Map<Biome, Boolean> parseMaybeBiomesFrom(ConfigurationNode node, String... keys) {
        List<String> biomes = getMaybeList(node, keys);
        if (biomes.isEmpty()) return defaultBiomes;
        HashMap<Biome, Boolean> result = new HashMap<>();
        result.put(null, containsAll(biomes));
        for (String name : biomes) {
            name = name.toUpperCase();
            boolean biomeNegated = false;
            boolean matched = false;

            if (name.startsWith("-")) {
                result.put(null, true);
                biomeNegated = true;
                name = name.substring(1);
            }
            // TODO: write some tests
            for (Biome biomeMatch : Biome.values()) {
                if (name.equalsIgnoreCase(biomeMatch.name())) {
                    result.put(biomeMatch, !biomeNegated);
                    matched = true;
                    break;
                } else if (name.equalsIgnoreCase("ALL") || name.equalsIgnoreCase("ANY")) {
                    result.put(null, true);
                    matched = true;
                    break;
                }
                Log.logInfo("Biome match: checking " + name + " against " + biomeMatch.name() + ", match = " + matched, HIGHEST);
            }
            if (!matched) {
                Log.logWarning("Invalid biome " + name + "; skipping...");
            }
        }
        return result;
    }

    public static Map<MoonPhaseCheck.MoonPhase, Boolean> parseMoonPhaseFrom(ConfigurationNode node) {
        List<String> moonPhases = getMaybeList(node, "moon", "moons", "moonphase", "moonphases");
        if (moonPhases.isEmpty()) return defaultMoonPhaseLevels;
        HashMap<MoonPhaseCheck.MoonPhase, Boolean> result = new HashMap<>();
        result.put(null, containsAll(moonPhases));
        for (String name : moonPhases) {
            name = name.toUpperCase();
            boolean moonPhaseNegated = false;
            boolean matched = false;

            if (name.startsWith("-")) {
                result.put(null, true);
                moonPhaseNegated = true;
                name = name.substring(1);
            }

            for (MoonPhaseCheck.MoonPhase moonPhaseMatch : MoonPhaseCheck.MoonPhase.values()) {
                if (name.equalsIgnoreCase(moonPhaseMatch.name())) {
                    result.put(moonPhaseMatch, !moonPhaseNegated);
                    matched = true;
                    break;
                } else if (name.equalsIgnoreCase("ALL") || name.equalsIgnoreCase("ANY")) {
                    result.put(null, true);
                    matched = true;
                    break;
                }
                Log.logInfo("Moon phase match: checking " + name + " against " + moonPhaseMatch.name() + ", match = " + matched, HIGHEST);
            }
            if (!matched) {
                Log.logWarning("Invalid moon phase " + name + "; skipping...");
            }
        }
        return result;
    }

    public static Map<String, Boolean> parseGroupsFrom(ConfigurationNode node) {
        List<String> groups = getMaybeList(node, "permissiongroup", "permissiongroups");
        List<String> groupsExcept = getMaybeList(node, "permissiongroupexcept", "permissiongroupsexcept");
        return getBooleanMap(groups, groupsExcept, defaultPermissionGroups);
    }

    public static Map<String, Boolean> parsePermissionsFrom(ConfigurationNode node) {
        List<String> permissions = getMaybeList(node, "permission", "permissions");
        List<String> permissionsExcept = getMaybeList(node, "permissionexcept", "permissionsexcept");
        return getBooleanMap(permissions, permissionsExcept, defaultPermissions);
    }

    public static Map<String, Boolean> parseRegionsFrom(ConfigurationNode node) {
        List<String> regions = getMaybeList(node, "region", "regions");
        List<String> regionsExcept = getMaybeList(node, "regionexcept", "regionsexcept");
        return getBooleanMap(regions, regionsExcept, defaultRegions);
    }

    public static Set<String> parseStructuresFrom(ConfigurationNode node) {
        List<String> structure = getMaybeList(node, "structure", "structures");
        if (structure.isEmpty()) return defaultStructures;
        return new HashSet<>(structure);
    }

    private static Map<String, Boolean> getBooleanMap(List<String> pos, List<String> neg, Map<String, Boolean> def) {
        if (pos.isEmpty() && neg.isEmpty()) return def;
        Map<String, Boolean> result = new HashMap<>();
        for (String name : pos) {
            if (name.startsWith("-")) {
                result.put(name, false);
            } else result.put(name, true);
        }
        for (String name : neg) {
            result.put(name, false);
        }
        return result;
    }

    public static boolean containsAll(List<String> list) {
        for (String str : list) {
            if (str.equalsIgnoreCase("ALL") || str.equalsIgnoreCase("ANY")) return true;
        }
        return false;
    }

    public static void setDropFilter(ConfigurationNode node, CustomDrop drop) {
        Set<ODItem> itemsToFilter = new HashSet<>();
        boolean toKeepContents = false; // Default is contents remove, meaning we DON'T keep them
        List<String> filterList = OtherDropsConfig.getMaybeList(node, "drops.remove");
        if (filterList.isEmpty()) { // contentskeep is not read in if contentsremove is found
            filterList.addAll(OtherDropsConfig.getMaybeList(node, "drops.keep"));
            toKeepContents = true; // Since contentskeep is found, we WANT to keep
        }

        if (!filterList.isEmpty()) {
            for (String entry : filterList) {
                ODItem item = ODItem.parseItem(entry);
                if (item.itemStack != null || item.getMaterial() != null) {
                    itemsToFilter.add(item);
                }
            }
            drop.setDropsFilter(itemsToFilter);
            drop.setToKeepDrops(toKeepContents);
        }
    }

    public static Map<Agent, Boolean> parseAgentFrom(ConfigurationNode node) {
        List<String> tools = OtherDropsConfig.getMaybeList(node, "agent", "agents", "tool", "tools");
        List<String> toolsExcept = OtherDropsConfig.getMaybeList(node, "agentexcept", "agentsexcept", "toolexcept", "toolsexcept");
        Map<Agent, Boolean> toolMap = new HashMap<>();
        if (tools.isEmpty()) {
            toolMap.put(parseAgent("ALL"), true); // no tool defined - default to all
        } else {
            for (String tool : tools) {
                Agent agent;
                boolean flag = true;
                if (tool.startsWith("-")) {
                    agent = parseAgent(tool.substring(1));
                    flag = false;
                } else agent = parseAgent(tool);
                if (agent != null) toolMap.put(agent, flag);
            }
            for (String tool : toolsExcept) {
                Agent agent = parseAgent(tool);
                if (agent != null) toolMap.put(agent, false);
            }
        }
        return toolMap;
    }

    public static Agent parseAgent(String agent) {
        ODItem item = ODItem.parseItem(agent);
        String name = item.name;
        String upperName = name.toUpperCase();
        String data = item.getDataString();

        // Agent can be one of the following
        // - A tool; ie, a Material constant
        // - One of the Material synonyms NOTHING and DYE
        // - A MaterialGroup constant
        // - One of the special wildcards ANY, ANY_CREATURE, ANY_DAMAGE
        // - A DamageCause constant prefixed by DAMAGE_
        // - DAMAGE_FIRE_TICK and DAMAGE_CUSTOM are valid but not allowed
        // - DAMAGE_WATER is invalid but allowed, and stored as CUSTOM
        // - A EntityType constant prefixed by CREATURE_
        // - A projectile; ie a Material constant prefixed by PROJECTILE_
        if (MaterialGroup.isValid(name) || upperName.startsWith("ANY") || upperName.equals("ALL")) return AnySubject.parseAgent(name);
        else if (upperName.equals("PLAYER")) return PlayerSubject.parse(data);
        else if (upperName.startsWith("MYTHIC_MOB")) return MythicMobSubject.parse(data);
        else if (upperName.equals("PLAYERGROUP")) return new GroupSubject(data);
        else if (upperName.startsWith("DAMAGE_")) return EnvironmentAgent.parse(name, data);
        else {
            LivingSubject creatureSubject = CreatureSubject.parse(name, data, item.getDisplayName());

            if (creatureSubject != null) return creatureSubject;
            else if (upperName.startsWith("PROJECTILE")) return ProjectileAgent.parse(name, data);
            else if (upperName.startsWith("EXPLOSION")) return ExplosionAgent.parse(name, data);
            else return ToolAgent.parse(item);
        }
    }

    public static Target parseTarget(String blockName) {
        blockName = CommonMaterial.substituteAlias(blockName);

        ODItem item = ODItem.parseItem(blockName);
        String name = item.name;
        String upperName = item.name.toUpperCase();
        String data = item.getDataString();
        // Target name is one of the following:
        // - A Material constant that is a block, painting, or vehicle
        // - A EntityType constant prefixed by CREATURE_
        // - An integer representing a Material
        // - One of the keywords PLAYER or PLAYERGROUP
        // - Vehicle starting with VEHICLE (note: BOAT, MINECART, etc. can only be vehicles in a target so process accordingly)
        // - A MaterialGroup constant containing blocks
        if (upperName.equals("PLAYER")) return PlayerSubject.parse(data);
        else if (upperName.equals("PLAYERGROUP")) return new GroupSubject(data);
        else if (MaterialGroup.isValid(name) || upperName.startsWith("ANY") || upperName.equals("ALL")) return AnySubject.parseTarget(upperName);
        else if (upperName.startsWith("MYTHIC_MOB")) return MythicMobSubject.parse(data);
        else if (upperName.startsWith("VEHICLE") || upperName.matches("BOAT|MINECART|BOAT_SPRUCE|BOAT_JUNGLE|BOAT_BIRCH|BOAT_ACACIA|BOAT_DARK_OAk")) return VehicleTarget.parse(Material.getMaterial(upperName.replaceAll("VEHICLE_", "")), data);
        else {
            LivingSubject creatureSubject = CreatureSubject.parse(name, data, item.getDisplayName());

            if (creatureSubject != null) return creatureSubject;
            else if (upperName.equalsIgnoreCase("SPECIAL_LEAFDECAY")) return BlockTarget.parse("LEAVES", data, item.displayname); // for compatibility
            else return BlockTarget.parse(name, data, item.displayname);
        }
    }

    public ConfigurationNode getEventNode(SpecialResultHandler event) {
        String name = event.getName();
        if (events == null) {
            Log.logInfo("EventLoader (" + name + ") failed to get config-node, events is null.", HIGH);
            return null;
        }
        ConfigurationNode node = events.getConfigurationNode(name);
        if (node == null) {
            events.set(name, new HashMap<>());
            node = events.getConfigurationNode(name);
        }

        return node;
    }

    public static Verbosity getVerbosity() {
        return verbosity;
    }

    public static void setVerbosity(Verbosity verbosity) {
        OtherDropsConfig.verbosity = verbosity;
    }
}