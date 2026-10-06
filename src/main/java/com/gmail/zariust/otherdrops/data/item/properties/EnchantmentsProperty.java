package com.gmail.zariust.otherdrops.data.item.properties;

import com.gmail.zariust.common.CMEnchantment;
import com.gmail.zariust.common.CommonEnchantments;
import com.gmail.zariust.otherdrops.drop.DropType.DropFlags;
import com.gmail.zariust.otherdrops.options.IntRange;
import com.gmail.zariust.otherdrops.subject.Target;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Enchantments: sharpness#5, sharpness#1-3 (random level), sharpness#? (any level), RANDOM, noench (tools: no
 * enchantments). All of an item's enchantments are one property, because tool matching compares the whole set
 * (enchantments_restrict_matching). On enchanted books they are stored enchantments, usable in an anvil.
 */
public class EnchantmentsProperty implements ItemProperty {
    private final List<CMEnchantment> enchantments;

    public EnchantmentsProperty(@NotNull List<CMEnchantment> enchantments) {
        this.enchantments = List.copyOf(enchantments);
    }

    public static @Nullable ItemProperty parse(@NotNull Material material, @Nullable ItemMeta defaults, @NotNull String entry) {
        List<CMEnchantment> parsed = CommonEnchantments.parseEnchantments(entry);
        return parsed.isEmpty() ? null : new EnchantmentsProperty(parsed);
    }

    public static @Nullable ItemProperty read(@NotNull ItemStack stack) {
        Map<Enchantment, Integer> actual = enchantmentsOf(stack);
        if (actual.isEmpty()) return null;
        List<CMEnchantment> list = new ArrayList<>();
        for (Map.Entry<Enchantment, Integer> entry : actual.entrySet()) {
            CMEnchantment cm = new CMEnchantment();
            cm.setEnch(entry.getKey());
            cm.setLevelRange(new IntRange(entry.getValue()));
            list.add(cm);
        }
        return new EnchantmentsProperty(list);
    }

    private static Map<Enchantment, Integer> enchantmentsOf(ItemStack stack) {
        return stack.getItemMeta() instanceof EnchantmentStorageMeta meta ? meta.getStoredEnchants() : stack.getEnchantments();
    }

    @Override
    public void applyTo(@NotNull ItemStack stack, @Nullable Target source, @Nullable DropFlags flags) {
        if (stack.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            for (CMEnchantment cm : enchantments) {
                Enchantment enchantment = cm.getEnch(stack);
                if (enchantment != null) meta.addStoredEnchant(enchantment, cm.getLevel(), true);
            }
            stack.setItemMeta(meta);
        } else {
            CommonEnchantments.applyEnchantments(stack, enchantments);
        }
    }

    @Override
    public boolean matches(@Nullable ItemStack stack) {
        return stack != null && CommonEnchantments.matches(enchantments, enchantmentsOf(stack));
    }

    @Override
    public @NotNull String describe() {
        StringJoiner joiner = new StringJoiner("!");
        for (CMEnchantment cm : enchantments) {
            if (cm.getNoEnch()) {
                joiner.add("noench");
                continue;
            }
            String name = cm.getEnchRaw() == null ? "random" : cm.getEnchRaw().getKey().getKey();
            IntRange levels = cm.getLevelRange();
            if (levels == null) joiner.add(name + "#?");
            else if (levels.getMin().equals(levels.getMax())) joiner.add(name + "#" + levels.getMin());
            else joiner.add(name + "#" + levels.getMin() + "-" + levels.getMax());
        }
        return joiner.toString();
    }

    @Override
    public @Nullable ItemProperty merge(@NotNull ItemProperty other) {
        if (!(other instanceof EnchantmentsProperty o)) return null;
        List<CMEnchantment> combined = new ArrayList<>(enchantments);
        combined.addAll(o.enchantments);
        return new EnchantmentsProperty(combined);
    }
}
