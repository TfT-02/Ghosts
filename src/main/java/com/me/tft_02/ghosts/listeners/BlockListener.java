package com.me.tft_02.ghosts.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.config.Config;
import com.me.tft_02.ghosts.database.DatabaseManager;
import com.me.tft_02.ghosts.datatypes.TombBlock;
import com.me.tft_02.ghosts.util.Permissions;
import com.me.tft_02.ghosts.util.TombstoneManager;

public class BlockListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block b = event.getBlock();
        Player player = event.getPlayer();

        if (Ghosts.p.ghostManager.isGhost(player)) {
            event.setCancelled(true);
            return;
        }

        if (b.getType() == Material.WALL_SIGN) {
            org.bukkit.material.Sign signData = (org.bukkit.material.Sign) b.getState().getData();
            TombBlock tBlock = DatabaseManager.tombBlockList.get(b.getRelative(signData.getAttachedFace()).getLocation());
            if (tBlock == null) {
                return;
            }
        }

        if (b.getType() != Material.CHEST && b.getType() != Material.SIGN_POST) {
            return;
        }

        TombBlock tBlock = DatabaseManager.tombBlockList.get(b.getLocation());
        if (tBlock == null) {
            return;
        }

        if (Config.getInstance().getPreventDestroy() && !Permissions.breakTombs(player)) {
            player.sendMessage("You cannot break this tomb..");
            event.setCancelled(true);
            return;
        }

        TombstoneManager.removeTomb(tBlock, true);

        Player owner = Ghosts.p.getServer().getPlayer(tBlock.getOwner());
        if (owner != null) {
            owner.sendMessage("Your tomb has been destroyed by " + player.getName() + "!");
        }
    }
}
