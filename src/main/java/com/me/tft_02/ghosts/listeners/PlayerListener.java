package com.me.tft_02.ghosts.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.config.Config;
import com.me.tft_02.ghosts.database.DatabaseManager;
import com.me.tft_02.ghosts.datatypes.TombBlock;
import com.me.tft_02.ghosts.locale.LocaleLoader;
import com.me.tft_02.ghosts.managers.player.PlayerManager;
import com.me.tft_02.ghosts.runnables.ExplosionTrailTask;
import com.me.tft_02.ghosts.runnables.GroundExplosionTask;
import com.me.tft_02.ghosts.util.Misc;
import com.me.tft_02.ghosts.util.Permissions;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block.getType() != Material.CHEST) {
            return;
        }

        // We'll do quickloot on rightclick of chest if we're going to destroy it anyways
        //        if (!Ghosts.p.destroyQuickLoot || !Ghosts.p.noDestroy) {
        //            System.out.println("L88");
        //            return;
        //        }

        Player player = event.getPlayer();

        if (!Permissions.quickLoot(player)) {
            Ghosts.p.debug(player.getName() + " has no permission to quickloot");
            return;
        }

        TombBlock tombBlock = DatabaseManager.tombBlockList.get(block.getLocation());
        if (tombBlock == null || !(tombBlock.getBlock().getState() instanceof Chest)) {
            return;
        }

        if (!tombBlock.getOwner().equals(player.getName())) {
            Ghosts.p.debug(player.getName() + " is not the owner!");
            return;
        }

        // I think quickloot is succes here
        Ghosts.p.ghostManager.setGhost(player, false);
        PlayerManager.quickLoot(event, player, tombBlock);
    }

    /**
     * Monitor PlayerInteractEntityEvent events.
     * 
     * @param event The event to watch
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if (entity instanceof Player) {
            Player target = (Player) entity;

            if (Ghosts.p.ghostManager.isGhost(player) || Ghosts.p.ghostManager.isGhost(target)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Ghosts.p.ghostManager.addPlayer(player);

        if (Ghosts.p.ghostManager.isGhost(player)) {
            Ghosts.p.ghostManager.setGhost(player, false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();

        if (Ghosts.p.ghostManager.isGhost(player)) {
            item.setPickupDelay(60);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (!Ghosts.p.ghostManager.isGhost(player)) {
            Ghosts.p.ghostManager.setGhost(player, false);
            return;
        }

        if (!DatabaseManager.playerRespawns.containsKey(player.getName())) {
            return;
        }

        Location location = DatabaseManager.getLastDeathLocation(player);
        if (location == null) {
            return;
        }

        Location respawnLocation = Misc.getRandomLocation(location, Config.getInstance().getMinimumRange(), Config.getInstance().getMaximumRange());
        Ghosts.p.debug(player.getName() + " died at " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ());
        Ghosts.p.debug(player.getName() + " will respawn at " + respawnLocation.getBlockX() + " " + respawnLocation.getBlockY() + " " + respawnLocation.getBlockZ());

        player.sendMessage(LocaleLoader.getString("Ghost.Respawn"));
        event.setRespawnLocation(respawnLocation);
        DatabaseManager.playerRespawns.put(player.getName(), false);

        if (Config.getInstance().getSetOnFire()) {
            player.setFireTicks(12 * 20);
        }

        if (Config.getInstance().getThunder()) {
            player.getWorld().playSound(new Location(player.getWorld(), respawnLocation.getX(), 100, respawnLocation.getZ()), Sound.AMBIENCE_THUNDER, 1F, 1F);
        }

        if (Config.getInstance().getExplosionTrail()) {
            new ExplosionTrailTask(player, 20).runTaskTimer(Ghosts.p, 20, 5);
        }

        if (Config.getInstance().getExplosionImpact()) {
            new GroundExplosionTask(player).runTaskTimer(Ghosts.p, 2 * 20, 3);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        player.playSound(player.getLocation(), Sound.AMBIENCE_CAVE, 1F, 1F);
    }
}
