package com.me.tft_02.ghosts.hooks;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.events.hardcore.McMMOPlayerDeathPenaltyEvent;

import com.me.tft_02.ghosts.datatypes.player.GhostPlayer;
import com.me.tft_02.ghosts.util.player.UserManager;

public class McMMOListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMcMMOPlayerDeathPenalty(McMMOPlayerDeathPenaltyEvent event) {
        Player player = event.getPlayer();
        GhostPlayer ghostPlayer = UserManager.getPlayer(player);

        if (!UserManager.hasPlayerDataKey(player)) {
            return;
        }

        HashMap<String, Integer> experienceLost = new HashMap<String, Integer>();
        HashMap<String, Integer> levelChanged = event.getLevelChanged();

        for (String skillName : levelChanged.keySet()) {
            int level = ExperienceAPI.getLevel(player, skillName);
            if (level <= 0) {
                experienceLost.put(skillName, 0);
                continue;
            }

            int xp = 0;

            for (int i = 0; i < levelChanged.get(skillName); i++) {
                int level1 = level - 1 - i;
                xp += ExperienceAPI.getXpNeededToLevel(level1);
            }

            xp += event.getExperienceChanged().get(skillName);
            experienceLost.put(skillName, xp);
        }

        ghostPlayer.setSavedLostMcMMOXP(experienceLost);
    }
}
