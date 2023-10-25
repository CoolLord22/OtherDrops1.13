package com.gmail.zariust.otherdrops.subject;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.Dependencies;
import com.gmail.zariust.otherdrops.Log;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class MythicItemAgent extends ToolAgent {
    private String mythicItem = null;

    public MythicItemAgent(String mythicItem) {
        if(Dependencies.hasMythicMobs()) {
            if(!Dependencies.getMythicMobs().getItemManager().getItem(mythicItem).isPresent()) {
                Log.logInfo("Invalid mythic item tool specified/could not be found: " + mythicItem, Verbosity.HIGHEST);
                return;
            }
            this.mythicItem = mythicItem;
        }
    }
    
    @Override
    public boolean matches(Subject other) {
        if (!(other instanceof PlayerSubject))
            return false;

        if(mythicItem != null) {
            ItemStack playerItem = ((PlayerSubject) other).getTool().getActualTool();
            ItemStack mythicItemStack = Dependencies.getMythicMobs().getItemManager().getItemStack(mythicItem);
            Log.logInfo("Checking mythic tool: " + mythicItemStack + " vs player tool: " + playerItem, Verbosity.HIGHEST);
            if(mythicItemStack != null) {
                if(playerItem.getType() != mythicItemStack.getType()) { // if the two materials are not equal
                    Log.logInfo("MythicToolCheck - failed (different materials).", Verbosity.HIGHEST);
                    return false;
                }
                if(!mythicItemStack.hasItemMeta()) { // if mythic item has no custom data, the check should pass
                    Log.logInfo("MythicToolCheck - passed (no meta on MythicItem).", Verbosity.HIGHEST);
                    return true;
                }
                if(playerItem.hasItemMeta()) { // if the item has meta, lets check to make sure they match
                    ItemMeta thisMeta = playerItem.getItemMeta();
                    ItemMeta stackMeta = mythicItemStack.getItemMeta();
                    ((Damageable) thisMeta).setDamage(0);
                    ((Damageable) stackMeta).setDamage(0);
                    Log.logInfo("MythicToolCheck - returned value: " + Bukkit.getItemFactory().equals(thisMeta, stackMeta), Verbosity.HIGHEST);
                    return Bukkit.getItemFactory().equals(thisMeta, stackMeta);
                }
            }
        }
        return true;
    }
}
