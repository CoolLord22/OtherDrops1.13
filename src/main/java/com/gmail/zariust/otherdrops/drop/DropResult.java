package com.gmail.zariust.otherdrops.drop;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class DropResult {
    private boolean anyRan;
    private int quantity;
    public final List<Entity> droppedEntities = new ArrayList<>();
    private boolean overrideDefault;
    private boolean overrideDefaultXp = false; // default to false
    private boolean overrideEquipment = false;

    public DropResult() {
        quantity = 0;
    }

    public DropResult(int quant) {
        quantity = quant;
        anyRan = quant > 0;
    }

    public DropResult(boolean overrideDefault2) {
        this.overrideDefault = overrideDefault2;
        this.quantity = 0;
    }

    static DropResult fromQuantity(int quant) {
        return new DropResult(quant);
    }

    public static DropResult fromOverride(boolean overrideDefault2) {
        return new DropResult(overrideDefault2);
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean getOverrideDefault() {
        return overrideDefault;
    }

    public void setOverrideDefault(boolean overrideDefault) {
        this.overrideDefault = overrideDefault;
    }

    public boolean getOverrideDefaultXp() {
        return overrideDefaultXp;
    }

    public void setOverrideDefaultXp(boolean b) {
        overrideDefaultXp = b;

    }

    public void addDropped(Entity ent) {
        droppedEntities.add(ent);
    }

    public void addDropped(List<Entity> ent) {
        droppedEntities.addAll(ent);
    }

    public List<Entity> getDropped() {
        return droppedEntities;
    }

    public String getDroppedString() {
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        for (Entity ent : droppedEntities) {
            if (ent instanceof Item item) joiner.add(item.getItemStack().toString());
            else joiner.add(ent.toString());
        }
        return joiner.toString();
    }

    public void add(DropResult drop) {
        merge(drop);
        if (drop.getOverrideDefault()) this.setOverrideDefault(drop.getOverrideDefault());
        if (drop.getOverrideDefaultXp()) this.setOverrideDefaultXp(drop.getOverrideDefaultXp());
    }

    public void addWithoutOverride(DropResult drop) {
        merge(drop);
    }

    public static DropResult getFromOverrideDefault(boolean overrideDefault2) {
        return new DropResult(overrideDefault2);
    }

    /** Sums successful parts; the result is -1 (failed) only if every part so far failed its chance. */
    private void merge(DropResult drop) {
        int q = drop.getQuantity();
        if (q >= 0) {
            quantity = (anyRan ? quantity : 0) + q;
            anyRan = true;
        } else if (!anyRan) {
            quantity = -1;
        }
        addDropped(drop.getDropped());
    }

    public boolean isOverrideEquipment() {
        return overrideEquipment;
    }

    public void setOverrideEquipment(boolean overrideEquipment) {
        this.overrideEquipment = overrideEquipment;
    }

}
