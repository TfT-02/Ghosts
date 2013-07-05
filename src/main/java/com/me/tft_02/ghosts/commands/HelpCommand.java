package com.me.tft_02.ghosts.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.me.tft_02.ghosts.locale.LocaleLoader;

public class HelpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (args.length) {
            case 2:
                if (Integer.parseInt(args[1]) > 1) {
                    getHelpPage(Integer.parseInt(args[1]), sender);
                    return true;
                }
                else {
                    getHelpPage(1, sender);
                    return true;
                }

            default:
                getHelpPage(1, sender);
                return true;
        }
    }

    private void getHelpPage(int page, CommandSender sender) {
        int maxPages = 2;
        int nextPage = page + 1;

        if (page > maxPages) {
            sender.sendMessage(LocaleLoader.getString(LocaleLoader.getString("HelpCommand.0"), maxPages));
            return;
        }

        sender.sendMessage(LocaleLoader.getString("Help.Page_Header", page, maxPages));
        switch (page) {
            case 0:
                sender.sendMessage(LocaleLoader.getString("Help.Page_0.Line_0"));
                sender.sendMessage(LocaleLoader.getString("Help.Page_0.Line_1"));
                sender.sendMessage(LocaleLoader.getString("Help.Page_0.Line_2"));
                sender.sendMessage(LocaleLoader.getString("Help.Page_0.Line_3"));
                sender.sendMessage(LocaleLoader.getString("Help.Page_0.Line_4"));

            case 1:
                sender.sendMessage(LocaleLoader.getString("Help.Page_1.Line_0"));
                sender.sendMessage(LocaleLoader.getString("Help.Page_1.Line_1"));
                sender.sendMessage(LocaleLoader.getString("Help.Page_1.Line_2"));
                sender.sendMessage(LocaleLoader.getString("Help.Page_1.Line_3"));
                sender.sendMessage(LocaleLoader.getString("Help.Page_1.Line_4"));
            default:
                if (nextPage <= maxPages) {
                    sender.sendMessage(LocaleLoader.getString("Help.Page_Ending", "/ghosts help", nextPage));
                }
        }
    }
}
