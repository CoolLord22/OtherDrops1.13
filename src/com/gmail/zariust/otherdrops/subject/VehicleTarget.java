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

package com.gmail.zariust.otherdrops.subject;

import com.gmail.zariust.common.CommonEntity;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.data.ContainerData;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.SimpleData;
import com.gmail.zariust.otherdrops.data.VehicleData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.*;

import java.util.Collections;
import java.util.List;

public class VehicleTarget implements Target {
    private final Material material;
    private final Data data;
    private Entity vessel;

    public VehicleTarget() {
        this(null, null);
    }

    public VehicleTarget(Painting painting) {
        this(Material.PAINTING, new SimpleData(painting));
        vessel = painting;
    }

    public VehicleTarget(Vehicle vehicle) {
        this(CommonEntity.getVehicleType(vehicle), getVehicleData(vehicle));
        vessel = vehicle;
    }

    protected VehicleTarget(Material type, Data d) {
        material = type;
        data = d;
    }

    private static Data getVehicleData(Vehicle vehicle) {
        if (vehicle instanceof StorageMinecart) return new ContainerData((StorageMinecart) vehicle);
        else if (vehicle instanceof HopperMinecart) return new ContainerData((HopperMinecart) vehicle);
        else if (vehicle instanceof PoweredMinecart) return new SimpleData();
        else if (vehicle instanceof CommandMinecart) return new SimpleData();
        else if (vehicle instanceof ExplosiveMinecart) return new SimpleData();
        else if (vehicle instanceof Boat || vehicle instanceof Minecart) return new VehicleData(vehicle);
        return null;
    }

    @Override
    public ItemCategory getType() {
        return ItemCategory.BLOCK; // TODO: Should we add an ItemCategory.VEHICLE?
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof VehicleTarget targ)) return false;
        return material == targ.material && data.equals(targ.data);
    }

    @Override
    public int hashCode() {
        return new HashCode(this).get(material);
    }

    @Override
    public boolean matches(Subject block) {
        if (!(block instanceof VehicleTarget targ)) return false;

        boolean match;
        if (material == targ.material) match = true;
        if (data == null) { // Null data matches vehicles but not painting
            match = targ.material != Material.PAINTING;
        } else {
            match = data.matches(targ.data);
        }
        return match;
    }

    @Override
    public Location getLocation() {
        if (vessel == null) return null;
        return vessel.getLocation();
    }

    @Override
    public boolean overrideOn100Percent() {
        return true;
    }

    @Override
    public List<Target> canMatch() {
        return Collections.singletonList(this);
    }

    @Override
    public String getKey() {
        return material.toString();
    }

    @Override
    public void setTo(BlockTarget replacement) {
        if (vessel == null) {
            Log.logWarning("VehicleTarget had a null entity; could not remove it and replace with blocks.");
            return;
        }
        Block bl = vessel.getLocation().getBlock();
        new BlockTarget(bl).setTo(replacement);
        vessel.remove();
    }

    public Entity getVehicle() {
        return vessel;
    }

    @SuppressWarnings("incomplete-switch")
    public static Target parse(Material type, String state) {
        if (type == null) return null;
        Data data = null;
        try {
            data = switch (type) {
                case OAK_BOAT, SPRUCE_BOAT, BIRCH_BOAT, JUNGLE_BOAT, ACACIA_BOAT, DARK_OAK_BOAT, MINECART -> VehicleData.parse(type, state);
                case HOPPER_MINECART, CHEST_MINECART -> ContainerData.parse(type, state);
                case COMMAND_BLOCK_MINECART, TNT_MINECART, FURNACE_MINECART, PAINTING -> SimpleData.parse(type, state);
                default -> data;
            };
        } catch (IllegalArgumentException e) {
            Log.logWarning(e.getMessage());
            return null;
        }
        return new VehicleTarget(type, data);
    }

    @Override
    public Data getData() {
        return data;
    }

    @Override
    public String toString() {
        if (material == null) return "ANY_VEHICLE";
        String ret = "VEHICLE_" + material;
        // TODO: Will data ever be null, or will it just be 0?
        if (data != null) ret += "@" + data.get(material);
        return ret;
    }

    @Override
    public String getReadableName() {
        return toString();
    }
}
