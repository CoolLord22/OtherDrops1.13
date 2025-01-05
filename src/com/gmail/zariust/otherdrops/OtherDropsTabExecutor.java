package com.gmail.zariust.otherdrops;

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
            if(args.length == 1) {
                return StringUtil.copyPartialMatches(args[0], Arrays.asList("saveitem", "id", "write", "reload", "show", "customspawn",
                        "settings", "disable", "enable", "drop", "triggers"), new ArrayList<>());
            } else if(args[0].equalsIgnoreCase("saveitem")) {
                return StringUtil.copyPartialMatches(args[1], List.of("<item key>"), new ArrayList<>());
            } else if(args[0].equalsIgnoreCase("id")) {
                return StringUtil.copyPartialMatches(args[1], Arrays.asList("mob", "block"), new ArrayList<>());
            } else if(args[0].equalsIgnoreCase("show")) {
                return StringUtil.copyPartialMatches(args[1], List.of("<block>"), new ArrayList<>());
            } else if(args[0].equalsIgnoreCase("drop")) {
                return StringUtil.copyPartialMatches(args[1], List.of("<drop string>"), new ArrayList<>());
            }
        }
        return null;
    }
}
