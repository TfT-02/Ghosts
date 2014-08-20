package com.me.tft_02.ghosts.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.me.tft_02.ghosts.datatypes.StatsType;
import com.me.tft_02.ghosts.datatypes.player.GhostPlayer;
import com.me.tft_02.ghosts.locale.LocaleLoader;
import com.me.tft_02.ghosts.util.CommandUtils;
import com.me.tft_02.ghosts.util.player.UserManager;

public class StatsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return showStats(sender, args);
    }

    private boolean showStats(CommandSender sender, String[] args) {
        if (CommandUtils.noConsoleUsage(sender)) {
            return true;
        }

        Player player = (Player) sender;

//        if (!Permissions.stats(player)) {
//            return false;
//        }
        GhostPlayer ghostPlayer = UserManager.getPlayer(player);

        for (StatsType statsType : StatsType.values()) {
            player.sendMessage(LocaleLoader.getString("Commands.Stats.DisplayStat", statsType.toString(), ghostPlayer.getStats(statsType)));
        }

        return true;
    }
}
