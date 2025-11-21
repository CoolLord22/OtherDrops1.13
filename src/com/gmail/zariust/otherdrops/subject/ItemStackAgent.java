package com.gmail.zariust.otherdrops.subject;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.Log;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ItemStackAgent extends ToolAgent {
    private final ItemStack itemStack;
    private final String identifier;

    public ItemStackAgent(ItemStack item, String identifier) {
        super(item);
        this.itemStack = item;
        this.identifier = identifier;
    }

    @Override
    public boolean matches(Subject other) {
        if (!(other instanceof PlayerSubject playerSubject)) return false;

        if (itemStack != null) {
            ItemStack playerItem = playerSubject.getTool().getActualTool();
            Log.logInfo("Checking ItemStack tool: " + itemStack + " vs player tool: " + playerItem, Verbosity.HIGHEST);
            if (playerItem != null) {
                if (playerItem.getType() != itemStack.getType()) { // if the two materials are not equal
                    Log.logInfo("ItemStackToolCheck - failed (different materials).", Verbosity.HIGHEST);
                    return false;
                } else if (!itemStack.hasItemMeta()) { // if compare item has no custom data, the check should pass
                    Log.logInfo("ItemStackToolCheck - passed (no meta on comparison item).", Verbosity.HIGHEST);
                    return true;
                } else { // compare item has meta, lets check that it matches the player's item
                    if (!playerItem.hasItemMeta()) // player item had no meta
                        return false;

                    ItemMeta thisMeta = playerItem.getItemMeta().clone();
                    ItemMeta stackMeta = itemStack.getItemMeta().clone();

                    if (thisMeta.hasAttributeModifiers() || stackMeta.hasAttributeModifiers()) {
                        for (Attribute attr : Attribute.values()) {
                            Collection<AttributeModifier> mods1 = thisMeta.getAttributeModifiers(attr);
                            Collection<AttributeModifier> mods2 = stackMeta.getAttributeModifiers(attr);

                            if (mods1 != null && mods2 != null) {
                                // Compare by amount, operation, slot, etc. instead of UUID
                                if (!compareModifiersIgnoringUUID(mods1, mods2)) return false;
                            } else if (mods1 != null || mods2 != null) {
                                return false; // one has attributes and the other doesn't
                            }
                        }
                    }

                    thisMeta.setAttributeModifiers(null);
                    stackMeta.setAttributeModifiers(null);
                    ((Damageable) thisMeta).setDamage(0);
                    ((Damageable) stackMeta).setDamage(0);

                    Log.logInfo("ItemStackToolCheck - returned value: " + Bukkit.getItemFactory().equals(thisMeta, stackMeta), Verbosity.HIGHEST);
                    return Bukkit.getItemFactory().equals(thisMeta, stackMeta);
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "ITEM_STACK@" + identifier;
    }

    private static boolean compareModifiersIgnoringUUID(Collection<AttributeModifier> a, Collection<AttributeModifier> b) {
        if (a.size() != b.size()) return false;

        List<AttributeModifier> listA = new ArrayList<>(a);
        List<AttributeModifier> listB = new ArrayList<>(b);

        for (AttributeModifier modA : listA) {
            boolean matched = listB.removeIf(modB -> modA.getAmount() == modB.getAmount() && modA.getOperation() == modB.getOperation() && Objects.equals(modA.getSlot(), modB.getSlot()));
            if (!matched) return false;
        }
        return true;
    }
}
