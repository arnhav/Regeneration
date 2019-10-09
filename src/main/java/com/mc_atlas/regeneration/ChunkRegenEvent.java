package com.mc_atlas.regeneration;

import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ChunkRegenEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();	
	public HandlerList getHandlers() {return handlers;}
	public static HandlerList getHandlerList() {return handlers;}
	
	public ChunkRegenEvent(World w, int chunkx, int chunkz) {
		this.world = w;
		this.chunkX = chunkx;
		this.chunkz = chunkz;
	}
	
	private boolean cancelled = false;
	private int chunkX, chunkz;
	private World world;
	
	public int getChunkX() { return chunkX;}
	public int getChunkZ() { return chunkz;}
	public World getWorld() { return world;}
	
	@Override
	public boolean isCancelled() {
		// TODO Auto-generated method stub
		return cancelled;
	}
	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
		
	}
}
