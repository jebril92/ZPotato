package fr.zeygal.zpotato.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GUIUtils {

    public static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

            if (lore.length > 0) {
                List<String> loreList = new ArrayList<>();
                for (String line : lore) {
                    loreList.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(loreList);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    public static ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

            if (!lore.isEmpty()) {
                List<String> loreList = new ArrayList<>();
                for (String line : lore) {
                    loreList.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(loreList);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    public static ItemStack createGlowingItem(Material material, String name, String... lore) {
        ItemStack item = createItem(material, name, lore);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }

        return item;
    }

    public static ItemStack createSeparator() {
        return createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
    }

    public static ItemStack createBackButton() {
        return createItem(Material.ARROW, "&c&lRetour", "&7Cliquez pour revenir au menu précédent");
    }

    public static ItemStack createCloseButton() {
        return createItem(Material.BARRIER, "&c&lFermer", "&7Cliquez pour fermer ce menu");
    }

    public static void fillEmptySlots(ItemStack[] items, ItemStack filler) {
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null) {
                items[i] = filler;
            }
        }
    }
}