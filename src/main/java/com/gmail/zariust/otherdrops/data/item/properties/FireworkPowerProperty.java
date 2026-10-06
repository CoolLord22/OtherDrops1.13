package com.gmail.zariust.otherdrops.data.item.properties;

import com.gmail.zariust.otherdrops.drop.DropType.DropFlags;
import com.gmail.zariust.otherdrops.subject.Target;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Firework rocket flight duration: power=1 to power=3 (like the gunpowder in the recipe). */
public class FireworkPowerProperty implements ItemProperty {
    private final int power;

    public FireworkPowerProperty(int power) {
        this.power = power;
    }

    public static @Nullable ItemProperty parse(@NotNull Material material, @Nullable ItemMeta defaults, @NotNull String entry) {
        if (!(defaults instanceof FireworkMeta) || !entry.toLowerCase().matches("power=[0-9]+")) return null;
        int power = Integer.parseInt(entry.substring("power=".length()));
        return power >= 0 && power <= 127 ? new FireworkPowerProperty(power) : null;
    }

    public static @Nullable ItemProperty read(@NotNull ItemStack stack) {
        return stack.getItemMeta() instanceof FireworkMeta meta && meta.getPower() > 0 ? new FireworkPowerProperty(meta.getPower()) : null;
    }

    @Override
    public void applyTo(@NotNull ItemStack stack, @Nullable Target source, @Nullable DropFlags flags) {
        if (stack.getItemMeta() instanceof FireworkMeta meta) {
            meta.setPower(power);
            stack.setItemMeta(meta);
        }
    }

    @Override
    public boolean matches(@Nullable ItemStack stack) {
        return stack != null && stack.getItemMeta() instanceof FireworkMeta meta && meta.getPower() == power;
    }

    @Override
    public @NotNull String describe() {
        return "power=" + power;
    }
}
