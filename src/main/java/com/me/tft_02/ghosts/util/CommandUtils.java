package com.me.tft_02.ghosts.util;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.locale.LocaleLoader;

public class CommandUtils {

    public static boolean noConsoleUsage(CommandSender sender) {
        if (sender instanceof Player) {
            return false;
        }

        sender.sendMessage(LocaleLoader.getString("Commands.NoConsole"));
        return true;
    }

    /**
     * Check if a player exists
     * @param sender To send message if player does not exist
     * @param playerName Name of the player to check
     * @return true if the player exists
     */
    public static boolean checkPlayerExistence(CommandSender sender, String playerName) {
        OfflinePlayer offlinePlayer = Ghosts.p.getServer().getOfflinePlayer(playerName);
        if (offlinePlayer.hasPlayedBefore()) {
            return true;
        }

        sender.sendMessage(LocaleLoader.getString("Commands.NotExist"));
        return false;
    }
}
