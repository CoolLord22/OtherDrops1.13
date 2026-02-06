package com.gmail.zariust.otherdrops.data.itemmeta;

import com.gmail.zariust.common.CMEnchantment;
import com.gmail.zariust.common.CommonEnchantments;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.subject.Target;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.List;

public class OdEnchantedBookMeta extends OdItemMeta {
    public final List<CMEnchantment> cmEnch;

    public OdEnchantedBookMeta(List<CMEnchantment> cmEnch) {
        this.cmEnch = cmEnch;
    }

    @Override
    public ItemStack setOn(ItemStack stack, Target source) {
        if (cmEnch == null) return null;

        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) stack.getItemMeta();
        Log.logInfo("Adding enchantments");
        for (CMEnchantment ench : cmEnch) {
            meta.addStoredEnchant(ench.getEnch(), ench.getLevel(), true);
        }
        stack.setItemMeta(meta);
        return stack;
    }

    public static OdItemMeta parse(String sub) {
        if (sub.isEmpty()) return null;
        List<CMEnchantment> cmEnch = CommonEnchantments.parseEnchantments(sub);
        return new OdEnchantedBookMeta(cmEnch);
    }
}
