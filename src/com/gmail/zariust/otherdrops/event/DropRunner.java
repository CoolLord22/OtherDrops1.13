package com.gmail.zariust.otherdrops.event;

import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.*;
import com.gmail.zariust.otherdrops.drop.DropResult;
import com.gmail.zariust.otherdrops.drop.DropType;
import com.gmail.zariust.otherdrops.drop.DropType.DropFlags;
import com.gmail.zariust.otherdrops.options.SoundEffect;
import com.gmail.zariust.otherdrops.options.ToolDamage;
import com.gmail.zariust.otherdrops.parameters.Action;
import com.gmail.zariust.otherdrops.parameters.Trigger;
import com.gmail.zariust.otherdrops.parameters.actions.MessageAction;
import com.gmail.zariust.otherdrops.special.SpecialResult;
import com.gmail.zariust.otherdrops.subject.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

import static com.gmail.zariust.common.Verbosity.*;
import static java.lang.Math.max;

public class DropRunner implements Runnable {
	@SuppressWarnings("unused")
	private final OtherDrops plugin;
	OccurredEvent            currentEvent;
	SimpleDrop               customDrop;
	Player                   player;
	Location                 playerLoc;
	boolean                  defaultDrop;
	public static boolean defaultDamageDone;

	private int droppedQuantity = 0;
	private double amount = 1;

	public DropRunner(OtherDrops otherblocks, OccurredEvent target,
			SimpleDrop dropData, Player player, Location playerLoc,
			boolean defaultDrop) {
		this.plugin = otherblocks;
		this.currentEvent = target;
		this.customDrop = dropData;
		this.player = player;
		this.playerLoc = playerLoc;
		this.defaultDrop = defaultDrop;
	}

	public DropRunner(OtherDrops plugin2, OccurredEvent evt,
			CustomDrop customDrop2, Player player2, Location playerLoc2, boolean defaultDrop) {
		this.plugin = plugin2;
		this.currentEvent = evt;
		if (customDrop2 instanceof SimpleDrop)
			this.customDrop = (SimpleDrop) customDrop2;
		else
			Log.logWarning("DropRunner: customdrop is not simple. Customdrop: "
					+ customDrop2.toString(), Verbosity.NORMAL);
		this.player = player2;
		this.playerLoc = playerLoc2;
		this.defaultDrop = defaultDrop;
	}

	// @Override
	@Override
	public void run() {
		Log.logInfo("Starting SimpleDrop...", Verbosity.EXTREME);
		Player who = getPlayer();
		Location location = getLocation();

		checkIfDenied();

		if (currentEvent.getEvent() instanceof PlayerDropItemEvent) {
			PlayerDropItemEvent event = (PlayerDropItemEvent) currentEvent.getEvent();
			if(customDrop.getToolDamage() != null)
				event.getItemDrop().getItemStack().setAmount(event.getItemDrop().getItemStack().getAmount() - 1);
			currentEvent.getEvent().setCancelled(false);
		}

		if (!performDrop(who, location))
			return;

		if(Dependencies.hasWildStacker() && currentEvent.getVictim() instanceof LivingEntity) {
			StackedEntity se = WildStackerAPI.getStackedEntity(currentEvent.getVictim());
			List<ItemStack> tempLoot = new ArrayList<>();

			ItemStack air = new ItemStack(Material.AIR);
			tempLoot.add(air);

			se.setTempLootTable(tempLoot);
		}

		processActions();
		processCommands(customDrop.getCommands(), who, customDrop, currentEvent, amount);
		processReplacementBlock();
		processEffects(location);
		processToolDamage();
		processEventParameter();
	}

	/**
	 * 
	 */
	public void processActions() {
		for (Action action : customDrop.getActions())
			action.act(customDrop, currentEvent);
	}

	/**
	 * 
	 */
	public void processToolDamage() {
		Agent used = currentEvent.getTool();
		if (used != null) { // there's no tool for leaf decay
			// Tool damage
			if (customDrop.getToolDamage() != null && !(currentEvent.getEvent() instanceof PlayerDropItemEvent)) {
				used.damageTool(customDrop.getToolDamage(), customDrop.rng);
			} else {
				if (currentEvent.getEvent() instanceof BlockBreakEvent)
					if (droppedQuantity > 0 && currentEvent.isOverrideDefault() && !defaultDamageDone && !defaultDrop) {
						used.damageTool(new ToolDamage(1), customDrop.rng);
						defaultDamageDone = true;
					}
			}

		}
	}

	/**
	 * 
	 */
	public void processEventParameter() {
		try {
			Location oldLocation = currentEvent.getLocation();
			customDrop.randomiseLocation(currentEvent.getLocation(),
					customDrop.randomize);
			// And finally, events
			if (customDrop.getEvents() != null) {
				for (SpecialResult evt : customDrop.getEvents()) {
					if (evt.canRunFor(currentEvent))
						evt.executeAt(currentEvent);
				}
			}
			currentEvent.setLocation(oldLocation);
		} catch (Exception ex) {
			Log.logWarning("Exception while running special event results: "
					+ ex.getMessage(), NORMAL);
			if (OtherDropsConfig.getVerbosity().exceeds(HIGH))
				ex.printStackTrace();
		}
	}

	/**
	 * @param location
	 */
	public void processEffects(Location location) {
		// Effects after replacement block
		// TODO: I don't think effect should account for randomize/offset.
		if (customDrop.getEffects() != null)
			for (SoundEffect effect : customDrop.getEffects())
				effect.play(customDrop.randomiseLocation(location,
						customDrop.randomize));
	}

	/**
	 * 
	 */
	public void processReplacementBlock() {
		// Replacement block
		if (customDrop.getReplacementBlock() != null) { // note: we shouldn't
			// change the
			// replacementBlock,
			// just a copy of it.
			Target toReplace = currentEvent.getTarget();
			BlockTarget tempReplace = customDrop.getReplacementBlock();
			BlockTarget ifFarmlandUpOneBlock;
			BlockTarget getsBlockBeingChanged = new BlockTarget(toReplace.getLocation().getBlock());

            if(customDrop.getReplacementBlock().getMaterial() == null) {
				tempReplace = new BlockTarget(toReplace.getLocation().getBlock());
			}

			Log.logInfo("Replacing " + toReplace + " with "
					+ customDrop.getReplacementBlock().toString(),
					Verbosity.HIGHEST);

			if(tempReplace.getMaterial() == Material.AIR && currentEvent.getRealEvent() instanceof EntityDeathEvent) {
				if (!(currentEvent.getVictim() instanceof Player))
					currentEvent.getVictim().remove();
			} 

			if(getsBlockBeingChanged.getMaterial() == Material.FARMLAND && 
					(tempReplace.getMaterial() == Material.WHEAT | tempReplace.getMaterial() == Material.BEETROOTS 
					|| tempReplace.getMaterial() == Material.CARROTS || tempReplace.getMaterial() == Material.POTATOES 
					|| tempReplace.getMaterial() == Material.MELON_STEM || tempReplace.getMaterial() == Material.PUMPKIN_STEM)) {
				ifFarmlandUpOneBlock = new BlockTarget(toReplace.getLocation().add(0, 1, 0).getBlock());
				ifFarmlandUpOneBlock.setTo(tempReplace);
			}

			if(getsBlockBeingChanged.getMaterial() == Material.SOUL_SAND && (tempReplace.getMaterial() == Material.NETHER_WART)) {
				ifFarmlandUpOneBlock = new BlockTarget(toReplace.getLocation().add(0, 1, 0).getBlock());
				ifFarmlandUpOneBlock.setTo(tempReplace);
			}

			else {
				toReplace.setTo(tempReplace);
			}
			currentEvent.setCancelled(true);
		}
	}

	/**
	 * @param who
	 * @param location
	 */
	public boolean performDrop(Player who, Location location) {
		// Then the actual drop
		// May have unexpected effects when use with delay.
		double x, z;
		x = location.getBlockX();
		z = location.getBlockZ();
		location.setX(x + 0.5);
		location.setZ(z + 0.5);
		
		if (customDrop.getDropped() != null) {
			if (!customDrop.getDropped().toString().equalsIgnoreCase("DEFAULT")) {
				Target target = currentEvent.getTarget();
				boolean dropNaturally = true; // TODO: How to make this
				// specifiable in the config?
				boolean spreadDrop = customDrop.getDropSpread();

				double fortuneMultiplier = 1.0;
				if(customDrop.getFortuneEnhance() && currentEvent.getTool() instanceof PlayerSubject) {
					ItemStack tool = ((PlayerSubject) currentEvent.getTool()).getTool().getActualTool();
					if(tool.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)) {
						fortuneMultiplier = ((1.0)/ (tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) + 2.0)) + ((tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) + 1.0)/(2.0));
						Log.logInfo("Found fortune! Multiplier is..." + fortuneMultiplier, HIGHEST);
					}
				}
				amount = customDrop.quantity.getRandomIn(customDrop.rng) * fortuneMultiplier;

				String eventName = getEventName();
				DropFlags flags = DropType.flags(who, currentEvent.getTool(),
						dropNaturally, spreadDrop, customDrop.rng, eventName, currentEvent.getSpawnedReason(), currentEvent.getVictimName()); // TODO:
				// add
				// tool
				DropResult dropResult = customDrop.getDropped().drop(location, target, customDrop.getOffset(), amount, flags);
				droppedQuantity = dropResult.getQuantity();
				Log.logInfo("Override default is: " + dropResult.getOverrideDefault(), HIGHEST);
				if (dropResult.getOverrideDefault())
					currentEvent.setOverrideDefault(true);
				currentEvent.setOverrideDefaultXp(dropResult.getOverrideDefaultXp());

				Log.logInfo("SimpleDrop: dropped " + customDrop.getDropped().toString() + " x " + amount + " (dropped: " + droppedQuantity + ")", HIGHEST);
				if (droppedQuantity < 0) { // If the embedded chance roll fails,
					// assume default and bail out!
					Log.logInfo("Drop failed... setting cancelled to false", Verbosity.HIGHEST);
					currentEvent.setCancelled(false);
					return false;
				}

				// If the drop chance was 100% and no replacement block is
				// specified, make it air
				double chance = max(customDrop.getChance(), customDrop.getDropped().getChance());
				if (customDrop.getReplacementBlock() == null && chance >= 100.0 && target.overrideOn100Percent()) {
					if (target instanceof LivingSubject) { // need to be careful
						// not to replace
						// creatures with air
						// - this kills the
						// death animation
						currentEvent.setCancelled(true);
					} else if (target instanceof VehicleTarget) {
						currentEvent.setCancelled(true);
						((VehicleTarget) target).getVehicle().remove();
					} else if (currentEvent.getTrigger() == Trigger.BREAK) {
						if (currentEvent.isOverrideDefault() && !defaultDrop)
							currentEvent.setReplaceBlockWith(new BlockTarget(
									Material.AIR));
						currentEvent.setCancelled(false);
					}
				}
				amount *= customDrop.getDropped().getAmount();
				if (customDrop.getDropped() instanceof com.gmail.zariust.otherdrops.drop.MoneyDrop) {
					amount = customDrop.getDropped().total;
				}
				currentEvent.setCustomDropAmount(amount);

				setFishingDropVelocity(who, dropResult);
			} else {
				// DEFAULT event - set cancelled to false
				Log.logInfo("Performdrop: DEFAULT, so undo event cancellation.", Verbosity.HIGHEST);
				currentEvent.setCancelled(false);
				// TODO: some way of setting it so that if we've set false here
				// we don't set true on the same occureddrop?
				// this could save us from checking the DEFAULT drop outside the
				// loop in OtherDrops.performDrop()
			}
		}
		return true;

	}

	/**
	 * @return
	 */
	public String getEventName() {
		String eventName = "";
		if (currentEvent.getRealEvent() != null)
			eventName = currentEvent.getRealEvent().getEventName();
		return eventName;
	}

	/**
	 * @param who
	 * @param dropResult
	 */
	public void setFishingDropVelocity(Player who, DropResult dropResult) {
		// Set velocity on fish caught events, not on fish_failed as
		// cannot get sinker location
		if (dropResult.getDropped() != null
				&& (currentEvent.getTrigger() == Trigger.FISH_CAUGHT)
				&& who != null) {
			Log.logInfo("Setting velocity on fished entity...."
					+ dropResult.getDroppedString(), Verbosity.HIGHEST);
			for (Entity ent : dropResult.getDropped()) {
				setEntityVectorFromTo(currentEvent.getLocation(),
						who.getLocation(), ent);
			}
		}
	}

	/**
	 * 
	 */
	public void checkIfDenied() {
		// If drop is DENY then cancel event and set denied flag
		// If this is a player death event note that DENY also clears inventory
		// so set overrides default to true
		if (customDrop.isDenied()) {
			currentEvent.setCancelled(true);
			currentEvent.setDenied(true);
			if (currentEvent.getRealEvent() != null
					&& currentEvent.getRealEvent() instanceof EntityDeathEvent) {
				EntityDeathEvent evt = (EntityDeathEvent) currentEvent
						.getRealEvent();
				if ((evt.getEntity() instanceof Player)) {
					currentEvent.setOverrideDefault(true);
				}
			}
		}
	}

	/**
	 * @return
	 */
	public Location getLocation() {
		// We also need the location
		Location location = currentEvent.getLocation();
		if (customDrop.getTrigger() == Trigger.PLAYER_RESPAWN
				&& currentEvent.getPlayerAttacker() != null) {
			location = currentEvent.getPlayerAttacker().getLocation();
		}
		return location;
	}

	/**
	 * @return
	 */
	public Player getPlayer() {
		// We need a player for some things.
		Player who = null;
		if (currentEvent.getTool() instanceof PlayerSubject)
			who = ((PlayerSubject) currentEvent.getTool()).getPlayer();
		if (currentEvent.getTool() instanceof ProjectileAgent) {
			LivingSubject living = ((ProjectileAgent) currentEvent.getTool())
					.getShooter();
			// FIXME: why would this (living) ever be null?
			if (living != null)
				Log.logInfo(
						"droprunner.run: projectile agent detected... shooter = "
								+ living, HIGHEST);
			if (living instanceof PlayerSubject)
				who = ((PlayerSubject) living).getPlayer();
		}
		return who;
	}

	private void processCommands(List<String> commands, Player who,
			CustomDrop drop, OccurredEvent occurence, double amount) {
		if (commands != null) {
			for (String command : commands) {
				boolean suppress = false;
				Boolean override = false;
				// Five possible prefixes (slash is optional in all of them)
				// "/" - Run the command as the player, and send them any result
				// messages
				// "/!" - Run the command as the player, but send result
				// messages to the console
				// "/*" - Run the command as the player with op override, and
				// send them any result messages
				// "/!*" - Run the command as the player with op override, but
				// send result messages to the console
				// "/$" - Run the command as the console, but send the player
				// any result messages
				// "/!$" - Run the command as the console, but send result
				// messages to the console
				if (who != null)
					command = command.replaceAll("%p", who.getName());
				if (command.startsWith("/"))
					command = command.substring(1);
				if (command.startsWith("!")) {
					command = command.substring(1);
					suppress = true;
				}
				if (command.startsWith("*")) {
					command = command.substring(1);
					override = true;
				} else if (command.startsWith("$")) {
					command = command.substring(1);
					override = null;
				}

				command = command.trim();
				if (OtherDropsConfig.getVerbosity().exceeds(Verbosity.HIGH)) {
					String runAs = "PLAYER";
					if (override != null && override)
						runAs = "OP";
					else if (override == null)
						runAs = "CONSOLE";

					String outputTo = "player";
					if (suppress)
						outputTo = "console";

					Log.logInfo("CommandAction: running - '/" + command
							+ "' as " + runAs + ", output to " + outputTo,
							Verbosity.HIGH);
				}

				command = MessageAction.parseVariables(command, drop,
						occurence, amount);

				CommandSender from;
				if (who == null || override == null) {
					from = new PlayerConsoleWrapper(who, suppress);
				}
				else {
					from = new PlayerWrapper(who, override, suppress);
				}
				try {
					Bukkit.getServer().dispatchCommand(from, command);
				}
				catch (Exception ex) {
					Log.logWarning("For some reason we couldn't dispatch the command!");
				}
			}
		}
	}

	private void setEntityVectorFromTo(Location fromLocation,
			Location toLocation, Entity entity) {
		// Velocity from Minecraft Source + MCP Decompiler. Thank
		// you Notch and MCP :3
		double d1 = toLocation.getX() - fromLocation.getX();
		double d3 = toLocation.getY() - fromLocation.getY();
		double d5 = toLocation.getZ() - fromLocation.getZ();
		double d7 = ((float) Math.sqrt((d1 * d1 + d3 * d3 + d5 * d5)));
		double d9 = 0.10000000000000001D;
		double motionX = d1 * d9;
		double motionY = d3 * d9 + ((float) Math.sqrt(d7))
				* 0.080000000000000002D;
		double motionZ = d5 * d9;
		if (entity instanceof LivingEntity) { // FIXME: entities are not quite
			// going to player properly?
			entity.setVelocity(new Vector(motionX * 3, motionY * 3, motionZ * 3));
		} else {
			entity.setVelocity(new Vector(motionX, motionY, motionZ));
		}
	}
}
