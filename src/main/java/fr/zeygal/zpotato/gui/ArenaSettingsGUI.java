package fr.zeygal.zpotato.gui;

import fr.zeygal.zpotato.Main;
import fr.zeygal.zpotato.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ArenaSettingsGUI extends GUI {

    private static final int SIZE = 36;
    private static final String TITLE_PREFIX = "§6§lConfiguration: §e";

    private final Main plugin;

    public ArenaSettingsGUI(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public Inventory getInventory(Player player, String... args) {
        if (args.length == 0) {
            return null;
        }

        String arenaName = args[0];
        Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null) {
            return null;
        }

        Inventory inventory = Bukkit.createInventory(null, SIZE, TITLE_PREFIX + arenaName);

        ItemStack minPlayers = GUIUtils.createItem(
                Material.PLAYER_HEAD,
                "§e§lJoueurs minimum",
                "§7Nombre minimum de joueurs: §e" + arena.getMinPlayers(),
                "",
                "§a➜ Clic gauche pour augmenter",
                "§c➜ Clic droit pour diminuer"
        );
        inventory.setItem(10, minPlayers);

        ItemStack maxPlayers = GUIUtils.createItem(
                Material.SKELETON_SKULL,
                "§e§lJoueurs maximum",
                "§7Nombre maximum de joueurs: §e" + arena.getMaxPlayers(),
                "",
                "§a➜ Clic gauche pour augmenter",
                "§c➜ Clic droit pour diminuer"
        );
        inventory.setItem(12, maxPlayers);

        ItemStack potatoTimer = GUIUtils.createItem(
                Material.CLOCK,
                "§e§lDurée de la patate",
                "§7Temps avant explosion: §e" + arena.getPotatoTimer() + "s",
                "",
                "§a➜ Clic gauche pour augmenter (+5s)",
                "§c➜ Clic droit pour diminuer (-5s)"
        );
        inventory.setItem(14, potatoTimer);

        ItemStack setLobby = GUIUtils.createItem(
                arena.getLobby() != null ? Material.LIME_WOOL : Material.RED_WOOL,
                "§e§lPoint de Lobby",
                arena.getLobby() != null ? "§a✓ Lobby défini" : "§c✘ Lobby non défini",
                "",
                "§e➜ Cliquez pour définir à votre position actuelle"
        );
        inventory.setItem(16, setLobby);

        ItemStack setSpectator = GUIUtils.createItem(
                arena.getSpectatorLocation() != null ? Material.LIME_WOOL : Material.RED_WOOL,
                "§e§lZone Spectateur",
                arena.getSpectatorLocation() != null ? "§a✓ Zone spectateur définie" : "§c✘ Zone spectateur non définie",
                "",
                "§e➜ Cliquez pour définir à votre position actuelle"
        );
        inventory.setItem(20, setSpectator);

        ItemStack manageSpawns = GUIUtils.createItem(
                Material.ENDER_PEARL,
                "§e§lPoints de Spawn",
                "§7Nombre de points: §e" + arena.getSpawnLocations().size(),
                "",
                "§e➜ Clic gauche pour gérer les points de spawn",
                "§a➜ Clic droit pour ajouter un point à votre position"
        );
        inventory.setItem(22, manageSpawns);

        ItemStack deleteArena = GUIUtils.createItem(
                Material.BARRIER,
                "§c§lSupprimer l'arène",
                "§7Supprimer définitivement cette arène",
                "",
                "§c➜ Cliquez pour supprimer",
                "§4⚠ Cette action est irréversible!"
        );
        inventory.setItem(24, deleteArena);

        ItemStack backButton = GUIUtils.createBackButton();
        inventory.setItem(31, backButton);

        ItemStack filler = GUIUtils.createSeparator();
        GUIUtils.fillEmptySlots(inventory.getContents(), filler);

        return inventory;
    }

    @Override
    public void handleClick(InventoryClickEvent event, Player player, String... args) {
        if (args.length == 0) {
            player.closeInventory();
            return;
        }

        String arenaName = args[0];
        Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null) {
            player.closeInventory();
            return;
        }

        int slot = event.getRawSlot();

        if (slot == 31) {
            player.closeInventory();
            plugin.getGUIManager().openGUI(player, "arena_list");
            return;
        }

        if (slot == 10) {
            if (event.isLeftClick()) {
                int newMin = arena.getMinPlayers() + 1;
                if (newMin <= arena.getMaxPlayers()) {
                    arena.setMinPlayers(newMin);
                    plugin.getArenaManager().saveArenas();
                    player.sendMessage(plugin.getMessagesManager().getPrefix() + " §aLe nombre minimum de joueurs a été défini à §e" + newMin + "§a.");
                } else {
                    player.sendMessage(plugin.getMessagesManager().getPrefix() + " §cLe nombre minimum ne peut pas dépasser le maximum (§e" + arena.getMaxPlayers() + "§c).");
                }
            } else if (event.isRightClick()) {
                int newMin = arena.getMinPlayers() - 1;
                if (newMin >= 2) {
                    arena.setMinPlayers(newMin);
                    plugin.getArenaManager().saveArenas();
                    player.sendMessage(plugin.getMessagesManager().getPrefix() + " §aLe nombre minimum de joueurs a été défini à §e" + newMin + "§a.");
                } else {
                    player.sendMessage(plugin.getMessagesManager().getPrefix() + " §cLe nombre minimum ne peut pas être inférieur à 2.");
                }
            }
            plugin.getGUIManager().refreshGUI(player, arenaName);
            return;
        }

        if (slot == 12) {
            if (event.isLeftClick()) {
                int newMax = arena.getMaxPlayers() + 1;
                arena.setMaxPlayers(newMax);
                plugin.getArenaManager().saveArenas();
                player.sendMessage(plugin.getMessagesManager().getPrefix() + " §aLe nombre maximum de joueurs a été défini à §e" + newMax + "§a.");
            } else if (event.isRightClick()) {
                int newMax = arena.getMaxPlayers() - 1;
                if (newMax >= arena.getMinPlayers()) {
                    arena.setMaxPlayers(newMax);
                    plugin.getArenaManager().saveArenas();
                    player.sendMessage(plugin.getMessagesManager().getPrefix() + " §aLe nombre maximum de joueurs a été défini à §e" + newMax + "§a.");
                } else {
                    player.sendMessage(plugin.getMessagesManager().getPrefix() + " §cLe nombre maximum ne peut pas être inférieur au minimum (§e" + arena.getMinPlayers() + "§c).");
                }
            }
            plugin.getGUIManager().refreshGUI(player, arenaName);
            return;
        }

        if (slot == 14) {
            if (event.isLeftClick()) {
                int newTimer = arena.getPotatoTimer() + 5;
                arena.setPotatoTimer(newTimer);
                plugin.getArenaManager().saveArenas();
                player.sendMessage(plugin.getMessagesManager().getPrefix() + " §aLa durée de la patate a été définie à §e" + newTimer + "s§a.");
            } else if (event.isRightClick()) {
                int newTimer = arena.getPotatoTimer() - 5;
                if (newTimer >= 5) {
                    arena.setPotatoTimer(newTimer);
                    plugin.getArenaManager().saveArenas();
                    player.sendMessage(plugin.getMessagesManager().getPrefix() + " §aLa durée de la patate a été définie à §e" + newTimer + "s§a.");
                } else {
                    player.sendMessage(plugin.getMessagesManager().getPrefix() + " §cLa durée de la patate ne peut pas être inférieure à 5 secondes.");
                }
            }
            plugin.getGUIManager().refreshGUI(player, arenaName);
            return;
        }

        if (slot == 16) {
            arena.setLobby(player.getLocation());
            plugin.getArenaManager().saveArenas();
            player.sendMessage(plugin.getMessagesManager().getPrefix() + " §aLe lobby de l'arène §e" + arenaName + " §aa été défini à votre position.");
            plugin.getGUIManager().refreshGUI(player, arenaName);
            return;
        }

        if (slot == 20) {
            arena.setSpectatorLocation(player.getLocation());
            plugin.getArenaManager().saveArenas();
            player.sendMessage(plugin.getMessagesManager().getPrefix() + " §aLa zone spectateur de l'arène §e" + arenaName + " §aa été définie à votre position.");
            plugin.getGUIManager().refreshGUI(player, arenaName);
            return;
        }

        if (slot == 22) {
            if (event.isLeftClick()) {
                player.closeInventory();
                plugin.getGUIManager().openGUI(player, "spawn_manager", arenaName);
            } else if (event.isRightClick()) {
                arena.addSpawnLocation(player.getLocation());
                plugin.getArenaManager().saveArenas();
                player.sendMessage(plugin.getMessagesManager().getPrefix() + " §aUn point de spawn a été ajouté à l'arène §e" + arenaName + "§a.");
                plugin.getGUIManager().refreshGUI(player, arenaName);
            }
            return;
        }

        if (slot == 24) {
            player.closeInventory();
            plugin.getArenaManager().deleteArena(arenaName);
            player.sendMessage(plugin.getMessagesManager().getPrefix() + " §aL'arène §e" + arenaName + " §aa été supprimée avec succès.");
            plugin.getGUIManager().openGUI(player, "arena_list");
            return;
        }
    }

    @Override
    public String getTitle() {
        return TITLE_PREFIX;
    }

    @Override
    public int getSize() {
        return SIZE;
    }
}