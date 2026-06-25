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

import com.destroystokyo.paper.Title;
import com.destroystokyo.paper.block.TargetBlockInfo;
import com.destroystokyo.paper.entity.TargetEntityInfo;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.entity.LookAnchor;
import io.papermc.paper.entity.TeleportFlag;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.world.damagesource.CombatTracker;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.TriState;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.sign.Side;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.*;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.*;
import org.bukkit.inventory.InventoryView.Property;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.connection.PlayerGameConnection;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.checkerframework.checker.index.qual.NonNegative;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@SuppressWarnings("deprecation")
public class PlayerWrapper implements Player {
    private final Player               caller;
    private final ConsoleCommandSender console = Bukkit.getConsoleSender();
    private final boolean              suppress, override;

    public PlayerWrapper(Player player, boolean opOverride, boolean suppressMessages) {
        this.caller = player;
        this.suppress = suppressMessages;
        this.override = opOverride;
    }

    // OtherDrops code
    private CommandSender getSender() {
        return suppress ? console : caller;
    }

    // OtherDrops code
    private Permissible getPermissible() {
        return override ? console : caller;
    }

    @Override
    public boolean isOp() {
        return getPermissible().isOp();
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return null;
    }

    @Override
    // Special case for time-limited permissions; always go to the caller
    public PermissionAttachment addAttachment(Plugin plugin, int time) {
        return caller.addAttachment(plugin, time);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String perm,
            boolean val) {
        return null;
    }

    @Override
    // Special case for time-limited permissions; always go to the caller
    public PermissionAttachment addAttachment(Plugin plugin, String perm,
            boolean val, int time) {
        return caller.addAttachment(plugin, perm, val, time);
    }

    // OtherDrops code
    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return getPermissible().getEffectivePermissions();
    }

    // OtherDrops code
    @Override
    public boolean hasPermission(String perm) {
        return getPermissible().hasPermission(perm);
    }

    // OtherDrops code
    @Override
    public boolean hasPermission(Permission perm) {
        return getPermissible().hasPermission(perm);
    }

    // OtherDrops code
    @Override
    public boolean isPermissionSet(String perm) {
        return getPermissible().isPermissionSet(perm);
    }

    // OtherDrops code
    @Override
    public boolean isPermissionSet(Permission perm) {
        return getPermissible().isPermissionSet(perm);
    }

    // OtherDrops code
    @Override
    public void recalculatePermissions() {
        getPermissible().recalculatePermissions();
    }

    @Override
    public void removeAttachment(PermissionAttachment attached) {
    }

    // OtherDrops code
    @Override
    public void setOp(boolean is) {
        getPermissible().setOp(is);
    }

    // CommandSender methods; getName() may not be declared in CommandSender,
    // but it's used for any CommandSender that actually defines it
    @Override
    public String getName() {
        return caller.getName();
    }

    @Override
    public Server getServer() {
        return caller.getServer();
    }

    // OtherDrops code
    @Override
    public void sendMessage(String msg) {
        getSender().sendMessage(msg);
    }

    // Player, HumanEntity, LivingEntity, Entity methods... ugh, there are so
    // many of these...
    @Override
    public PlayerInventory getInventory() {
        return caller.getInventory();
    }

    @Override
    public ItemStack getItemInHand() {
        return caller.getInventory().getItemInMainHand();
    }

    @Override
    public void setItemInHand(ItemStack item) {
        caller.getInventory().setItemInMainHand(item);
    }

    @Override
    public boolean isSleeping() {
        return caller.isSleeping();
    }

    @Override
    public boolean isClimbing() {
        return false;
    }

    @Override
    public int getSleepTicks() {
        return caller.getSleepTicks();
    }

    @Override
    public double getEyeHeight() {
        return caller.getEyeHeight();
    }

    @Override
    public double getEyeHeight(boolean ignoreSneaking) {
        return caller.getEyeHeight(ignoreSneaking);
    }

    @Override
    public Location getEyeLocation() {
        return caller.getEyeLocation();
    }
    
    @Override
    public boolean isInsideVehicle() {
        return caller.isInsideVehicle();
    }

    @Override
    public boolean leaveVehicle() {
        return caller.leaveVehicle();
    }

    @Override
    public int getRemainingAir() {
        return caller.getRemainingAir();
    }

    @Override
    public void setRemainingAir(int ticks) {
        caller.setRemainingAir(ticks);
    }

    @Override
    public int getMaximumAir() {
        return caller.getMaximumAir();
    }

    @Override
    public void setMaximumAir(int ticks) {
        caller.setMaximumAir(ticks);
    }

    @Override
    public int getArrowCooldown() {
        return 0;
    }

    @Override
    public void setArrowCooldown(int i) {

    }

    @Override
    public int getArrowsInBody() {
        return 0;
    }

    @Override
    public void setArrowsInBody(int i) {

    }

    @Override
    public void setArrowsInBody(@NonNegative int count, boolean fireEvent) {

    }

    @Override
    public @NonNegative int getBeeStingerCooldown() {
        return 0;
    }

    @Override
    public void setBeeStingerCooldown(@NonNegative int ticks) {

    }

    @Override
    public @NonNegative int getBeeStingersInBody() {
        return 0;
    }

    @Override
    public void setBeeStingersInBody(@NonNegative int count) {

    }

    @Override
    public int getMaximumNoDamageTicks() {
        return caller.getMaximumNoDamageTicks();
    }

    @Override
    public void setMaximumNoDamageTicks(int ticks) {
        caller.setMaximumNoDamageTicks(ticks);
    }

    @Override
    public int getNoDamageTicks() {
        return caller.getNoDamageTicks();
    }

    @Override
    public void setNoDamageTicks(int ticks) {
        caller.setNoDamageTicks(ticks);
    }

    @Override
    public int getNoActionTicks() {
        return 0;
    }

    @Override
    public void setNoActionTicks(int ticks) {

    }

    @Override
    public Location getLocation() {
        return caller.getLocation();
    }

    @Override
    public void setVelocity(Vector velocity) {
        caller.setVelocity(velocity);
    }

    @Override
    public Vector getVelocity() {
        return caller.getVelocity();
    }

    @Override
    public World getWorld() {
        return caller.getWorld();
    }

    @Override
    public boolean teleport(Location location) {
        return caller.teleport(location);
    }

    @Override
    public boolean teleport(Entity destination) {
        return caller.teleport(destination);
    }

    @Override
    public List<Entity> getNearbyEntities(double x, double y, double z) {
        return caller.getNearbyEntities(x, y, z);
    }

    @Override
    public int getEntityId() {
        return caller.getEntityId();
    }

    @Override
    public int getFireTicks() {
        return caller.getFireTicks();
    }

    @Override
    public int getMaxFireTicks() {
        return caller.getMaxFireTicks();
    }

    @Override
    public void setFireTicks(int ticks) {
        caller.setFireTicks(ticks);
    }

    @Override
    public void setVisualFire(boolean b) {

    }

    @Override
    public void setVisualFire(@NotNull TriState fire) {

    }

    @Override
    public boolean isVisualFire() {
        return false;
    }

    @Override
    public @NotNull TriState getVisualFire() {
        return null;
    }

    @Override
    public int getFreezeTicks() {
        return 0;
    }

    @Override
    public int getMaxFreezeTicks() {
        return 0;
    }

    @Override
    public void setFreezeTicks(int i) {

    }

    @Override
    public boolean isFrozen() {
        return false;
    }

    @Override
    public void remove() {
        caller.remove();
    }

    @Override
    public boolean isDead() {
        return caller.isDead();
    }

	@Override
    public Entity getPassenger() {
        return caller.getPassenger();
    }

	@Override
	public List<Entity> getPassengers() {
        return caller.getPassengers();
	}
	
    @Override
    public boolean setPassenger(Entity passenger) {
        return caller.setPassenger(passenger);
    }

    @Override
    public boolean isEmpty() {
        return caller.isEmpty();
    }

    @Override
    public boolean eject() {
        return caller.eject();
    }

    @Override
    public @NotNull ItemStack getPickItemStack() {
        return null;
    }

    @Override
    public float getFallDistance() {
        return caller.getFallDistance();
    }

    @Override
    public void setFallDistance(float distance) {
        caller.setFallDistance(distance);
    }

    @Override
    public void setLastDamageCause(EntityDamageEvent event) {
        caller.setLastDamageCause(event);
    }

    @Override
    public EntityDamageEvent getLastDamageCause() {
        return caller.getLastDamageCause();
    }

    @Override
    public UUID getUniqueId() {
        return caller.getUniqueId();
    }

    @NotNull
    @Override
    public PlayerProfile getPlayerProfile() {
        return caller.getPlayerProfile();
    }

    @Override
    public boolean isOnline() {
        return caller.isOnline();
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public String getDisplayName() {
        return caller.getDisplayName();
    }

    @Override
    public void setDisplayName(String name) {
        caller.setDisplayName(name);
    }

    @Override
    public void playerListName(@org.jspecify.annotations.Nullable Component name) {

    }

    @Override
    public Component playerListName() {
        return null;
    }

    @Override
    public @org.jspecify.annotations.Nullable Component playerListHeader() {
        return null;
    }

    @Override
    public @org.jspecify.annotations.Nullable Component playerListFooter() {
        return null;
    }

    @Override
    public void setCompassTarget(Location loc) {
        caller.setCompassTarget(loc);
    }

    @Override
    public Location getCompassTarget() {
        return caller.getCompassTarget();
    }

    @Override
    public InetSocketAddress getAddress() {
        return caller.getAddress();
    }

    @Override
    public int getProtocolVersion() {
        return 0;
    }

    @Override
    public @org.jspecify.annotations.Nullable InetSocketAddress getVirtualHost() {
        return null;
    }

    @Override
    public void sendRawMessage(String message) {
        caller.sendRawMessage(message);
    }

    @Override
    public void sendRawMessage(@Nullable UUID uuid, @NotNull String s) {

    }

    @Override
    public void kickPlayer(String message) {
        caller.kickPlayer(message);
    }

    @Override
    public void kick(@org.jspecify.annotations.Nullable Component message, PlayerKickEvent.Cause cause) {

    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @org.jspecify.annotations.Nullable E ban(
            @org.jspecify.annotations.Nullable String reason, @org.jspecify.annotations.Nullable Date expires,
            @org.jspecify.annotations.Nullable String source, boolean kickPlayer) {
        return null;
    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @org.jspecify.annotations.Nullable E ban(
            @org.jspecify.annotations.Nullable String reason, @org.jspecify.annotations.Nullable Instant expires,
            @org.jspecify.annotations.Nullable String source, boolean kickPlayer) {
        return null;
    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @org.jspecify.annotations.Nullable E ban(
            @org.jspecify.annotations.Nullable String reason, @org.jspecify.annotations.Nullable Duration duration,
            @org.jspecify.annotations.Nullable String source, boolean kickPlayer) {
        return null;
    }

    @Override
    public @org.jspecify.annotations.Nullable BanEntry<InetAddress> banIp(
            @org.jspecify.annotations.Nullable String reason, @org.jspecify.annotations.Nullable Date expires,
            @org.jspecify.annotations.Nullable String source, boolean kickPlayer) {
        return null;
    }

    @Override
    public @org.jspecify.annotations.Nullable BanEntry<InetAddress> banIp(
            @org.jspecify.annotations.Nullable String reason, @org.jspecify.annotations.Nullable Instant expires,
            @org.jspecify.annotations.Nullable String source, boolean kickPlayer) {
        return null;
    }

    @Override
    public @org.jspecify.annotations.Nullable BanEntry<InetAddress> banIp(
            @org.jspecify.annotations.Nullable String reason, @org.jspecify.annotations.Nullable Duration duration,
            @org.jspecify.annotations.Nullable String source, boolean kickPlayer) {
        return null;
    }

    @Override
    public void chat(String msg) {
        caller.chat(msg);
    }

    @Override
    public boolean performCommand(String command) {
        return caller.performCommand(command);
    }

    @Override
    public boolean isSneaking() {
        return caller.isSneaking();
    }

    @Override
    public void setSneaking(boolean sneak) {
        caller.setSneaking(sneak);
    }

    @Override
    public void setPose(@NotNull Pose pose, boolean fixed) {

    }

    @Override
    public boolean hasFixedPose() {
        return false;
    }

    @Override
    public void saveData() {
        caller.saveData();
    }

    @Override
    public void loadData() {
        caller.loadData();
    }

    @Override
    public void setSleepingIgnored(boolean isSleeping) {
        caller.setSleepingIgnored(isSleeping);
    }

    @Override
    public boolean isSleepingIgnored() {
        return caller.isSleepingIgnored();
    }

    @Override
    public void playNote(Location loc, byte instrument, byte note) {
        caller.playNote(loc, instrument, note);
    }

    @Override
    public void playNote(Location loc, Instrument instrument, Note note) {
        caller.playNote(loc, instrument, note);
    }

    @Override
    public void playEffect(Location loc, Effect effect, int data) {
        caller.playEffect(loc, effect, data);
    }

    @Override
    public void sendBlockChange(Location loc, Material material, byte data) {
        caller.sendBlockChange(loc, material, data);
    }

    @Override
    public void sendMap(MapView map) {
        caller.sendMap(map);
    }

    @Override
    public void sendHurtAnimation(float v) {

    }

    @Override
    public void addCustomChatCompletions(@NotNull Collection<String> collection) {

    }

    @Override
    public void removeCustomChatCompletions(@NotNull Collection<String> collection) {

    }

    @Override
    public void setCustomChatCompletions(@NotNull Collection<String> collection) {

    }

    @Override
    public void updateInventory() {
        caller.updateInventory();
    }

    @Nullable
    @Override
    public GameMode getPreviousGameMode() {
        return null;
    }

    @Override
    public void incrementStatistic(Statistic statistic) {
        caller.incrementStatistic(statistic);
    }

    @Override
    public void incrementStatistic(Statistic statistic, int amount) {
        caller.incrementStatistic(statistic, amount);
    }

    @Override
    public void incrementStatistic(Statistic statistic, Material material) {
        caller.incrementStatistic(statistic, material);
    }

    @Override
    public void incrementStatistic(Statistic statistic, Material material,
            int amount) {
        caller.incrementStatistic(statistic, material, amount);
    }

    @Override
    public void setPlayerTime(long time, boolean relative) {
        caller.setPlayerTime(time, relative);
    }

    @Override
    public long getPlayerTime() {
        return caller.getPlayerTime();
    }

    @Override
    public long getPlayerTimeOffset() {
        return caller.getPlayerTimeOffset();
    }

    @Override
    public boolean isPlayerTimeRelative() {
        return caller.isPlayerTimeRelative();
    }

    @Override
    public void resetPlayerTime() {
        caller.resetPlayerTime();
    }

    @Override
    public GameMode getGameMode() {
        return caller.getGameMode();
    }

    @Override
    public void setGameMode(GameMode mode) {
        caller.setGameMode(mode);
    }

    @Override
    public boolean isBanned() {
        return caller.isBanned();
    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @org.jspecify.annotations.Nullable E ban(
            @org.jspecify.annotations.Nullable String reason, @org.jspecify.annotations.Nullable Date expires,
            @org.jspecify.annotations.Nullable String source) {
        return null;
    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @org.jspecify.annotations.Nullable E ban(
            @org.jspecify.annotations.Nullable String reason, @org.jspecify.annotations.Nullable Instant expires,
            @org.jspecify.annotations.Nullable String source) {
        return null;
    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @org.jspecify.annotations.Nullable E ban(
            @org.jspecify.annotations.Nullable String reason, @org.jspecify.annotations.Nullable Duration duration,
            @org.jspecify.annotations.Nullable String source) {
        return null;
    }

    @Override
    public boolean isWhitelisted() {
        return caller.isWhitelisted();
    }

    @Override
    public void setWhitelisted(boolean wl) {
        caller.setWhitelisted(wl);
    }

    @Override
    public float getExhaustion() {
        return caller.getExhaustion();
    }

    @Override
    public int getFoodLevel() {
        return caller.getFoodLevel();
    }

    @Override
    public int getLevel() {
        return caller.getLevel();
    }

    @Override
    public float getSaturation() {
        return caller.getSaturation();
    }

    @Override
    public int getTotalExperience() {
        return caller.getTotalExperience();
    }

    @Override
    public void setExhaustion(float exhaustion) {
        caller.setExhaustion(exhaustion);
    }

    @Override
    public void setFoodLevel(int food) {
        caller.setFoodLevel(food);
    }

    @Override
    public int getSaturatedRegenRate() {
        return 0;
    }

    @Override
    public void setSaturatedRegenRate(int i) {

    }

    @Override
    public int getUnsaturatedRegenRate() {
        return 0;
    }

    @Override
    public void setUnsaturatedRegenRate(int i) {

    }

    @Override
    public int getStarvationRate() {
        return 0;
    }

    @Override
    public void setStarvationRate(int i) {

    }

    @Nullable
    @Override
    public Location getLastDeathLocation() {
        return null;
    }

    @Override
    public void setLastDeathLocation(@Nullable Location location) {

    }

    @Nullable
    @Override
    public Firework fireworkBoost(@NotNull ItemStack itemStack) {
        return null;
    }

    @Override
    public void setLevel(int lvl) {
        caller.setLevel(lvl);
    }

    @Override
    public void setSaturation(float saturation) {
        caller.setSaturation(saturation);
    }

    @Override
    public void setTotalExperience(int xp) {
        caller.setTotalExperience(xp);
    }

    @Override
    public void sendExperienceChange(float v) {

    }

    @Override
    public void sendExperienceChange(float v, int i) {

    }

    @Override
    public Location getBedSpawnLocation() {
        return caller.getBedSpawnLocation();
    }

    @Override
    public long getLastLogin() {
        return 0;
    }

    @Override
    public long getLastSeen() {
        return 0;
    }

    @Override
    public @org.jspecify.annotations.Nullable Location getRespawnLocation(boolean loadLocationAndValidate) {
        return null;
    }

    @Override
    public boolean isSprinting() {
        return caller.isSprinting();
    }

    @Override
    public void setSprinting(boolean run) {
        caller.setSprinting(run);
    }

    @Override
    public int getTicksLived() {
        return caller.getTicksLived();
    }

    @Override
    public void setTicksLived(int value) {
        caller.setTicksLived(value);
    }

    @Override
    public Player getPlayer() {
        return caller.getPlayer();
    }

    @Override
    public Map<String, Object> serialize() {
        return caller.serialize();
    }

    @Override
    public String getPlayerListName() {
        return caller.getPlayerListName();
    }

    @Override
    public void setPlayerListName(String name) {
        caller.setPlayerListName(name);
    }

    @Override
    public int getPlayerListOrder() {
        return 0;
    }

    @Override
    public void setPlayerListOrder(int order) {

    }

    @Override
    public boolean teleport(Location location, TeleportCause cause) {
        return caller.teleport(location, cause);
    }

    @Override
    public boolean teleport(Entity destination, TeleportCause cause) {
        return caller.teleport(destination, cause);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> teleportAsync(@NotNull Location loc,
                                                             @NotNull PlayerTeleportEvent.TeleportCause cause,
                                                             @NotNull TeleportFlag @NotNull ... teleportFlags) {
        return null;
    }

    @Override
    public void giveExp(int amount) {
        caller.giveExp(amount);
    }

    @Override
    public float getExp() {
        return caller.getExp();
    }

    @Override
    public void setExp(float exp) {
        caller.setExp(exp);
    }

    @Override
    public Player getKiller() {
        return caller.getKiller();
    }

    @Override
    public void setKiller(@Nullable Player killer) {

    }

    @Override
    public long getFirstPlayed() {
        return caller.getFirstPlayed();
    }

    @Override
    public long getLastPlayed() {
        return caller.getLastPlayed();
    }

    @Override
    public boolean hasPlayedBefore() {
        return caller.hasPlayedBefore();
    }

    @Override
    public Set<String> getListeningPluginChannels() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sendPluginMessage(Plugin arg0, String arg1, byte[] arg2) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public boolean getAllowFlight() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setAllowFlight(boolean arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public void playEffect(EntityEffect arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public boolean canSee(Player arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void hideEntity(@NotNull Plugin plugin, @NotNull Entity entity) {

    }

    @Override
    public void showEntity(@NotNull Plugin plugin, @NotNull Entity entity) {

    }

    @Override
    public boolean canSee(@NotNull Entity entity) {
        return false;
    }

    @Override
    public void hidePlayer(Player arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public void setBedSpawnLocation(Location arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public void showPlayer(Player arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public boolean addPotionEffect(PotionEffect arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean addPotionEffect(PotionEffect arg0, boolean arg1) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean addPotionEffects(Collection<PotionEffect> arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<PotionEffect> getActivePotionEffects() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean clearActivePotionEffects() {
        return false;
    }

    @Override
    public boolean hasPotionEffect(PotionEffectType arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void removePotionEffect(PotionEffectType arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public <T> void playEffect(Location arg0, Effect arg1, T arg2) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public boolean breakBlock(@NotNull Block block) {
        return false;
    }

    @Override
    public void closeInventory() {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public void closeInventory(InventoryCloseEvent.Reason reason) {

    }

    @Override
    public ItemStack getItemOnCursor() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public InventoryView getOpenInventory() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public InventoryView openEnchanting(Location arg0, boolean arg1) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public InventoryView openInventory(Inventory arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void openInventory(InventoryView arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public InventoryView openWorkbench(Location arg0, boolean arg1) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setItemOnCursor(ItemStack arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public boolean setWindowProperty(Property arg0, int arg1) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getEnchantmentSeed() {
        return 0;
    }

    @Override
    public void setEnchantmentSeed(int i) {

    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public EntityType getType() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @NotNull
    @Override
    public Sound getSwimSound() {
        return null;
    }

    @NotNull
    @Override
    public Sound getSwimSplashSound() {
        return null;
    }

    @NotNull
    @Override
    public Sound getSwimHighSpeedSplashSound() {
        return null;
    }

    @Override
    public List<MetadataValue> getMetadata(String arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean hasMetadata(String arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void removeMetadata(String arg0, Plugin arg1) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public void setMetadata(String arg0, MetadataValue arg1) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public void abandonConversation(Conversation arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public void acceptConversationInput(String arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean beginConversation(Conversation arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean isConversing() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void sendMessage(String[] arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public void sendMessage(@Nullable UUID uuid, @NotNull String s) {

    }

    @Override
    public void sendMessage(@Nullable UUID uuid, @NotNull String... strings) {

    }

    @Override
    public Entity getVehicle() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void abandonConversation(Conversation arg0,
            ConversationAbandonedEvent arg1) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Inventory getEnderChest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getExpToLevel() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public float getAttackCooldown() {
        return 0;
    }

    @Override
    public boolean isBlocking() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean getCanPickupItems() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public EntityEquipment getEquipment() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean getRemoveWhenFarAway() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean hasLineOfSight(Entity arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean hasLineOfSight(@NotNull Location location) {
        return false;
    }

    @Override
    public void setCanPickupItems(boolean arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public void setRemoveWhenFarAway(boolean arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public Location getLocation(Location arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean isValid() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void resetMaxHealth() {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public float getFlySpeed() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float getWalkSpeed() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void giveExpLevels(int arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public boolean isFlying() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void playSound(Location arg0, Sound arg1, float arg2, float arg3) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public void playSound(Location location, String sound, SoundCategory category, float volume, float pitch) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public void playSound(@NotNull Entity entity, @NotNull Sound sound, float v, float v1) {

    }

    @Override
    public void playSound(@NotNull Entity entity, @NotNull String s, float v, float v1) {

    }

    @Override
    public void playSound(@NotNull Entity entity, @NotNull Sound sound, @NotNull SoundCategory soundCategory, float v, float v1) {

    }

    @Override
    public void playSound(@NotNull Entity entity, @NotNull String s, @NotNull SoundCategory soundCategory, float v, float v1) {

    }

    @Override
    public void playSound(Location location, Sound sound, SoundCategory category, float volume, float pitch) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public void setBedSpawnLocation(Location arg0, boolean arg1) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public void setFlySpeed(float arg0) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public void setFlying(boolean arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    // for 1.4.6
    public void setTexturePack(String arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public void setWalkSpeed(float arg0) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public @Nullable Component customName() {
        return null;
    }

    @Override
    public void customName(@Nullable Component customName) {

    }

    @Override
    public String getCustomName() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean isCustomNameVisible() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setVisibleByDefault(boolean b) {

    }

    @Override
    public boolean isVisibleByDefault() {
        return false;
    }

    @Override
    public @NotNull Set<Player> getTrackedBy() {
        return Set.of();
    }

    @Override
    public boolean isTrackedBy(@NotNull Player player) {
        return false;
    }

    @Override
    public void setCustomName(String arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public void setCustomNameVisible(boolean arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public WeatherType getPlayerWeather() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Scoreboard getScoreboard() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    @Deprecated
    public boolean isOnGround() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean isInWater() {
        return false;
    }

    @Override
    public void resetPlayerWeather() {
        throw new UnsupportedOperationException("Not supported yet."); 
        
    }

    @Override
    public int getExpCooldown() {
        return 0;
    }

    @Override
    public void setExpCooldown(int i) {

    }

    @Override
    public void setPlayerWeather(WeatherType arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 
        
    }

    @Override
    public void setScoreboard(Scoreboard arg0) throws IllegalArgumentException,
            IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet."); 
        
    }

    @Nullable
    @Override
    public WorldBorder getWorldBorder() {
        return null;
    }

    @Override
    public void setWorldBorder(@Nullable WorldBorder worldBorder) {

    }

    @Override
    public void setLastDamage(double arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public void damage(double arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public void damage(double arg0, Entity arg1) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public void damage(double amount, @NotNull DamageSource damageSource) {

    }

    @Override
    public void setHealth(double arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public void heal(double amount, @NotNull EntityRegainHealthEvent.RegainReason reason) {

    }

    @Override
    public double getAbsorptionAmount() {
        return 0;
    }

    @Override
    public void setAbsorptionAmount(double v) {

    }

    @Override
    public void setMaxHealth(double arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public double getLastDamage() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public double getHealth() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public double getMaxHealth() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Entity getLeashHolder() throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean isLeashed() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean setLeashHolder(Entity arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getHealthScale() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean isHealthScaled() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void playSound(Location arg0, String arg1, float arg2, float arg3) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public void setHealthScale(double arg0) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public void setHealthScaled(boolean arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public void setResourcePack(String arg0) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public void stopSound(Sound sound) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void stopSound(String string) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void sendSignChange(Location lctn, String[] strings) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void decrementStatistic(Statistic ststc) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void decrementStatistic(Statistic ststc, int i) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setStatistic(Statistic ststc, int i) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getStatistic(Statistic ststc) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void decrementStatistic(Statistic ststc, Material mtrl) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getStatistic(Statistic ststc, Material mtrl) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void decrementStatistic(Statistic ststc, Material mtrl, int i) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setStatistic(Statistic ststc, Material mtrl, int i) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void incrementStatistic(Statistic ststc, EntityType et) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void decrementStatistic(Statistic ststc, EntityType et) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getStatistic(Statistic ststc, EntityType et) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void incrementStatistic(Statistic ststc, EntityType et, int i) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void decrementStatistic(Statistic ststc, EntityType et, int i) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setStatistic(Statistic ststc, EntityType et, int i) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Entity getSpectatorTarget() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setSpectatorTarget(Entity entity) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void sendTitle(String string, String string1) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void sendTitle(String string, String string1, int int1, int int2, int int3) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void stopSound(Sound sound, SoundCategory category) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void stopSound(String sound, SoundCategory category) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void stopSound(@NotNull SoundCategory soundCategory) {

    }

    @Override
    public void stopAllSounds() {

    }

    @Override
    public void resetTitle() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void spawnParticle(Particle prtcl, Location lctn, int i) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void spawnParticle(Particle prtcl, double d, double d1, double d2, int i) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public <T> void spawnParticle(Particle prtcl, Location lctn, int i, T t) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public <T> void spawnParticle(Particle prtcl, double d, double d1, double d2, int i, T t) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void spawnParticle(Particle prtcl, Location lctn, int i, double d, double d1, double d2) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void spawnParticle(Particle prtcl, double d, double d1, double d2, int i, double d3, double d4, double d5) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public <T> void spawnParticle(Particle prtcl, Location lctn, int i, double d, double d1, double d2, T t) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public <T> void spawnParticle(Particle prtcl, double d, double d1, double d2, int i, double d3, double d4, double d5, T t) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void spawnParticle(Particle prtcl, Location lctn, int i, double d, double d1, double d2, double d3) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void spawnParticle(Particle prtcl, double d, double d1, double d2, int i, double d3, double d4, double d5, double d6) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public <T> void spawnParticle(Particle prtcl, Location lctn, int i, double d, double d1, double d2, double d3, T t) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public <T> void spawnParticle(Particle prtcl, double d, double d1, double d2, int i, double d3, double d4, double d5, double d6, T t) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX,
                                  double offsetY, double offsetZ, double extra,
                                  @org.jspecify.annotations.Nullable T data, boolean force) {

    }

    @Override
    public MainHand getMainHand() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public InventoryView openMerchant(Villager vlgr, boolean bln) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
    
    @Override
    public InventoryView openMerchant(Merchant merchant, boolean force) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

    @Override
    public @org.jspecify.annotations.Nullable InventoryView openAnvil(
            @org.jspecify.annotations.Nullable Location location, boolean force) {
        return null;
    }

    @Override
    public @org.jspecify.annotations.Nullable InventoryView openCartographyTable(
            @org.jspecify.annotations.Nullable Location location, boolean force) {
        return null;
    }

    @Override
    public @org.jspecify.annotations.Nullable InventoryView openGrindstone(
            @org.jspecify.annotations.Nullable Location location, boolean force) {
        return null;
    }

    @Override
    public @org.jspecify.annotations.Nullable InventoryView openLoom(
            @org.jspecify.annotations.Nullable Location location, boolean force) {
        return null;
    }

    @Override
    public @org.jspecify.annotations.Nullable InventoryView openSmithingTable(
            @org.jspecify.annotations.Nullable Location location, boolean force) {
        return null;
    }

    @Override
    public @org.jspecify.annotations.Nullable InventoryView openStonecutter(
            @org.jspecify.annotations.Nullable Location location, boolean force) {
        return null;
    }

    @Override
    public List<Block> getLineOfSight(Set<Material> set, int i) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Block getTargetBlock(Set<Material> set, int i) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public @Nullable Block getTargetBlock(int maxDistance, @NotNull TargetBlockInfo.FluidMode fluidMode) {
        return null;
    }

    @Override
    public @Nullable BlockFace getTargetBlockFace(int maxDistance, @NotNull TargetBlockInfo.FluidMode fluidMode) {
        return null;
    }

    @Override
    public @Nullable BlockFace getTargetBlockFace(int maxDistance, @NotNull FluidCollisionMode fluidMode) {
        return null;
    }

    @Override
    public @Nullable TargetBlockInfo getTargetBlockInfo(int maxDistance, @NotNull TargetBlockInfo.FluidMode fluidMode) {
        return null;
    }

    @Override
    public @Nullable Entity getTargetEntity(int maxDistance, boolean ignoreBlocks) {
        return null;
    }

    @Override
    public @Nullable TargetEntityInfo getTargetEntityInfo(int maxDistance, boolean ignoreBlocks) {
        return null;
    }

    @Override
    public @Nullable RayTraceResult rayTraceEntities(int maxDistance, boolean ignoreBlocks) {
        return null;
    }

    @Override
    public List<Block> getLastTwoTargetBlocks(Set<Material> set, int i) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean isGliding() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setGliding(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setAI(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean hasAI() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void attack(@NotNull Entity entity) {

    }

    @Override
    public void swingMainHand() {

    }

    @Override
    public void swingOffHand() {

    }

    @Override
    public void playHurtAnimation(float yaw) {

    }

    @Override
    public void setCollidable(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean isCollidable() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @NotNull
    @Override
    public Set<UUID> getCollidableExemptions() {
        return null;
    }

    @Override
    public AttributeInstance getAttribute(Attribute atrbt) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void registerAttribute(@NotNull Attribute attribute) {

    }

    @Override
    public void setGlowing(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean isGlowing() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setInvulnerable(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean isInvulnerable() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean isSilent() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setSilent(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean hasGravity() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setGravity(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> type, Vector vector) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public <T extends Projectile> @NotNull T launchProjectile(@NotNull Class<? extends T> projectile,
                                                              @Nullable Vector velocity,
                                                              @Nullable Consumer<? super T> function) {
        return null;
    }

    @Override
    public boolean isHandRaised() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean isJumping() {
        return false;
    }

    @Override
    public void setJumping(boolean jumping) {

    }

    @Override
    public void playPickupItemAnimation(@NotNull Item item, int quantity) {

    }

    @Override
    public float getHurtDirection() {
        return 0;
    }

    @Nullable
    @Override
    public ItemStack getItemInUse() {
        return null;
    }

    @Override
    public int getItemInUseTicks() {
        return 0;
    }

    @Override
    public void setItemInUseTicks(int ticks) {

    }

    @Override
    public PotionEffect getPotionEffect(PotionEffectType pet) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getPortalCooldown() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setPortalCooldown(int i) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Set<String> getScoreboardTags() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean addScoreboardTag(String string) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean removeScoreboardTag(String string) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

	@Override
	public int getCooldown(Material arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Entity getShoulderEntityLeft() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Entity getShoulderEntityRight() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean hasCooldown(Material arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setCooldown(Material arg0, int arg1) {
		throw new UnsupportedOperationException("Not supported yet.");
		
	}

	@Override
	public void setShoulderEntityLeft(Entity arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
		
	}

	@Override
	public void setShoulderEntityRight(Entity arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
		
	}

    @Override
    public boolean dropItem(boolean b) {
        return false;
    }

    @Override
	public boolean addPassenger(Entity arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public double getHeight() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public PistonMoveReaction getPistonMoveReaction() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public double getWidth() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean removePassenger(Entity arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public AdvancementProgress getAdvancementProgress(Advancement arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getLocale() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void hidePlayer(Plugin arg0, Player arg1) {
		throw new UnsupportedOperationException("Not supported yet.");		
	}

	@Override
	public void setResourcePack(String arg0, byte[] arg1) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

    @Override
    public void setResourcePack(@NotNull String s, @Nullable byte[] bytes, @Nullable String s1) {

    }

    @Override
    public void setResourcePack(@NotNull String s, @Nullable byte[] bytes, boolean b) {

    }

    @Override
    public void setResourcePack(@NotNull String s, @Nullable byte[] bytes, @Nullable String s1, boolean b) {

    }

    @Override
	public void showPlayer(Plugin arg0, Player arg1) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isSwimming() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setSwimming(boolean arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void sendBlockChange(Location arg0, BlockData arg1) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

    @Override
    public void sendBlockChanges(@NotNull Collection<BlockState> collection, boolean b) {

    }

    @Override
    public void sendBlockDamage(@NotNull Location location, float v) {

    }

    @Override
    public void sendBlockDamage(@NotNull Location location, float v, @NotNull Entity entity) {

    }

    @Override
    public void sendBlockDamage(@NotNull Location location, float v, int i) {

    }

    @Override
    public void sendEquipmentChange(@NotNull LivingEntity livingEntity, @NotNull EquipmentSlot equipmentSlot, @Nullable ItemStack itemStack) {

    }

    @Override
    public void sendEquipmentChange(@NotNull LivingEntity livingEntity, @NotNull Map<EquipmentSlot, ItemStack> map) {

    }

    @Override
	public boolean isRiptiding() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

    @Override
    public void setRiptiding(boolean riptiding) {

    }

    @Override
	public boolean isPersistent() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setPersistent(boolean arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getPlayerListFooter() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getPlayerListHeader() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setPlayerListFooter(String arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setPlayerListHeader(String arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setPlayerListHeaderFooter(String arg0, String arg1) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Spigot spigot() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

    @Override
    public @NotNull Component name() {
        return null;
    }

    @Override
    public @NotNull Component teamDisplayName() {
        return null;
    }

    @Override
    public @Nullable Location getOrigin() {
        return null;
    }

    @Override
    public boolean fromMobSpawner() {
        return false;
    }

    @NotNull
    @Override
    public CreatureSpawnEvent.SpawnReason getEntitySpawnReason() {
        return null;
    }

    @Override
    public boolean isUnderWater() {
        return false;
    }

    @Override
    public boolean isInRain() {
        return false;
    }

    @Override
    public boolean isInLava() {
        return false;
    }

    @Override
    public boolean isTicking() {
        return false;
    }

    @Override
    public @NotNull Set<Player> getTrackedPlayers() {
        return Set.of();
    }

    @Override
    public boolean spawnAt(@NotNull Location location, @NotNull CreatureSpawnEvent.SpawnReason reason) {
        return false;
    }

    @Override
    public boolean isInPowderedSnow() {
        return false;
    }

    @Override
    public double getX() {
        return 0;
    }

    @Override
    public double getY() {
        return 0;
    }

    @Override
    public double getZ() {
        return 0;
    }

    @Override
    public float getPitch() {
        return 0;
    }

    @Override
    public float getYaw() {
        return 0;
    }

    @Override
    public boolean collidesAt(@NotNull Location location) {
        return false;
    }

    @Override
    public boolean wouldCollideUsing(@NotNull BoundingBox boundingBox) {
        return false;
    }

    @Override
    public @NotNull EntityScheduler getScheduler() {
        return null;
    }

    @Override
    public @NotNull String getScoreboardEntryName() {
        return "";
    }

    @Override
    public void broadcastHurtAnimation(@NotNull Collection<Player> players) {

    }

    @Override
	public void updateCommands() {
		throw new UnsupportedOperationException("Not supported yet.");
		
	}

	@Override
	public boolean discoverRecipe(NamespacedKey arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int discoverRecipes(Collection<NamespacedKey> arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean undiscoverRecipe(NamespacedKey arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int undiscoverRecipes(Collection<NamespacedKey> arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

    @Override
    public boolean hasDiscoveredRecipe(@NotNull NamespacedKey namespacedKey) {
        return false;
    }

    @NotNull
    @Override
    public Set<NamespacedKey> getDiscoveredRecipes() {
        return null;
    }

    @Override
	public BlockFace getFacing() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Block getTargetBlockExact(int arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Block getTargetBlockExact(int arg0, FluidCollisionMode arg1) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public RayTraceResult rayTraceBlocks(double arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public RayTraceResult rayTraceBlocks(double arg0, FluidCollisionMode arg1) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public BoundingBox getBoundingBox() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getClientViewDistance() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

    @Override
    public int getPing() {
        return 0;
    }

    @Override
	public Location getBedLocation() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean sleep(Location arg0, boolean arg1) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void wakeup(boolean arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setRotation(float arg0, float arg1) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

    @Override
    public boolean teleport(@NotNull Location location, @NotNull PlayerTeleportEvent.TeleportCause cause,
                            @NotNull TeleportFlag @NotNull ... teleportFlags) {
        return false;
    }

    @Override
    public void lookAt(double x, double y, double z, @NotNull LookAnchor entityAnchor) {

    }

    @Override
	public <T> T getMemory(MemoryKey<T> arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public <T> void setMemory(MemoryKey<T> arg0, T arg1) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

    @Nullable
    @Override
    public Sound getHurtSound() {
        return null;
    }

    @Nullable
    @Override
    public Sound getDeathSound() {
        return null;
    }

    @NotNull
    @Override
    public Sound getFallDamageSound(int i) {
        return null;
    }

    @NotNull
    @Override
    public Sound getFallDamageSoundSmall() {
        return null;
    }

    @NotNull
    @Override
    public Sound getFallDamageSoundBig() {
        return null;
    }

    @NotNull
    @Override
    public Sound getDrinkingSound(@NotNull ItemStack itemStack) {
        return null;
    }

    @NotNull
    @Override
    public Sound getEatingSound(@NotNull ItemStack itemStack) {
        return null;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return false;
    }

    @NotNull
    @Override
    public EntityCategory getCategory() {
        return null;
    }

    @Override
    public float getSidewaysMovement() {
        return 0;
    }

    @Override
    public float getUpwardsMovement() {
        return 0;
    }

    @Override
    public float getForwardsMovement() {
        return 0;
    }

    @Override
    public void startUsingItem(@NotNull EquipmentSlot hand) {

    }

    @Override
    public void completeUsingActiveItem() {

    }

    @Override
    public @NotNull ItemStack getActiveItem() {
        return null;
    }

    @Override
    public void clearActiveItem() {

    }

    @Override
    public int getActiveItemRemainingTime() {
        return 0;
    }

    @Override
    public void setActiveItemRemainingTime(@Range(from = 0L, to = 2147483647L) int ticks) {

    }

    @Override
    public boolean hasActiveItem() {
        return false;
    }

    @Override
    public int getActiveItemUsedTime() {
        return 0;
    }

    @Override
    public @NotNull EquipmentSlot getActiveItemHand() {
        return null;
    }

    @Override
    public void setInvisible(boolean b) {

    }

    @Override
    public boolean isInvisible() {
        return false;
    }

    @Override
    public void setNoPhysics(boolean noPhysics) {

    }

    @Override
    public boolean hasNoPhysics() {
        return false;
    }

    @Override
    public boolean isFreezeTickingLocked() {
        return false;
    }

    @Override
    public void lockFreezeTicks(boolean locked) {

    }

    @Override
	public Pose getPose() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

    @NotNull
    @Override
    public SpawnCategory getSpawnCategory() {
        return null;
    }

    @Override
    public boolean isInWorld() {
        return false;
    }

    @Override
    public @Nullable String getAsString() {
        return "";
    }

    @Override
    public @Nullable EntitySnapshot createSnapshot() {
        return null;
    }

    @Override
    public @NotNull Entity copy() {
        return null;
    }

    @Override
    public @NotNull Entity copy(@NotNull Location to) {
        return null;
    }

    @Override
	public PersistentDataContainer getPersistentDataContainer() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void openBook(ItemStack arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

    @Override
    public void openSign(@NotNull Sign sign) {

    }

    @Override
    public void openSign(@NotNull Sign sign, @NotNull Side side) {

    }

    @Override
    public void showDemoScreen() {

    }

    @Override
    public boolean isAllowingServerListings() {
        return false;
    }

    @Override
	public void sendSignChange(Location arg0, String[] arg1, DyeColor arg2) throws IllegalArgumentException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

    @Override
    public void sendSignChange(@NotNull Location location, @Nullable String[] strings, @NotNull DyeColor dyeColor, boolean b) throws IllegalArgumentException {

    }

    @NotNull
    @Override
    public PlayerGameConnection getConnection() {
        return caller.getConnection();
    }

    @Override
    public int getDeathScreenScore() {
        return caller.getDeathScreenScore();
    }

    @Override
    public void setDeathScreenScore(int score) {
        caller.setDeathScreenScore(score);
    }

    @NotNull
    @Override
    public Set<net.kyori.adventure.bossbar.BossBar> activeBossBars() {
        Set<net.kyori.adventure.bossbar.BossBar> set =  new HashSet<>();
        for (BossBar bossBar : caller.activeBossBars()) {
            set.add(bossBar);
        }
        return set;
    }

    @Override
    public Component displayName() {
        return null;
    }

    @Override
    public void displayName(@org.jspecify.annotations.Nullable Component displayName) {

    }

    @Override
    public void sendEntityEffect(@NotNull EntityEffect effect, @NotNull Entity entity) {
        caller.sendEntityEffect(effect, entity);
    }

    @NotNull
    @Override
    public io.papermc.paper.entity.PlayerGiveResult give(@NotNull Collection<ItemStack> items, boolean allowDrop) {
        return caller.give(items, allowDrop);
    }

    @NotNull
    @Override
    public java.util.Set<Long> getSentChunkKeys() {
        return caller.getSentChunkKeys();
    }

    @NotNull
    @Override
    public java.util.Set<Chunk> getSentChunks() {
        return caller.getSentChunks();
    }

    @Override
    public boolean isChunkSent(long chunkKey) {
        return caller.isChunkSent(chunkKey);
    }

    @NotNull
    @Override
    public java.time.Duration getIdleDuration() {
        return caller.getIdleDuration();
    }

    @Override
    public void resetIdleDuration() {
        caller.resetIdleDuration();
    }

    @NotNull
    @Override
    public java.util.Collection<EnderPearl> getEnderPearls() {
        return caller.getEnderPearls();
    }

    @NotNull
    @Override
    public org.bukkit.Input getCurrentInput() {
        return caller.getCurrentInput();
    }

    @Override
    public void setRespawnLocation(@Nullable Location location, boolean force) {
        caller.setRespawnLocation(location, force);
    }

    @Nullable
    @Override
    public InetSocketAddress getHAProxyAddress() {
        return caller.getHAProxyAddress();
    }

    @Override
    public boolean isTransferred() {
        return caller.isTransferred();
    }

    @NotNull
    @Override
    public java.util.concurrent.CompletableFuture<byte[]> retrieveCookie(@NotNull NamespacedKey key) {
        return caller.retrieveCookie(key);
    }

    @Override
    public void storeCookie(@NotNull NamespacedKey key, byte @NotNull [] data) {
        caller.storeCookie(key, data);
    }

    @Override
    public void transfer(@NotNull String host, int port) {
        caller.transfer(host, port);
    }

    @Override
    public void showWinScreen() {
        caller.showWinScreen();
    }

    @Override
    public boolean hasSeenWinScreen() {
        return caller.hasSeenWinScreen();
    }

    @Override
    public void setHasSeenWinScreen(boolean seen) {
        caller.setHasSeenWinScreen(seen);
    }

    @Override
    public void sendActionBar(String message) {

    }

    @Override
    public void sendActionBar(char alternateChar, String message) {

    }

    @Override
    public void sendActionBar(BaseComponent... message) {

    }

    @Override
    public void setPlayerListHeaderFooter(BaseComponent @org.jspecify.annotations.Nullable [] header,
                                          BaseComponent @org.jspecify.annotations.Nullable [] footer) {

    }

    @Override
    public void setPlayerListHeaderFooter(@org.jspecify.annotations.Nullable BaseComponent header,
                                          @org.jspecify.annotations.Nullable BaseComponent footer) {

    }

    @Override
    public void setTitleTimes(int fadeInTicks, int stayTicks, int fadeOutTicks) {

    }

    @Override
    public void setSubtitle(BaseComponent[] subtitle) {

    }

    @Override
    public void setSubtitle(BaseComponent subtitle) {

    }

    @Override
    public void showTitle(@org.jspecify.annotations.Nullable BaseComponent[] title) {

    }

    @Override
    public void showTitle(@org.jspecify.annotations.Nullable BaseComponent title) {

    }

    @Override
    public void showTitle(@org.jspecify.annotations.Nullable BaseComponent[] title,
                          @org.jspecify.annotations.Nullable BaseComponent[] subtitle, int fadeInTicks, int stayTicks,
                          int fadeOutTicks) {

    }

    @Override
    public void showTitle(@org.jspecify.annotations.Nullable BaseComponent title,
                          @org.jspecify.annotations.Nullable BaseComponent subtitle, int fadeInTicks, int stayTicks,
                          int fadeOutTicks) {

    }

    @Override
    public void sendTitle(Title title) {

    }

    @Override
    public void updateTitle(Title title) {

    }

    @Override
    public void hideTitle() {

    }

    @Override
    public void sendHealthUpdate(double health, int food, float saturation) {
        caller.sendHealthUpdate(health, food, saturation);
    }

    @Override
    public void sendHealthUpdate() {
        caller.sendHealthUpdate();
    }

    @Override
    public int calculateTotalExperiencePoints() {
        return caller.calculateTotalExperiencePoints();
    }

    @Override
    public void setExperienceLevelAndProgress(int totalExperience) {
        caller.setExperienceLevelAndProgress(totalExperience);
    }

    @Override
    public int getExperiencePointsNeededForNextLevel() {
        return caller.getExperiencePointsNeededForNextLevel();
    }

    @Override
    public void giveExp(int amount, boolean applyMending) {
        caller.giveExp(amount, applyMending);
    }

    @Override
    public int applyMending(int amount) {
        return caller.applyMending(amount);
    }

    @Override
    public void setFlyingFallDamage(@NotNull net.kyori.adventure.util.TriState state) {
        caller.setFlyingFallDamage(state);
    }

    @NotNull
    @Override
    public net.kyori.adventure.util.TriState hasFlyingFallDamage() {
        return caller.hasFlyingFallDamage();
    }

    @Override
    public boolean isListed(@NotNull Player other) {
        return caller.isListed(other);
    }

    @Override
    public boolean unlistPlayer(@NotNull Player other) {
        return caller.unlistPlayer(other);
    }

    @Override
    public boolean listPlayer(@NotNull Player other) {
        return caller.listPlayer(other);
    }

    @Override
    public void setResourcePack(@NotNull UUID id, @NotNull String url, byte @Nullable [] hash, @Nullable String prompt, boolean force) {
        caller.setResourcePack(id, url, hash, prompt, force);
    }

    @Override
    public void setResourcePack(@NotNull UUID id, @NotNull String url, byte @Nullable [] hash, @Nullable net.kyori.adventure.text.Component prompt, boolean force) {
        caller.setResourcePack(id, url, hash, prompt, force);
    }

    @Override
    public org.bukkit.event.player.PlayerResourcePackStatusEvent.Status getResourcePackStatus() {
        return caller.getResourcePackStatus();
    }

    @Override
    public void addResourcePack(@NotNull UUID id, @NotNull String url, byte @Nullable [] hash, @Nullable String prompt, boolean force) {
        caller.addResourcePack(id, url, hash, prompt, force);
    }

    @Override
    public void removeResourcePack(@NotNull UUID id) {
        caller.removeResourcePack(id);
    }

    @Override
    public void removeResourcePacks() {
        caller.removeResourcePacks();
    }

    @Override
    public void setPlayerProfile(@NotNull PlayerProfile profile) {
        caller.setPlayerProfile(profile);
    }

    @Override
    public float getCooldownPeriod() {
        return caller.getCooldownPeriod();
    }

    @Override
    public float getCooledAttackStrength(float adjustTicks) {
        return caller.getCooledAttackStrength(adjustTicks);
    }

    @Override
    public void resetCooldown() {
        caller.resetCooldown();
    }

    @NotNull
    @Override
    public <T> T getClientOption(@NotNull com.destroystokyo.paper.ClientOption<T> option) {
        return caller.getClientOption(option);
    }

    @Override
    public void sendOpLevel(byte level) {
        caller.sendOpLevel(level);
    }

    @Override
    public void addAdditionalChatCompletions(@NotNull Collection<String> completions) {
        caller.addAdditionalChatCompletions(completions);
    }

    @Override
    public void removeAdditionalChatCompletions(@NotNull Collection<String> completions) {
        caller.removeAdditionalChatCompletions(completions);
    }

    @Nullable
    @Override
    public String getClientBrandName() {
        return caller.getClientBrandName();
    }

    @Override
    public void lookAt(@NotNull Entity entity, @NotNull io.papermc.paper.entity.LookAnchor playerAnchor, @NotNull io.papermc.paper.entity.LookAnchor entityAnchor) {
        caller.lookAt(entity, playerAnchor, entityAnchor);
    }

    @Override
    public void showElderGuardian(boolean silent) {
        caller.showElderGuardian(silent);
    }

    @Override
    public int getWardenWarningCooldown() {
        return caller.getWardenWarningCooldown();
    }

    @Override
    public void setWardenWarningCooldown(int cooldown) {
        caller.setWardenWarningCooldown(cooldown);
    }

    @Override
    public int getWardenTimeSinceLastWarning() {
        return caller.getWardenTimeSinceLastWarning();
    }

    @Override
    public void setWardenTimeSinceLastWarning(int time) {
        caller.setWardenTimeSinceLastWarning(time);
    }

    @Override
    public int getWardenWarningLevel() {
        return caller.getWardenWarningLevel();
    }

    @Override
    public void setWardenWarningLevel(int level) {
        caller.setWardenWarningLevel(level);
    }

    @Override
    public void increaseWardenWarningLevel() {
        caller.increaseWardenWarningLevel();
    }

    @Override
    public void sendLinks(@NotNull org.bukkit.ServerLinks links) {
        caller.sendLinks(links);
    }

    @Override
    public boolean getAffectsSpawning() {
        return caller.getAffectsSpawning();
    }

    @Override
    public void setAffectsSpawning(boolean affects) {
        caller.setAffectsSpawning(affects);
    }

    @Override
    public int getViewDistance() {
        return caller.getViewDistance();
    }

    @Override
    public void setViewDistance(int viewDistance) {
        caller.setViewDistance(viewDistance);
    }

    @Override
    public int getSimulationDistance() {
        return caller.getSimulationDistance();
    }

    @Override
    public void setSimulationDistance(int simulationDistance) {
        caller.setSimulationDistance(simulationDistance);
    }

    @Override
    public int getSendViewDistance() {
        return caller.getSendViewDistance();
    }

    @Override
    public void setSendViewDistance(int viewDistance) {
        caller.setSendViewDistance(viewDistance);
    }

    @NotNull
    @Override
    public java.util.Locale locale() {
        return caller.locale();
    }

    @Override
    public void openVirtualSign(@NotNull io.papermc.paper.math.Position position, @NotNull Side side) {
        caller.openVirtualSign(position, side);
    }

    @Override
    public void sendSignChange(@NotNull Location location, @NotNull java.util.List<? extends net.kyori.adventure.text.Component> lines, @NotNull DyeColor dyeColor, boolean hasGlowingText) throws IllegalArgumentException {
        caller.sendSignChange(location, lines, dyeColor, hasGlowingText);
    }

    @Override
    public void sendBlockUpdate(@NotNull Location location, @NotNull org.bukkit.block.TileState tileState) throws IllegalArgumentException {
        caller.sendBlockUpdate(location, tileState);
    }

    @Override
    public void sendPotionEffectChange(@NotNull LivingEntity entity, @NotNull PotionEffect effect) {
        caller.sendPotionEffectChange(entity, effect);
    }

    @Override
    public void sendPotionEffectChangeRemove(@NotNull LivingEntity entity, @NotNull PotionEffectType type) {
        caller.sendPotionEffectChangeRemove(entity, type);
    }

    @Override
    public void sendMultiBlockChange(@NotNull java.util.Map<? extends io.papermc.paper.math.Position, BlockData> blockChanges) {
        caller.sendMultiBlockChange(blockChanges);
    }

    @Override
    public void sendBlockChanges(@NotNull Collection<BlockState> blocks) {
        caller.sendBlockChanges(blocks);
    }

    @Override
    public void playSound(@NotNull Location location, @NotNull Sound sound, @NotNull SoundCategory category, float volume, float pitch, long seed) {
        caller.playSound(location, sound, category, volume, pitch, seed);
    }

    @Override
    public void playSound(@NotNull Location location, @NotNull String sound, @NotNull SoundCategory category, float volume, float pitch, long seed) {
        caller.playSound(location, sound, category, volume, pitch, seed);
    }

    @Override
    public void playSound(@NotNull Entity entity, @NotNull Sound sound, @NotNull SoundCategory category, float volume, float pitch, long seed) {
        caller.playSound(entity, sound, category, volume, pitch, seed);
    }

    @Override
    public void playSound(@NotNull Entity entity, @NotNull String sound, @NotNull SoundCategory category, float volume, float pitch, long seed) {
        caller.playSound(entity, sound, category, volume, pitch, seed);
    }

    // HumanEntity methods added for newer API
    @Override
    public void setHurtDirection(float direction) {
        caller.setHurtDirection(direction);
    }

    @Override
    public void knockback(double strength, double directionX, double directionZ) {

    }

    @Override
    public void broadcastSlotBreak(@NotNull EquipmentSlot slot) {

    }

    @Override
    public void broadcastSlotBreak(@NotNull EquipmentSlot slot, @NotNull Collection<Player> players) {

    }

    @Override
    public @NotNull ItemStack damageItemStack(@NotNull ItemStack stack, int amount) {
        return null;
    }

    @Override
    public void damageItemStack(@NotNull EquipmentSlot slot, int amount) {

    }

    @Override
    public float getBodyYaw() {
        return 0;
    }

    @Override
    public void setBodyYaw(float bodyYaw) {

    }

    @Override
    public boolean canUseEquipmentSlot(@NotNull EquipmentSlot slot) {
        return false;
    }

    @Override
    public @NotNull CombatTracker getCombatTracker() {
        return null;
    }

    @Override
    public void setWaypointStyle(@Nullable Key key) {

    }

    @Override
    public void setWaypointColor(@Nullable Color color) {

    }

    @Override
    public @NotNull Key getWaypointStyle() {
        return null;
    }

    @Override
    public @Nullable Color getWaypointColor() {
        return null;
    }

    @Override
    public boolean isDeeplySleeping() {
        return caller.isDeeplySleeping();
    }

    @Override
    public boolean hasCooldown(@NotNull ItemStack item) {
        return caller.hasCooldown(item);
    }

    @Override
    public int getCooldown(@NotNull ItemStack item) {
        return caller.getCooldown(item);
    }

    @Override
    public void setCooldown(@NotNull ItemStack item, int ticks) {
        caller.setCooldown(item, ticks);
    }

    @Override
    public int getCooldown(@NotNull net.kyori.adventure.key.Key key) {
        return caller.getCooldown(key);
    }

    @Override
    public void setCooldown(@NotNull net.kyori.adventure.key.Key key, int ticks) {
        caller.setCooldown(key, ticks);
    }

    @Nullable
    @Override
    public Location getPotentialRespawnLocation() {
        return caller.getPotentialRespawnLocation();
    }

    @Nullable
    @Override
    public FishHook getFishHook() {
        return caller.getFishHook();
    }

    @Override
    public void startRiptideAttack(int duration, float attackDamage, @NotNull ItemStack item) {
        caller.startRiptideAttack(duration, attackDamage, item);
    }

    @NotNull
    @Override
    public Entity releaseLeftShoulderEntity() {
        return caller.releaseLeftShoulderEntity();
    }

    @NotNull
    @Override
    public Entity releaseRightShoulderEntity() {
        return caller.releaseRightShoulderEntity();
    }

    @Nullable
    @Override
    public Item dropItem(int slot, int amount, boolean throwRandomly, @Nullable java.util.function.Consumer<Item> consumer) {
        return caller.dropItem(slot, amount, throwRandomly, consumer);
    }

    @Nullable
    @Override
    public Item dropItem(@NotNull EquipmentSlot slot, int amount, boolean throwRandomly, @Nullable java.util.function.Consumer<Item> consumer) {
        return caller.dropItem(slot, amount, throwRandomly, consumer);
    }

    @Nullable
    @Override
    public Item dropItem(@NotNull ItemStack itemStack, boolean throwRandomly, @Nullable java.util.function.Consumer<Item> consumer) {
        return caller.dropItem(itemStack, throwRandomly, consumer);
    }

    @Override
    public TriState getFrictionState() {
        return null;
    }

    @Override
    public void setFrictionState(TriState state) {

    }

    @Override
    public <T> @org.jspecify.annotations.Nullable T getData(DataComponentType.Valued<T> type) {
        return null;
    }

    @Override
    public <T> @org.jspecify.annotations.Nullable T getDataOrDefault(DataComponentType.Valued<? extends T> type,
                                                                     @org.jspecify.annotations.Nullable T fallback) {
        return null;
    }

    @Override
    public boolean hasData(DataComponentType type) {
        return false;
    }
}

