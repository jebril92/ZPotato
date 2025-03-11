package fr.zeygal.zpotato.gui;

import fr.zeygal.zpotato.Main;
import fr.zeygal.zpotato.arena.Arena;
import fr.zeygal.zpotato.arena.ArenaState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArenaListGUI extends GUI {

    private static final int ROWS = 6;
    private static final int SIZE = ROWS * 9;
    private static final String TITLE = "§6§lArena Management";

    private final Main plugin;

    public ArenaListGUI(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public Inventory getInventory(Player player, String... args) {
        Inventory inventory = Bukkit.createInventory(null, SIZE, TITLE);

        Map<String, Arena> arenas = plugin.getArenaManager().getAllArenas();
        List<String> arenaNames = new ArrayList<>(arenas.keySet());

        int slot = 10;
        for (String arenaName : arenaNames) {
            Arena arena = arenas.get(arenaName);

            Material material;
            String statusText;
            List<String> lore = new ArrayList<>();

            switch (arena.getState()) {
                case RUNNING:
                    material = Material.RED_CONCRETE;
                    statusText = "§c§lRUNNING";
                    break;
                case STARTING:
                    material = Material.YELLOW_CONCRETE;
                    statusText = "§e§lSTARTING";
                    break;
                case WAITING:
                    if (arena.isValid()) {
                        material = Material.GREEN_CONCRETE;
                        statusText = "§a§lAVAILABLE";
                    } else {
                        material = Material.GRAY_CONCRETE;
                        statusText = "§7§lINCOMPLETE";
                    }
                    break;
                default:
                    material = Material.GRAY_CONCRETE;
                    statusText = "§7§lINCOMPLETE";
            }

            lore.add("§7Status: " + statusText);
            lore.add("§7Players: §e" + arena.getPlayerCount() + "/" + arena.getMaxPlayers());
            lore.add("§7Min. players: §e" + arena.getMinPlayers());
            lore.add("§7Potato duration: §e" + arena.getPotatoTimer() + "s");
            lore.add("");

            lore.add(arena.getLobby() == null ? "§c✘ Lobby not defined" : "§a✓ Lobby defined");
            lore.add(arena.getSpectatorLocation() == null ? "§c✘ Spectator area not defined" : "§a✓ Spectator area defined");
            lore.add(arena.getSpawnLocations().isEmpty() ? "§c✘ No spawn points" : "§a✓ Spawn points: §e" + arena.getSpawnLocations().size());

            lore.add("");
            lore.add("§e➜ Left click to manage this arena");

            if (arena.getState() == ArenaState.WAITING) {
                lore.add("§a➜ Right click to start the game");
            } else if (arena.getState() == ArenaState.RUNNING || arena.getState() == ArenaState.STARTING) {
                lore.add("§c➜ Right click to stop the game");
            }

            ItemStack arenaItem = GUIUtils.createItem(material, "§6§l" + arenaName, lore);
            inventory.setItem(slot, arenaItem);

            slot++;
            if (slot % 9 == 8) {
                slot += 2;
            }

            if (slot >= 44) {
                break;
            }
        }

        ItemStack createButton = GUIUtils.createItem(
                Material.EMERALD_BLOCK,
                "§a§lCreate a new arena",
                "§7Click to create a new arena"
        );
        inventory.setItem(49, createButton);

        inventory.setItem(53, GUIUtils.createCloseButton());

        ItemStack filler = GUIUtils.createSeparator();
        GUIUtils.fillEmptySlots(inventory.getContents(), filler);

        return inventory;
    }

    @Override
    public void handleClick(InventoryClickEvent event, Player player, String... args) {
        int slot = event.getRawSlot();

        if (slot == 53) {
            player.closeInventory();
            return;
        }

        if (slot == 49) {
            player.closeInventory();
            player.sendMessage(plugin.getMessagesManager().getPrefix() + " §aUse §e/hp create <name> [min] [max] §ato create an arena.");
            return;
        }

        if (slot >= 10 && slot < 44) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
                ItemMeta meta = clickedItem.getItemMeta();
                String arenaName = meta.getDisplayName().substring(4); // Remove "§6§l" prefix
                Arena arena = plugin.getArenaManager().getArena(arenaName);

                if (arena != null) {
                    if (event.isLeftClick()) {
                        player.closeInventory();
                        plugin.getGUIManager().openGUI(player, "arena_settings", arenaName);
                    }
                    else if (event.isRightClick()) {
                        handleArenaStateToggle(player, arena);
                    }
                }
            }
        }
    }

    private void handleArenaStateToggle(Player player, Arena arena) {
        String arenaName = arena.getName();

        if (arena.getState() == ArenaState.WAITING) {
            if (arena.canStart()) {
                plugin.getGameManager().startGame(arenaName);
                player.sendMessage(plugin.getMessagesManager().getPrefix() + " §aStarting game in arena §e" + arenaName + "§a.");
            } else {
                player.sendMessage(plugin.getMessagesManager().getPrefix() + " §cArena §e" + arenaName + " §ccannot be started. Check the configuration.");
            }
        } else if (arena.getState() == ArenaState.RUNNING || arena.getState() == ArenaState.STARTING) {
            plugin.getGameManager().stopGame(arenaName);
            player.sendMessage(plugin.getMessagesManager().getPrefix() + " §aStopping game in arena §e" + arenaName + "§a.");
        }

        plugin.getGUIManager().refreshGUI(player);
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public int getSize() {
        return SIZE;
    }
}