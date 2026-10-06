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

import com.gmail.zariust.common.CommonEntity;
import com.gmail.zariust.common.CommonMaterial;
import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.mob.CreatureData;
import com.gmail.zariust.otherdrops.options.DoubleRange;
import com.gmail.zariust.otherdrops.options.IntRange;
import com.gmail.zariust.otherdrops.subject.BlockTarget;
import com.gmail.zariust.otherdrops.subject.CreatureSubject;
import com.gmail.zariust.otherdrops.subject.Target;
import com.gmail.zariust.otherdrops.subject.VehicleTarget;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.block.data.type.TechnicalPiston;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Vehicle;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;

public class SelfDrop extends DropType {
    private final IntRange count;
    private int rolledCount;

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
    protected DropResult performDrop(Target source, Location from, DropFlags flags) {
        DropResult dropResult = DropResult.fromOverride(this.overrideDefault);

        if (source instanceof CreatureSubject creatureSubject) {
            Entity mob = creatureSubject.getAgent();
            Data data = CreatureData.parse(mob);
            // Data data = new CreatureData(CommonEntity.getCreatureData(mob));
            EntityType type = mob.getType();
            dropResult.addWithoutOverride(drop(from, flags.recipient, type, data));
        } else if (source instanceof VehicleTarget vehicleTarget) {
            Entity entity = vehicleTarget.getVehicle();
            if (entity instanceof Painting) {
                dropResult.addWithoutOverride(drop(from, new ItemStack(Material.PAINTING, 1), flags));
            } else if (entity instanceof Vehicle) {
                Material material = CommonEntity.getVehicleType(entity);
                dropResult.addWithoutOverride(drop(from, new ItemStack(material, 1), flags));
            } else return dropResult;
        } else if (source instanceof BlockTarget blockTarget) {
            Block block = blockTarget.getBlock();
            Material itemType = itemFor(block);
            if (itemType == null) return dropResult;        // air, fire, liquids…: nothing to drop
            int quantity = count.getRandomIn(flags.rng);
            ItemStack stack = new ItemStack(itemType, quantity);
            if (keepsBlockState(itemType)) copyBlockState(block, stack);
            dropResult.addWithoutOverride(drop(from, stack, flags));
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

    /** Minecraft 1.20.2+ knows the item for every block; on older versions we fall back to the rules below. */
    private static final Method PLACEMENT_MATERIAL = findPlacementMaterial();
    private static Method findPlacementMaterial() {
        try {
            return BlockData.class.getMethod("getPlacementMaterial");
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
    /** The item for a block, like pick-block: REDSTONE_WIRE -> REDSTONE, OAK_WALL_SIGN -> OAK_SIGN… Null if none. */
    private static Material itemFor(Block block) {
        Material type = block.getType();
        if (type.isAir()) return null;
        if (type == Material.MOVING_PISTON || type == Material.PISTON_HEAD)
            return block.getBlockData() instanceof TechnicalPiston piston && piston.getType() == TechnicalPiston.Type.STICKY
                    ? Material.STICKY_PISTON : Material.PISTON;
        if (PLACEMENT_MATERIAL != null) {
            try {
                if (PLACEMENT_MATERIAL.invoke(block.getBlockData()) instanceof Material m && m.isItem() && !m.isAir()) return m;
            } catch (ReflectiveOperationException ignored) {
            }
        }
        if (type.isItem()) return type;
        Material mapped = switch (type.name()) {
            case "REDSTONE_WIRE" -> Material.REDSTONE;
            case "TRIPWIRE" -> Material.STRING;
            case "WHEAT" -> Material.WHEAT_SEEDS;
            case "CARROTS" -> Material.CARROT;
            case "POTATOES" -> Material.POTATO;
            case "BEETROOTS" -> Material.BEETROOT_SEEDS;
            case "COCOA" -> Material.COCOA_BEANS;
            case "MELON_STEM", "ATTACHED_MELON_STEM" -> Material.MELON_SEEDS;
            case "PUMPKIN_STEM", "ATTACHED_PUMPKIN_STEM" -> Material.PUMPKIN_SEEDS;
            case "SWEET_BERRY_BUSH" -> Material.SWEET_BERRIES;
            case "CAVE_VINES", "CAVE_VINES_PLANT" -> Material.GLOW_BERRIES;
            case "KELP_PLANT" -> Material.KELP;
            case "BAMBOO_SAPLING" -> Material.BAMBOO;
            default -> null;
        };
        if (mapped != null) return mapped;
        // wall variants: OAK_WALL_SIGN -> OAK_SIGN, WALL_TORCH -> TORCH, *_WALL_BANNER, *_WALL_HEAD, *_WALL_FAN…
        Material unwalled = Material.matchMaterial(type.name().replace("WALL_", ""));
        return unwalled != null && unwalled.isItem() ? unwalled : null;
    }

    /** Blocks whose THIS item keeps their settings, like silk touch / Ctrl+pick-block:
     *  spawners and trial spawners (mob type, spawn settings), beehives and bee nests (the bees and the honey level). */
    private static boolean keepsBlockState(Material type) {
        return CommonMaterial.isSpawner(type) || type == Material.BEEHIVE || type == Material.BEE_NEST;
    }
    private static void copyBlockState(Block block, ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        try {
            if (meta instanceof BlockStateMeta stateMeta) stateMeta.setBlockState(block.getState());  // mob type, bees inside…
            if (block.getBlockData() instanceof Beehive hive && meta instanceof BlockDataMeta dataMeta) {
                // only the honey level, so the hive still faces the player when it's placed again
                dataMeta.setBlockData(Bukkit.createBlockData(block.getType(), "[honey_level=" + hive.getHoneyLevel() + "]"));
            }
            stack.setItemMeta(meta);
        } catch (IllegalArgumentException e) { // a server version whose item meta can't hold this state
            Log.logInfo("THIS: couldn't copy the " + block.getType() + "'s settings to the item (" + e.getMessage() + ").", Verbosity.HIGH);
        }
    }
}
