package com.gmail.zariust.otherdrops.data.item.properties;

import com.gmail.zariust.otherdrops.drop.DropType.DropFlags;
import com.gmail.zariust.otherdrops.subject.Target;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A color: a dye color (RED, light blue…) or hex (#3366FF).
 * Leather armor and leather horse armor (and wolf armor on newer versions, via ColorableArmorMeta, which extends
 * LeatherArmorMeta): dye color. Potions: liquid color. Filled maps: map item color.
 * Firework rockets/stars: a simple effect in that color (shorthand; see FireworkEffectsProperty for full effects).
 */
public class ColorProperty implements ItemProperty {
    private final Color color;

    public ColorProperty(@NotNull Color color) {
        this.color = color;
    }

    public static @Nullable ItemProperty parse(@NotNull Material material, @Nullable ItemMeta defaults, @NotNull String entry) {
        if (!(defaults instanceof LeatherArmorMeta || defaults instanceof PotionMeta || defaults instanceof MapMeta
                || defaults instanceof FireworkMeta || defaults instanceof FireworkEffectMeta)) return null;
        Color color = parseColor(entry);
        return color == null ? null : new ColorProperty(color);
    }

    public static @Nullable ItemProperty read(@NotNull ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta instanceof LeatherArmorMeta leather) {
            Color color = leather.getColor();
            return color.equals(Bukkit.getItemFactory().getDefaultLeatherColor()) ? null : new ColorProperty(color);
        }
        if (meta instanceof PotionMeta potion && potion.hasColor()) return new ColorProperty(potion.getColor());
        if (meta instanceof MapMeta map && map.hasColor()) return new ColorProperty(map.getColor());
        return null; // fireworks are read by FireworkEffectsProperty
    }

    /** "#RRGGBB", or a dye color in any case with spaces, dashes or underscores. Null if neither. */
    public static @Nullable Color parseColor(@NotNull String text) {
        String value = text.trim();
        if (value.matches("#[0-9a-fA-F]{6}")) return Color.fromRGB(Integer.parseInt(value.substring(1), 16));
        String name = value.toUpperCase().replaceAll("[\\s-]", "_");
        for (DyeColor dye : DyeColor.values())
            if (dye.name().equals(name)) return dye.getColor();
        return null;
    }

    /** The dye color's name if it is exactly one, otherwise #RRGGBB. */
    public static @NotNull String describeColor(@NotNull Color color) {
        for (DyeColor dye : DyeColor.values())
            if (dye.getColor().equals(color)) return dye.name();
        return String.format("#%06X", color.asRGB());
    }

    @Override
    public void applyTo(@NotNull ItemStack stack, @Nullable Target source, @Nullable DropFlags flags) {
        ItemMeta meta = stack.getItemMeta();
        if (meta instanceof LeatherArmorMeta leather) leather.setColor(color);
        else if (meta instanceof PotionMeta potion) potion.setColor(color);
        else if (meta instanceof MapMeta map) map.setColor(color);
        else if (meta instanceof FireworkMeta firework) firework.addEffect(FireworkEffect.builder().withColor(color).build());
        else if (meta instanceof FireworkEffectMeta star) star.setEffect(FireworkEffect.builder().withColor(color).build());
        else return;
        stack.setItemMeta(meta);
    }

    @Override
    public boolean matches(@Nullable ItemStack stack) {
        ItemMeta meta = stack == null ? null : stack.getItemMeta();
        if (meta instanceof LeatherArmorMeta leather) return color.equals(leather.getColor());
        if (meta instanceof PotionMeta potion) return color.equals(potion.getColor());
        if (meta instanceof MapMeta map) return color.equals(map.getColor());
        if (meta instanceof FireworkMeta firework)
            return firework.getEffects().stream().anyMatch(effect -> effect.getColors().contains(color));
        if (meta instanceof FireworkEffectMeta star) return star.hasEffect() && star.getEffect().getColors().contains(color);
        return false;
    }

    @Override
    public @NotNull String describe() {
        return describeColor(color);
    }
}
