package com.gmail.zariust.otherdrops.data.item;

import com.gmail.zariust.common.CommonMaterial;
import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.Dependencies;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.config.ConfigSubject;
import com.gmail.zariust.otherdrops.data.item.properties.DisplayTextProperty;
import com.gmail.zariust.otherdrops.data.item.properties.ItemProperties;
import com.gmail.zariust.otherdrops.data.item.properties.ItemProperty;
import com.gmail.zariust.otherdrops.drop.DropType;
import com.gmail.zariust.otherdrops.subject.Target;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ODItem {

    /** Actual ItemStack from a saved namespace/custom item */

    private final @Nullable ItemStack savedItem;

    private final @NotNull Material material;

    private final @NotNull List<ItemProperty> properties;
    private final @NotNull String description;

    private ODItem(@NotNull Material material, @NotNull List<ItemProperty> properties, @Nullable ItemStack savedItem, @NotNull String description) {
        this.material = material;
        this.properties = List.copyOf(properties);
        this.savedItem = savedItem;
        this.description = description;
    }

    /** A plain item of this material, with no properties (Ex: one member of an ANY_ group). */
    public static @NotNull ODItem of(@NotNull Material material) {
        return new ODItem(material, List.of(), null, material.name());
    }

    public static @Nullable ODItem parse(@NotNull ConfigSubject subject) {
        ItemStack saved = findSavedItem(subject);
        if (saved != null) return new ODItem(saved.getType(), List.of(), saved, subject.describe());
        if (isSavedItemIdentifier(subject.getIdentifier())) return null; // already warned

        if (subject.getIdentifier().matches("[0-9]+")) {
            Log.logWarning("'" + subject.describe() + "': numerical item IDs are no longer supported; use the item's name.");
            return null;
        }
        Material material = CommonMaterial.matchMaterial(subject.getIdentifier());
        if (material == null) return null;

        List<ItemProperty> properties = new ArrayList<>(ItemProperties.parse(material, subject.getDataTokens(), subject.describe()));
        if (subject.getDisplayText() != null) properties.add(DisplayTextProperty.parse(subject.getDisplayText()));
        return new ODItem(material, properties, null, subject.describe());
    }

    private static boolean isSavedItemIdentifier(String identifier) {
        return identifier.equals("OD_ITEM") || identifier.equals("MYTHIC_ITEM") || identifier.equals("NAMESPACE_ITEM");
    }

    /** OD_ITEM@key, MYTHIC_ITEM@name, NAMESPACE_ITEM@namespace:key -> the stack, or null (with a warning) if not found. */
    private static @Nullable ItemStack findSavedItem(ConfigSubject subject) {
        String identifier = subject.getIdentifier();
        if (!isSavedItemIdentifier(identifier)) return null;
        String key = subject.getRawData();

        ItemStack loaded = OtherDropsConfig.commonItemstack.getItemStack(identifier + "@" + key); // saved or already loaded
        if (loaded != null) return loaded;

        switch (identifier) {
            case "MYTHIC_ITEM" -> {
                if (Dependencies.getMythicMobs() != null && Dependencies.getMythicMobs().getItemManager().getItem(key).isPresent()) {
                    ItemStack item = Dependencies.getMythicMobs().getItemManager().getItemStack(key);
                    remember("MYTHIC_" + key, item);
                    return item;
                }
                Log.logWarning("Invalid MythicMobs item: " + key);
            }
            case "NAMESPACE_ITEM" -> {
                String[] split = key.toLowerCase().split(":");
                NamespacedKey recipeKey = split.length == 2 ? NamespacedKey.fromString(split[0] + ":" + split[1]) : null;
                Recipe recipe = recipeKey == null ? null : Bukkit.getRecipe(recipeKey);
                if (recipe != null) {
                    ItemStack item = recipe.getResult();
                    remember("NAMESPACE_" + split[0] + "_" + split[1], item);
                    return item;
                }
                Log.logWarning("Invalid namespaced item: " + key + " (use namespace:key of a registered recipe).");
            }
            default -> Log.logWarning("No saved item called '" + key + "' (save one with /od saveitem " + key + ").");
        }
        return null;
    }


    public @NotNull Material getMaterial() {
        return material;
    }

    public @NotNull ItemStack create(int amount, @Nullable Target source, @Nullable DropType.DropFlags flags) {
        if (savedItem != null) { // exact copy: meta, PDC, attributes, components…
            ItemStack copy = savedItem.clone();
            copy.setAmount(amount);
            return copy;
        }
        ItemStack stack = new ItemStack(material, amount);
        ItemProperties.applyAll(properties, stack, source, flags);
        return stack;
    }

    /** Does this real item fit this definition? */
    public boolean matches(@Nullable ItemStack stack) {
        if (stack == null) return false;
        if (savedItem != null) return compare(savedItem, stack, "ODItem/matches");
        return stack.getType() == material && ItemProperties.matchesAll(properties, stack);
    }

    /** Saved items: same type, and the same meta ignoring damage and attribute-modifier UUIDs. */
    public static boolean compare(@NotNull ItemStack primaryItem, @NotNull ItemStack playerItem, String caller) {
        if (primaryItem.getType() != playerItem.getType()) {
            Log.logInfo(caller + " - failed (different materials).", Verbosity.HIGHEST);
            return false;
        }
        if (!primaryItem.hasItemMeta()) {
            Log.logInfo(caller + " - passed (no meta on primary item).", Verbosity.HIGHEST);
            return true;
        }
        if (!playerItem.hasItemMeta()) return false;

        ItemMeta primaryMeta = primaryItem.getItemMeta().clone();
        ItemMeta playerMeta = playerItem.getItemMeta().clone();

        if (playerMeta.hasAttributeModifiers() || primaryMeta.hasAttributeModifiers()) {
            for (Attribute attr : Attribute.values()) {
                Collection<AttributeModifier> mods1 = playerMeta.getAttributeModifiers(attr);
                Collection<AttributeModifier> mods2 = primaryMeta.getAttributeModifiers(attr);
                if (mods1 != null && mods2 != null) {
                    if (!compareModifiersIgnoringUUID(mods1, mods2)) return false;
                } else if (mods1 != null || mods2 != null) {
                    return false; // one has attributes and the other doesn't
                }
            }
        }

        playerMeta.setAttributeModifiers(null);
        primaryMeta.setAttributeModifiers(null);
        if (playerMeta instanceof Damageable d) d.setDamage(0);
        if (primaryMeta instanceof Damageable d) d.setDamage(0);
        return Bukkit.getItemFactory().equals(playerMeta, primaryMeta);
    }

    /** Compares by amount, operation and slot instead of UUID. */
    private static boolean compareModifiersIgnoringUUID(Collection<AttributeModifier> a, Collection<AttributeModifier> b) {
        if (a.size() != b.size()) return false;
        List<AttributeModifier> remaining = new ArrayList<>(b);
        for (AttributeModifier modA : a) {
            boolean matched = remaining.removeIf(modB -> modA.getAmount() == modB.getAmount()
                    && modA.getOperation() == modB.getOperation() && Objects.equals(modA.getSlot(), modB.getSlot()));
            if (!matched) return false;
        }
        return true;
    }

    private static void remember(String identifier, ItemStack item) {
        OtherDrops.loadedItems.put(new NamespacedKey(OtherDrops.plugin, identifier.toLowerCase()), item);
        Log.logInfo("Storing item in memory: " + identifier + " = " + item, Verbosity.HIGHEST);
    }

    @Override
    public String toString() {
        return description;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ODItem item && description.equals(item.description);
    }

    @Override
    public int hashCode() {
        return description.hashCode();
    }
}
