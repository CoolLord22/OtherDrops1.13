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

package com.gmail.zariust.otherdrops;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.data.CreatureData;
import com.gmail.zariust.otherdrops.drop.DropResult;
import com.gmail.zariust.otherdrops.drop.DropType;
import com.gmail.zariust.otherdrops.drop.DropType.DropFlags;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.DropsList;
import com.gmail.zariust.otherdrops.event.GroupDropEvent;
import com.gmail.zariust.otherdrops.event.SimpleDrop;
import com.gmail.zariust.otherdrops.parameters.Trigger;
import com.gmail.zariust.otherdrops.parameters.conditions.Cooldown;
import com.gmail.zariust.otherdrops.subject.PlayerSubject;
import com.gmail.zariust.otherdrops.subject.Target;
import com.herocraftonline.heroes.characters.Hero;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.gmail.zariust.otherdrops.OtherDrops.SPAWNED_BY;

public class OtherDropsCommand implements CommandExecutor {
    private enum OBCommand {
        SAVEITEM("saveitem", "si", "otherdrops.admin.saveitem"),
        ID("id", "i", "otherdrops.admin.id"),
        WRITE("write", "w", "otherdrops.admin.id"),
        RELOAD("reload", "r", "otherdrops.admin.reloadconfig"),
        SHOW("show", "s", "otherdrops.admin.show"),
        CUSTOMSPAWN("customspawn", "c", "otherdrops.admin.customspawn"),
        SETTINGS("settings", "st", "otherdrops.admin.settings"),
        DISABLE("disable,disabled,off", "", "otherdrops.admin.enabledisable"),
        ENABLE("enable,enabled,on", "e", "otherdrops.admin.enabledisable"),
        HEROESTEST("heroestest", "ht", ""),
        DROP("drop", "d,o", "otherdrops.admin.drop"),
        TRIGGERS("triggers", "t", "otherdrops.admin.triggers");

        private final String cmdName;
        private final String cmdShort;
        private final String perm;

        OBCommand(String name, String abbr, String perm) {
            cmdName = name;
            cmdShort = abbr;
            this.perm = perm;
        }

        public static OBCommand match(String label, String firstArg) {
            boolean arg = label.equalsIgnoreCase("od");
            for (OBCommand cmd : values()) {
                if (arg) {
                    for (String item : cmd.cmdName.split(",")) {
                        if (firstArg.equalsIgnoreCase(item)) return cmd;
                    }
                } else if (label.equalsIgnoreCase("od" + cmd.cmdShort) || label.equalsIgnoreCase("od" + cmd.cmdName))
                    return cmd;
                else {
                    for (String shortcut : cmd.cmdShort.split(",")) {
                        if (label.equalsIgnoreCase("od" + shortcut)) return cmd;

                        // special case for "o" as a shortcut by itself (eg. "/o")
                        if (shortcut.equalsIgnoreCase("o") && label.equalsIgnoreCase(shortcut)) return cmd;
                    }
                }
            }
            return null;
        }

        public String[] trim(String[] args, StringBuffer name) {
            if (args.length == 0) return args;
            if (!args[0].equalsIgnoreCase(cmdName)) return args;
            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, newArgs.length);
            if (name != null) name.append(" ").append(args[0]);
            return newArgs;
        }
    }

    private final OtherDrops otherdrops;
    public static class dropStringLoc {
        String dropString;
        Location loc;
        Player player;

        public dropStringLoc(String drop, Location location, Player thisPlayer) {
            dropString = drop;
            loc = location;
            player = thisPlayer;
        }
    }

    public OtherDropsCommand(OtherDrops plugin) {
        otherdrops = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        OBCommand cmd = OBCommand.match(label, args.length >= 1 ? args[0] : "");
        if (cmd == null) return false;
        StringBuffer cmdName = new StringBuffer(label);
        args = cmd.trim(args, cmdName);

        if (!checkCommandPermissions(sender, args, cmd)) return true;

        switch (cmd) {
            case SAVEITEM:
                cmdSaveItem(sender, args);
                break;
            case ID:
                cmdId(sender, args);
                break;
            case RELOAD:
                cmdReload(sender);
                break;
            case CUSTOMSPAWN:
                cmdCustomSpawn(sender, args, cmdName);
            case SHOW:
                cmdShow(sender, args, cmdName);
                break;
            case SETTINGS:
                cmdSettings(sender);
                break;
            case ENABLE:
                cmdEnable(sender);
                break;
            case DISABLE:
                cmdDisable(sender);
                break;
            case DROP:
                cmdDrop(sender, args);
                break;
            case HEROESTEST:
                cmdHeroesTest(sender);
                break;
            case WRITE:
                cmdWriteFile(sender);
                break;
            case TRIGGERS:
                StringBuilder triggers = new StringBuilder();
                for (Trigger value : Trigger.values()) {
                    triggers.append(value.toString()).append("&f, &a");
                }
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Available OtherDrops triggers: &a" + triggers.substring(0, triggers.length() - 6)));
                break;
            default:
                break;

        }
        return true;
    }

    private void cmdHeroesTest(CommandSender sender) {
        if (sender instanceof Player playerSender) {
            if (Dependencies.hasHeroes()) {
                sender.sendMessage("Player is in class: " + playerSender.getDisplayName() + "->" + Dependencies.getHeroes().getCharacterManager().getHero(playerSender).getClass());
                sender.sendMessage("Other players Heroes classes output to server.log");
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {

                    Hero heroPlayer = Dependencies.getHeroes().getCharacterManager().getHero(player);
                    Log.logInfo("Player:" + player.getDisplayName() + "->" + heroPlayer.getHeroClass().toString() + "(" + heroPlayer.getLevel() + ")");
                }
            }
        }
    }

    private void cmdDrop(CommandSender sender, String[] args) {
        if (args.length > 0) {
            dropStringLoc dsl = new dropStringLoc("", null, null);
            getLocationFromDropString(sender, args, dsl);

            sender.sendMessage("Dropped at: " + dsl.loc.toString());
            if (dsl.loc != null) {
                dsl.dropString = dsl.dropString.substring(0, dsl.dropString.length() - 1);
                DropType drop = DropType.parse(dsl.dropString, "");
                if (drop == null) {
                    sender.sendMessage("ODDrop - failed to parse drop.");
                    return;
                }

                DropFlags flags = DropType.flags(dsl.player, (dsl.player == null ? null : new PlayerSubject(dsl.player, EquipmentSlot.HAND)), false, true, false, OtherDrops.rng, "odd", "odd", "");
                DropResult dropResult = drop.drop(dsl.loc, null, null, 1, flags);

                String dropped;
                dropped = dropResult.getDroppedString();
                sender.sendMessage("Dropped: " + dropResult.getQuantity() + "x" + dropped);
            }
        } else {
            sender.sendMessage("Usage: /odd <item/mob>@<data> - drops the given item or mob");
        }
    }

    private void cmdDisable(CommandSender sender) {
        if (otherdrops.enabled) {
            OtherDrops.disableOtherDrops();
            sender.sendMessage(ChatColor.RED + "OtherDrops disabled.");
        } else {
            sender.sendMessage(ChatColor.GRAY + "OtherDrops is already disabled.");
        }
    }

    private void cmdEnable(CommandSender sender) {
        if (!otherdrops.enabled) {
            OtherDrops.enableOtherDrops();
            sender.sendMessage(ChatColor.GREEN + "OtherDrops enabled.");
        } else {
            sender.sendMessage(ChatColor.GRAY + "OtherDrops is already enabled.");
        }
    }

    private void cmdSettings(CommandSender sender) {
        sender.sendMessage("OtherDrops settings:");
        sender.sendMessage((otherdrops.enabled ? ChatColor.GREEN + "OtherDrops enabled." : ChatColor.RED + "OtherDrops disabled."));
        sender.sendMessage("Verbosity: " + ChatColor.GRAY + OtherDropsConfig.getVerbosity());
        sender.sendMessage("Disable XP if no default drop: " + ChatColor.GRAY + OtherDropsConfig.disableXpOnNonDefault);
        sender.sendMessage("Money Precision (for messages): " + ChatColor.GRAY + OtherDropsConfig.moneyPrecision);
        sender.sendMessage("Use unsafe enchantments: " + ChatColor.GRAY + OtherDropsConfig.enchantmentsUseUnsafe);
        sender.sendMessage("Ignore enchantment start/maxlevel: " + ChatColor.GRAY + OtherDropsConfig.enchantmentsIgnoreLevel);
    }

    private void cmdCustomSpawn(CommandSender sender, String[] args, StringBuffer cmdName) {

        if (args.length == 0) {
            sender.sendMessage("Error, no drop specified. Please use /" + cmdName + " <custommob>, optionally /" + cmdName + " w:world x y z <custommob>/<quantity>");
            return;
        }


        dropStringLoc dsl = new dropStringLoc("", null, null);
        getLocationFromDropString(sender, args, dsl);

        String[] split = dsl.dropString.split("/");
        dsl.dropString = split[0];
        int quantity = 1;
        if (split.length > 1) {
            quantity = Integer.parseInt(split[1].trim());
        }
        Log.logInfo("Attempting to spawn: " + dsl.dropString + " (x" + quantity + ") at location " + dsl.loc.toString(), Verbosity.HIGHEST);
        for (int i = 0; i < quantity; i++) {// (String arg : args) {
            try {
                CustomMobSupport.spawnCustomMob(dsl.dropString.trim(), dsl.loc);
            } catch (Exception e) {
                Log.logInfo("Failed to spawn mob: " + e);
            }
        }
    }

    private void cmdShow(CommandSender sender, String[] args, StringBuffer cmdName) {
        if (args.length == 0) {
            sender.sendMessage("Error, no block. Please use /" + cmdName + " <block>");
            return;
        }
        Target target = OtherDropsConfig.parseTarget(args[0]);
        for (Trigger trigger : Trigger.values())
            showBlockInfo(sender, trigger, target);
    }

    private void cmdReload(CommandSender sender) {
        otherdrops.config.load(sender);
        Cooldown.cooldowns.clear();
        sender.sendMessage("OtherDrops config reloaded.");
        Log.logInfo("Config reloaded by " + getName(sender) + ".");
    }

    private void cmdId(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {
            ItemStack playerItem = player.getInventory().getItemInMainHand();

            if (args.length > 0 && args[0].toLowerCase().matches("(mob|creature)")) {
                Entity mob = getTarget(player);
                if (mob instanceof LivingEntity le) {
                    // TODO: parse via CreatureDrop (need to create CreatureDrop.parse(entity)
                    String spawnReason = mob.getPersistentDataContainer().getOrDefault(SPAWNED_BY, PersistentDataType.STRING,  "not set");
                    sender.sendMessage("OdId: mob details: " + mob.getType() + "@" + CreatureData.parse(mob) + " spawnedby: " + spawnReason + CustomMobSupport.getCustomMobName(le));
                } else {
                    sender.sendMessage("No living entity found.");
                }
            } else {
                String itemMsg = playerItem.getType() + "@" + playerItem.getDurability() + " maxdura:" + playerItem.getType().getMaxDurability() + " dura%:" + getDurabilityPercentage(playerItem) + " detail: " + playerItem;
                if (playerItem.getItemMeta() != null) {
                    playerItem.getItemMeta().getDisplayName();
                    itemMsg += " name: \"" + playerItem.getItemMeta().getDisplayName().replaceAll(" §", "&") + "\"";
                }

                ((Player) sender).sendRawMessage(ChatColor.GREEN + "Item in hand: " + ChatColor.WHITE + itemMsg.replaceAll("§", "&"));
                sender.sendMessage("");

                Block block = player.getTargetBlock(new HashSet<>(), 100);
                ((Player) sender).sendRawMessage(ChatColor.GREEN + "Block looked at is " + ChatColor.WHITE + block + " mat: " + block.getType() + " lightlevel: " + block.getLightLevel() + " lightfromsky: " + block.getLightFromSky() + " biome: " + block.getBiome());
            }

            StringBuilder itemFinalWriteData = new StringBuilder();

            itemFinalWriteData.append(playerItem.getType());
            itemFinalWriteData.append("@").append(playerItem.getDurability());
            if (!playerItem.getEnchantments().isEmpty()) {
                itemFinalWriteData.append("!");
                for (Enchantment enchInMap : playerItem.getEnchantments().keySet()) {
                    itemFinalWriteData.append(enchInMap.getKey().toString().replace("minecraft:", "")).append("#").append(playerItem.getEnchantmentLevel(enchInMap)).append("!");
                }
            }
            if (playerItem.getItemMeta() != null) {
                itemFinalWriteData.append("~").append(playerItem.getItemMeta().getDisplayName());
                if (playerItem.getItemMeta().getLore() != null) {
                    List<String> loreList = playerItem.getItemMeta().getLore();
                    for (String loreLine : loreList) {
                        itemFinalWriteData.append(";").append(loreLine);
                    }
                }
            }
            sender.sendMessage("");
            player.sendRawMessage(ChatColor.GREEN + "The item config is:§r " + ChatColor.WHITE + itemFinalWriteData.toString().replaceAll("§", "&"));
        }
    }

    private void cmdWriteFile(CommandSender sender) {
        if (sender instanceof Player player) {
            File folder = new File("plugins" + File.separator + "OtherDrops");
            BufferedWriter out;
            ItemStack playerItem = player.getInventory().getItemInMainHand();
            StringBuilder itemFinalWriteData = new StringBuilder();
            itemFinalWriteData.append(playerItem.getType());
            itemFinalWriteData.append("@").append(playerItem.getDurability());
            if (!playerItem.getEnchantments().isEmpty()) {
                itemFinalWriteData.append("!");
                for (Enchantment enchInMap : playerItem.getEnchantments().keySet()) {
                    itemFinalWriteData.append(enchInMap.getKey().toString().replace("minecraft:", "")).append("#").append(playerItem.getEnchantmentLevel(enchInMap)).append("!");
                }
            }
            if (playerItem.getItemMeta() != null) {
                itemFinalWriteData.append("~").append(playerItem.getItemMeta().getDisplayName());
                if (playerItem.getItemMeta().getLore() != null) {
                    List<String> loreList = playerItem.getItemMeta().getLore();
                    for (String loreLine : loreList) {
                        itemFinalWriteData.append(";").append(loreLine);
                    }
                }
            }
            try {
                File configFile = new File(folder.getAbsolutePath() + File.separator + "ItemOutput" + ".txt");
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
                out = new BufferedWriter(new FileWriter(configFile));
                out.write(itemFinalWriteData + "\n");
                out.close();
            } catch (IOException exception) {
                Log.logError("Encountered an error while writing ItemOutput.", exception);
            }

            player.sendRawMessage(ChatColor.GREEN + "The item config is:§r " + ChatColor.WHITE + itemFinalWriteData.toString().replaceAll("§", "&"));
        }
    }

    private void cmdSaveItem(CommandSender sender, String[] args) {
        if (args.length > 0) {
            String key = args[0].toLowerCase().replaceAll("od_item@", "");
            if (sender instanceof Player player) {
                ItemStack playerItem = player.getInventory().getItemInMainHand().clone();
                if (OtherDropsConfig.commonItemstack.saveODItemStack(key, playerItem)) {
                    ((Player) sender).sendRawMessage(ChatColor.GREEN + "Successfully saved item " + key);
                    return;
                } else ((Player) sender).sendRawMessage(ChatColor.RED + "An error occurred, please check console!");
            }
        }

        sender.sendMessage(ChatColor.RED + "Must be a player, usage: /od saveitem <key>");
    }

    // returns null if not durability is not valid (ie. Has no maxdurability)
    private Double getDurabilityPercentage(ItemStack item) {
        double maxDura = item.getType().getMaxDurability();
        double dura = item.getDurability();

        if (maxDura < 1) return null;
        return (double) (Math.round((float) (1 - (dura / maxDura)) * 10000) / 100);
    }

    private static Entity getTarget(final Player player) {
        BlockIterator iterator = new BlockIterator(player.getWorld(), player.getLocation().toVector(), player.getEyeLocation().getDirection(), 0, 100);
        while (iterator.hasNext()) {
            Block item = iterator.next();
            for (Entity entity : player.getNearbyEntities(100, 100, 100)) {
                int acc = 2;
                for (int x = -acc; x < acc; x++)
                    for (int z = -acc; z < acc; z++)
                        for (int y = -acc; y < acc; y++)
                            if (entity.getLocation().getBlock().getRelative(x, y, z).equals(item)) {
                                return entity;
                            }
            }
        }
        return null;
    }

    private String getName(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) return "CONSOLE";
        else if (sender instanceof Player) return sender.getName();
        else return "UNKNOWN";
    }

    private String stringHelper(Object object) {
        if (object == null) return null;
        else return object.toString();
    }

    private void showBlockInfo(CommandSender sender, Trigger trigger, Target block) {
        StringBuilder message = new StringBuilder();
        message.append("Block §a").append(block).append("§f (").append(trigger).append("):");

        DropsList dropGroups = otherdrops.config.blocksHash.getList(trigger, block);
        int i = 1;

        if (dropGroups != null) {
            for (CustomDrop drop : dropGroups) {
                if (drop != null) {
                    message.append("\n §fDrop Number: §a").append(i++);
                    if (drop instanceof GroupDropEvent) addDropInfo(message, (GroupDropEvent) drop);
                    else addDropInfo(message, (SimpleDrop) drop);
                }
                message.append("\n ");
            }
            sender.sendMessage(message.toString());
        }
    }

    private void addDropConditions(StringBuilder message, CustomDrop drop) {
        Map<String, String> messageMap = new HashMap<>();

        // Conditions
        messageMap.put("Agent", drop.getToolString()); // make null if "any"
        // Chance and delay
        messageMap.put("Chance", String.valueOf(drop.getChance())); // make null
        // if = 100
        messageMap.put("Exclusive key", drop.getExclusiveKey());
        messageMap.put("Delay", drop.getDelayRange()); // make null if 0

        for (Entry<String, String> entry : messageMap.entrySet()) {
            if (entry.getValue() != null) {
                message.append("\n  §7").append(entry.getKey()).append(": §f").append(entry.getValue());
            }
        }
    }

    private void addDropInfo(StringBuilder message, SimpleDrop drop) {
        addDropConditions(message, drop);
        Map<String, String> messageMap = new HashMap<>();

        messageMap.put("Drop", stringHelper(drop.getDropped())); // TODO: this
        // returns the
        // object, not
        // a string?
        messageMap.put("Quantity", stringHelper(drop.getQuantityRange())); // make
        // null
        // if
        // 1
        // (Default)
        messageMap.put("Attacker damage", stringHelper(drop.getAttackerDamageRange()));
        messageMap.put("Tool damage", stringHelper(drop.getToolDamage()));
        messageMap.put("Drop spread", drop.getDropSpreadChance() + "% chance");
        messageMap.put("Replacement block", stringHelper(drop.getReplacement()));
        messageMap.put("Commands", stringHelper(drop.getCommands())); // make
        // null if
        // empty
        // list
        messageMap.put("Messages", drop.getMessagesString());
        messageMap.put("Sound effects", drop.getEffectsString());
        messageMap.put("Events", stringHelper(drop.getEvents()));

        for (Entry<String, String> entry : messageMap.entrySet()) {
            if (entry.getValue() != null) {
                message.append("\n  §7").append(entry.getKey()).append(":§f ").append(entry.getValue());
            }
        }
    }

    private void addDropInfo(StringBuilder message, GroupDropEvent group) {
        addDropConditions(message, group);
        message.append(" Drop group: ").append(group.getName());
        char j = 'A';
        for (CustomDrop subDrop : group.getDrops()) {
            message.append("\n §fNumber: §a").append(j++);
            if (j > 'Z') j = 'a';
            if (subDrop instanceof GroupDropEvent) addDropInfo(message, (GroupDropEvent) subDrop);
            else addDropInfo(message, (SimpleDrop) subDrop);
        }
    }

    private void getLocationFromDropString(CommandSender sender, String[] args, dropStringLoc dsl) {
        World world = null;
        String playerName = "unknown";
        if (sender instanceof Player) {
            dsl.player = (Player) sender;
            playerName = dsl.player.getDisplayName();
            // loc = player.getLocation();
            dsl.loc = dsl.player.getTargetBlock(new HashSet<>(), 100).getLocation().add(0, 1, 0); // (???, max distance)
            world = dsl.loc.getWorld();
        } else if (sender instanceof BlockCommandSender) {
            playerName = sender.getName();
            dsl.loc = ((BlockCommandSender) sender).getBlock().getLocation();
            world = dsl.loc.getWorld();
        }

        Float x = null, y = null, z = null;
        boolean notext = false;
        int xCounter = 0;
        for (int i = 0; i < args.length; i++) {// (String arg : args) {
            String arg = args[i];
            if (xCounter < 3 && !notext) {
                if (arg.matches("[0-9.-]+")) {
                    if (xCounter == 0) x = Float.valueOf(arg);
                    if (xCounter == 1) y = Float.valueOf(arg);
                    if (xCounter == 2) z = Float.valueOf(arg);
                    xCounter++;
                    continue;
                } else if (arg.matches("p:.*")) {
                    if (arg.matches("p:")) arg += args[1 + (i++)];
                    Log.dMsg("PLAYER FOUND" + arg);
                    try {
                        dsl.loc = Bukkit.getPlayer(arg.substring(2)).getLocation();
                    } catch (Exception ex) {
                        sender.sendMessage("Failed to locate player: " + arg.substring(2) + ", aborting.");
                        return;
                    }
                    // do not increment count
                    continue;
                } else if (arg.matches("w:.*")) {
                    if (arg.matches("w:")) arg += args[1 + (i++)];
                    try {
                        world = Bukkit.getWorld(arg.substring(2));
                    } catch (Exception ex) {
                        sender.sendMessage("Failed to locate world: " + arg.substring(2) + ", aborting.");
                        return;
                    }
                    // do not increment count
                    continue;
                }
            }
            dsl.dropString += arg + " ";
            notext = true;
        }
        if (world != null && x != null && y != null & z != null) {
            dsl.loc = new Location(world, x, y, z);
        } else if (dsl.loc == null) {
            sender.sendMessage("No valid location given, aborting.");
            if (sender instanceof BlockCommandSender) Log.logInfo("/odd from commandblock (" + playerName + "): No valid location given, aborting.");
            return;
        }

        if (dsl.dropString.isEmpty()) {
            sender.sendMessage("No drop given, aborting.");
            if (sender instanceof BlockCommandSender) Log.logInfo("/odd from commandblock (" + playerName + "): No drop given, aborting.");
        }
    }

    private boolean checkCommandPermissions(CommandSender sender, String[] args, OBCommand cmd) {
        boolean pass = false;
        if (cmd.perm.isEmpty()) pass = true;
        else if (Dependencies.hasPermission(sender, cmd.perm)) pass = true;

        if (!pass) sender.sendMessage("You don't have permission for this command.");
        return pass;
    }
}
