package com.me.tft_02.ghosts.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.me.tft_02.ghosts.config.Config;
import com.me.tft_02.ghosts.database.DatabaseManager;

public class BlockUtils {
    private BlockUtils() {}

    /**
     * Check if a given block is a Tombstone
     *
     * @param location The {@link Location} of the block to check
     * @return true if the block is a tombstone, false otherwise
     */
    public static boolean isTombStone(Location location) {
        return DatabaseManager.tombBlockList.get(location) != null;
    }

    /**
     * Check if a given block should allow for the activation of abilities
     *
     * @param blockState The {@link BlockState} of the block to check
     * @return true if the block should allow ability activation, false otherwise
     */
    public static boolean canActivateAbilities(BlockState blockState) {
        switch (blockState.getType()) {
            case BED_BLOCK:
            case BREWING_STAND:
            case BOOKSHELF:
            case BURNING_FURNACE:
            case CAKE_BLOCK:
            case CHEST:
            case DISPENSER:
            case ENCHANTMENT_TABLE:
            case ENDER_CHEST:
            case FENCE_GATE:
            case FURNACE:
            case IRON_DOOR_BLOCK:
            case JUKEBOX:
            case LEVER:
            case NOTE_BLOCK:
            case STONE_BUTTON:
            case WOOD_BUTTON:
            case TRAP_DOOR:
            case WALL_SIGN:
            case WOODEN_DOOR:
            case WORKBENCH:
            case BEACON:
            case ANVIL:
            case DROPPER:
            case HOPPER:
            case TRAPPED_CHEST:
                return false;

            default:
                return true;
        }
    }

    /**
     * Check if a given block cannot be replaced
     *
     * @param blockState The {@link BlockState} of the block to check
     * @return true if the block cannot be replaced, false otherwise
     */
    public static boolean cannotBeReplaced(BlockState blockState) {
        switch (blockState.getType()) {
            case STEP:
            case TORCH:
            case REDSTONE_WIRE:
            case RAILS:
            case ACTIVATOR_RAIL:
            case DETECTOR_RAIL:
            case STONE_PLATE:
            case WOOD_PLATE:
            case REDSTONE_TORCH_ON:
            case REDSTONE_TORCH_OFF:
            case CAKE_BLOCK:
                return true;

            default:
                return false;
        }
    }

    /**
     * Check if a given block can be replaced
     *
     * @param blockState The {@link BlockState} of the block to check
     * @return true if the block can be replaced, false otherwise
     */
    public static boolean canBeReplaced(BlockState blockState) {
        switch (blockState.getType()) {
            case AIR:
            case SAPLING:
            case WATER:
            case STATIONARY_WATER:
            case LAVA:
            case STATIONARY_LAVA:
            case YELLOW_FLOWER:
            case RED_ROSE:
            case BROWN_MUSHROOM:
            case RED_MUSHROOM:
            case FIRE:
            case CROPS:
            case SNOW:
            case SUGAR_CANE:
            case GRAVEL:
            case SAND:
                return true;

            default:
                return false;
        }
    }

    /**
     * Creates a sign at the specified block
     *
     * @param signBlock The block to put a sign
     * @param player The Player who owns the sign
     */
    public static void createSign(Block signBlock, Player player) {
        String date = new SimpleDateFormat(Config.getInstance().getDateFormat()).format(new Date());
        String time = new SimpleDateFormat(Config.getInstance().getTimeFormat()).format(new Date());
        String name = player.getName();

        signBlock.setType(Material.SIGN_POST);
        final Sign sign = (Sign) signBlock.getState();

        for (int x = 0; x < 4; x++) {
            String line = Config.getInstance().signMessage[x];
            line = line.replace("{name}", name);
            line = line.replace("{date}", date);
            line = line.replace("{time}", time);

            if (line.length() > 15) {
                line = line.substring(0, 15);
            }
            sign.setLine(x, line);
        }
    }

    /**
     * Check the four sides of the base block to see if there's room for a large chest
     */
    public static Block findLarge(Block base) {
        // Check all 4 sides for air.
        Block block;

        block = base.getRelative(BlockFace.NORTH);
        if (canBeReplaced(block.getState()) && (!Config.getInstance().getNoInterfere() || !checkChest(block, Material.CHEST))) {
            return block;
        }

        block = base.getRelative(BlockFace.EAST);
        if (canBeReplaced(block.getState()) && (!Config.getInstance().getNoInterfere() || !checkChest(block, Material.CHEST))) {
            return block;
        }

        block = base.getRelative(BlockFace.SOUTH);
        if (canBeReplaced(block.getState()) && (!Config.getInstance().getNoInterfere() || !checkChest(block, Material.CHEST))) {
            return block;
        }

        block = base.getRelative(BlockFace.WEST);
        if (canBeReplaced(block.getState()) && (!Config.getInstance().getNoInterfere() || !checkChest(block, Material.CHEST))) {
            return block;
        }

        return null;
    }

    /**
     * Check the four sides of the base block if material exists
     *
     * @param base The base block to check
     * @param material The Material to check for
     * @return true if the material is next to base
     */
    public static boolean checkChest(Block base, Material material) {
        if (base.getRelative(BlockFace.NORTH).getType() == material) {
            return true;
        }
        if (base.getRelative(BlockFace.EAST).getType() == material) {
            return true;
        }
        if (base.getRelative(BlockFace.SOUTH).getType() == material) {
            return true;
        }
        if (base.getRelative(BlockFace.WEST).getType() == material) {
            return true;
        }
        return false;
    }

    /**
     * Find a block near the base block to place the tombstone
     * @param base
     * @return
     */
    public static Block findPlace(Block base, boolean cardinalSearch) {
        if (canBeReplaced(base.getState())) {
            return base;
        }

        if (cardinalSearch) {
            Block block;
            block = base.getRelative(BlockFace.NORTH);
            if (canBeReplaced(block.getState())) {
                return block;
            }
            block = base.getRelative(BlockFace.EAST);
            if (canBeReplaced(block.getState())) {
                return block;
            }
            block = base.getRelative(BlockFace.SOUTH);
            if (canBeReplaced(block.getState())) {
                return block;
            }
            block = base.getRelative(BlockFace.WEST);
            if (canBeReplaced(block.getState())) {
                return block;
            }
            block = base.getRelative(BlockFace.SELF);
            if (canBeReplaced(block.getState())) {
                return block;
            }

            return null;
        }
        int baseX = base.getX();
        int baseY = base.getY();
        int baseZ = base.getZ();
        World world = base.getWorld();

        for (int x = baseX - 1; x < baseX + 1; x++) {
            for (int z = baseZ - 1; z < baseZ + 1; z++) {
                Block block = world.getBlockAt(x, baseY, z);
                if (canBeReplaced(block.getState())) {
                    return block;
                }
            }
        }

        return null;
    }
}
