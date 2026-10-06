package com.gmail.zariust.otherdrops.data.item.properties;

import com.gmail.zariust.otherdrops.drop.DropType.DropFlags;
import com.gmail.zariust.otherdrops.subject.Target;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.TropicalFish;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.TropicalFishBucketMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** The fish in a tropical fish bucket: fish=PATTERN:BODY_COLOR:PATTERN_COLOR (Ex: fish=KOB:ORANGE:WHITE). */
public class TropicalFishProperty implements ItemProperty {
    private final TropicalFish.Pattern pattern;
    private final DyeColor bodyColor;
    private final DyeColor patternColor;

    public TropicalFishProperty(@NotNull TropicalFish.Pattern pattern, @NotNull DyeColor bodyColor, @NotNull DyeColor patternColor) {
        this.pattern = pattern;
        this.bodyColor = bodyColor;
        this.patternColor = patternColor;
    }

    public static @Nullable ItemProperty parse(@NotNull Material material, @Nullable ItemMeta defaults, @NotNull String entry) {
        if (!(defaults instanceof TropicalFishBucketMeta) || !entry.toLowerCase().startsWith("fish=")) return null;
        String[] parts = entry.substring("fish=".length()).split(":");
        if (parts.length != 3) return null;
        TropicalFish.Pattern pattern = null;
        for (TropicalFish.Pattern p : TropicalFish.Pattern.values())
            if (p.name().equalsIgnoreCase(parts[0].trim())) pattern = p;
        DyeColor body = BannerPatternsProperty.dyeColorOf(parts[1]);
        DyeColor patternColor = BannerPatternsProperty.dyeColorOf(parts[2]);
        return pattern == null || body == null || patternColor == null ? null : new TropicalFishProperty(pattern, body, patternColor);
    }

    public static @Nullable ItemProperty read(@NotNull ItemStack stack) {
        if (!(stack.getItemMeta() instanceof TropicalFishBucketMeta meta) || !meta.hasVariant()) return null;
        return new TropicalFishProperty(meta.getPattern(), meta.getBodyColor(), meta.getPatternColor());
    }

    @Override
    public void applyTo(@NotNull ItemStack stack, @Nullable Target source, @Nullable DropFlags flags) {
        if (stack.getItemMeta() instanceof TropicalFishBucketMeta meta) {
            meta.setPattern(pattern);
            meta.setBodyColor(bodyColor);
            meta.setPatternColor(patternColor);
            stack.setItemMeta(meta);
        }
    }

    @Override
    public boolean matches(@Nullable ItemStack stack) {
        return stack != null && stack.getItemMeta() instanceof TropicalFishBucketMeta meta && meta.hasVariant()
                && meta.getPattern() == pattern && meta.getBodyColor() == bodyColor && meta.getPatternColor() == patternColor;
    }

    @Override
    public @NotNull String describe() {
        return "fish=" + pattern.name() + ":" + bodyColor.name() + ":" + patternColor.name();
    }
}
