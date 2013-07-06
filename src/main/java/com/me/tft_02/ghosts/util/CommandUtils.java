package com.me.tft_02.ghosts.util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.me.tft_02.ghosts.locale.LocaleLoader;

public class CommandUtils {

    public static boolean noConsoleUsage(CommandSender sender) {
        if (sender instanceof Player) {
            return false;
        }

        sender.sendMessage(LocaleLoader.getString("Commands.NoConsole"));
        return true;
    }
}
