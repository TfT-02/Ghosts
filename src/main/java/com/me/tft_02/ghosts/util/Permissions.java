package com.me.tft_02.ghosts.util;

import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

public class Permissions {

    public static boolean ghost(Permissible permissible) {
        return permissible.hasPermission("ghosts.ghost");
    }

    public static boolean quickLoot(Permissible permissible) {
        return permissible.hasPermission("ghosts.quickloot");
    }

    public static boolean freechest(Permissible permissible) {
        return permissible.hasPermission("ghosts.freechest");
    }

    public static boolean largechest(Permissible permissible) {
        return permissible.hasPermission("ghosts.largechest");
    }

    public static boolean breakTombs(Permissible permissible) {
        return permissible.hasPermission("ghosts.breakTombs");
    }

    public static boolean sign(Player player) {
        // TODO Auto-generated method stub
        return false;
    }

    public static boolean freesign(Player player) {
        // TODO Auto-generated method stub
        return false;
    }
}
