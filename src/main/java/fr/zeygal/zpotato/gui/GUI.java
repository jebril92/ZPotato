package fr.zeygal.zpotato.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public abstract class GUI {

    public abstract Inventory getInventory(Player player, String... args);

    public abstract void handleClick(InventoryClickEvent event, Player player, String... args);

    public abstract String getTitle();

    public abstract int getSize();

    public void onClose(Player player) {
    }
}