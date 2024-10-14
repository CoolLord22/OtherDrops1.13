package com.gmail.zariust.otherdrops.drop;

import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.options.DoubleRange;
import com.gmail.zariust.otherdrops.options.IntRange;
import com.gmail.zariust.otherdrops.subject.Target;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class NamespaceItemDrop extends DropType {
    private final String namespace;
    private final String key;
    private final ItemStack item;
    private final IntRange quantity;
    private int rolledQuantity;

    public NamespaceItemDrop(String namespace, String key, ItemStack item, IntRange range, double percent) {
        super(DropCategory.NAMESPACE_ITEM, percent);
        this.namespace = namespace;
        this.key = key;
        this.item = item;
        this.quantity = range;
    }

    @Override
    protected DropResult performDrop(Target source, Location at, DropFlags flags) {
        DropResult dropResult = DropResult.fromOverride(this.overrideDefault);
        rolledQuantity = quantity.getRandomIn(flags.rng);
        int amount = rolledQuantity;
        Player playerReceivingItem = flags.recipient;
        while (amount-- > 0) {
            if((!OtherDropsConfig.globalFallToGround || flags.dropToInventory) && playerReceivingItem != null) {
                dropResult.addWithoutOverride(drop(playerReceivingItem, item, at, true));
            }
            else {
                dropResult.addWithoutOverride(drop(at, item, true));
            }
        }

        return dropResult;
    }

    @Override
    public double getAmount() {
        return rolledQuantity;
    }

    @Override
    public DoubleRange getAmountRange() {
        return quantity.toDoubleRange();
    }

    @Override
    protected String getName() {
        return "NAMESPACE_ITEM@" + namespace + ":" + key;
    }
}
