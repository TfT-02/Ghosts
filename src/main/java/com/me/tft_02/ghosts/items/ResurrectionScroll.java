package com.me.tft_02.ghosts.items;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import com.me.tft_02.ghosts.Ghosts;
import com.me.tft_02.ghosts.config.Config;
import com.me.tft_02.ghosts.locale.LocaleLoader;
import com.me.tft_02.ghosts.util.ItemUtils;
import com.me.tft_02.ghosts.util.Permissions;

public class ResurrectionScroll {
    private static Location location;

    private ResurrectionScroll() {}

    public static void activationCheck(Player player) {
        activationCheck(player, null);
    }

    /**
     * Check for item usage.
     *
     * @param player Player whose item usage to check
     */
    public static boolean activationCheck(Player player, Player target) {
        ItemStack inHand = player.getItemInHand();

        if (!ItemUtils.isResurrectionScroll(inHand)) {
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

        int amount = inHand.getAmount();

        if (amount < Config.getInstance().getResurrectionScrollUseCost()) {
            player.sendMessage(LocaleLoader.getString("Skills.NeedMore", LocaleLoader.getString("Item.ResurrectionScroll.Name")));
            return false;
        }

        player.getLocation().getWorld().playSound(player.getLocation(), Sound.PORTAL_TRAVEL, 1F, 1F);
        player.setItemInHand(new ItemStack(getResurrectionScroll(player.getItemInHand().getAmount() - Config.getInstance().getResurrectionScrollUseCost())));
        player.updateInventory();
        player.sendMessage(LocaleLoader.getString("Item.ResurrectionScroll.Success"));
        return true;
    }

    public static ItemStack getResurrectionScroll(int amount) {
        ItemStack itemStack = new ItemStack(Config.getInstance().getResurrectionScrollItem(), amount);

        itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 10);

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.GOLD + LocaleLoader.getString("Item.ResurrectionScroll.Name"));

        List<String> itemLore = new ArrayList<String>();
        itemLore.add("Ghosts Item");
        itemLore.add(LocaleLoader.getString("Item.ResurrectionScroll.Lore.0"));
        itemLore.add(LocaleLoader.getString("Item.ResurrectionScroll.Lore.1"));
        itemMeta.setLore(itemLore);

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ShapedRecipe getResurrectionScrollRecipe() {
        Material ingredientEdges = Config.getInstance().getResurrectionScrollIngredientEdges();
        Material ingredientMiddle = Config.getInstance().getResurrectionScrollIngredientMiddle();

        ShapedRecipe resurrectionScroll = new ShapedRecipe(getResurrectionScroll(1));
        resurrectionScroll.shape("AAA", "ABA", "AAA");
        resurrectionScroll.setIngredient('A', ingredientEdges);
        resurrectionScroll.setIngredient('B', ingredientMiddle);

        return resurrectionScroll;
    }
}
