package com.gmail.zariust.otherdrops.data.item.properties;

import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.drop.DropType.DropFlags;
import com.gmail.zariust.otherdrops.subject.Target;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Bundle contents: contents=ITEM*COUNT,ITEM… (Ex: contents=DIAMOND*3,EMERALD,OD_ITEM@coin*2).
 * Plain materials or saved items. As a tool: the bundle must contain at least these.
 * Needs BundleMeta; registered only when available.
 */
public class BundleContentsProperty implements ItemProperty {
    private final List<ItemStack> items;

    public BundleContentsProperty(@NotNull List<ItemStack> items) {
        this.items = List.copyOf(items);
    }

    public static @Nullable ItemProperty parse(@NotNull Material material, @Nullable ItemMeta defaults, @NotNull String entry) {
        if (!(defaults instanceof BundleMeta) || !entry.toLowerCase().startsWith("contents=")) return null;
        List<ItemStack> items = new ArrayList<>();
        for (String part : entry.substring("contents=".length()).split(",")) {
            ItemStack item = parseItem(part.trim());
            if (item == null) return null;
            items.add(item);
        }
        return items.isEmpty() ? null : new BundleContentsProperty(items);
    }

    public static @Nullable ItemProperty read(@NotNull ItemStack stack) {
        return stack.getItemMeta() instanceof BundleMeta meta && meta.hasItems() ? new BundleContentsProperty(meta.getItems()) : null;
    }

    /** "DIAMOND*3" or "OD_ITEM@coin*2"; count defaults to 1. */
    static @Nullable ItemStack parseItem(String text) {
        String[] split = text.split("\\*", 2);
        int amount = 1;
        try {
            if (split.length > 1) amount = Integer.parseInt(split[1].trim());
        } catch (NumberFormatException e) {
            return null;
        }
        ItemStack saved = OtherDropsConfig.commonItemstack.getItemStack(split[0]);
        if (saved != null) {
            ItemStack copy = saved.clone();
            copy.setAmount(amount);
            return copy;
        }
        Material material = Material.matchMaterial(split[0]);
        return material == null || material.isAir() ? null : new ItemStack(material, amount);
    }

    @Override
    public void applyTo(@NotNull ItemStack stack, @Nullable Target source, @Nullable DropFlags flags) {
        if (stack.getItemMeta() instanceof BundleMeta meta) {
            items.forEach(item -> meta.addItem(item.clone()));
            stack.setItemMeta(meta);
        }
    }

    @Override
    public boolean matches(@Nullable ItemStack stack) {
        if (stack == null || !(stack.getItemMeta() instanceof BundleMeta meta)) return false;
        for (ItemStack wanted : items) {
            int found = meta.getItems().stream().filter(wanted::isSimilar).mapToInt(ItemStack::getAmount).sum();
            if (found < wanted.getAmount()) return false;
        }
        return true;
    }

    @Override
    public @NotNull String describe() {
        StringJoiner joiner = new StringJoiner(",", "contents=", "");
        for (ItemStack item : items) joiner.add(item.getType().name() + (item.getAmount() > 1 ? "*" + item.getAmount() : ""));
        return joiner.toString();
    }

    @Override
    public @Nullable ItemProperty merge(@NotNull ItemProperty other) {
        if (!(other instanceof BundleContentsProperty o)) return null;
        List<ItemStack> combined = new ArrayList<>(items);
        combined.addAll(o.items);
        return new BundleContentsProperty(combined);
    }
}
