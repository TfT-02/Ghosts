package com.me.tft_02.ghosts.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.config.Config;
import com.me.tft_02.ghosts.datatypes.RecoveryType;
import com.me.tft_02.ghosts.locale.LocaleLoader;
import com.me.tft_02.ghosts.managers.TombstoneManager;
import com.me.tft_02.ghosts.managers.player.PlayerManager;
import com.me.tft_02.ghosts.util.CommandUtils;
import com.me.tft_02.ghosts.util.Permissions;

public class GiveUpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return giveUp(sender, args);
    }

    private boolean giveUp(CommandSender sender, String[] args) {
        if (CommandUtils.noConsoleUsage(sender)) {
            return true;
        }

        Player player = (Player) sender;

        if (!Permissions.giveUp(player)) {
            return false;
        }

        if (!(args.length >= 1 && args[0].equalsIgnoreCase("confirm"))) {
            sendConfirmPrompt(player);
            return true;
        }

        if (PlayerManager.resurrect(player)) {
            // Destroy tombstone if enabled
            if (Config.getInstance().getDestroyTomb(RecoveryType.GIVE_UP)) {
                TombstoneManager.destroyAllTombstones(player, false, true);
            }

            return true;
        }

        return false;
    }

    private void sendConfirmPrompt(Player player) {
        double recoveryVanillaXP = Config.getInstance().getRecoveryVanillaXP(RecoveryType.GIVE_UP);
        double recoverymcMMOXP = Config.getInstance().getRecoverymcMMOXP(RecoveryType.GIVE_UP);
        boolean destroyTomb = Config.getInstance().getDestroyTomb(RecoveryType.GIVE_UP);
        StringBuilder additionalInfo = new StringBuilder();

        if (Ghosts.p.isMcMMOEnabled()) {
            additionalInfo.append(", ");
            additionalInfo.append(LocaleLoader.getString("Commands.GiveUp.Prompt.mcMMOXP", recoverymcMMOXP));
        }

        if (destroyTomb) {
            additionalInfo.append(" ");
            additionalInfo.append(LocaleLoader.getString("Commands.GiveUp.Prompt.TombDestroyed"));
        }

        player.sendMessage(LocaleLoader.getString("Commands.GiveUp.Prompt.0"));
        player.sendMessage(LocaleLoader.getString("Commands.GiveUp.Prompt.1"));
        player.sendMessage(LocaleLoader.getString("Commands.GiveUp.Prompt.2", recoveryVanillaXP) + additionalInfo.toString());
        player.sendMessage(LocaleLoader.getString("Commands.GiveUp.Prompt.3", "/giveup [confirm]"));
    }
}
