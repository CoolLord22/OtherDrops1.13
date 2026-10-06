package com.gmail.zariust.otherdrops.data.item.properties;

import com.gmail.zariust.otherdrops.drop.DropType.DropFlags;
import com.gmail.zariust.otherdrops.subject.Target;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Armor trim: trim=MATERIAL:PATTERN using Minecraft's names (Ex: trim=gold:coast, trim=amethyst:sentry).
 * Needs ArmorMeta (1.19.4+); registered only when available.
 */
public class ArmorTrimProperty implements ItemProperty {
    private final TrimMaterial trimMaterial;
    private final TrimPattern trimPattern;

    public ArmorTrimProperty(@NotNull TrimMaterial trimMaterial, @NotNull TrimPattern trimPattern) {
        this.trimMaterial = trimMaterial;
        this.trimPattern = trimPattern;
    }

    public static @Nullable ItemProperty parse(@NotNull Material material, @Nullable ItemMeta defaults, @NotNull String entry) {
        if (!(defaults instanceof ArmorMeta) || !entry.toLowerCase().startsWith("trim=")) return null;
        String[] parts = entry.substring("trim=".length()).split(":", 2);
        if (parts.length < 2) return null;
        TrimMaterial trimMaterial = Registry.TRIM_MATERIAL.get(NamespacedKey.minecraft(parts[0].trim().toLowerCase()));
        TrimPattern trimPattern = Registry.TRIM_PATTERN.get(NamespacedKey.minecraft(parts[1].trim().toLowerCase()));
        return trimMaterial == null || trimPattern == null ? null : new ArmorTrimProperty(trimMaterial, trimPattern);
    }

    public static @Nullable ItemProperty read(@NotNull ItemStack stack) {
        if (!(stack.getItemMeta() instanceof ArmorMeta meta) || !meta.hasTrim() || meta.getTrim() == null) return null;
        return new ArmorTrimProperty(meta.getTrim().getMaterial(), meta.getTrim().getPattern());
    }

    @Override
    public void applyTo(@NotNull ItemStack stack, @Nullable Target source, @Nullable DropFlags flags) {
        if (stack.getItemMeta() instanceof ArmorMeta meta) {
            meta.setTrim(new ArmorTrim(trimMaterial, trimPattern));
            stack.setItemMeta(meta);
        }
    }

    @Override
    public boolean matches(@Nullable ItemStack stack) {
        if (stack == null || !(stack.getItemMeta() instanceof ArmorMeta meta) || meta.getTrim() == null) return false;
        return meta.getTrim().getMaterial().equals(trimMaterial) && meta.getTrim().getPattern().equals(trimPattern);
    }

    @Override
    public @NotNull String describe() {
        return "trim=" + trimMaterial.getKey().getKey() + ":" + trimPattern.getKey().getKey();
    }
}
