package com.me.tft_02.ghosts.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.managers.player.PlayerManager;
import com.me.tft_02.ghosts.util.CommandUtils;
import com.me.tft_02.ghosts.util.Permissions;

public class ResurrectCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("resurrect")) {
            switch (args.length) {
                case 1:
                    return resurrectOthers(sender, args);
                default:
                    return resurrect(sender);
            }
        }
        return false;
    }

    private boolean resurrect(CommandSender sender) {
        if (CommandUtils.noConsoleUsage(sender)) {
            return true;
        }

        Player player = (Player) sender;

        if (!Permissions.ressurect(player)) {
            return false;
        }

        PlayerManager.resurrect(player);
        return true;
    }

    private boolean resurrectOthers(CommandSender sender, String[] args) {
        if (!Permissions.ressurectOthers(sender)) {
            return false;
        }

        OfflinePlayer offlinePlayer = Ghosts.p.getServer().getOfflinePlayer(args[0]);
        PlayerManager.resurrect(offlinePlayer);
        return true;
    }
}