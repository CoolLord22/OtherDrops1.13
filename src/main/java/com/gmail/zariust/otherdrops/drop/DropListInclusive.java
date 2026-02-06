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

import main.java.com.gmail.zariust.common.CreatureGroup;
import main.java.com.gmail.zariust.common.MaterialGroup;
import main.java.com.gmail.zariust.otherdrops.options.DoubleRange;
import main.java.com.gmail.zariust.otherdrops.options.IntRange;
import main.java.com.gmail.zariust.otherdrops.subject.Target;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class represents an set of inclusive drops, eg. drop: [blah, blah2, etc]
 * # drops each item in list
 *
 * @author Celtic Minstrel, Zarius
 */
public class DropListInclusive extends DropType {
    private final List<DropType> group;

    public DropListInclusive(DropType... drops) {
        this(Arrays.asList(drops));
    }

    public DropListInclusive(List<DropType> drops) {
        super(DropCategory.GROUP);
        group = drops;
    }

    public DropListInclusive(List<Material> materials, int defaultData, IntRange amount, double chance) {
        this(materialsToDrops(materials, defaultData, amount, chance));
    }

    public DropListInclusive(List<EntityType> creatures, IntRange amount, double chance) {
        this(creaturesToDrops(creatures, amount, chance));
    }

    private static DropType[] materialsToDrops(List<Material> materials, int defaultData, IntRange amount, double chance) {
        DropType[] drops = new DropType[materials.size()];
        for (int i = 0; i < drops.length; i++) {
            drops[i] = new ItemDrop(amount, materials.get(i), defaultData, chance, null, "");
        }
        return drops;
    }

    private static DropType[] creaturesToDrops(List<EntityType> creatures, IntRange amount, double chance) {
        DropType[] drops = new DropType[creatures.size()];
        for (int i = 0; i < drops.length; i++) {
            drops[i] = new CreatureDrop(amount, creatures.get(i), chance);
        }
        return drops;
    }

    public List<DropType> getGroup() {
        return group;
    }

    @Override
    protected DropResult performDrop(Target source, Location where, DropFlags flags) {
        // don't set override default - it's set for each individual drop
        DropResult returnRes = DropResult.fromQuantity(0);

        for (DropType drop : group) {
            returnRes.add(drop.drop(source.getLocation(), source, null, 1, flags));
        }
        return returnRes;
    }

    public static DropType parse(List<String> dropList, String defaultData) {
        List<DropType> drops = new ArrayList<>();
        for (String dropName : dropList) {
            DropType drop = DropType.parse(dropName, defaultData);
            if (drop != null) drops.add(drop);
        }
        return new DropListInclusive(drops);
    }

    public static DropType parse(String drop, String data, IntRange amount, double chance) {
        drop = drop.toUpperCase().replace("EVERY_", "^ANY_");
        MaterialGroup group = MaterialGroup.get(drop.substring(1));
        if (group == null) {
            if (drop.equals("^ANY_CREATURE"))
                return new DropListInclusive(CreatureGroup.CREATURE_ANY.creatures(), amount, chance);
            else if (drop.equals("^ANY_VEHICLE_SPAWN"))
                return new DropListInclusive(new VehicleDrop(amount, Material.MINECART, chance), new VehicleDrop(amount, Material.COMMAND_BLOCK_MINECART, chance), new VehicleDrop(amount, Material.TNT_MINECART, chance), new VehicleDrop(amount, Material.FURNACE_MINECART, chance), new VehicleDrop(amount, Material.HOPPER_MINECART, chance), new VehicleDrop(amount, Material.CHEST_MINECART, chance), new VehicleDrop(amount, Material.OAK_BOAT, chance), new VehicleDrop(amount, Material.ACACIA_BOAT, chance), new VehicleDrop(amount, Material.BIRCH_BOAT, chance), new VehicleDrop(amount, Material.DARK_OAK_BOAT, chance), new VehicleDrop(amount, Material.JUNGLE_BOAT, chance), new VehicleDrop(amount, Material.SPRUCE_BOAT, chance));
            else {
                drop = drop.replace("^ANY_", "^");
                CreatureGroup cgroup = CreatureGroup.get(drop.substring(1));
                if (cgroup != null) return new DropListInclusive(cgroup.creatures(), amount, chance);
            }
            return null;
        }
        int intData = 0;
        try {
            intData = Integer.parseInt(data);
        } catch (NumberFormatException ignored) {
        }
        return new DropListInclusive(group.materials(), intData, amount, chance);
    }

    @Override
    public String getName() {
        return group.toString();
    }

    @Override
    public double getAmount() {
        // TODO: Should it return group.size()?
        return 1;
    }

    @Override
    public DoubleRange getAmountRange() {
        // TODO: Should it return group.size()?
        return new DoubleRange(1.0);
    }
}
