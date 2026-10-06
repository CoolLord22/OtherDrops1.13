package com.gmail.zariust.otherdrops.data.item.properties;

import com.gmail.zariust.otherdrops.drop.DropType.DropFlags;
import com.gmail.zariust.otherdrops.subject.Target;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * A loaded crossbow: charged=ARROW, charged=FIREWORK_ROCKET, or several (charged=ARROW,ARROW,ARROW for multishot).
 * Saved items work too (charged=OD_ITEM@key). As a tool: the crossbow must be loaded with at least these.
 */
public class CrossbowProjectilesProperty implements ItemProperty {
    private final List<ItemStack> projectiles;

    public CrossbowProjectilesProperty(@NotNull List<ItemStack> projectiles) {
        this.projectiles = List.copyOf(projectiles);
    }

    public static @Nullable ItemProperty parse(@NotNull Material material, @Nullable ItemMeta defaults, @NotNull String entry) {
        if (!(defaults instanceof CrossbowMeta) || !entry.toLowerCase().startsWith("charged=")) return null;
        List<ItemStack> projectiles = new ArrayList<>();
        for (String part : entry.substring("charged=".length()).split(",")) {
            ItemStack item = BundleContentsProperty.parseItem(part.trim());
            if (item == null) return null;
            projectiles.add(item);
        }
        return projectiles.isEmpty() ? null : new CrossbowProjectilesProperty(projectiles);
    }

    public static @Nullable ItemProperty read(@NotNull ItemStack stack) {
        return stack.getItemMeta() instanceof CrossbowMeta meta && meta.hasChargedProjectiles()
                ? new CrossbowProjectilesProperty(meta.getChargedProjectiles()) : null;
    }

    @Override
    public void applyTo(@NotNull ItemStack stack, @Nullable Target source, @Nullable DropFlags flags) {
        if (stack.getItemMeta() instanceof CrossbowMeta meta) {
            projectiles.forEach(projectile -> meta.addChargedProjectile(projectile.clone()));
            stack.setItemMeta(meta);
        }
    }

    @Override
    public boolean matches(@Nullable ItemStack stack) {
        if (stack == null || !(stack.getItemMeta() instanceof CrossbowMeta meta)) return false;
        List<ItemStack> loaded = new ArrayList<>(meta.getChargedProjectiles());
        for (ItemStack wanted : projectiles) {
            ItemStack match = loaded.stream().filter(wanted::isSimilar).findFirst().orElse(null);
            if (match == null) return false;
            loaded.remove(match);
        }
        return true;
    }

    @Override
    public @NotNull String describe() {
        StringJoiner joiner = new StringJoiner(",", "charged=", "");
        for (ItemStack projectile : projectiles) joiner.add(projectile.getType().name());
        return joiner.toString();
    }

    @Override
    public @Nullable ItemProperty merge(@NotNull ItemProperty other) {
        if (!(other instanceof CrossbowProjectilesProperty o)) return null;
        List<ItemStack> combined = new ArrayList<>(projectiles);
        combined.addAll(o.projectiles);
        return new CrossbowProjectilesProperty(combined);
    }
}
