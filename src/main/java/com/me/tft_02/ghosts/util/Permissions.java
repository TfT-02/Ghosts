package com.me.tft_02.ghosts.util;

import org.bukkit.permissions.Permissible;

public class Permissions {

    public static boolean ghost(Permissible permissible) {
        return permissible.hasPermission("ghosts.ghost");
    }

    public static boolean doubleJump(Permissible permissible) {
        return permissible.hasPermission("ghosts.doublejump");
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

    public static boolean sign(Permissible permissible) {
        return permissible.hasPermission("ghosts.sign");
    }

    public static boolean freesign(Permissible permissible) {
        return permissible.hasPermission("ghosts.freesign");
    }

    public static boolean ressurect(Permissible permissible) {
        return permissible.hasPermission("ghosts.commands.ressurect");
    }

    public static boolean ressurectOthers(Permissible permissible) {
        return permissible.hasPermission("ghosts.commands.ressurect.others");
    }

    public static boolean ressurectScroll(Permissible permissible) {
        return permissible.hasPermission("ghosts.items.ressurectscroll");
    }

    public static boolean ressurectScrollOthers(Permissible permissible) {
        return permissible.hasPermission("ghosts.items.ressurectscroll.others");
    }
}
