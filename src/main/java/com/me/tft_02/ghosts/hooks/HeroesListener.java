package com.me.tft_02.ghosts.hooks;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.herocraftonline.dev.heroes.api.ExperienceChangeEvent;

public class HeroesListener  implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExperienceChange(ExperienceChangeEvent event) {
        double expChange = event.getExpChange();

        if (expChange >= 0) {
            return;
        }

        // expChange
    }
}
