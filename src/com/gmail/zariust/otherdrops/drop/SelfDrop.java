// OtherDrops - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant, Zarius Tularial, Celtic Minstrel
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.	 If not, see <http://www.gnu.org/licenses/>.

package com.gmail.zariust.otherdrops.drop;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Vehicle;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.PistonExtensionMaterial;

import com.gmail.zariust.common.CommonEntity;
import com.gmail.zariust.otherdrops.data.CreatureData;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.options.DoubleRange;
import com.gmail.zariust.otherdrops.options.IntRange;
import com.gmail.zariust.otherdrops.subject.BlockTarget;
import com.gmail.zariust.otherdrops.subject.CreatureSubject;
import com.gmail.zariust.otherdrops.subject.Target;
import com.gmail.zariust.otherdrops.subject.VehicleTarget;

public class SelfDrop extends DropType {
    private final IntRange count;
    private int            rolledCount;

    public SelfDrop() {
        this(100.0);
    }

    public SelfDrop(double chance) {
        this(new IntRange(1), chance);
    }

    public SelfDrop(IntRange amount) {
        this(amount, 100.0);
    }

    public SelfDrop(IntRange intRange, double chance) { // Rome!
        super(DropCategory.DEFAULT, chance);
        count = intRange;
    }

	@Override
    protected DropResult performDrop(Target source, Location from,
            DropFlags flags) {
        DropResult dropResult = DropResult.fromOverride(this.overrideDefault);

        if (source instanceof CreatureSubject) {
            Entity mob = ((CreatureSubject) source).getAgent();
            Data data = CreatureData.parse(mob);
            // Data data = new CreatureData(CommonEntity.getCreatureData(mob));
            EntityType type = mob.getType();
            dropResult.addWithoutOverride(drop(from, flags.recipient, type,
                    data));
        } else if (source instanceof VehicleTarget) {
            Entity entity = ((VehicleTarget) source).getVehicle();
            if (entity instanceof Painting) {
                dropResult.addWithoutOverride(drop(from, new ItemStack(
                        Material.PAINTING, 1), flags.naturally));
            } else if (entity instanceof Vehicle) {
                Material material = CommonEntity.getVehicleType(entity);
                dropResult.addWithoutOverride(drop(from, new ItemStack(
                        material, 1), flags.naturally));
            } else
                return dropResult;
        } else if (source instanceof BlockTarget) {
            Block block = ((BlockTarget) source).getBlock();
            Material material = block.getType();
			int data = block.getData(), quantity = count.getRandomIn(flags.rng);
            switch (material) {
            case AIR:
            case REDSTONE_WIRE:
                data = 0;
                material = Material.REDSTONE;
                break;
           /* case SIGN:
            case WALL_SIGN:
                data = 0;
                material = Material.SIGN;
                break; */
            case MOVING_PISTON:
                data = 0;
                PistonExtensionMaterial ext = (PistonExtensionMaterial) block.getState().getData();
                material = ext.isSticky() ? Material.STICKY_PISTON : Material.PISTON;
                break;
            case SPAWNER:
                CreatureSpawner spawner = (CreatureSpawner) block.getState();
                data = spawner.getSpawnedType().getTypeId();
                break;
            default: // Most block data doesn't transfer to the item of the same
                     // ID
                data = 0;
                break;
            }
            ItemStack stack = new ItemStack(material, quantity, (short) data);
            dropResult.addWithoutOverride(drop(from, stack, flags.naturally));
            rolledCount = quantity;
        }
        return dropResult;
    }

    @Override
    public double getAmount() {
        return rolledCount;
    }

    @Override
    public String getName() {
        return "THIS";
    }

    @Override
    public DoubleRange getAmountRange() {
        return count.toDoubleRange();
    }
}
