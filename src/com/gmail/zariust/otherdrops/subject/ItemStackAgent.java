package com.gmail.zariust.otherdrops.subject;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDrops;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemStackAgent extends ToolAgent {
    private String itemIdentifier = null;

    public ItemStackAgent(ItemStack item, String itemIdentifier) {
        super(item);
        this.itemIdentifier = itemIdentifier;
    }

    @Override
    public boolean matches(Subject other) {
        if (!(other instanceof PlayerSubject))
            return false;

        if(itemIdentifier != null) {
            ItemStack playerItem = ((PlayerSubject) other).getTool().getActualTool();
            ItemStack compareItem = OtherDrops.loadedItems.get(new NamespacedKey(OtherDrops.plugin, itemIdentifier));
            Log.logInfo("Checking ItemStack tool: " + compareItem + " vs player tool: " + playerItem, Verbosity.HIGHEST);
            if(compareItem != null) {
                if(playerItem.getType() != compareItem.getType()) { // if the two materials are not equal
                    Log.logInfo("ItemStackToolCheck - failed (different materials).", Verbosity.HIGHEST);
                    return false;
                } else if(!compareItem.hasItemMeta()) { // if compare item has no custom data, the check should pass
                    Log.logInfo("ItemStackToolCheck - passed (no meta on comparison item).", Verbosity.HIGHEST);
                    return true;
                } else { // compare item has meta, lets check that it matches the player's item
                    if(!playerItem.hasItemMeta()) // player item had no meta
                        return false;
                    ItemMeta thisMeta = playerItem.getItemMeta();
                    ItemMeta stackMeta = compareItem.getItemMeta();
                    ((Damageable) thisMeta).setDamage(0);
                    ((Damageable) stackMeta).setDamage(0);
                    Log.logInfo("ItemStackToolCheck - returned value: " + Bukkit.getItemFactory().equals(thisMeta, stackMeta), Verbosity.HIGHEST);
                    return Bukkit.getItemFactory().equals(thisMeta, stackMeta);
                }
            }
        }
        return true;
    }
}
