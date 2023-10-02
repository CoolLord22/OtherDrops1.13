package com.gmail.zariust.otherdrops.drop;

import com.gmail.zariust.otherdrops.options.DoubleRange;
import com.gmail.zariust.otherdrops.options.IntRange;
import com.gmail.zariust.otherdrops.subject.Target;
import org.bukkit.Location;

public class MythicDrop extends DropType {
    private final String mythicDrop;
    private final IntRange quantity;
    private int rolledQuantity;

    public MythicDrop(String mythicDrop, IntRange range, double percent) {
        super(DropCategory.MYTHIC, percent);
        this.mythicDrop = mythicDrop;
        this.quantity = range;
    }

    @Override
    protected DropResult performDrop(Target source, Location at, DropFlags flags) {
        DropResult dropResult = DropResult.fromOverride(this.overrideDefault);
        rolledQuantity = quantity.getRandomIn(flags.rng);
        int amount = rolledQuantity;
        while (amount-- > 0) {
            dropResult.addWithoutOverride(drop(at, mythicDrop));
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
        return "MYTHIC_" + mythicDrop;
    }
}
