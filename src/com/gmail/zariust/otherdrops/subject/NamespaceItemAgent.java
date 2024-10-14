package com.gmail.zariust.otherdrops.subject;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.Log;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class NamespaceItemAgent extends ToolAgent {
    private String state = null;

    public NamespaceItemAgent(ItemStack item, String state) {
        super(item);
        this.state = state;
    }

    @Override
    public boolean matches(Subject other) {
        if (!(other instanceof PlayerSubject))
            return false;

        if(state != null) {
            ItemStack playerItem = ((PlayerSubject) other).getTool().getActualTool();
            ItemStack namespaceItemStack = Bukkit.getRecipe(NamespacedKey.fromString(state)).getResult();
            Log.logInfo("Checking NamespaceItem tool: " + namespaceItemStack + " vs player tool: " + playerItem, Verbosity.HIGHEST);

            if (playerItem.getType() != namespaceItemStack.getType()) { // if the two materials are not equal
                Log.logInfo("NamespaceItemToolCheck - failed (different materials).", Verbosity.HIGHEST);
                return false;
            } else if (!namespaceItemStack.hasItemMeta()) { // if NamespaceItem has no custom data, the check should pass
                Log.logInfo("NamespaceItemToolCheck - passed (no meta on NamespaceItem).", Verbosity.HIGHEST);
                return true;
            } else { // NamespaceItem has meta, lets check that it matches the player's item
                if (!playerItem.hasItemMeta()) // player item had no meta
                    return false;
                ItemMeta thisMeta = playerItem.getItemMeta();
                ItemMeta stackMeta = namespaceItemStack.getItemMeta();
                ((Damageable) thisMeta).setDamage(0);
                ((Damageable) stackMeta).setDamage(0);
                Log.logInfo("NamespaceItemToolCheck - returned value: " + Bukkit.getItemFactory().equals(thisMeta, stackMeta), Verbosity.HIGHEST);
                return Bukkit.getItemFactory().equals(thisMeta, stackMeta);
            }
        }
        return true;
    }
}
