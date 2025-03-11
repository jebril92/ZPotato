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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArenaListGUI extends GUI {

    private static final int ROWS = 6;
    private static final int SIZE = ROWS * 9;
    private static final String TITLE = "§6§lGestion des Arènes";

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

            if (arena.getState() == ArenaState.RUNNING) {
                material = Material.RED_CONCRETE;
                statusText = "§c§lEN COURS";
            } else if (arena.getState() == ArenaState.STARTING) {
                material = Material.YELLOW_CONCRETE;
                statusText = "§e§lDÉMARRAGE";
            } else if (!arena.isValid()) {
                material = Material.GRAY_CONCRETE;
                statusText = "§7§lINCOMPLÈTE";
            } else {
                material = Material.GREEN_CONCRETE;
                statusText = "§a§lDISPONIBLE";
            }

            lore.add("§7État: " + statusText);
            lore.add("§7Joueurs: §e" + arena.getPlayerCount() + "/" + arena.getMaxPlayers());
            lore.add("§7Min. joueurs: §e" + arena.getMinPlayers());
            lore.add("§7Durée patate: §e" + arena.getPotatoTimer() + "s");
            lore.add("");

            if (arena.getLobby() == null) {
                lore.add("§c✘ Lobby non défini");
            } else {
                lore.add("§a✓ Lobby défini");
            }

            if (arena.getSpectatorLocation() == null) {
                lore.add("§c✘ Zone spectateur non définie");
            } else {
                lore.add("§a✓ Zone spectateur définie");
            }

            if (arena.getSpawnLocations().isEmpty()) {
                lore.add("§c✘ Aucun point de spawn");
            } else {
                lore.add("§a✓ Points de spawn: §e" + arena.getSpawnLocations().size());
            }

            lore.add("");
            lore.add("§e➜ Clic gauche pour gérer cette arène");

            if (arena.getState() == ArenaState.WAITING) {
                lore.add("§a➜ Clic droit pour démarrer la partie");
            } else if (arena.getState() == ArenaState.RUNNING || arena.getState() == ArenaState.STARTING) {
                lore.add("§c➜ Clic droit pour arrêter la partie");
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
                "§a§lCréer une nouvelle arène",
                "§7Cliquez pour créer une nouvelle arène"
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
            player.sendMessage(plugin.getMessagesManager().getPrefix() + " §aUtilisez §e/hp create <nom> [min] [max] §apour créer une arène.");
            return;
        }

        if (slot >= 10 && slot < 44) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
                String arenaName = clickedItem.getItemMeta().getDisplayName().substring(4);
                Arena arena = plugin.getArenaManager().getArena(arenaName);

                if (arena != null) {
                    if (event.isLeftClick()) {
                        player.closeInventory();
                        plugin.getGUIManager().openGUI(player, "arena_settings", arenaName);
                    }
                    else if (event.isRightClick()) {
                        if (arena.getState() == ArenaState.WAITING) {
                            if (arena.canStart()) {
                                plugin.getGameManager().startGame(arenaName);
                                player.sendMessage(plugin.getMessagesManager().getPrefix() + " §aLancement de la partie dans l'arène §e" + arenaName + "§a.");
                            } else {
                                player.sendMessage(plugin.getMessagesManager().getPrefix() + " §cL'arène §e" + arenaName + " §cne peut pas être démarrée. Vérifiez la configuration.");
                            }
                        } else if (arena.getState() == ArenaState.RUNNING || arena.getState() == ArenaState.STARTING) {
                            plugin.getGameManager().stopGame(arenaName);
                            player.sendMessage(plugin.getMessagesManager().getPrefix() + " §aArrêt de la partie dans l'arène §e" + arenaName + "§a.");
                        }

                        plugin.getGUIManager().refreshGUI(player);
                    }
                }
            }
        }
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