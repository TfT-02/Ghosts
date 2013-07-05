package com.me.tft_02.ghosts.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.me.tft_02.ghosts.Ghosts;

public class ReloadCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Ghosts.p.reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "Configuration reloaded.");
        return true;
    }
}
