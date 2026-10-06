package com.gmail.zariust.otherdrops.data.item.properties;

import com.gmail.zariust.otherdrops.drop.DropType.DropFlags;
import com.gmail.zariust.otherdrops.subject.Target;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Banner patterns, in order: pattern=COLOR:TYPE (Ex: pattern=RED:STRIPE_TOP!pattern=WHITE:CROSS).
 * The banner's base color is its material (RED_BANNER). Pattern types are Spigot's PatternType names.
 * As a tool: the banner must have exactly these patterns in this order.
 */
public class BannerPatternsProperty implements ItemProperty {
    private final List<Pattern> patterns;

    public BannerPatternsProperty(@NotNull List<Pattern> patterns) {
        this.patterns = List.copyOf(patterns);
    }

    public static @Nullable ItemProperty parse(@NotNull Material material, @Nullable ItemMeta defaults, @NotNull String entry) {
        if (!(defaults instanceof BannerMeta) || !entry.toLowerCase().startsWith("pattern=")) return null;
        String[] parts = entry.substring("pattern=".length()).split(":", 2);
        if (parts.length < 2) return null;
        DyeColor color = dyeColorOf(parts[0]);
        PatternType type = patternTypeOf(parts[1]);
        return color == null || type == null ? null : new BannerPatternsProperty(List.of(new Pattern(color, type)));
    }

    public static @Nullable ItemProperty read(@NotNull ItemStack stack) {
        return stack.getItemMeta() instanceof BannerMeta meta && meta.numberOfPatterns() > 0
                ? new BannerPatternsProperty(meta.getPatterns()) : null;
    }

    static @Nullable DyeColor dyeColorOf(String text) {
        String name = text.trim().toUpperCase().replaceAll("[\\s-]", "_");
        for (DyeColor dye : DyeColor.values()) if (dye.name().equals(name)) return dye;
        return null;
    }

    private static @Nullable PatternType patternTypeOf(String text) {
        String name = text.trim().toUpperCase().replaceAll("[\\s-]", "_");
        for (PatternType type : PatternType.values())
            if (type.name().equals(name) || type.getIdentifier().equalsIgnoreCase(text.trim())) return type;
        return null;
    }

    @Override
    public void applyTo(@NotNull ItemStack stack, @Nullable Target source, @Nullable DropFlags flags) {
        if (stack.getItemMeta() instanceof BannerMeta meta) {
            patterns.forEach(meta::addPattern);
            stack.setItemMeta(meta);
        }
    }

    @Override
    public boolean matches(@Nullable ItemStack stack) {
        return stack != null && stack.getItemMeta() instanceof BannerMeta meta && meta.getPatterns().equals(patterns);
    }

    @Override
    public @NotNull String describe() {
        StringJoiner joiner = new StringJoiner("!");
        for (Pattern pattern : patterns) joiner.add("pattern=" + pattern.getColor().name() + ":" + pattern.getPattern().name());
        return joiner.toString();
    }

    @Override
    public @Nullable ItemProperty merge(@NotNull ItemProperty other) {
        if (!(other instanceof BannerPatternsProperty o)) return null;
        List<Pattern> combined = new ArrayList<>(patterns);
        combined.addAll(o.patterns);
        return new BannerPatternsProperty(combined);
    }
}
