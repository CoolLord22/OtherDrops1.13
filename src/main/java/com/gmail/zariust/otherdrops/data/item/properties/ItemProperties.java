package com.gmail.zariust.otherdrops.data.item.properties;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.drop.DropType.DropFlags;
import com.gmail.zariust.otherdrops.subject.Target;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Supplier;

/**
 * The registry of item traits, and everything that works on a list of them: parsing config entries, reading a real
 * item, applying, matching and writing back to config text.
 * <p>
 * To add a trait: write the property class (static parse/read + applyTo/matches/describe), then add one register line.
 * Traits that need a newer Bukkit API name the class they need; on older servers they're skipped.
 */
public final class ItemProperties {
    private ItemProperties() {
    }

    private static final List<ItemPropertyType> TYPES = new ArrayList<>();

    static {
        // Order matters for parsing: the first type that accepts an entry wins. Keyed entries (effect=, pattern=…)
        // can't collide; plain words are tried in this order, and head owners accept anything, so they go last.
        register("display text", null, () -> new ItemPropertyType("display text", (material, defaults, entry) -> null, DisplayTextProperty::read));
        register("item flags", null, () -> new ItemPropertyType("item flags", FlagsProperty::parse, FlagsProperty::read));
        register("enchantments", null, () -> new ItemPropertyType("enchantments", EnchantmentsProperty::parse, EnchantmentsProperty::read));
        register("damage", null, () -> new ItemPropertyType("damage", DamageProperty::parse, DamageProperty::read));
        register("potion effects", null, () -> new ItemPropertyType("potion effects", PotionEffectsProperty::parse, PotionEffectsProperty::read));
        register("color", null, () -> new ItemPropertyType("color", ColorProperty::parse, ColorProperty::read));
        register("firework effects", null, () -> new ItemPropertyType("firework effects", FireworkEffectsProperty::parse, FireworkEffectsProperty::read));
        register("firework power", null, () -> new ItemPropertyType("firework power", FireworkPowerProperty::parse, FireworkPowerProperty::read));
        register("book", null, () -> new ItemPropertyType("book", BookProperty::parse, BookProperty::read));
        register("banner patterns", null, () -> new ItemPropertyType("banner patterns", BannerPatternsProperty::parse, BannerPatternsProperty::read));
        register("armor trim", "org.bukkit.inventory.meta.ArmorMeta", () -> new ItemPropertyType("armor trim", ArmorTrimProperty::parse, ArmorTrimProperty::read));
        register("bundle contents", "org.bukkit.inventory.meta.BundleMeta", () -> new ItemPropertyType("bundle contents", BundleContentsProperty::parse, BundleContentsProperty::read));
        register("crossbow projectiles", null, () -> new ItemPropertyType("crossbow projectiles", CrossbowProjectilesProperty::parse, CrossbowProjectilesProperty::read));
        register("map", null, () -> new ItemPropertyType("map", MapProperty::parse, MapProperty::read));
        register("tropical fish", null, () -> new ItemPropertyType("tropical fish", TropicalFishProperty::parse, TropicalFishProperty::read));
        register("spawner mob", null, () -> new ItemPropertyType("spawner mob", SpawnerTypeProperty::parse, SpawnerTypeProperty::read));
        register("head owner", null, () -> new ItemPropertyType("head owner", HeadOwnerProperty::parse, HeadOwnerProperty::read));
    }

    /**
     * Registers a trait. If it needs a Bukkit class that this server doesn't have, it's skipped (logged at HIGHEST).
     * The Supplier delays loading the property class until we know the API exists.
     */
    private static void register(@NotNull String name, @Nullable String requiredBukkitClass, @NotNull Supplier<ItemPropertyType> type) {
        if (requiredBukkitClass != null) {
            try {
                Class.forName(requiredBukkitClass);
            } catch (ClassNotFoundException e) {
                Log.logInfo("Item property '" + name + "' isn't available on this server version (needs " + requiredBukkitClass + "); skipping.", Verbosity.HIGHEST);
                return;
            }
        }
        try {
            TYPES.add(type.get());
        } catch (LinkageError e) {
            Log.logInfo("Item property '" + name + "' couldn't be loaded on this server version (" + e + "); skipping.", Verbosity.HIGHEST);
        }
    }

    public static @NotNull List<ItemPropertyType> types() {
        return Collections.unmodifiableList(TYPES);
    }

    /**
     * Config -> properties. Each entry is one piece after "@" (already split on "!"). List-like traits that appear in
     * several entries are merged. Unrecognised entries are skipped with one warning naming them.
     *
     * @param source the whole item string, for warnings
     */
    public static @NotNull List<ItemProperty> parse(@NotNull Material material, @NotNull List<String> entries, @NotNull String source) {
        List<ItemProperty> result = new ArrayList<>();
        if (entries.isEmpty()) return result;
        ItemMeta defaults = material.isAir() ? null : Bukkit.getItemFactory().getItemMeta(material);

        for (String entry : entries) {
            ItemProperty property = null;
            for (ItemPropertyType type : TYPES) {
                property = type.parser().parse(material, defaults, entry);
                if (property != null) break;
            }
            if (property == null) {
                Log.logWarning("'" + entry + "' isn't a valid property for " + material + " (in '" + source + "'); skipping...");
                continue;
            }
            addMerged(result, property);
        }
        return result;
    }

    /** Real item -> properties: every trait this item has, including name and lore. */
    public static @NotNull List<ItemProperty> read(@NotNull ItemStack stack) {
        List<ItemProperty> result = new ArrayList<>();
        for (ItemPropertyType type : TYPES) {
            ItemProperty property = type.reader().read(stack);
            if (property != null) result.add(property);
        }
        return result;
    }

    /** Applies every property to a new item. */
    public static void applyAll(@NotNull List<ItemProperty> properties, @NotNull ItemStack stack,
                                @Nullable Target source, @Nullable DropFlags flags) {
        for (ItemProperty property : properties) property.applyTo(stack, source, flags);
    }

    /** True if the item has every property. */
    public static boolean matchesAll(@NotNull List<ItemProperty> properties, @Nullable ItemStack stack) {
        for (ItemProperty property : properties) if (!property.matches(stack)) return false;
        return true;
    }

    /** "sharpness#5!HIDE_ENCHANTS!RED": the properties as config text. */
    public static @NotNull String describe(@NotNull List<ItemProperty> properties) {
        StringJoiner joiner = new StringJoiner("!");
        for (ItemProperty property : properties) joiner.add(property.describe());
        return joiner.toString();
    }

    /** A real item as a full config string: MATERIAL@entries~Name;lore (for /od id and /od write). */
    public static @NotNull String toConfigString(@NotNull ItemStack stack) {
        StringJoiner entries = new StringJoiner("!");
        DisplayTextProperty display = null;
        for (ItemProperty property : read(stack)) {
            if (property instanceof DisplayTextProperty text) display = text;   // goes after "~", at the end
            else entries.add(property.describe());
        }
        StringBuilder result = new StringBuilder(stack.getType().name());
        if (entries.length() > 0) result.append('@').append(entries);
        if (display != null) result.append('~').append(display.describe());
        return result.toString();
    }

    private static void addMerged(List<ItemProperty> properties, ItemProperty property) {
        for (int i = 0; i < properties.size(); i++) {
            ItemProperty merged = properties.get(i).merge(property);
            if (merged != null) {
                properties.set(i, merged);
                return;
            }
        }
        properties.add(property);
    }
}
