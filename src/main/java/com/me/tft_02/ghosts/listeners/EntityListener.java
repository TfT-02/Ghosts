package com.me.tft_02.ghosts.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.database.DatabaseManager;
import com.me.tft_02.ghosts.managers.TombstoneManager;
import com.me.tft_02.ghosts.util.Permissions;

public class EntityListener implements Listener {

    public EntityListener() {}

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof LivingEntity)) {
            return;
        }

        LivingEntity livingEntity = (LivingEntity) entity;

        if (livingEntity instanceof Player) {
            Player player = (Player) entity;

            if (Ghosts.p.ghostManager.isGhost(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getDamager();

        if (entity instanceof Player) {
            Player player = (Player) entity;

            if (Ghosts.p.ghostManager.isGhost(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent event) {
        LivingEntity livingEntity = event.getTarget();

        if (livingEntity instanceof Player) {
            Player player = (Player) livingEntity;

            if (Ghosts.p.ghostManager.isGhost(player)) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Handle PotionSplashEvent events that involve modifying the event.
     *
     * @param event The event to modify
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        LivingEntity shooter = event.getPotion().getShooter();

        if (!(shooter instanceof Player)) {
            return;
        }

        Player player = (Player) shooter;

        for (LivingEntity entity : event.getAffectedEntities()) {
            if (entity instanceof Player) {
                Player target = (Player) entity;

                if (player != target) {
                    if (Ghosts.p.ghostManager.isGhost(player)) {
                        event.setIntensity(target, 0);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (!Permissions.ghost(player)) {
            return;
        }

        if (event.getDrops().size() == 0) {
            return;
        }

        //        for (String world : Ghosts.p.disableInWorlds) {
        //            String curWorld = player.getWorld().getName();
        //            if (world.equalsIgnoreCase(curWorld)) {
        //                player.sendMessage("Ghosts disabled in " + curWorld + ". Inv dropped.");
        //                return;
        //            }
        //        }

        if (!TombstoneManager.createTombstone(event)) {
            return;
        }

        // GHOST MANAGER
        if (!Ghosts.p.ghostManager.isGhost(player)) {
            Ghosts.p.ghostManager.setGhost(player, true);
            DatabaseManager.playerRespawns.put(player.getName(), true);
            DatabaseManager.playerLastDeathLocation.put(player.getName(), player.getLocation());
        }
    }
}
