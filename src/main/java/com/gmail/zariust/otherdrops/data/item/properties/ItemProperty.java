package com.gmail.zariust.otherdrops.data.item.properties;

import com.gmail.zariust.otherdrops.drop.DropType;
import com.gmail.zariust.otherdrops.subject.Target;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** One trait of an item (name & lore, enchantments, damage, a color…). Built by its class's static parse/read. */
public interface ItemProperty {
    /** Drops, replacetool, mob equipment: put this trait on a new item. source/flags are null when there's no drop context. */
    void applyTo(@NotNull ItemStack stack, @Nullable Target source, @Nullable DropType.DropFlags flags);

    /** Tools, itemrequirement, drop filters: does this item have this trait? */
    boolean matches(@Nullable ItemStack stack);

    /** The config text for this trait. Parsing it gives this trait back (used by /od id and /od write). */
    @NotNull String describe();

    /**
     * List-like traits (enchantments, flags, potion effects, patterns…) combine when they appear in several entries.
     * Returns the combined property, or null if these two don't combine.
     */
    default @Nullable ItemProperty merge(@NotNull ItemProperty other) {
        return null;
    }
}