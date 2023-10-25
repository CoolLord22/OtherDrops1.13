// Log.java - Bukkit Plugin Logger Wrapper
// Copyright (C) 2012 Zarius Tularial
//
// This file released under Evil Software License v1.1
// <http://fredrikvold.info/ESL.htm>

package com.gmail.zariust.otherdrops;

import com.gmail.zariust.common.Verbosity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Logger;

import static com.gmail.zariust.common.Verbosity.EXTREME;

public class Log {
    static ConsoleCommandSender console = null;
    static String pluginName = "";
    static Logger logger = Logger.getLogger("Minecraft");

    public Log(JavaPlugin plugin) {
        if (plugin != null) {
            pluginName = plugin.getDescription().getName();
        }
        console = Bukkit.getServer().getConsoleSender();
    }

    // LogInfo & Logwarning - display messages with a standard prefix
    public static void logWarning(String msg) {
        Log.logger.warning("[" + pluginName + "] " + msg);
    }

    public static void logInfo(List<String> msgs) {
        if (msgs == null || msgs.isEmpty())
            return;

        for (String msg : msgs) {
            logInfo(msg);
        }
    }

    public static void logInfo(String msg) {
        if (OtherDropsConfig.verbosity.exceeds(Verbosity.NORMAL))
            Log.logger.info("[" + pluginName + "] " + msg);
    }

    public static void logInfoNoVerbosity(String msg) {
        Log.logger.info("[" + pluginName + "] " + msg);
    }

    public static void dMsg(String msg) {
        // Deliberately doesn't check gColorLogMessage as I want these messages
        // to stand out in case they
        // are left in by accident
        if (OtherDropsConfig.verbosity.exceeds(Verbosity.HIGHEST))
            if (console != null && OtherDropsConfig.gColorLogMessages) {
                console.sendMessage(ChatColor.RED + "[" + pluginName + "] " + ChatColor.RESET + msg);
            } else {
                Log.logger.info("[" + pluginName + "] " + msg);
            }
    }

    // LogInfo & LogWarning - if given a level will report the message
    // only for that level & above
    public static void logInfo(String msg, Verbosity level) {
        if (OtherDropsConfig.verbosity.exceeds(level)) {
            if (console != null && OtherDropsConfig.gColorLogMessages) {
                ChatColor col = ChatColor.GREEN;
                switch (level) {
                    case EXTREME:
                        col = ChatColor.GOLD;
                        break;
                    case HIGHEST:
                        col = ChatColor.YELLOW;
                        break;
                    case HIGH:
                        col = ChatColor.AQUA;
                        break;
                    case NORMAL, LOW:
                        col = ChatColor.RESET;
                        break;
                    default:
                        break;
                }
                console.sendMessage(col + "[" + pluginName + "] " + ChatColor.RESET + msg);
            } else {
                Log.logger.info("[" + pluginName + "] " + msg);
            }
        }
    }

    public static void logWarning(String msg, Verbosity level) {
        if (OtherDropsConfig.verbosity.exceeds(level))
            logWarning(msg);
    }

    // TODO: This is only for temporary debug purposes.
    public static void stackTrace() {
        if (OtherDropsConfig.verbosity.exceeds(EXTREME))
            Thread.dumpStack();
    }
}
