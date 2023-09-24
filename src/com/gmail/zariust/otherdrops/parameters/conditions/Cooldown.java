package com.gmail.zariust.otherdrops.parameters.conditions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

/**
 * @author Tirelessly @ Bukkit Forums, zarius (removed player name conditions)
 * 
 */
public class Cooldown {
    public static Set<PlayerCooldown> cooldowns = new HashSet<PlayerCooldown>();

    public static void addCooldown(String cooldownName, UUID playerID, long lengthInMillis) {
        PlayerCooldown pc = new PlayerCooldown(cooldownName, playerID, lengthInMillis);
        Iterator<PlayerCooldown> it = cooldowns.iterator();
        // This section prevents duplicate cooldowns
        while (it.hasNext()) {
            PlayerCooldown iterated = it.next();
            if (iterated.getUUID().equals(pc.getUUID()) && iterated.getCooldownName().equalsIgnoreCase(pc.getCooldownName())) {
                it.remove();
            }
        }
        cooldowns.add(pc);
    }

    public static PlayerCooldown getCooldown(String cooldownName, UUID Player) {
        Iterator<PlayerCooldown> it = cooldowns.iterator();
        while (it.hasNext()) {
            PlayerCooldown pc = it.next();
            if (pc.getCooldownName().equalsIgnoreCase(cooldownName) && pc.getUUID().equals(Player)) {
                return pc;
            }
        }
        return null;
    }

    public static void addGlobalCooldown(String cooldownName, long lengthInMillis) {
        PlayerCooldown pc = new PlayerCooldown(cooldownName, null, lengthInMillis);
        Iterator<PlayerCooldown> it = cooldowns.iterator();
        // This section prevents duplicate cooldowns
        while (it.hasNext()) {
            PlayerCooldown iterated = it.next();
            if (iterated.getCooldownName().equalsIgnoreCase(pc.getCooldownName())) {
                it.remove();
            }
        }
        cooldowns.add(pc);
    }

    public static PlayerCooldown getGlobalCooldown(String cooldownName) {
        Iterator<PlayerCooldown> it = cooldowns.iterator();
        while (it.hasNext()) {
            PlayerCooldown pc = it.next();
            if (pc.getCooldownName().equalsIgnoreCase(cooldownName)) {
                return pc;
            }
        }
        return null;
    }
}

class PlayerCooldown {

    private long         startTime;
    private final String cooldownName;
    private final UUID   player;
    private final long   lengthInMillis;
    private long         endTime;

    PlayerCooldown(String cooldownName, UUID playerID, long lengthInMillis) {
        this.cooldownName = cooldownName;
        this.startTime = System.currentTimeMillis();
        this.player = playerID;
        this.lengthInMillis = lengthInMillis;
        this.endTime = startTime + this.lengthInMillis;
    }

    public boolean isOver() {
        return endTime < System.currentTimeMillis();
    }

    public int getTimeLeft() {
        return (int) (endTime - System.currentTimeMillis());
    }

    public String getCooldownName() {
        return cooldownName;
    }
    
    public UUID getUUID() {
    	return player;
    }
    
    public void reset() {
        startTime = System.currentTimeMillis();
        endTime = startTime + lengthInMillis;
    }
}
