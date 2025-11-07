package com.gmail.zariust.otherdrops.things;

import com.gmail.zariust.common.CMEnchantment;
import com.gmail.zariust.common.CommonEnchantments;
import com.gmail.zariust.common.CommonMaterial;
import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.Dependencies;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.ItemData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ODItem {
    public String name;
    private String dataString;
    public String enchantmentString;
    public List<CMEnchantment> enchantments = new ArrayList<>();
    public List<ItemFlag> itemFlags = new ArrayList<>();
    public String displayname;
    public final List<String> lore = new ArrayList<>();
    public ItemStack itemStack;

    private Material material;
    private Data data;

    public ODItem() {
    }

    public ODItem(ItemStack itemStack, String identifier) {
        this.itemStack = itemStack;
        this.name = identifier;
    }

    public static ODItem parseItem(String drop, String defaultData) {
        ItemStack loadedItem = OtherDropsConfig.commonItemstack.getItemStack(drop);
        if (loadedItem != null) {
            return new ODItem(loadedItem, drop);
        }
        if (drop.toUpperCase().startsWith("MYTHIC_ITEM@")) {
            String input = drop.replaceAll("MYTHIC_ITEM@", "");
            String itemIdentifier = "MYTHIC_" + input;

            if (Dependencies.getMythicMobs().getItemManager().getItem(input).isPresent()) {
                loadedItem = Dependencies.getMythicMobs().getItemManager().getItemStack(input);
                OtherDrops.loadedItems.put(new NamespacedKey(OtherDrops.plugin, itemIdentifier), loadedItem);
                Log.logInfo("Saving item: " + loadedItem, Verbosity.HIGHEST);
                return new ODItem(loadedItem, itemIdentifier);
            }
            Log.logWarning("Invalid MythicItem: " + input);
        } else if (drop.toUpperCase().startsWith("NAMESPACE_ITEM@")) {
            String input = drop.replaceAll("NAMESPACE_ITEM@", "");
            String[] inputSplit = input.toLowerCase().split(":");

            if (inputSplit.length == 2) {
                String itemIdentifier = "NAMESPACE_" + inputSplit[0] + "_" + inputSplit[1];

                NamespacedKey recipeKey = NamespacedKey.fromString(inputSplit[0] + ":" + inputSplit[1]);
                if (recipeKey != null) {
                    Recipe recipe = Bukkit.getRecipe(recipeKey);
                    if (recipe != null) {
                        loadedItem = recipe.getResult();

                        OtherDrops.loadedItems.put(new NamespacedKey(OtherDrops.plugin, itemIdentifier), loadedItem);
                        Log.logInfo("Saving item: " + loadedItem, Verbosity.HIGHEST);
                        return new ODItem(loadedItem, itemIdentifier);
                    }
                }
            }
            Log.logWarning("Invalid registered namespace item identifier: " + input);
        }

        ODItem item = new ODItem();
        item.dataString = defaultData;

        String[] firstSplit = drop.split("[@:;~]", 2);
        if (firstSplit.length > 1) {
            // if extra fields are found, parse them - firstly separating out the type of "thing" this is
            item.name = firstSplit[0];
            String firstChar = drop.substring(item.name.length(), item.name.length() + 1);
            if (firstChar.matches("[^~]")) {
                // only want to use a semi-colon rather than @ or : but preserve the ~
                firstChar = ";";
            } else if (firstChar.matches("~")) {
                item.displayname = "";
            }
            drop = firstChar + firstSplit[1];

            // check for initial data value and enchantment to support old format
            if (drop.matches("([;])([^;!]+)!.*")) {
                Log.dMsg("PARSING INTIAL DATA");
                String[] dataEnchSplit = drop.split("!", 2);
                item.dataString = dataEnchSplit[0].substring(1);
                drop = ";" + dataEnchSplit[1];

            }

            // then, loop through each ";<value>" or "~<value>" pair and parse accordingly
            Pattern p = Pattern.compile("([~;])([^~;]+)");
            Matcher m = p.matcher(drop);
            while (m.find()) {
                String key = m.group(1);
                String value = m.group(2);
                value = value.replaceAll("slashCharPlaceholder", "/");

                if (key != null) {
                    if (key.equals("~")) {
                        item.displayname = ChatColor.translateAlternateColorCodes('&', value);
                    } else if (item.displayname != null && !item.displayname.isEmpty()) {// displayname found, treat next as lore
                        value = ChatColor.translateAlternateColorCodes('&', value);
                        item.lore.add(value);
                    } else if (getItemFlag(value) != null) {
                        item.itemFlags.add(getItemFlag(value));
                    } else { // first check for enchantment
                        List<CMEnchantment> ench = CommonEnchantments.parseEnchantments(value);
                        if (ench.isEmpty()) {
                            item.dataString = value; // otherwise assume data
                        } else {
                            item.enchantments.addAll(ench);
                        }
                    }
                }
            }
        } else {
            item.name = drop;
        }

        if (drop.endsWith("~")) item.displayname = "";

        return item;
    }

    public Material getMaterial() {
        if (this.material == null) {
            if (this.name.matches("[0-9]+")) {
                Log.logWarning("Error while parsing: " + this.name + ". Support for numerical IDs has been dropped!");
                Log.logWarning("The drop has been disabled to prevent issues!");
            } else {
                material = CommonMaterial.matchMaterial(this.name);
            }
        }
        return this.material;
    }

    public String getDataString() {
        return (dataString == null ? "" : dataString);
    }

    public Data getData() {
        if (data == null && dataString != null) {
            if (dataString.equals("!")) dataString = "";

            // Parse data, which could be an integer or an appropriate enum
            // name
            this.data = parseDataFromString(this.dataString);
        }
        return data;
    }

    public Data parseDataFromString(String dataString) {
        Data returnVal = null;
        try {
            int d = Integer.parseInt(dataString);
            returnVal = new ItemData(d);
        } catch (NumberFormatException ignored) {
        }
        if (returnVal == null) {
            try {
                returnVal = ItemData.parse(this.getMaterial(), dataString);
                if (returnVal == null) returnVal = new ItemData(0);
            } catch (IllegalArgumentException e) {
                Log.logWarning(e.getMessage());
            }
        }
        return returnVal;
    }

    public String getDisplayName() {
        return displayname;
    }

    public List<CMEnchantment> getEnchantments() {
        if (enchantments == null) {
            if (enchantmentString == null) {
                enchantments = new ArrayList<>();
            } else {
                enchantments = CommonEnchantments.parseEnchantments(enchantmentString);
            }
        }
        return enchantments;
    }

    public static ODItem parseItem(String blockName) {
        return parseItem(blockName, "");
    }

    private static ItemFlag getItemFlag(String flagName) {
        for (ItemFlag value : ItemFlag.values()) {
            if (value.toString().replaceAll("_", "").equalsIgnoreCase(flagName.replaceAll("_", ""))) return value;
        }
        Log.logInfo("ODItem Parsing: ItemFlag not found: " + flagName, Verbosity.HIGH);
        return null;
    }

    public boolean matches(ItemStack playerItem) {
        if (playerItem != null) {
            if (this.itemStack != null) {
                if (playerItem.getType() != this.itemStack.getType()) { // if the two materials are not equal
                    Log.logInfo("ODItem matches - failed (different materials).", Verbosity.HIGHEST);
                    return false;
                } else if (!this.itemStack.hasItemMeta()) { // if compare item has no custom data, the check should pass
                    Log.logInfo("ODItem matches - passed (no meta on comparison item).", Verbosity.HIGHEST);
                    return true;
                } else { // compare item has meta, lets check that it matches the player's item
                    if (!playerItem.hasItemMeta()) // player item had no meta
                        return false;
                    ItemMeta thisMeta = playerItem.getItemMeta();
                    ItemMeta stackMeta = this.itemStack.getItemMeta();
                    ((Damageable) thisMeta).setDamage(0);
                    ((Damageable) stackMeta).setDamage(0);
                    Log.logInfo("ODItem matches - returned value: " + Bukkit.getItemFactory().equals(thisMeta, stackMeta), Verbosity.HIGHEST);
                    return Bukkit.getItemFactory().equals(thisMeta, stackMeta);
                }
            } else {
                if (playerItem.getType().equals(this.getMaterial())) {
                    boolean isContained = true;
                    if (this.displayname != null)
                        if (!this.displayname.equals(playerItem.getItemMeta().getDisplayName())) isContained = false;

                    if (isContained && !this.lore.isEmpty())
                        if (!this.lore.equals(playerItem.getItemMeta().getLore())) isContained = false;

                    if (isContained && !this.getEnchantments().isEmpty())
                        if (playerItem.getEnchantments().isEmpty()) isContained = false;
                        else
                            isContained = CommonEnchantments.matches(this.getEnchantments(), playerItem.getEnchantments());

                    return isContained;
                }
            }
        }
        return false;
    }
}