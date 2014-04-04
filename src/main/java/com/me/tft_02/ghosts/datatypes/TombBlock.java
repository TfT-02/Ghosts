package com.me.tft_02.ghosts.datatypes;

import org.bukkit.block.Block;

public class TombBlock {
    private Block block;
    private Block largeBlock;
    private Block sign;
    private long time;
    private String owner;
    private int ownerLevel;

    public TombBlock(Block block, Block largeBlock, Block sign, String owner, int ownerLevel, long time) {
        this.block = block;
        this.largeBlock = largeBlock;
        this.sign = sign;
        this.owner = owner;
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

    public String getOwner() {
        return owner;
    }

    public int getOwnerLevel() {
        return ownerLevel;
    }

    public long getTime() {
        return time;
    }
}
