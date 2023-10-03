package com.gmail.zariust.otherdrops.subject;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.Log;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class MythicItemAgent extends ToolAgent {
    private String mythicItem = null;

    public MythicItemAgent(String mythicItem) {
        if(!MythicBukkit.inst().getItemManager().getItem(mythicItem).isPresent()) {
            Log.logInfo("Invalid mythic item tool specified/could not be found: " + mythicItem, Verbosity.HIGHEST);
            return;
        }
        this.mythicItem = mythicItem;
    }
    
    @Override
    public boolean matches(Subject other) {
        if (!(other instanceof PlayerSubject))
            return false;

        if(mythicItem != null) {
            Log.logWarning("Checking mythic tool");
            ItemStack playerItem = ((PlayerSubject) other).getTool().getActualTool();
            ItemStack mythicItemStack = MythicBukkit.inst().getItemManager().getItemStack(mythicItem);
            if(mythicItemStack != null) {
                if(playerItem.getType() != mythicItemStack.getType())
                    return false;
                if(!mythicItemStack.hasItemMeta())
                    return true;
                if(playerItem.hasItemMeta()) {
                    ItemMeta thisMeta = playerItem.getItemMeta();
                    ItemMeta stackMeta = mythicItemStack.getItemMeta();
                    ((Damageable) thisMeta).setDamage(0);
                    ((Damageable) stackMeta).setDamage(0);
                    return Bukkit.getItemFactory().equals(thisMeta, stackMeta);
                }
            }
        }
        return true;
    }
}
