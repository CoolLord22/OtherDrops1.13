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

package com.gmail.zariust.otherdrops.event;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.Dependencies;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.event.ExclusiveMap.ExclusiveKey;
import com.gmail.zariust.otherdrops.options.Flag;
import com.gmail.zariust.otherdrops.options.IntRange;
import com.gmail.zariust.otherdrops.parameters.Action;
import com.gmail.zariust.otherdrops.parameters.Condition;
import com.gmail.zariust.otherdrops.parameters.Trigger;
import com.gmail.zariust.otherdrops.subject.Agent;
import com.gmail.zariust.otherdrops.subject.Target;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

import static com.gmail.zariust.common.Verbosity.HIGHEST;

public abstract class CustomDrop extends AbstractDropEvent implements Runnable {
    // Fortune enhancer setting
    private Boolean                 fortuneEnhance;
    // Conditions
    private Map<Agent, Boolean>     tools;
    private Set<Flag>               flags;
    private final Flag.FlagState    flagState = new Flag.FlagState();
    // Chance
    private double                  chance;
    private String                  exclusiveKey;
    // Delay
    private IntRange                delay;
    // Execution; this is the actual event that this matched
    protected OccurredEvent         currentEvent;

    // Will this drop the default items?
    public abstract boolean isDefault();

    // The name of this drop
    public abstract String getDropName();

    protected List<String>        messages;
    private final List<Action>    actions    = new ArrayList<Action>();
    private final List<Condition> conditions = new ArrayList<Condition>();
    private boolean               defaultOverride;

    // Conditions
    @Override
    public boolean matches(AbstractDropEvent other) {
        // TODO: not as elegant as the single liner but needed for debugging
        Double rolledValue = rng.nextDouble();
        boolean chancePassed = rolledValue <= chance / 100.0;
        if (!chancePassed) {
            Log.logInfo("Drop failed due to chance *matches* (" + String.valueOf(chance)
                    + ", rolled: " + rolledValue * 100 + ")", HIGHEST);
            return false;
        }

        if (!basicMatch(other)) {
            Log.logInfo("CustomDrop.matches(): basic match failed.", HIGHEST);
            return false;
        }
        if (other instanceof OccurredEvent) {
            OccurredEvent drop = (OccurredEvent) other;
            currentEvent = drop;

            if (!isTool(drop.getTool()))
                return false;
            if (!checkFlags(drop)) {
                Log.logInfo("CustomDrop.matches(): a flag match failed.",
                        HIGHEST);
                return false;
            }

            boolean inMobArenaFlag = false;
            for (Flag activeflag : flags) {
                if (activeflag.toString().matches("IN_MOB_ARENA"))
                    inMobArenaFlag = true;
            }

            if (!inMobArenaFlag)
                if (Dependencies.hasMobArena())
                    if (Dependencies.getMobArenaHandler().inRunningRegion(
                            this.currentEvent.getLocation()))
                        return false;

            for (Condition condition : conditions) {
                if (!condition.check(this, currentEvent))
                    return false;
            }

            return true;
        }

        Log.logInfo(
                "CustomDrop.matches(): match failed - not an OccuredEvent?",
                HIGHEST);
        return false;
    }

    public void setTool(Map<Agent, Boolean> tool) {
        this.tools = tool;
    }

    public Map<Agent, Boolean> getTool() {
        return tools;
    }

    public String getToolString() {
        return mapToString(tools);
    }

    public boolean isTool(Agent tool) {
        boolean positiveMatch = false;
        if (tools == null)
            return true;
        // tools={DIAMOND_SPADE@=true}
        // tool=PLAYER@Xarqn with DIAMOND_SPADE@4
        // Note: tools.get(tool) fails with a player.

        // Check for tool matches
        for (Map.Entry<Agent, Boolean> agent : tools.entrySet()) {
            if (!agent.getValue())
                continue;
            if (agent.getKey().matches(tool)) {
                positiveMatch = true;
                break;
            }
        }

        // Check for tool exception matches
        for (Map.Entry<Agent, Boolean> agent : tools.entrySet()) {
            if (agent.getValue())
                continue;
            if (agent.getKey().matches(tool)) {
                positiveMatch = false;
                break;
            }
        }
        if (!positiveMatch)
            Log.logInfo(
                    "Tool match = " + positiveMatch + " - tool="
                            + String.valueOf(tool) + " tools="
                            + tools.toString(), HIGHEST);
        return positiveMatch;
    }

    public static <T> boolean checkList(T obj, Map<T, Boolean> list) {
        // Check if null - return true (this should only happen if no defaults
        // have been set)
        if (list == null || obj == null)
            return true;

        // Check if empty (this should only happen if an invalid world or biome,
        // etc is set)
        // We return false as the user obviously wants it only to occur for a
        // specific world, even if that world doesn't exist
        if (list.isEmpty())
            return false;

        // Check if a key matches (important to do this before checking for null
        // key [all])
        // eg. for the config [ALL, -DESERT] this will return false for desert
        // before it gets to true for all
        if (list.containsKey(obj))
            return list.get(obj);

        return list.get(null);
    }

    public void setFortuneEnhance(Boolean enabled) {
        fortuneEnhance = enabled;
    }

    public Boolean getFortuneEnhance() {
        return fortuneEnhance != null && fortuneEnhance;
    }

    public void setFlags(Set<Flag> newFlags) {
        flags = newFlags;
    }

    public void setFlag(Flag flag) {
        if (flags == null)
            setFlags(new HashSet<Flag>());
        flags.add(flag);
    }

    public boolean hasFlag(Flag flag) {
        if (flags == null)
            setFlags(new HashSet<Flag>());
        return flags.contains(flag);
    }

    public void unsetFlag(Flag flag) {
        if (flags == null)
            setFlags(new HashSet<Flag>());
        flags.remove(flag);
    }

    public Flag.FlagState getFlagState() {
        return flagState;
    }

    public boolean checkFlags(OccurredEvent drop) {
        boolean shouldDrop = true;
        for (Flag flag : Flag.values()) {
            // Error: flags.contains(flag) was returning true even for flags not
            // in the hashset
            boolean match = false;
            for (Flag activeflag : flags) {
                if (activeflag.toString().matches(flag.toString()))
                    match = true;
            }
            // Logic issue: if flags that are not active are processed we may
            // override continuedropping and dropthis settings...
            if (match == true) {
                flag.matches(drop, match, flagState);
                shouldDrop = shouldDrop && flagState.dropThis;
            }
        }
        return shouldDrop;
    }

    // Chance
    public boolean willDrop(ExclusiveMap exclusives) {
        if (exclusives != null && exclusiveKey != null) {
            if (!exclusives.contains(exclusiveKey)) {
                Data data = currentEvent.getTarget().getData();
                exclusives.put(exclusiveKey, data);
            }
            ExclusiveKey key = exclusives.get(exclusiveKey);
            key.cumul += getChance();
            if (key.select > key.cumul) {
                Log.logInfo("Drop failed due to exclusive key (" + exclusiveKey
                        + ").", HIGHEST);
                return false;
            }
        }
        // TODO: not as elegant as the single liner but needed for debugging
        Double rolledValue = rng.nextDouble();
        boolean chancePassed = rolledValue <= chance / 100.0;
        if (chancePassed) {
            return true;
        } else {
            Log.logInfo("Drop failed due to chance *exclusiveMap* (" + String.valueOf(chance)
                    + ", rolled: " + rolledValue * 100 + ")", HIGHEST);
            return false;
        }
    }

    public void setChance(double percent) {
        chance = percent;
    }

    public double getChance() {
        return chance;
    }

    public void setExclusiveKey(String key) {
        exclusiveKey = key;
    }

    public String getExclusiveKey() {
        return exclusiveKey;
    }

    protected CustomDrop(Target targ, Trigger trigger) {
        super(targ, trigger);
    }

    // Delay
    public int getRandomDelay() {
        if (delay.getMin() == delay.getMax())
            return delay.getMin();

        int randomVal = (delay.getMin() + rng.nextInt(delay.getMax()
                - delay.getMin() + 1));
        return randomVal;
    }

    public String getDelayRange() {
        return delay.getMin().equals(delay.getMax()) ? delay.getMin()
                .toString() : delay.getMin().toString() + "-"
                + delay.getMax().toString();
    }

    public void setDelay(IntRange val) {
        delay = val;
    }

    public void setDelay(int val) {
        delay = new IntRange(val, val);
    }

    public IntRange getDelay() {
        return this.delay;
    }

    public void setDelay(int low, int high) {
        delay = new IntRange(low, high);
    }

    public void perform(OccurredEvent evt) {
        currentEvent = evt;

        int schedule = getRandomDelay();
        // if(schedule > 0.0)
        // Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(OtherDrops.plugin,
        // this, schedule);
        // else run();

        Location playerLoc = null;
        Player player = null; // FIXME: need to get player early - in event
        // if (evt.player != null) playerLoc = player.getLocation();
        DropRunner dropRunner = new DropRunner(OtherDrops.plugin, evt, this,
                player, playerLoc, this.isDefault());

        // schedule the task - NOTE: this must be a sync task due to the changes
        // made in the performActualDrop function
        if (schedule > 0.0)
            Bukkit.getServer()
                    .getScheduler()
                    .scheduleSyncDelayedTask(OtherDrops.plugin, dropRunner,
                            schedule);
        else
            dropRunner.run();
        // }
    }

    private static String setToString(Set<?> set) {
        if (set.size() > 1)
            return set.toString();
        if (set.isEmpty())
            return "(any/none)";
        List<Object> list = new ArrayList<Object>();
        list.addAll(set);
        if (list.get(0) == null) {
            Log.logWarning(
                    "CustomDropEvent.setToString - list.get(0) is null?",
                    Verbosity.HIGHEST);
            return "";
        }
        return list.get(0).toString();
    }

    public static String mapToString(Map<?, Boolean> map) {
        return (map == null) ? null : setToString(stripFalse(map));
    }

    private static Set<?> stripFalse(Map<?, Boolean> map) {
        Set<Object> set = new HashSet<Object>();
        for (Object key : map.keySet()) {
            if (map.get(key))
                set.add(key);
        }
        return set;
    }

    @Override
    public String getLogMessage() {
        StringBuilder log = new StringBuilder();
        log.append(this + ": ");
        // Tool
        log.append(mapToString(tools));
        // Placeholder for drops info
        log.append(" now drops %d");
        // Chance
        log.append(" with " + Double.toString(chance) + "% chance");
        return log.toString();
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public List<String> getMessages() {
        return messages;
    }

    @Override
    public String toString() {
        return (trigger.toString() + " on "
                + ((target == null) ? "<no block>" : target.toString())
                + " drops " + getDropName());
    }

    public void addActions(List<Action> parse) {
        if (parse != null)
            this.actions.addAll(parse);
    }

    public List<Action> getActions() {
        return this.actions;
    }

    public void addConditions(List<Condition> parse) {
        if (parse != null)
            this.conditions.addAll(parse);
    }

    public boolean getDefaultOverride() {
        return this.defaultOverride;
    }

    public void setDefaultOverride(boolean set) {
        this.defaultOverride = set;
    }
}
