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

package main.java.com.gmail.zariust.otherdrops.drop;

import main.java.com.gmail.zariust.otherdrops.data.ContainerData;
import main.java.com.gmail.zariust.otherdrops.data.Data;
import main.java.com.gmail.zariust.otherdrops.data.VehicleData;
import main.java.com.gmail.zariust.otherdrops.options.DoubleRange;
import main.java.com.gmail.zariust.otherdrops.options.IntRange;
import main.java.com.gmail.zariust.otherdrops.subject.Target;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Painting;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.StorageMinecart;

public class VehicleDrop extends DropType {
    private final Material vessel;
    private final IntRange quantity;
    private int rolledQuantity;
    private final Data data;

    public VehicleDrop(Material vehicle) {
        this(new IntRange(1), vehicle);
    }

    public VehicleDrop(Material vehicle, double percent) {
        this(new IntRange(1), vehicle, percent);
    }

    public VehicleDrop(IntRange amount, Material vehicle) {
        this(amount, vehicle, 100.0);
    }

    public VehicleDrop(IntRange amount, Material vehicle, double percent) {
        this(amount, vehicle, null, percent);
    }

    public VehicleDrop(IntRange amount, Material vehicle, Data d, double percent) {
        super(DropCategory.VEHICLE, percent);
        vessel = vehicle;
        quantity = amount;
        data = d;
    }

    @Override
    protected DropResult performDrop(Target source, Location where, DropFlags flags) {
        DropResult dropResult = DropResult.fromOverride(this.overrideDefault);

        int quantityActuallyDropped = 0;
        World world = where.getWorld();
        rolledQuantity = quantity.getRandomIn(flags.rng);
        int amount = rolledQuantity;
        while (amount-- > 0) {
            quantityActuallyDropped++;
            Entity entity;
            switch (vessel) {
                case OAK_BOAT:
                case SPRUCE_BOAT:
                case BIRCH_BOAT:
                case JUNGLE_BOAT:
                case ACACIA_BOAT:
                case DARK_OAK_BOAT:
                    entity = world.spawn(where, Boat.class);
                    break;
                case MINECART:
                    entity = world.spawn(where, Minecart.class);
                    break;
                case HOPPER_MINECART:
                    entity = world.spawn(where, HopperMinecart.class);
                    break;
                case TNT_MINECART:
                    entity = world.spawn(where, ExplosiveMinecart.class);
                    break;
                case COMMAND_BLOCK_MINECART:
                    entity = world.spawn(where, CommandMinecart.class);
                    break;
                case FURNACE_MINECART:
                    entity = world.spawn(where, PoweredMinecart.class);
                    break;
                case CHEST_MINECART:
                    entity = world.spawn(where, StorageMinecart.class);
                    break;
                case PAINTING: // Probably won't actually work
                    entity = world.spawn(where, Painting.class);
                    break;
                default:
                    continue;
            }
            data.setOn(entity, flags.recipient);
        }
        dropResult.setQuantity(quantityActuallyDropped);
        return dropResult;
    }

    public static DropType parse(String drop, String data, IntRange amount, double chance) {
        drop = drop.toUpperCase().replace("VEHICLE_", "");
        String[] split;
        if (drop.matches("\\w+:.*")) {
            split = drop.split(":", 2);
        } else split = drop.split("@", 2);
        if (split.length > 1) data = split[1];
        String name = split[0];
        switch (name) {
            case "OAK_BOAT" -> {
                return new VehicleDrop(amount, Material.OAK_BOAT, chance);
            }
            case "ACACIA_BOAT" -> {
                return new VehicleDrop(amount, Material.ACACIA_BOAT, chance);
            }
            case "BIRCH_BOAT" -> {
                return new VehicleDrop(amount, Material.BIRCH_BOAT, chance);
            }
            case "DARK_OAK_BOAT" -> {
                return new VehicleDrop(amount, Material.DARK_OAK_BOAT, chance);
            }
            case "SPRUCE_BOAT" -> {
                return new VehicleDrop(amount, Material.SPRUCE_BOAT, chance);
            }
            case "JUNGLE_BOAT" -> {
                return new VehicleDrop(amount, Material.JUNGLE_BOAT, chance);
            }
            case "FURNACE_MINECART" -> {
                return new VehicleDrop(amount, Material.FURNACE_MINECART, chance); // TODO: Power? (needs API?)
            }
            case "CHEST_MINECART" -> {
                Data state = ContainerData.parse(Material.CHEST_MINECART, data);
                return new VehicleDrop(amount, Material.CHEST_MINECART, state, chance);
            }
            case "TNT_MINECART" -> {
                return new VehicleDrop(amount, Material.TNT_MINECART, chance);
            }
            case "MINECART" -> {
                Data state = VehicleData.parse(Material.MINECART, data);
                return new VehicleDrop(amount, Material.MINECART, state, chance);
            }
            case "PAINTING" -> {
                return new VehicleDrop(amount, Material.PAINTING, chance); // TODO:Art? (needs API)
            }
        }
        return null;
    }

    @Override
    public String getName() {
        String ret = "VEHICLE_" + vessel.toString();
        if (data != null) ret += "@" + data.get(vessel);
        return ret;
    }

    @Override
    public double getAmount() {
        return rolledQuantity;
    }

    @Override
    public DoubleRange getAmountRange() {
        return quantity.toDoubleRange();
    }
}
