package com.gmail.zariust.otherdrops.parameters.actions;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.parameters.Action;
import com.gmail.zariust.otherdrops.things.ODVariables;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.*;

public class SpecialMessageAction extends Action {

    // TODO: Add logger messages
    public enum SendType {
        VICTIM, ATTACKER, RADIUS, WORLD, SERVER;

        public static SendType fromString(String s) {
            for (SendType type : SendType.values()) {
                if (type.name().equalsIgnoreCase(s)) {
                    return type;
                }
            }
            Log.logInfo("SpecialMessageAction - Invalid send-to type specified: (" + s + ") defaulting to ATTACKER.", Verbosity.NORMAL);
            return ATTACKER;
        }

        @Override
        public String toString() {
            return name();
        }
    }

    public SpecialMessageAction(List<ODBar> messages, SendType sendType) {
        this.messages = messages;
        this.sendType = sendType;
    }

    protected SendType sendType;
    private final List<ODBar> messages; // this can contain variables, parse at runtime

    public abstract static class ODBar {
        String message;

        public ODBar(String message) {
            this.message = message;
        }
    }

    public static class ODBossBar extends ODBar {
        Integer timeToBeShowed;
        BarColor barColor;
        BarStyle barStyle;
        double progress;

        public ODBossBar(BarColor barColor, BarStyle barStyle, Integer timeToBeShowed, String message, double progress) {
            super(message);
            this.timeToBeShowed = timeToBeShowed;
            this.barColor = barColor;
            this.barStyle = barStyle;
            this.progress = progress;
        }
    }

    public static class ODActionBar extends ODBar {
        public ODActionBar(String message) {
            super(message);
        }
    }

    public static class ODTitleMessage extends ODBar {
        Integer fadeIn;
        Integer stay;
        Integer fadeOut;
        String subtitle;

        public ODTitleMessage(Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle) {
            super(title);
            this.fadeIn = fadeIn;
            this.stay = stay;
            this.fadeOut = fadeOut;
            this.subtitle = subtitle;
        }
    }

    @Override
    public boolean act(CustomDrop drop, OccurredEvent occurrence) {
        Set<Player> players = new HashSet<>();
        Log.logInfo("Gathering players for send-to type: " + sendType.toString(), Verbosity.HIGH);
        switch (sendType) {
            case ATTACKER:
                if (occurrence.getPlayerAttacker() != null)
                    players.add(occurrence.getPlayerAttacker());
                break;
            case VICTIM:
                if (occurrence.getPlayerVictim() != null)
                    players.add(occurrence.getPlayerVictim());
                break;
            case RADIUS:
                Location loc = occurrence.getLocation();
                for (Player player : loc.getWorld().getPlayers()) {
                    if (player.getLocation().getX() > (loc.getX() - OtherDropsConfig.gActionRadius)
                            || player.getLocation().getX() < (loc.getX() + OtherDropsConfig.gActionRadius))
                        if (player.getLocation().getY() > (loc.getY() - OtherDropsConfig.gActionRadius)
                                || player.getLocation().getY() < (loc.getY() + OtherDropsConfig.gActionRadius))
                            if (player.getLocation().getZ() > (loc.getZ() - OtherDropsConfig.gActionRadius)
                                    || player.getLocation().getZ() < (loc.getZ() + OtherDropsConfig.gActionRadius))
                                players.add(player);
                }

                break;
            case SERVER:
                players.addAll(Bukkit.getServer().getOnlinePlayers());
                break;
            case WORLD:
                players.addAll(occurrence.getLocation().getWorld().getPlayers());
                break;
        }
        for(ODBar msg : messages) {
            process(drop, occurrence, msg, players);
        }
        return false;
    }

    private void process(CustomDrop drop, OccurredEvent occurrence, ODBar odBar, Set<Player> players) {
        String message = ODVariables.preParse(odBar.message);
        message = MessageAction.parseVariables(message, drop, occurrence, occurrence.getCustomDropAmount());

        if(odBar instanceof ODBossBar barData) {
            NamespacedKey key = new NamespacedKey(OtherDrops.plugin, UUID.randomUUID().toString());
            BossBar bossBar = Bukkit.createBossBar(key, message, barData.barColor, barData.barStyle);
            bossBar.setProgress(barData.progress);
            for(Player p : players)
                bossBar.addPlayer(p);
            bossBar.setVisible(true);
            OtherDrops.bossBars.add(key);

            OtherDrops.plugin.getServer().getScheduler().runTaskLater(OtherDrops.plugin, () -> {
                bossBar.removeAll();
                Bukkit.removeBossBar(key);
                OtherDrops.bossBars.remove(key);
            }, barData.timeToBeShowed * 20L);
        } else if(odBar instanceof ODActionBar) {
            for(Player p : players) {
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
            }
        } else if(odBar instanceof ODTitleMessage barData) {
            for (Player p : players) {
                String subtitle = ODVariables.preParse(barData.subtitle);
                subtitle = MessageAction.parseVariables(subtitle, drop, occurrence, occurrence.getCustomDropAmount());
                p.sendTitle(message, subtitle, barData.fadeIn, barData.stay, barData.fadeOut);
            }
        }
    }

    @Override
    public List<Action> parse(ConfigurationNode parseMe) {
        List<ODBar> tempMessages = new ArrayList<>();
        Set<SendType> sendTypes = new HashSet<>();

        if(parseMe.getKeys().contains("actionbar")) {
            ConfigurationNode newNode = parseMe.getConfigurationNode("actionbar");
            sendTypes.add(SendType.fromString(newNode.getString("sendto")));
            String message = newNode.getString("message", "");

            tempMessages.add(new ODActionBar(message));
        }
        if(parseMe.getKeys().contains("bossbar")) {
            ConfigurationNode newNode = parseMe.getConfigurationNode("bossbar");
            sendTypes.add(SendType.fromString(newNode.getString("sendto")));
            String message = newNode.getString("message", "");
            BarColor barColor = BarColor.valueOf(newNode.getString("color", "RED").toUpperCase());
            BarStyle barStyle = BarStyle.valueOf(newNode.getString("style", "SOLID").toUpperCase());
            Integer timeToBeShowed = newNode.getInt("time", 10);
            double progress = newNode.getDouble("progress", 1.0);

            tempMessages.add(new ODBossBar(barColor, barStyle, timeToBeShowed, message, progress));
        }
        if(parseMe.getKeys().contains("title")) {
            ConfigurationNode newNode = parseMe.getConfigurationNode("title");
            sendTypes.add(SendType.fromString(newNode.getString("sendto")));
            String title = newNode.getString("title", "");
            String subtitle = newNode.getString("subtitle", "");
            Integer fadeIn = newNode.getInt("fadein", 10);
            Integer stay = newNode.getInt("stay", 70);
            Integer fadeOut = newNode.getInt("fadeout", 20);

            tempMessages.add(new ODTitleMessage(fadeIn, stay, fadeOut, title, subtitle));
        }

        SendType toSend = SendType.VICTIM;
        for(SendType type : sendTypes) {
            if(type.ordinal() > toSend.ordinal())
                toSend = type;
        }

        return List.of(new SpecialMessageAction(tempMessages, toSend));
    }
}
