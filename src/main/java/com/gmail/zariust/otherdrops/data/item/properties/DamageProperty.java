package com.gmail.zariust.otherdrops.data.item.properties;

import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.drop.DropType.DropFlags;
import com.gmail.zariust.otherdrops.options.IntRange;
import com.gmail.zariust.otherdrops.subject.Target;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.gmail.zariust.otherdrops.OtherDrops.GET_MAX_DAMAGE;

/**
 * Durability already lost: "26" or a range "1500-1560". On drops a range picks a random amount; as a tool the item's
 * damage must be inside the range (Ex: a nearly broken pickaxe).
 */
public class DamageProperty implements ItemProperty {
    private final IntRange damage;

    public DamageProperty(@NotNull IntRange damage) {
        this.damage = damage;
    }

    public static @Nullable ItemProperty parse(@NotNull Material material, @Nullable ItemMeta defaults, @NotNull String entry) {
        if (!entry.matches("[0-9]+(-[0-9]+)?")) return null;
        if (material.getMaxDurability() <= 0 || !(defaults instanceof Damageable)) return null;
        return new DamageProperty(IntRange.parse(entry));
    }

    public static @Nullable ItemProperty read(@NotNull ItemStack stack) {
        if (!(stack.getItemMeta() instanceof Damageable meta) || !meta.hasDamage() || meta.getDamage() <= 0) return null;
        return new DamageProperty(new IntRange(meta.getDamage()));
    }

    @Override
    public void applyTo(@NotNull ItemStack stack, @Nullable Target source, @Nullable DropFlags flags) {
        if (stack.getItemMeta() instanceof Damageable meta) {
            meta.setDamage(damage.getRandomIn(OtherDrops.rng));
            stack.setItemMeta(meta);
        }
    }

    @Override
    public boolean matches(@Nullable ItemStack stack) {
        return stack != null && stack.getItemMeta() instanceof Damageable meta && damage.contains(meta.getDamage());
    }

    @Override
    public @NotNull String describe() {
        return damage.getMin().equals(damage.getMax()) ? String.valueOf(damage.getMin()) : damage.getMin() + "-" + damage.getMax();
    }

    public static int getDurability(ItemStack stack) {
        if(!(stack.getItemMeta() instanceof Damageable damageable)) return 0;
        return damageable.getDamage();
    }

    public static int getMaxDurability(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if(meta instanceof Damageable && GET_MAX_DAMAGE != null) {
            try {
                Integer max = (Integer) GET_MAX_DAMAGE.invoke(meta);
                if (max != null && max > 0) {
                    return max;
                }
            } catch (Exception ignored) {}
        }
        return stack.getType().getMaxDurability();
    }
}
