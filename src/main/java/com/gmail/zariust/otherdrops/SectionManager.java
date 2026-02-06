package com.gmail.zariust.otherdrops;

import com.gmail.zariust.otherdrops.event.*;
import com.gmail.zariust.otherdrops.parameters.Trigger;
import com.gmail.zariust.otherdrops.parameters.actions.MessageAction;
import com.gmail.zariust.otherdrops.subject.BlockTarget;
import com.gmail.zariust.otherdrops.subject.PlayerSubject;
import com.gmail.zariust.otherdrops.subject.Subject.ItemCategory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;

import java.util.ArrayList;
import java.util.List;

import static com.gmail.zariust.common.Verbosity.*;

public class SectionManager {

    private final OtherDrops parent;

    public SectionManager(OtherDrops parent) {
        this.parent = parent;
    }

    /**
     * Matches an actual drop against the configuration and runs any configured drops that are found.
     *
     * @param occurrence The actual drop.
     */
    public void performDrop(OccurredEvent occurrence) {
        DropsList customDrops = parent.config.blocksHash.getList(occurrence.getTrigger(), occurrence.getTarget());
        if (customDrops == null) {
            if (OtherDropsConfig.verbosity.exceeds(HIGH)) { // check verbosity
                // outside logInfo so that "toString()" functions are processed otherwise
                // set spawn event log message to extreme as otherwise too  common
                if (occurrence.getEvent() instanceof CreatureSpawnEvent)
                    Log.logInfo("PerformDrop ("
                            + (occurrence.getTrigger() == null ? "" : occurrence.getTrigger().toString())
                            + ", "
                            + (occurrence.getTarget() == null ? "" : occurrence.getTarget().toString())
                            + " w/ "
                            + (occurrence.getTool() == null ? "" : occurrence .getTool().toString())
                            + ") no potential drops found", EXTREME);
                else
                    Log.logInfo("PerformDrop ("
                            + (occurrence.getTrigger() == null ? "" : occurrence.getTrigger().toString())
                            + ", "
                            + (occurrence.getTarget() == null ? "" : occurrence.getTarget().toString())
                            + " w/ "
                            + (occurrence.getTool() == null ? "" : occurrence.getTool().toString())
                            + ") no potential drops found", HIGHEST);
            }
            return; // TODO: if no drops, just return - is this right?
        }
        // TODO: return a list of drops found? difficult due to multi-classes?
        if (OtherDropsConfig.verbosity.exceeds(HIGH))
            Log.logInfo("PerformDrop - potential drops found: " + customDrops + " tool: " + (occurrence.getTool() == null ? "" : occurrence.getTool().toString()), HIGH);

        // check if block is excepted (for any)
        for (CustomDrop drop : customDrops) {
            if (drop.getTarget() instanceof BlockTarget any) {
                if (any.except != null) {
                    Material compareTo = null;
                    Material compareToOffhand = null;
                    if (occurrence.getEvent() instanceof BlockBreakEvent) {
                        compareTo = ((BlockBreakEvent) occurrence.getEvent()).getBlock().getType();
                    } else if (occurrence.getEvent() instanceof PlayerInteractEvent pie) {
                        compareTo = pie.getPlayer().getInventory().getItemInMainHand().getType();
                        compareToOffhand = pie.getPlayer().getInventory().getItemInOffHand().getType();
                    }

                    if (any.except.contains(compareTo) || any.except.contains(compareToOffhand)) {
                        return;
                    }
                }
            }
        }

        DropRunner.defaultDamageDone = false;
        // Loop through the drops and check for a match, process uniques, etc
        List<SimpleDrop> scheduledDrops = gatherDrops(customDrops, occurrence);
        if (OtherDropsConfig.verbosity.exceeds(HIGHEST)) Log.logInfo("PerformDrop: scheduled drops=" + scheduledDrops, HIGHEST);

        // check for any DEFAULT drops
        boolean defaultDrop = false;
        int dropCount = 0;
        for (SimpleDrop simpleDrop : scheduledDrops) {
            if (simpleDrop.getDropped() != null)
                // if (!simpleDrop.getDropped().toString().equalsIgnoreCase("AIR")) // skip drops that don't actually drop anything
                dropCount++;
            if (simpleDrop.isDefault()) {
                defaultDrop = true;
                occurrence.setOverrideDefault(false); // DEFAULT drop
            }
            if (simpleDrop.getDropped() != null && simpleDrop.getDropped().toString().equalsIgnoreCase("AIR"))
                occurrence.setOverrideDefault(true); // NOTHING drop
        }

        for (SimpleDrop simpleDrop : scheduledDrops) {
            Log.logInfo("PerformDrop: scheduling " + simpleDrop.getDropName(), HIGH);
            scheduleDrop(occurrence, simpleDrop, defaultDrop);
        }

        if (occurrence.isOverrideEquipment() && occurrence.getRealEvent() instanceof EntityDeathEvent evt) {
            if (!(evt.getEntity() instanceof Player)) clearMobEquipment(evt.getEntity());
        }
        // Cancel event, if applicable
        if (occurrence.isOverrideDefault() && !defaultDrop) {
            clearDrops(occurrence, dropCount);
        } else {
            occurrence.setCancelled(false);
        }
        if (occurrence.getRealEvent() != null) {
            if (occurrence.getRealEvent() instanceof EntityDeathEvent evt) {
                if (occurrence.isOverrideDefaultXp()) {
                    Log.logInfo("PerformDrop: entitydeath - isOverrideDefaultXP=true, clearing xp drop.", HIGH);
                    evt.setDroppedExp(0);
                }
            }
        }

        if (occurrence.getTrigger() == Trigger.HIT) {
            if (occurrence.getEvent() instanceof EntityDamageByEntityEvent) {
                occurrence.setCancelled(false);
            }
        }

        if (occurrence.getReplaceBlockWith() != null) occurrence.getTarget().setTo(occurrence.getReplaceBlockWith());

        if (occurrence.isDenied()) occurrence.setCancelled(true);

        // Make sure explosion events are not cancelled (as this will cancel the whole explosion
        // Individual blocks are prevented (if DENY is set) in the entity listener
        if (occurrence.getEvent() instanceof EntityExplodeEvent) occurrence.setCancelled(false);
        Log.logInfo("PerformDrop: finished. defaultdrop=" + defaultDrop + " dropcount=" + dropCount + " cancelled=" + occurrence.isCancelled() + " denied=" + occurrence.isDenied(), HIGH);
    }

    private void clearDrops(OccurredEvent occurrence, int dropCount) {
        if (occurrence.getEvent() instanceof LeavesDecayEvent) {
            occurrence.setCancelled(true);
            ((LeavesDecayEvent) occurrence.getEvent()).getBlock().setType(Material.AIR);
            return;
        }

        if (occurrence.getEvent() instanceof BlockBreakEvent || occurrence.getEvent() instanceof PlayerFishEvent) {
            if (occurrence.getTool().getType() != ItemCategory.EXPLOSION) {

                Log.logInfo("PerformDrop: blockbreak or fishing - not default drop - cancelling event (dropcount=" + dropCount + ").", HIGH);
                if (occurrence.getEvent() instanceof PlayerFishEvent pfe) {
                    if (pfe.getCaught() != null) pfe.getCaught().remove();
                } else {
                    occurrence.setCancelled(true);
                    // Process action through logging plugins, if any - this is only because we generally cancel the break event
                    if (occurrence.getTarget() instanceof BlockTarget && occurrence.getTrigger() == Trigger.BREAK) {
                        Block block = occurrence.getLocation().getBlock();
                        Player player = null;
                        if (occurrence.getTool() instanceof PlayerSubject) player = ((PlayerSubject) occurrence.getTool()).getPlayer();
                        DropNotificationEvent dNE = new DropNotificationEvent(player, block, (BlockBreakEvent) occurrence.getEvent());
                        Bukkit.getPluginManager().callEvent(dNE);
                    }
                }
            }
        } else if (occurrence.getRealEvent() != null) {
            if (occurrence.getRealEvent() instanceof EntityDeathEvent evt) {
                if ((evt.getEntity() instanceof Player) && !(occurrence.isDenied())) {
                    Log.logInfo("Player death - not clearing.");
                } else {
                    Log.logInfo("PerformDrop: entitydeath - clearing drops.", HIGHEST);
                    evt.getDrops().clear();
                    if (!(evt.getEntity() instanceof Player)) {
                        clearMobEquipment(evt.getEntity());

                        // and if denied just remove the entity to stop animation (as we cannot cancel the event)
                        if (occurrence.isDenied()) {
                            evt.getEntity().remove();
                        }
                    }
                }
                if (OtherDropsConfig.disableXpOnNonDefault) {
                    Log.logInfo("PerformDrop: entitydeath - no default drop, clearing xp drop.", HIGH);
                    evt.setDroppedExp(0);
                }
            }
        }
    }

    private void clearMobEquipment(LivingEntity entity) {
        EntityEquipment eq = entity.getEquipment();
        if (eq != null) {
            eq.setHelmetDropChance(0);
            eq.setChestplateDropChance(0);
            eq.setLeggingsDropChance(0);
            eq.setBootsDropChance(0);
            eq.setItemInMainHandDropChance(0);
            eq.setItemInOffHandDropChance(0);
        }

    }

    private List<SimpleDrop> gatherDrops(DropsList customDrops, OccurredEvent occurrence) {
        // OtherDrops.logInfo("Gatherdrops start.", HIGHEST);
        List<CustomDrop> matchedDrops = new ArrayList<>();
        List<CustomDrop> uniqueList = new ArrayList<>();
        RandomDropCollection<CustomDrop> randomDropCollection = new RandomDropCollection<>();

        // First, loop through all drops and gather successful & unique ones into two lists
        // Note: since we don't know if this drop will be cleared by uniques, don't do any events in here
        for (CustomDrop customDrop : customDrops) {
            if (customDrop instanceof GroupDropEvent groupCustomDrop) {
                if (groupCustomDrop.matches(occurrence)) { // FIXME: include chance check at top of matches
                    // OtherDrops.logInfo("PerformDrop: found group ("+groupCustomDrop.getGroupsString()+")", HIGHEST);
                    matchedDrops.add(groupCustomDrop);
                    if (!groupCustomDrop.getFlagState().continueDropping) { // This means a unique flag found
                        // OtherDrops.logInfo("PerformDrop: group ("+groupCustomDrop.getName()+") is UNIQUE.", HIGHEST);
                        uniqueList.add(groupCustomDrop);
                    }
                    if (groupCustomDrop.isWeighted()) {
                        Log.logInfo("PerformDrop: found weighted group drop: " + groupCustomDrop.getDropName() + " with weight: " + groupCustomDrop.getWeight(), HIGHEST);
                        randomDropCollection.add(groupCustomDrop.getWeight(), groupCustomDrop);
                    } else {
                        if (!randomDropCollection.isEmpty())
                            Log.logWarning("Non-weighted group-drop found in a weighted section: " + groupCustomDrop.getDropName());
                    }
                } else {
                    // OtherDrops.logInfo("PerformDrop: Dropgroup ("+groupCustomDrop.getLogMessage()+") did not match ("+occurrence.getLogMessage()+").",
                    // HIGHEST);
                }
            } else { // SimpleDrop - so add to a list
                if (customDrop.matches(occurrence)) {
                    matchedDrops.add(customDrop);
                    if (!customDrop.getFlagState().continueDropping) { // This means a unique flag found
                        uniqueList.add(customDrop);
                    }
                    if (customDrop.isWeighted()) {
                        Log.logInfo("PerformDrop: found weighted drop: " + customDrop.getDropName() + " with weight: " + customDrop.getWeight(), HIGHEST);
                        randomDropCollection.add(customDrop.getWeight(), customDrop);
                    } else {
                        if (!randomDropCollection.isEmpty())
                            Log.logWarning("Non-weighted drop: " + customDrop.getDropName() + " found in a weighted section. This drop WILL be ignored!");
                    }
                } else {
                    // OtherDrops.logInfo("PerformDrop: Drop ("+occurrence.getLogMessage()+") did not match ("+customDrop.getLogMessage()+").",
                    // HIGHEST);
                }
            }
        }

        // If there were weighted drops, pick a random one to add to the list of drops
        if (!randomDropCollection.isEmpty()) {
            matchedDrops.clear();
            CustomDrop selected = randomDropCollection.next();
            Log.logInfo("PerformDrop: getWeighted, selecting: " + selected.getDropName(), HIGHEST);
            matchedDrops.add(selected);
        }

        // If there were unique, pick a random one and clear the rest
        if (!uniqueList.isEmpty()) {
            matchedDrops.clear();
            matchedDrops.add(getSingleRandomUnique(uniqueList));
        }

        // Loop through what's left and check for groups that need to be
        // recursed into, otherwise add to final list and return
        List<SimpleDrop> finalDrops = new ArrayList<>();
        for (CustomDrop customDrop : matchedDrops) {
            if (customDrop instanceof GroupDropEvent groupCustomDrop) {
                // Process dropGroup events here...
                // Display dropgroup "message:"
                String message = MessageAction.getRandomMessage(customDrop, occurrence, customDrop.getMessages(), true);
                if (!message.isEmpty() && occurrence.getTool() instanceof PlayerSubject) {
                    ((PlayerSubject) occurrence.getTool()).getPlayer().sendMessage(message);
                }

                finalDrops.addAll(gatherDrops(groupCustomDrop.getDrops(), occurrence));
            } else {
                // OtherDrops.logInfo("PerformDrop: adding " + customDrop.getDropName(), HIGHEST);
                finalDrops.add((SimpleDrop) customDrop);
            }
        }

        // OtherDrops.logInfo("Gatherdrops end... finaldrops: "+finalDrops.toString(), HIGHEST);
        return finalDrops;
    }

    public CustomDrop getSingleRandomUnique(List<CustomDrop> uniqueList) {
        CustomDrop random = uniqueList.get(OtherDrops.rng.nextInt(uniqueList.size()));
        Log.logInfo("PerformDrop: getunique, selecting: " + random.getDropName(), HIGHEST);
        return random;
    }

    public void scheduleDrop(OccurredEvent evt, CustomDrop customDrop, boolean defaultDrop) {
        int schedule = customDrop.getRandomDelay();

        Location playerLoc = null;
        Player player = null; // FIXME: need to get player early - in event
        // if (evt.player != null) playerLoc = player.getLocation();
        DropRunner dropRunner = new DropRunner(OtherDrops.plugin, evt, customDrop, player, playerLoc, defaultDrop);

        // schedule the task - NOTE: this must be a sync task due to the changes
        // made in the performActualDrop function
        if (schedule > 0.0) Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(OtherDrops.plugin, dropRunner, schedule);
        else dropRunner.run();
        // }
    }

    /* For testing only, so far
    public void dropCreatureEquipment(LivingEntity le) {
        // Log.dMsg(String.valueOf(le.getEquipment().getBootsDropChance()));
        // Log.dMsg(le.getEquipment().toString());


         * if (le.getEquipment().getBoots() != null) { if OtherDrops.rng.nextFloat() > } DropType.parse(le.getEquipment().getBoots ()+"/"+le.getEquipment().getBootsDropChance(), ""); Log.dMsg(String.valueOf(le.getEquipment().getBootsDropChance())); Log.dMsg(le.getEquipment().toString());

    }*/
}
