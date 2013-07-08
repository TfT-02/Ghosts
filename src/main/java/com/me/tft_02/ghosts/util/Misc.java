package com.me.tft_02.ghosts.util;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;

import com.me.tft_02.ghosts.config.Config;

public class Misc {
    private static Random random = new Random();

    public static final int TIME_CONVERSION_FACTOR = 1000;
    public static final int TICK_CONVERSION_FACTOR = 20;

    /**
     * Gets a random location near the specified location in a range of range
     *
     */
    public static Location getRandomLocation(Location location, int minRange, int maxRange) {
        World world = location.getWorld();
        int blockX = location.getBlockX();
        int blockZ = location.getBlockZ();

        int distance;
        distance = minRange + random.nextInt(maxRange - minRange);
        blockX = (random.nextBoolean()) ? blockX + (distance) : blockX - (distance);

        distance = minRange + random.nextInt(maxRange - minRange);
        blockZ = (random.nextBoolean()) ? blockZ + (distance) : blockZ - (distance);

        int blockY = (Config.getInstance().getRespawnFromSky()) ? world.getMaxHeight() : world.getHighestBlockYAt(blockX, blockZ);

        return new Location(world, blockX, blockY, blockZ);
    }

    /**
     * Gets the Yaw from one location to another in relation to North.
     *
     */
    public static double getYawTo(Location from, Location to) {
        final int distX = to.getBlockX() - from.getBlockX();
        final int distZ = to.getBlockZ() - from.getBlockZ();
        double degrees = Math.toDegrees(Math.atan2(-distX, distZ));
        degrees += 180;
        return degrees;
    }

    /**
     * Converts a rotation to a cardinal direction name.
     * Author: sk89q - Original function from CommandBook plugin
     * @param rot
     * @return
     */
    public static String getDirection(double rot) {
        if (0 <= rot && rot < 22.5) {
            return "North";
        }
        else if (22.5 <= rot && rot < 67.5) {
            return "Northeast";
        }
        else if (67.5 <= rot && rot < 112.5) {
            return "East";
        }
        else if (112.5 <= rot && rot < 157.5) {
            return "Southeast";
        }
        else if (157.5 <= rot && rot < 202.5) {
            return "South";
        }
        else if (202.5 <= rot && rot < 247.5) {
            return "Southwest";
        }
        else if (247.5 <= rot && rot < 292.5) {
            return "West";
        }
        else if (292.5 <= rot && rot < 337.5) {
            return "Northwest";
        }
        else if (337.5 <= rot && rot < 360.0) {
            return "North";
        }
        else {
            return null;
        }
    }

    public static String getPrettyTime(int seconds) {
        int hours = seconds / 3600;
        int remainder = seconds - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        if (mins == 0 && hours == 0) {
            return secs + "s";
        }
        if (hours == 0) {
            return mins + "m " + secs + "s";
        }
        else {
            return hours + "h " + mins + "m " + secs + "s";
        }
    }
}
