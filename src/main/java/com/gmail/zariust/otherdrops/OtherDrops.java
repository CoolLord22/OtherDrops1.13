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

package com.gmail.zariust.otherdrops;


import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.listener.*;
import com.gmail.zariust.otherdrops.metrics.BStats;
import com.gmail.zariust.otherdrops.options.Weather;
import com.gmail.zariust.otherdrops.parameters.conditions.MoonPhaseCheck;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.io.*;
import java.util.*;

public class OtherDrops extends JavaPlugin {
    public static OtherDrops plugin;
    public static NamespacedKey PLAYER_PLACED;
    boolean enabled;
    public Log log = null;
    public final SectionManager sectionManager;

    public static final List<String> NetherBiomes = new ArrayList<>(Arrays.asList("NETHER", "NETHER_WASTES", "CRIMSON_FOREST", "WARPED_FOREST", "SOUL_SAND_VALLEY", "BASALT_DELTAS"));

    // Global random number generator - used throughout the whole plugin
    public static final Random rng = new Random();

    // Config stuff
    public OtherDropsConfig config = null;
    private BStats metrics;
    public Updater updateChecker;

    public static final Set<NamespacedKey> bossBars = new HashSet<>();
    public static final Map<NamespacedKey, ItemStack> loadedItems = new HashMap<>();

    public OtherDrops() {
        plugin = this;
        PLAYER_PLACED = new NamespacedKey(this, "playerplaced");
        this.sectionManager = new SectionManager(this);
    }

    @Override
    public void onEnable() {
        try {
            Bukkit.getLogger().warning("Beginning legacy material support. This may temporarily delay server startup. This call has been moved to onEnable() to help prevent the lag spike when first BlockTarget interaction was triggered.");
            Bukkit.getWorlds().get(0).getBlockAt(Bukkit.getWorlds().get(0).getSpawnLocation()).getData();
        } catch (Exception ignored) {
        }
        File oldFolder = new File("plugins" + File.separator + "OtherDrops_1.13");
        if (oldFolder.exists()) {
            Bukkit.getLogger().warning("Detected old directory plugins/OtherDrops_1.13! Copying directory to OtherDrops...");
            copyFiles(oldFolder.listFiles());
            Bukkit.getLogger().warning("Successfully copied files. Deleting old directory plugins/OtherDrops_1.13!");
            deleteDirectories(oldFolder);
        }
        initLogger();
        metrics = new BStats(this);
        metrics.registerMetrics();
        Bukkit.getServer().getPluginManager().registerEvents(new ServerStartupListener(this), this);
        Log.logInfo("OtherDrops pre-loading finished, waiting for server startup event to parse config.");
    }

    public void copyFiles(File[] files) {
        for (File file : files) {
            if (file.isDirectory()) {
                copyFiles(file.listFiles());
            } else {
                try {
                    File destDir = new File(file.getPath().replaceAll("OtherDrops_1.13", "OtherDrops"));
                    if (destDir.getParentFile() != null) {
                        destDir.getParentFile().mkdirs();
                    }
                    InputStream in = new FileInputStream(file);
                    OutputStream out = new FileOutputStream(destDir);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) out.write(buffer, 0, length);
                    in.close();
                    out.close();
                } catch (Exception e) {
                    Log.logError("Encountered an error while copying directory to OtherDrops/", e);
                }
            }
        }
    }

    private void deleteDirectories(File dir) {
        if (!dir.isDirectory()) dir.delete();
        if (dir.isDirectory()) {
            if (dir.list().length == 0) dir.delete();
            for (File temp : dir.listFiles()) {
                deleteDirectories(temp);
                if (dir.list().length == 0) dir.delete();
            }
        }
    }

    // Exports known enum lists to text files as this can assist in viewing what values are available to use and/or new values that have
    // been injected by mods - I realise it could be improved a lot but it's better than nothing :)
    public void exportEnumLists() {
        Log.logInfo("OtherDrops printing export lists.", Verbosity.HIGH);
        writeNames(null, "org.bukkit.Material");
        writeNames(null, "org.bukkit.Sound");
        writeNames(null, "org.bukkit.block.Biome");
        writeNames(null, "org.bukkit.entity.EntityType");
        writeNames("Weather", Weather.class);
        writeNames("MoonPhase", MoonPhaseCheck.MoonPhase.class);
        writeNames(null, "org.bukkit.event.entity.CreatureSpawnEvent$SpawnReason");
        writeNames(null, "org.bukkit.event.entity.EntityDamageEvent$DamageCause");
        writeNames(null, "org.bukkit.DyeColor");
        writeNames(null, "org.bukkit.entity.Villager$Profession");
        writeNames(null, "org.bukkit.entity.Villager$Type");
        writeNames("Horse.Color", "org.bukkit.entity.Horse$Color");
        writeNames("Horse.Style", "org.bukkit.entity.Horse$Style");

        writeNames("Axolotl.Variant", "org.bukkit.entity.Axolotl$Variant");
        writeNames("Cat.Type", "org.bukkit.entity.Cat$Type");
        writeNames("Fox.Type", "org.bukkit.entity.Fox$Type");
        writeNames("Frog.Variant", "org.bukkit.entity.Frog$Variant");
        writeNames("Llama.Color", "org.bukkit.entity.Llama$Color");
        writeNames("Parrot.Variant", "org.bukkit.entity.Parrot$Variant");
        writeNames("Rabbit.Type", "org.bukkit.entity.Rabbit$Type");

        File folder = new File("plugins" + File.separator + "OtherDrops");
        BufferedWriter out;
        // Have tried to refactor this out however enchantment class doesn't see to be an true enum so doesn't work with the writeNames method
        try {
            File configFile = new File(folder.getAbsolutePath() + File.separator + "known_lists" + File.separator + "Enchantments" + ".txt");
            configFile.getParentFile().mkdirs();
            configFile.createNewFile();
            out = new BufferedWriter(new FileWriter(configFile));
            for (Enchantment mat : Enchantment.values()) {
                out.write(mat.getKey().toString().replace("minecraft:", "") + "\n");
            }
            out.close();
        } catch (IOException exception) {
            Log.logError("Encountered an error while exporting lists.", exception);
        }

        try {
            File configFile = new File(folder.getAbsolutePath() + File.separator + "known_lists" + File.separator + "PotionEffectType" + ".txt");
            configFile.getParentFile().mkdirs();
            configFile.createNewFile();
            out = new BufferedWriter(new FileWriter(configFile));
            for (PotionEffectType mat : PotionEffectType.values()) {
                if (mat != null) out.write(mat.getName() + "\n");
            }
            out.close();
        } catch (IOException exception) {
            Log.logError("Encountered an error while exporting lists.", exception);
        }

        try {
            File configFile = new File(folder.getAbsolutePath() + File.separator + "known_lists" + File.separator + "Tags" + ".txt");
            configFile.getParentFile().mkdirs();
            configFile.createNewFile();
            out = new BufferedWriter(new FileWriter(configFile));

            String[] registries = {"blocks", "items"};

            for (String registry : registries) {
                Iterable<Tag<Material>> tags = Bukkit.getTags(registry, Material.class);
                for (Tag<Material> tag : tags) {
                    out.write(tag.getKey().toString().toUpperCase().replace("MINECRAFT:", "").replace('/', '_') + "\n");
                }
            }

            Iterable<Tag<EntityType>> tags = Bukkit.getTags("entity_types", EntityType.class);
            for (Tag<EntityType> tag : tags) {
                out.write(tag.getKey().toString().toUpperCase().replace("MINECRAFT:", "").replace('/', '_') + "\n");
            }

            out.close();
        } catch (IOException exception) {
            Log.logError("Encountered an error while exporting lists.", exception);
        }

        CustomMobSupport.exportCustomMobNames(folder);
        CustomMobSupport.exportCustomBlockNames(folder);

        exportServerDetails(folder);
        // Other lists to consider: villageprof, cattype, skeletype
    }

    public static void exportServerDetails(File folder) {
        BufferedWriter out;
        try {
            File configFile = new File(folder.getAbsolutePath() + File.separator + "known_lists" + File.separator + "ServerDetails" + ".txt");
            configFile.getParentFile().mkdirs();
            configFile.createNewFile();
            out = new BufferedWriter(new FileWriter(configFile));

            // Write out details
            out.write("Bukkit Version: " + Bukkit.getBukkitVersion() + "\n");
            out.write("Version: " + Bukkit.getVersion() + "\n");

            out.close();
        } catch (IOException exception) {
            Log.logError("Encountered an error while exporting server details.", exception);
        }
    }

    public static void writeNames(String fileName, String className) {
        try {
            Class<?> cls = Class.forName(className);
            if (cls.isEnum()) {
                if (fileName != null) writeNames(fileName, (Class<? extends Enum<?>>) cls);
                writeNames(cls.getSimpleName(), (Class<? extends Enum<?>>) cls);
            }
        } catch (ClassNotFoundException e) {
            Log.logWarning("Could not find class for known_lists: " + className);
        }
    }

    public static void writeNames(String filename, Class<? extends Enum<?>> e) {
        List<String> list = new ArrayList<>();

        for (Enum<?> stuff : e.getEnumConstants()) {
            list.add(stuff.toString());
        }

        try {
            BufferedWriter out;
            File folder = new File("plugins" + File.separator + "OtherDrops");
            File configFile = new File(folder.getAbsolutePath() + File.separator + "known_lists" + File.separator + filename + ".txt");
            configFile.getParentFile().mkdirs();
            configFile.createNewFile();
            out = new BufferedWriter(new FileWriter(configFile));
            Collections.sort(list);
            for (String mat : list)
                out.write(mat + "\n");
            out.close();
        } catch (IOException exception) {
            Log.logError("Encountered an error while exporting lists.", exception);
        }
    }

    public void registerCommands() {
        this.getCommand("od").setExecutor(new OtherDropsCommand(this));
        this.getCommand("od").setTabCompleter(new OtherDropsTabExecutor());
    }

    public void initConfig() {
        // Create the data folder (if not there already) and load the config
        getDataFolder().mkdirs();
        config = new OtherDropsConfig(this);
        config.load(null); // load global config, dependencies then scan drops file
    }

    private void initLogger() {
        // Set plugin name & version, this must be at the start of onEnable
        // Used in log messages throughout
        this.log = new Log(this);
    }

    public void registerParameters() {
        com.gmail.zariust.otherdrops.parameters.Action.registerDefaultActions();
        com.gmail.zariust.otherdrops.parameters.Condition.registerDefaultConditions();
    }

    @Override
    public void onDisable() {
        for (NamespacedKey bar : bossBars) {
            Bukkit.removeBossBar(bar);
        }
        Log.logInfo("Unloaded.");
    }

    public static void enableOtherDrops() {
        PluginManager pm = Bukkit.getServer().getPluginManager();
        String registered = "Loaded listeners: ";

        pm.registerEvents(new PlayerJoinUpdateChecker(plugin), plugin);

        if (OtherDropsConfig.dropForBlocks) {
            registered += "Block, ";
            pm.registerEvents(new OdBlockListener(plugin), plugin);
            // registered += "PistonListener, ";
            // pm.registerEvents(new OdPistonListener(plugin), plugin);

        }
        if (OtherDropsConfig.dropForCreatures) {
            registered += "Entity, ";
            pm.registerEvents(new OdEntityListener(plugin), plugin);
        }
        if (OtherDropsConfig.dropForInteract || OtherDropsConfig.dropForItemDrop) {
            registered += "Player (left/rightclick/item drop), ";
            pm.registerEvents(new OdPlayerListener(plugin), plugin);
        }
        if (OtherDropsConfig.dropForFishing) {
            registered += "Fishing, ";
            pm.registerEvents(new OdFishingListener(plugin), plugin);
        }
        if (OtherDropsConfig.dropForSpawned) {
            registered += "MobSpawn, ";
            pm.registerEvents(new OdSpawnListener(plugin), plugin);
        }
        if (OtherDropsConfig.dropForRedstoneTrigger) {
            registered += "Redstone, ";
            pm.registerEvents(new OdRedstoneListener(plugin), plugin);
        }
        if (OtherDropsConfig.dropForPlayerJoin) {
            registered += "PlayerJoin, ";
            pm.registerEvents(new OdPlayerJoinListener(plugin), plugin);
        }
        if (OtherDropsConfig.dropForPlayerRespawn) {
            registered += "PlayerRespawn, ";
            pm.registerEvents(new OdPlayerRespawnListener(plugin), plugin);
        }
        if (OtherDropsConfig.dropForPlayerConsume) {
            registered += "PlayerConsume, ";
            pm.registerEvents(new OdPlayerConsumeListener(plugin), plugin);
        }
        if (OtherDropsConfig.dropForPlayerMove) {
            registered += "Playermove, ";
            pm.registerEvents(new OdPlayerMoveListener(plugin), plugin);
        }
        if (OtherDropsConfig.dropForBlockGrow) {
            registered += "BlockGrow, ";
            pm.registerEvents(new OdBlockGrowListener(plugin), plugin);
        }
        if (OtherDropsConfig.dropForProjectileHit) {
            registered += "ProjectileHit, ";
            pm.registerEvents(new OdProjectileHitListener(plugin), plugin);
        }
        if (OtherDropsConfig.dropForBlockPlace) {
            registered += "BlockPlace, ";
            pm.registerEvents(new OdBlockPlaceListener(plugin), plugin);
        }
        if (OtherDropsConfig.dropForJobsLevelUp || OtherDropsConfig.dropForJobsPayment || OtherDropsConfig.dropForJobsExpGain) {
            registered += "JobsListeners, ";
            pm.registerEvents(new JobsListener(plugin), plugin);
        }
        registered += "Vehicle.";
        pm.registerEvents(new OdVehicleListener(plugin), plugin);

        // BlockTo seems to trigger quite often, leaving off unless explicitly enabled for now
        if (OtherDropsConfig.enableBlockTo) {
            // pm.registerEvent(Event.Type.BLOCK_FROMTO, blockListener, config.priority, this);
        }

        plugin.enabled = true;
        Log.logInfo("Register listeners: " + registered, Verbosity.HIGH);
    }

    public static void disableOtherDrops() {
        HandlerList.unregisterAll(plugin);
        plugin.enabled = false;
    }

    public List<String> getGroups(Player player) {
        List<String> foundGroups = new ArrayList<>();
        Set<PermissionAttachmentInfo> permissions = player.getEffectivePermissions();
        for (PermissionAttachmentInfo perm : permissions) {
            String groupPerm = perm.getPermission();
            if (groupPerm.startsWith("group.")) foundGroups.add(groupPerm.substring(6));
            else if (groupPerm.startsWith("groups.")) foundGroups.add(groupPerm.substring(7));
        }
        return foundGroups;
    }

    public static boolean inGroup(Player agent, String group) {
        return agent.hasPermission("group." + group) || agent.hasPermission("groups." + group);
    }
}
