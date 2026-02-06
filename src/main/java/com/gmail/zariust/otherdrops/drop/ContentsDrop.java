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

import com.gmail.zariust.otherdrops.data.CreatureData;
import com.gmail.zariust.otherdrops.options.DoubleRange;
import com.gmail.zariust.otherdrops.subject.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class ContentsDrop extends DropType {
    public ContentsDrop() {
        super(DropCategory.CONTENTS);
    }

    @Override
    protected DropResult performDrop(Target source, Location where, DropFlags flags) {
        DropResult dropResult = DropResult.fromOverride(this.overrideDefault);
        // First locate the object; it's a block, storage minecart, or player
        if (source instanceof BlockTarget blockTarget) {
            Block block = blockTarget.getBlock();
            BlockState state = block.getState();
            if (state instanceof InventoryHolder inventoryHolder) {
                Inventory container = inventoryHolder.getInventory();
                if (state instanceof Furnace oven) {
                    ItemStack cooking = container.getItem(0); // first item is the item being smelted
                    if (oven.getCookTime() > 0) cooking.setAmount(cooking.getAmount() - 1);
                    if (cooking != null && cooking.getAmount() <= 0) container.setItem(0, null);
                } else if (state instanceof Jukebox jukebox) { // Drop the currently playing record
                    Material mat = jukebox.getPlaying();
                    dropResult.addWithoutOverride(drop(where, new ItemStack(mat, 1), flags));
                }
                dropResult.addWithoutOverride(drop(where, container, flags));
            } else if (state instanceof CreatureSpawner) // Drop the creature in the spawner
                dropResult.addWithoutOverride(drop(where, flags.recipient, ((CreatureSpawner) state).getSpawnedType(), CreatureData.parse(((CreatureSpawner) state).getSpawnedType(), 0)));
        } else { // It's not a container block, so it must be an entity
            if (source instanceof PlayerSubject playerSubject)
                dropResult.addWithoutOverride(drop(where, playerSubject.getPlayer().getInventory(), flags));
            else if (source instanceof VehicleTarget vehicleTarget) {
                Entity vehicle = vehicleTarget.getVehicle();
                if (vehicle instanceof StorageMinecart storageMinecart)
                    dropResult.addWithoutOverride(drop(where, storageMinecart.getInventory(), flags));
            } else if (source instanceof CreatureSubject) {
                // Endermen!
                Entity creature = ((CreatureSubject) source).getAgent();
                if (creature instanceof Enderman) {
                    ItemStack stack = ((Enderman) creature).getCarriedMaterial().toItemStack(1);
                    dropResult.addWithoutOverride(drop(where, stack, flags));
                } else if (creature instanceof LivingEntity livingEntity) {
                    ItemStack stack = livingEntity.getEquipment().getItemInMainHand();
                    ItemStack stackOffHand = livingEntity.getEquipment().getItemInOffHand();
                    dropResult.addWithoutOverride(drop(where, stack, flags));
                    dropResult.addWithoutOverride(drop(where, stackOffHand, flags));

                    EntityEquipment mobEquipment = livingEntity.getEquipment();
                    ItemStack helmet = mobEquipment.getHelmet();
                    ItemStack chest = mobEquipment.getChestplate();
                    ItemStack legging = mobEquipment.getLeggings();
                    ItemStack boot = mobEquipment.getBoots();
                    if(helmet != null) dropResult.addWithoutOverride(drop(where, helmet, flags));
                    if(chest != null) dropResult.addWithoutOverride(drop(where, chest, flags));
                    if(legging != null) dropResult.addWithoutOverride(drop(where, legging, flags));
                    if(boot != null) dropResult.addWithoutOverride(drop(where, boot, flags));
                }
            }
        }

        return dropResult;
    }

    private static DropResult drop(Location where, Inventory container, DropFlags flags) {
        DropResult dropResult = new DropResult();
        for (ItemStack item : container.getContents()) {
            if (item == null) continue;
            dropResult.add(drop(where, item, flags));
        }
        return dropResult;
    }

    @Override
    public String getName() {
        return "CONTENTS";
    }

    @Override
    public double getAmount() {
        return 1;
    }

    @Override
    public DoubleRange getAmountRange() {
        return new DoubleRange(1.0);
    }
}
