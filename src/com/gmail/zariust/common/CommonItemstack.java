package com.gmail.zariust.common;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDrops;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;

public class CommonItemstack {
    OtherDrops plugin;
    File savedItemsFile;
    YamlConfiguration config;

    public CommonItemstack(OtherDrops plugin) {
        this.plugin = plugin;

        savedItemsFile = new File(plugin.getDataFolder(), "ODItems.yml");
        if (!savedItemsFile.exists()) {
            savedItemsFile.getParentFile().mkdirs();
            try {
                savedItemsFile.createNewFile();
            } catch (final IOException e) {
                Log.logWarning("An error occurred while creating ODItems.yml!");
            }
        }
        config = new YamlConfiguration();
        try {
            config.load(savedItemsFile);
        } catch (IOException | InvalidConfigurationException e) {
            Log.logWarning("An error occurred while loading ODItems.yml!");
        }
    }

    public boolean saveItemStack(String key, ItemStack itemStack) {
        config.set(key, itemStack);
        NamespacedKey nkey = new NamespacedKey(plugin, "OD_ITEM_" + key);
        OtherDrops.loadedItems.put(nkey, itemStack);
        try {
            config.save(savedItemsFile);
            return true;
        } catch (final IOException e) {
            Log.logWarning("An error occurred while saving ODItems.yml!");
        }
        return false;
    }

    public void loadItemStacks() {
        int count = 0;
        for(String key : config.getKeys(false)) {
            NamespacedKey nkey = new NamespacedKey(plugin, "OD_ITEM_" + key);
            ItemStack itemStack = config.getItemStack(key);
            OtherDrops.loadedItems.put(nkey, itemStack);
            count++;
        }
        Log.logInfo("Successfully loaded " + count + " ODItems.", Verbosity.NORMAL);
    }

    public ItemStack getItemStack(String key) {
        NamespacedKey nkey = new NamespacedKey(plugin, "OD_ITEM_" + key);
        if(OtherDrops.loadedItems.containsKey(nkey)) {
            return OtherDrops.loadedItems.get(nkey);
        }
        Log.logWarning("ODItems." + nkey + " not found in ODItems.yml!");
        return null;
    }
}
