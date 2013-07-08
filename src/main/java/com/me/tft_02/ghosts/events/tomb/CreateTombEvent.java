package com.me.tft_02.ghosts.events.tomb;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class CreateTombEvent extends PlayerEvent implements Cancellable {
    private Block block;
    private boolean cancelled;

    public CreateTombEvent(Player player, Block block) {
        super(player);

        this.setBlock(block);
        this.cancelled = false;
    }

    /**
     * @return The itemStack being soulbound
     */
    public Block getBlock() {
        return block;
    }

    /**
     * @return Set the itemStack being soulbound
     */
    public void setBlock(Block block) {
        this.block = block;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}