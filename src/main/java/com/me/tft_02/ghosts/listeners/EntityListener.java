package com.me.tft_02.ghosts.listeners;

import java.util.List;

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
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.config.Config;
import com.me.tft_02.ghosts.database.DatabaseManager;
import com.me.tft_02.ghosts.managers.TombstoneManager;
import com.me.tft_02.ghosts.managers.player.GhostManager;
import com.me.tft_02.ghosts.managers.player.PlayerManager;
import com.me.tft_02.ghosts.util.ItemUtils;
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

            if (Ghosts.p.getGhostManager().isGhost(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getDamager();

        if (entity instanceof Player) {
            Player player = (Player) entity;

            if (Ghosts.p.getGhostManager().isGhost(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent event) {
        LivingEntity livingEntity = event.getTarget();

        if (livingEntity instanceof Player) {
            Player player = (Player) livingEntity;

            if (Ghosts.p.getGhostManager().isGhost(player)) {
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
        ProjectileSource shooter = event.getPotion().getShooter();

        if (!(shooter instanceof Player)) {
            return;
        }

        Player player = (Player) shooter;

        for (LivingEntity entity : event.getAffectedEntities()) {
            if (!(entity instanceof Player) || player == entity) {
                continue;
            }

            if (Ghosts.p.getGhostManager().isGhost(player)) {
                event.setIntensity(entity, 0);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (!Permissions.ghost(player)) {
            return;
        }

        if (GhostManager.isDisabledInWorld(player.getWorld())) {
            return;
        }

        List<ItemStack> drops = event.getDrops();
        drops = ItemUtils.saveGhostItems(player, drops);

        if (drops.size() == 0) {
            return;
        }

        if (!TombstoneManager.createTombstone(event.getEntity(), drops)) {
            return;
        }

        // Tombstone succesfully created, clear drops
        event.getDrops().clear();

        // Handle vanilla xp drops
        if (Config.getInstance().getLossesOverrideKeepLevel()) {
            event.setKeepLevel(false);
            event.setDroppedExp(0);
        }

        PlayerManager.loseAndSaveXP(player);

        // GHOST MANAGER
        if (!Ghosts.p.getGhostManager().isGhost(player)) {
            Ghosts.p.getGhostManager().setGhost(player, true);
            DatabaseManager.playerRespawns.put(player.getUniqueId(), true);
            DatabaseManager.setLastDeathLocation(player);
        }
    }
}
