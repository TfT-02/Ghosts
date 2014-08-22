package com.me.tft_02.ghosts.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.me.tft_02.ghosts.datatypes.StatsType;
import com.me.tft_02.ghosts.datatypes.player.GhostPlayer;
import com.me.tft_02.ghosts.items.ResurrectionScroll.Tier;
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

        String indent = "  ";
        player.sendMessage(LocaleLoader.getString("Commands.Stats.Header"));
        player.sendMessage(indent + LocaleLoader.getString("Commands.Stats.Deaths" , ghostPlayer.getStats(StatsType.DEATHS)));
        player.sendMessage(indent + LocaleLoader.getString("Commands.Stats.TombsFound" , ghostPlayer.getStats(StatsType.FIND_TOMB)));
        player.sendMessage(indent + LocaleLoader.getString("Commands.Stats.GivenUp" , ghostPlayer.getStats(StatsType.GIVEN_UP)));
        player.sendMessage(LocaleLoader.getString("Commands.Stats.ItemsUsed"));
        player.sendMessage(indent + LocaleLoader.getString("Commands.Stats.RessScroll", Tier.ONE.toNumerical(), ghostPlayer.getStats(StatsType.RESURRECTION_SCROLLS_USED_T1), ghostPlayer.getStats(StatsType.RESURRECTION_SCROLLS_USED_OTHERS_T1), ghostPlayer.getStats(StatsType.RESURRECTION_SCROLLS_RECEIVED_T1)));
        player.sendMessage(indent + LocaleLoader.getString("Commands.Stats.RessScroll", Tier.TWO.toNumerical(), ghostPlayer.getStats(StatsType.RESURRECTION_SCROLLS_USED_T2), ghostPlayer.getStats(StatsType.RESURRECTION_SCROLLS_USED_OTHERS_T2), ghostPlayer.getStats(StatsType.RESURRECTION_SCROLLS_RECEIVED_T2)));
        player.sendMessage(indent + LocaleLoader.getString("Commands.Stats.RessScroll", Tier.THREE.toNumerical(), ghostPlayer.getStats(StatsType.RESURRECTION_SCROLLS_USED_T3), ghostPlayer.getStats(StatsType.RESURRECTION_SCROLLS_USED_OTHERS_T3), ghostPlayer.getStats(StatsType.RESURRECTION_SCROLLS_RECEIVED_T3)));

        return true;
    }
}
