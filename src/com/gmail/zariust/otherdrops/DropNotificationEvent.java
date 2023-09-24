package com.gmail.zariust.otherdrops;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;

public class DropNotificationEvent extends Event {
	private Player player;
	private Block block;
	private BlockBreakEvent event;
	
	public DropNotificationEvent(Player player, Block block, BlockBreakEvent event) {
		this.player = player;
		this.block = block;
		this.event = event;
	}

	public Player getPlayer() {
		return this.player;
	}
	
	public Block getBlock() {
		return this.block;
	}
	
	public BlockBreakEvent getEvent() {
		return this.event;
	}

    private static final HandlerList HANDLERS = new HandlerList();

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
