package com.me.tft_02.ghosts.items;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.config.Config;
import com.me.tft_02.ghosts.datatypes.RecoveryType;
import com.me.tft_02.ghosts.datatypes.StatsType;
import com.me.tft_02.ghosts.datatypes.player.GhostPlayer;
import com.me.tft_02.ghosts.locale.LocaleLoader;
import com.me.tft_02.ghosts.managers.TombstoneManager;
import com.me.tft_02.ghosts.managers.player.PlayerManager;
import com.me.tft_02.ghosts.util.ItemUtils;
import com.me.tft_02.ghosts.util.Permissions;
import com.me.tft_02.ghosts.util.player.UserManager;

public class ResurrectionScroll {
    private ResurrectionScroll() {}

    public enum Tier {
        THREE(3),
        TWO(2),
        ONE(1);

        int numerical;

        private Tier(int numerical) {
            this.numerical = numerical;
        }

        public int toNumerical() {
            return numerical;
        }

        public static Tier fromNumerical(int numerical) {
            for (Tier tier : Tier.values()) {
                if (tier.toNumerical() == numerical) {
                    return tier;
                }
            }
            return null;
        }

        public static Tier getTier(ItemStack itemStack) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) {
                return null;
            }

            for (String lore : itemMeta.getLore()) {
                if (!lore.contains(ChatColor.GOLD + "TIER: ")) {
                    continue;
                }

                for (Tier tier : Tier.values()) {
                    if (tier.toNumerical() == Integer.parseInt(lore.substring(8))) {
                        return tier;
                    }
                }
            }
            return null;
        }

        protected double getRecoveryVanillaXP() {
            return Config.getInstance().getRecoveryVanillaXP(RecoveryType.RESURRECTION_SCROLL, this);
        }

        protected double getRecoverymcMMOXP() {
            return Config.getInstance().getRecoverymcMMOXP(RecoveryType.RESURRECTION_SCROLL, this);
        }

        protected double getRecoveryItems() {
            return Config.getInstance().getRecoveryItems(RecoveryType.RESURRECTION_SCROLL, this);
        }

        protected boolean getDestroyTomb() {
            return Config.getInstance().getDestroyTomb(RecoveryType.RESURRECTION_SCROLL, this);
        }
    }

    public static void activationCheck(Player player) {
        activationCheck(player, null);
    }

    /**
     * Check for item usage.
     *
     * @param player Player whose item usage to check
     */
    public static boolean activationCheck(Player player, Player target) {
        ItemStack itemInHand = player.getItemInHand();

        if (!ItemUtils.isResurrectionScroll(itemInHand)) {
            return false;
        }

        if (!Permissions.ressurectScroll(player)) {
            player.sendMessage(LocaleLoader.getString("General.NoPermission"));
            return false;
        }

        // Trying to use the scroll on self while not a ghost
        if (!Ghosts.p.getGhostManager().isGhost(player) && target == null) {
            player.sendMessage(LocaleLoader.getString("Item.ResurrectionScroll.Fail.Self"));
            return false;
        }

        // Trying to use on others while a ghost
        if (Ghosts.p.getGhostManager().isGhost(player) && target != null) {
            player.sendMessage(LocaleLoader.getString("Item.ResurrectionScroll.Fail.Others"));
            return false;
        }

        int amount = itemInHand.getAmount();

        if (amount < Config.getInstance().getResurrectionScrollUseCost()) {
            player.sendMessage(LocaleLoader.getString("Skills.NeedMore", LocaleLoader.getString("Item.ResurrectionScroll.Name")));
            return false;
        }

        player.getLocation().getWorld().playSound(player.getLocation(), Sound.PORTAL_TRAVEL, 1F, 1F);
        Tier tier = Tier.getTier(itemInHand);
        player.setItemInHand(new ItemStack(getResurrectionScroll(itemInHand.getAmount() - Config.getInstance().getResurrectionScrollUseCost(), tier.toNumerical())));
        player.updateInventory();
        player.sendMessage(LocaleLoader.getString("Item.ResurrectionScroll.Success"));
        PlayerManager.resurrect(player);

        // Recover saved experience
        PlayerManager.recoverLostXP(player, RecoveryType.RESURRECTION_SCROLL, tier);

        // Destroy tombstone if enabled
        if (Config.getInstance().getDestroyTomb(RecoveryType.RESURRECTION_SCROLL, tier)) {
            TombstoneManager.destroyAllTombstones(player, false, true);
        }

        GhostPlayer ghostPlayer = UserManager.getPlayer(player);
        switch (tier) {
            case THREE:
                ghostPlayer.increaseStats(StatsType.RESS_SCROLL_USED_T3);
                break;
            case TWO:
                ghostPlayer.increaseStats(StatsType.RESS_SCROLL_USED_T2);
                break;
            case ONE:
                ghostPlayer.increaseStats(StatsType.RESS_SCROLL_USED_T1);
                break;
        }

        return true;
    }

    public static ItemStack getResurrectionScroll(int amount, int tier) {
        ItemStack itemStack = new ItemStack(Config.getInstance().getResurrectionScrollItem(), amount);

        itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 10);

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.GOLD + LocaleLoader.getString("Item.ResurrectionScroll.Name"));

        List<String> itemLore = new ArrayList<String>();
        itemLore.add("Ghosts Item");
        itemLore.add(ChatColor.GOLD + "TIER: " + tier);
        itemLore.add(LocaleLoader.getString("Item.ResurrectionScroll.Lore.0"));
        itemLore.add(LocaleLoader.getString("Item.ResurrectionScroll.Lore.1"));
        itemMeta.setLore(itemLore);

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ShapedRecipe getResurrectionScrollRecipe() {
        Material ingredientEdges = Config.getInstance().getResurrectionScrollIngredientEdges();
        Material ingredientMiddle = Config.getInstance().getResurrectionScrollIngredientMiddle();

        ShapedRecipe resurrectionScroll = new ShapedRecipe(getResurrectionScroll(1, 1));
        resurrectionScroll.shape("AAA", "ABA", "AAA");
        resurrectionScroll.setIngredient('A', ingredientEdges);
        resurrectionScroll.setIngredient('B', ingredientMiddle);

        return resurrectionScroll;
    }

    public static ShapelessRecipe getResurrectionScrollUpgradeRecipe() {
        ShapelessRecipe upgradeRecipe = new ShapelessRecipe(getResurrectionScroll(1, 1));
        upgradeRecipe.addIngredient(getResurrectionScroll(1, 1).getData());
        upgradeRecipe.addIngredient(Config.getInstance().getResurrectionScrollIngredientUpgrade());

        return upgradeRecipe;
    }
}
