package com.me.tft_02.ghosts.hooks;

import java.util.HashMap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.gmail.nossr50.events.hardcore.McMMOPlayerDeathPenaltyEvent;

public class McMMOListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMcMMOPlayerDeathPenalty(McMMOPlayerDeathPenaltyEvent event) {
        HashMap<String, Integer> levelChanged = event.getLevelChanged();
        HashMap<String, Float> experienceChanged = event.getExperienceChanged();

    }
}
