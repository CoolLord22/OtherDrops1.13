package com.gmail.zariust.otherdrops.data.item.properties;

import com.gmail.zariust.otherdrops.drop.DropType;
import com.gmail.zariust.otherdrops.subject.Target;
import com.gmail.zariust.otherdrops.things.ODVariables;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * The text after "~": a display name, then lore lines separated by ";" (Ex: "~&bExcalibur;&7Forged in fire").
 * Positional (always after "~"), so it's parsed directly rather than through the registry.
 * An empty name ("~" on its own) means "no custom name" when matching. Names and lore can contain variables.
 */
public class DisplayTextProperty implements ItemProperty {
    private final String displayName;   // colors translated; may contain variables (%p, %v…)
    private final List<String> lore;    // same

    public DisplayTextProperty(@NotNull String displayName, @NotNull List<String> lore) {
        this.displayName = displayName;
        this.lore = List.copyOf(lore);
    }

    /** Config -> property, from the text after "~". */
    public static @NotNull DisplayTextProperty parse(@NotNull String text) {
        String[] lines = text.split(";");
        String name = lines.length == 0 ? "" : ODVariables.preParse(lines[0]);
        List<String> lore = new ArrayList<>();
        for (String line : Arrays.asList(lines).subList(Math.min(1, lines.length), lines.length))
            lore.add(ODVariables.preParse(line));
        return new DisplayTextProperty(name, lore);
    }

    /** Item -> property, or null if the item has no custom name and no lore. */
    public static @Nullable DisplayTextProperty read(@NotNull ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null || (!meta.hasDisplayName() && !meta.hasLore())) return null;
        String name = meta.hasDisplayName() ? meta.getDisplayName() : "";
        List<String> lore = meta.hasLore() && meta.getLore() != null ? meta.getLore() : List.of();
        return new DisplayTextProperty(name, lore);
    }

    public @NotNull String getDisplayName() {
        return displayName;
    }

    public @NotNull List<String> getLore() {
        return lore;
    }

    @Override
    public void applyTo(@NotNull ItemStack stack, @Nullable Target source, @Nullable DropType.DropFlags flags) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;
        if (!displayName.isEmpty()) meta.setDisplayName(resolve(displayName, stack, flags));
        if (!lore.isEmpty()) {
            List<String> resolved = new ArrayList<>();
            for (String line : lore) resolved.add(resolve(line, stack, flags));
            meta.setLore(resolved);
        }
        stack.setItemMeta(meta);
    }

    @Override
    public boolean matches(@Nullable ItemStack stack) {
        if (stack == null) return false;
        ItemMeta meta = stack.getItemMeta();
        String actualName = meta != null && meta.hasDisplayName() ? meta.getDisplayName() : "";
        if (!displayName.equals(actualName)) return false;   // "" = must have no custom name
        if (lore.isEmpty()) return true;                     // no lore in config: any lore is fine
        return meta != null && meta.hasLore() && lore.equals(meta.getLore());
    }

    @Override
    public @NotNull String describe() {
        StringJoiner joiner = new StringJoiner(";");
        joiner.add(displayName.replace('§', '&'));
        for (String line : lore) joiner.add(line.replace('§', '&'));
        return joiner.toString();
    }

    /** Variables in names/lore are resolved per drop, from the drop's context. */
    private static String resolve(String text, ItemStack stack, @Nullable DropType.DropFlags flags) {
        if (flags == null) return text;
        return new ODVariables()
                .setPlayerName(flags.getRecipientName())
                .setVictimName(flags.getVictim())
                .setToolName(flags.getToolName())
                .setDropName(stack.getType().name())
                .setQuantity(String.valueOf(stack.getAmount()))
                .parse(text);
    }
}