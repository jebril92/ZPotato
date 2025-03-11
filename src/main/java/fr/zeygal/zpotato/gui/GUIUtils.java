package fr.zeygal.zpotato.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GUIUtils {

    public static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

            if (lore.length > 0) {
                List<String> loreList = Arrays.stream(lore)
                        .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                        .collect(Collectors.toList());
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
                List<String> loreList = lore.stream()
                        .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                        .collect(Collectors.toList());
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
        return createItem(Material.ARROW, "&c&lBack", "&7Click to return to the previous menu");
    }

    public static ItemStack createCloseButton() {
        return createItem(Material.BARRIER, "&c&lClose", "&7Click to close this menu");
    }

    public static void fillEmptySlots(ItemStack[] items, ItemStack filler) {
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null) {
                items[i] = filler;
            }
        }
    }
}