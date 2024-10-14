package com.gmail.zariust.otherdrops.drop;

import com.gmail.zariust.otherdrops.options.DoubleRange;
import com.gmail.zariust.otherdrops.options.IntRange;
import com.gmail.zariust.otherdrops.subject.Target;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class MythicCreatureDrop extends DropType {
    private final String mythicCreature;
    private final IntRange quantity;
    private int rolledQuantity;

    public MythicCreatureDrop(String mythicCreature, IntRange range, double percent) {
        super(DropCategory.MYTHIC_CREATURE, percent);
        this.mythicCreature = mythicCreature;
        this.quantity = range;
    }

    @Override
    protected DropResult performDrop(Target source, Location at, DropFlags flags) {
        DropResult dropResult = DropResult.fromOverride(this.overrideDefault);
        rolledQuantity = quantity.getRandomIn(flags.rng);
        int amount = rolledQuantity;
        Player playerReceivingItem = flags.recipient;
        while (amount-- > 0) {
            dropResult.addWithoutOverride(drop(at, mythicCreature));
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
        return "MYTHIC_MOB@" + mythicCreature;
    }
}
