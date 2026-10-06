package com.gmail.zariust.otherdrops.data.item.properties;

import com.gmail.zariust.otherdrops.drop.DropType.DropFlags;
import com.gmail.zariust.otherdrops.subject.Target;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Potion effects: EFFECT@DURATION@LEVEL (Ex: strength@1200@2; duration in ticks, defaults 100; level defaults 1).
 * Potions, splash/lingering potions, tipped arrows and suspicious stew. Effect names are Minecraft's
 * (strength, slowness…; legacy names like INCREASE_DAMAGE also work).
 * As a tool: every listed effect must be on the item at that level, as a custom effect or as the brewed base potion.
 */
public class PotionEffectsProperty implements ItemProperty {
    private final List<PotionEffect> effects;

    public PotionEffectsProperty(@NotNull List<PotionEffect> effects) {
        this.effects = List.copyOf(effects);
    }

    public static @Nullable ItemProperty parse(@NotNull Material material, @Nullable ItemMeta defaults, @NotNull String entry) {
        if (!(defaults instanceof PotionMeta || defaults instanceof SuspiciousStewMeta)) return null;
        String[] split = entry.split("@");
        PotionEffectType type = findType(split[0]);
        if (type == null) return null;
        int duration = 100;
        int level = 1;
        try {
            if (split.length > 1) duration = Integer.parseInt(split[1].trim());
            if (split.length > 2) level = Integer.parseInt(split[2].trim());
        } catch (NumberFormatException e) {
            return null;
        }
        return new PotionEffectsProperty(List.of(new PotionEffect(type, duration, Math.max(0, level - 1))));
    }

    public static @Nullable ItemProperty read(@NotNull ItemStack stack) {
        List<PotionEffect> custom = customEffects(stack.getItemMeta());
        return custom == null || custom.isEmpty() ? null : new PotionEffectsProperty(custom);
    }

    /** Modern key first (strength, works on 1.18+), then the legacy name (INCREASE_DAMAGE, for 1.17). Never throws. */
    public static @Nullable PotionEffectType findType(@NotNull String name) {
        String trimmed = name.trim();
        if (trimmed.isEmpty()) return null;
        try {
            PotionEffectType byKey = PotionEffectType.getByKey(NamespacedKey.minecraft(trimmed.toLowerCase().replace(' ', '_')));
            if (byKey != null) return byKey;
        } catch (Throwable ignored) {
        }
        try {
            return PotionEffectType.getByName(trimmed.toUpperCase());
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static @Nullable List<PotionEffect> customEffects(@Nullable ItemMeta meta) {
        if (meta instanceof PotionMeta potion) return potion.getCustomEffects();
        if (meta instanceof SuspiciousStewMeta stew) return stew.getCustomEffects();
        return null;
    }

    @Override
    public void applyTo(@NotNull ItemStack stack, @Nullable Target source, @Nullable DropFlags flags) {
        ItemMeta meta = stack.getItemMeta();
        if (meta instanceof PotionMeta potion) effects.forEach(effect -> potion.addCustomEffect(effect, true));
        else if (meta instanceof SuspiciousStewMeta stew) effects.forEach(effect -> stew.addCustomEffect(effect, true));
        else return;
        stack.setItemMeta(meta);
    }

    @Override
    public boolean matches(@Nullable ItemStack stack) {
        if (stack == null) return false;
        ItemMeta meta = stack.getItemMeta();
        List<PotionEffect> actual = customEffects(meta);
        if (actual == null) return false;
        for (PotionEffect wanted : effects) {
            boolean found = actual.stream().anyMatch(e -> e.getType().equals(wanted.getType()) && e.getAmplifier() == wanted.getAmplifier());
            if (!found && !isBasePotion(meta, wanted)) return false;
        }
        return true;
    }

    /** Brewed potions get their effect from the base potion type rather than a custom effect. */
    private static boolean isBasePotion(ItemMeta meta, PotionEffect wanted) {
        if (!(meta instanceof PotionMeta potion)) return false;
        try {
            return wanted.getType().equals(potion.getBasePotionData().getType().getEffectType());
        } catch (Throwable ignored) { // API differs between versions
            return false;
        }
    }

    @Override
    public @NotNull String describe() {
        StringJoiner joiner = new StringJoiner("!");
        for (PotionEffect effect : effects)
            joiner.add(nameOf(effect.getType()) + "@" + effect.getDuration() + "@" + (effect.getAmplifier() + 1));
        return joiner.toString();
    }

    /** Modern key (strength) where available (1.18+), otherwise the legacy name (1.17). */
    @SuppressWarnings("deprecation")
    private static String nameOf(PotionEffectType type) {
        try {
            return type.getKey().getKey();
        } catch (Throwable ignored) {
            return type.getName();
        }
    }

    @Override
    public @Nullable ItemProperty merge(@NotNull ItemProperty other) {
        if (!(other instanceof PotionEffectsProperty o)) return null;
        List<PotionEffect> combined = new ArrayList<>(effects);
        combined.addAll(o.effects);
        return new PotionEffectsProperty(combined);
    }
}
