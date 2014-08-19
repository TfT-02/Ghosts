package com.me.tft_02.ghosts.datatypes;

import java.util.UUID;

import org.bukkit.block.Block;

public class TombBlock {
    private Block block;
    private Block largeBlock;
    private Block sign;
    private long time;
    private UUID ownerUniqueId;
    private String ownerName;
    private int ownerLevel;

    public TombBlock(Block block, Block largeBlock, Block sign, UUID ownerUniqueId, String ownerName, int ownerLevel, long time) {
        this.block = block;
        this.largeBlock = largeBlock;
        this.sign = sign;
        this.ownerUniqueId = ownerUniqueId;
        this.ownerName = ownerName;
        this.ownerLevel = ownerLevel;
        this.time = time;
    }

    public Block getBlock() {
        return block;
    }

    public Block getLargeBlock() {
        return largeBlock;
    }

    public Block getSign() {
        return sign;
    }

    public UUID getOwnerUniqueId() {
        return ownerUniqueId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public int getOwnerLevel() {
        return ownerLevel;
    }

    public long getTime() {
        return time;
    }
}
