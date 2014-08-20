package com.me.tft_02.ghosts.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.datatypes.RecoveryType;

public class StringUtils {
    /**
     * Gets a capitalized version of the target string.
     *
     * @param target String to capitalize
     * @return the capitalized string
     */
    public static String getCapitalized(String target) {
        return target.substring(0, 1).toUpperCase() + target.substring(1).toLowerCase();
    }

    public static String getPrettyRecoveryTypeString(RecoveryType recoveryType) {
        return createPrettyEnumString(recoveryType.toString());
    }

    private static String createPrettyEnumString(String baseString) {
        String[] substrings = baseString.split("_");
        String prettyString = "";
        int size = 1;

        for (String string : substrings) {
            prettyString = prettyString.concat(getCapitalized(string));

            if (size < substrings.length) {
                prettyString = prettyString.concat(" ");
            }

            size++;
        }

        return prettyString;
    }

    /**
     * Determine if a string represents an Integer
     *
     * @param string String to check
     * @return true if the string is an Integer, false otherwise
     */
    public static boolean isInt(String string) {
        try {
            Integer.parseInt(string);
            return true;
        }
        catch (NumberFormatException nFE) {
            return false;
        }
    }

    public static String sterilizeBlock(Block block) {
        return (block != null) ? sterilizeLocation(block.getLocation()) : "null";
    }

    public static Block deSterilizeBlock(String block) {
        Location location = deSterilizeLocation(block);
        return (location != null) ? location.getWorld().getBlockAt(location) : null;
    }

    public static String sterilizeLocation(Location location) {
        return (location != null) ? location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() : "null";
    }

    public static Location deSterilizeLocation(String location) {
        if (location.equalsIgnoreCase("null")) {
            return null;
        }

        String[] split = location.split(",");
        //world,x,y,z
        World world = Ghosts.p.getServer().getWorld(split[0]);
        if (world == null) {
            return null;
        }

        return new Location(world, Double.valueOf(split[1]), Double.valueOf(split[2]), Double.valueOf(split[3]));
    }
}
