package com.gmail.zariust.otherdrops;

import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OtherDropsTabExecutor implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(command.getName().equalsIgnoreCase("od")) {
            ArrayList<String> list = new ArrayList<String>();
            String lastArg = "";
            if(args.length == 1) {
                return StringUtil.copyPartialMatches(args[0], Arrays.asList("saveitem", "id", "write", "reload", "show", "customspawn",
                        "settings", "disable", "enable", "drop", "triggers"), new ArrayList<>());
            } else if(args[0].equalsIgnoreCase("saveitem")) {
                list.add("<item key>");
                for(NamespacedKey key : OtherDrops.loadedItems.keySet()) {
                    list.add(key.getKey().replaceAll("od_item_", "OD_ITEM@"));
                }
                lastArg = args[1];
            } else if(args[0].equalsIgnoreCase("id")) {
                return StringUtil.copyPartialMatches(args[1], Arrays.asList("mob", "block"), new ArrayList<>());
            } else if(args[0].equalsIgnoreCase("show")) {
                return StringUtil.copyPartialMatches(args[1], List.of("<block>"), new ArrayList<>());
            } else if(args[0].equalsIgnoreCase("drop")) {
                list.add("<drop string>");
                for(NamespacedKey key : OtherDrops.loadedItems.keySet()) {
                    list.add(key.getKey().replaceAll("od_item_", "OD_ITEM@"));
                }
                lastArg = args[1];
            }
            return StringUtil.copyPartialMatches(lastArg, list, new ArrayList<>());
        }
        return null;
    }
}
