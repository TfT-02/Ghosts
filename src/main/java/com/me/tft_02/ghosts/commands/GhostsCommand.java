package com.me.tft_02.ghosts.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.locale.LocaleLoader;

public class GhostsCommand implements CommandExecutor {
    private CommandExecutor reloadCommand = new ReloadCommand();
    private CommandExecutor helpCommand = new HelpCommand();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (args.length) {
            case 1:
                if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
                    return helpCommand.onCommand(sender, command, label, args);
                }
                else if (args[0].equalsIgnoreCase("reload")) {
                    return reloadCommand.onCommand(sender, command, label, args);
                }
            default:
                return printPluginInfo(sender);
        }
    }

    private boolean printPluginInfo(CommandSender sender) {
        sender.sendMessage(LocaleLoader.getString("General.Plugin_Header", Ghosts.p.getDescription().getName(), Ghosts.p.getDescription().getAuthors()));
        sender.sendMessage(LocaleLoader.getString("General.Running_Version", Ghosts.p.getDescription().getVersion()));
        sender.sendMessage(LocaleLoader.getString("General.Use_Help"));
        return true;
    }
}
