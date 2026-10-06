package com.gmail.zariust.otherdrops.data.item.properties;

import com.gmail.zariust.otherdrops.drop.DropType.DropFlags;
import com.gmail.zariust.otherdrops.subject.Target;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Firework explosions: effect=SHAPE:COLORS:fade=COLORS:flicker:trail, parts in any order, colors comma-separated.
 * Ex: effect=BALL_LARGE:RED,ORANGE:fade=YELLOW:trail. Shapes: BALL, BALL_LARGE, STAR, BURST, CREEPER (default BALL).
 * Rockets can have several effects; a firework star has one.
 */
public class FireworkEffectsProperty implements ItemProperty {
    private final List<FireworkEffect> effects;

    public FireworkEffectsProperty(@NotNull List<FireworkEffect> effects) {
        this.effects = List.copyOf(effects);
    }

    public static @Nullable ItemProperty parse(@NotNull Material material, @Nullable ItemMeta defaults, @NotNull String entry) {
        if (!(defaults instanceof FireworkMeta || defaults instanceof FireworkEffectMeta)) return null;
        if (!entry.toLowerCase().startsWith("effect=")) return null;
        FireworkEffect.Builder builder = FireworkEffect.builder();
        boolean hasColor = false;
        for (String part : entry.substring("effect=".length()).split(":")) {
            String p = part.trim();
            if (p.equalsIgnoreCase("flicker")) builder.flicker(true);
            else if (p.equalsIgnoreCase("trail")) builder.trail(true);
            else if (p.toLowerCase().startsWith("fade=")) {
                List<Color> fade = parseColors(p.substring("fade=".length()));
                if (fade == null) return null;
                builder.withFade(fade);
            } else {
                FireworkEffect.Type shape = shapeOf(p);
                if (shape != null) builder.with(shape);
                else {
                    List<Color> colors = parseColors(p);
                    if (colors == null) return null;
                    builder.withColor(colors);
                    hasColor = true;
                }
            }
        }
        return hasColor ? new FireworkEffectsProperty(List.of(builder.build())) : null; // an effect needs a color
    }

    public static @Nullable ItemProperty read(@NotNull ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta instanceof FireworkMeta firework && firework.hasEffects()) return new FireworkEffectsProperty(firework.getEffects());
        if (meta instanceof FireworkEffectMeta star && star.hasEffect()) return new FireworkEffectsProperty(List.of(star.getEffect()));
        return null;
    }

    private static @Nullable FireworkEffect.Type shapeOf(String text) {
        for (FireworkEffect.Type type : FireworkEffect.Type.values())
            if (type.name().equalsIgnoreCase(text.replace(' ', '_'))) return type;
        return null;
    }

    private static @Nullable List<Color> parseColors(String text) {
        List<Color> colors = new ArrayList<>();
        for (String part : text.split(",")) {
            Color color = ColorProperty.parseColor(part);
            if (color == null) return null;
            colors.add(color);
        }
        return colors;
    }

    @Override
    public void applyTo(@NotNull ItemStack stack, @Nullable Target source, @Nullable DropFlags flags) {
        ItemMeta meta = stack.getItemMeta();
        if (meta instanceof FireworkMeta firework) firework.addEffects(effects);
        else if (meta instanceof FireworkEffectMeta star && !effects.isEmpty()) star.setEffect(effects.get(0));
        else return;
        stack.setItemMeta(meta);
    }

    @Override
    public boolean matches(@Nullable ItemStack stack) {
        ItemMeta meta = stack == null ? null : stack.getItemMeta();
        List<FireworkEffect> actual;
        if (meta instanceof FireworkMeta firework) actual = firework.getEffects();
        else if (meta instanceof FireworkEffectMeta star && star.hasEffect()) actual = List.of(star.getEffect());
        else return false;
        return actual.containsAll(effects);
    }

    @Override
    public @NotNull String describe() {
        StringJoiner all = new StringJoiner("!");
        for (FireworkEffect effect : effects) {
            StringJoiner parts = new StringJoiner(":", "effect=", "");
            parts.add(effect.getType().name());
            parts.add(joinColors(effect.getColors()));
            if (!effect.getFadeColors().isEmpty()) parts.add("fade=" + joinColors(effect.getFadeColors()));
            if (effect.hasFlicker()) parts.add("flicker");
            if (effect.hasTrail()) parts.add("trail");
            all.add(parts.toString());
        }
        return all.toString();
    }

    private static String joinColors(List<Color> colors) {
        StringJoiner joiner = new StringJoiner(",");
        for (Color color : colors) joiner.add(ColorProperty.describeColor(color));
        return joiner.toString();
    }

    @Override
    public @Nullable ItemProperty merge(@NotNull ItemProperty other) {
        if (!(other instanceof FireworkEffectsProperty o)) return null;
        List<FireworkEffect> combined = new ArrayList<>(effects);
        combined.addAll(o.effects);
        return new FireworkEffectsProperty(combined);
    }
}
