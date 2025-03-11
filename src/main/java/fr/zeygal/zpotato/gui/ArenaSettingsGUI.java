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
    private static final String TITLE_PREFIX = "§6§lSettings: §e";

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

        // Min Players Setting
        ItemStack minPlayers = GUIUtils.createItem(
                Material.PLAYER_HEAD,
                "§e§lMinimum Players",
                "§7Minimum number of players: §e" + arena.getMinPlayers(),
                "",
                "§a➜ Left click to increase",
                "§c➜ Right click to decrease"
        );
        inventory.setItem(10, minPlayers);

        // Max Players Setting
        ItemStack maxPlayers = GUIUtils.createItem(
                Material.SKELETON_SKULL,
                "§e§lMaximum Players",
                "§7Maximum number of players: §e" + arena.getMaxPlayers(),
                "",
                "§a➜ Left click to increase",
                "§c➜ Right click to decrease"
        );
        inventory.setItem(12, maxPlayers);

        // Potato Timer Setting
        ItemStack potatoTimer = GUIUtils.createItem(
                Material.CLOCK,
                "§e§lPotato Timer",
                "§7Time before explosion: §e" + arena.getPotatoTimer() + "s",
                "",
                "§a➜ Left click to increase (+5s)",
                "§c➜ Right click to decrease (-5s)"
        );
        inventory.setItem(14, potatoTimer);

        // Lobby Point Setting
        ItemStack setLobby = GUIUtils.createItem(
                arena.getLobby() != null ? Material.LIME_WOOL : Material.RED_WOOL,
                "§e§lLobby Point",
                arena.getLobby() != null ? "§a✓ Lobby defined" : "§c✘ Lobby not defined",
                "",
                "§e➜ Click to set to your current position"
        );
        inventory.setItem(16, setLobby);

        // Spectator Area Setting
        ItemStack setSpectator = GUIUtils.createItem(
                arena.getSpectatorLocation() != null ? Material.LIME_WOOL : Material.RED_WOOL,
                "§e§lSpectator Area",
                arena.getSpectatorLocation() != null ? "§a✓ Spectator area defined" : "§c✘ Spectator area not defined",
                "",
                "§e➜ Click to set to your current position"
        );
        inventory.setItem(20, setSpectator);

        // Spawn Points Management
        ItemStack manageSpawns = GUIUtils.createItem(
                Material.ENDER_PEARL,
                "§e§lSpawn Points",
                "§7Number of points: §e" + arena.getSpawnLocations().size(),
                "",
                "§e➜ Left click to manage spawn points",
                "§a➜ Right click to add a point at your position"
        );
        inventory.setItem(22, manageSpawns);

        // Delete Arena Option
        ItemStack deleteArena = GUIUtils.createItem(
                Material.BARRIER,
                "§c§lDelete Arena",
                "§7Permanently delete this arena",
                "",
                "§c➜ Click to delete",
                "§4⚠ This action is irreversible!"
        );
        inventory.setItem(24, deleteArena);

        // Back Button
        ItemStack backButton = GUIUtils.createBackButton();
        inventory.setItem(31, backButton);

        // Fill empty slots with separator
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

        switch (slot) {
            case 10: // Min Players
                handleMinPlayersClick(event, player, arena, arenaName);
                break;
            case 12: // Max Players
                handleMaxPlayersClick(event, player, arena, arenaName);
                break;
            case 14: // Potato Timer
                handlePotatoTimerClick(event, player, arena, arenaName);
                break;
            case 16: // Set Lobby
                arena.setLobby(player.getLocation());
                plugin.getArenaManager().saveArenas();
                player.sendMessage(plugin.getMessagesManager().getPrefix() + " §aThe lobby of arena §e" + arenaName + " §ahas been set to your position.");
                plugin.getGUIManager().refreshGUI(player, arenaName);
                break;
            case 20: // Set Spectator
                arena.setSpectatorLocation(player.getLocation());
                plugin.getArenaManager().saveArenas();
                player.sendMessage(plugin.getMessagesManager().getPrefix() + " §aThe spectator area of arena §e" + arenaName + " §ahas been set to your position.");
                plugin.getGUIManager().refreshGUI(player, arenaName);
                break;
            case 22: // Manage Spawns
                handleSpawnManagementClick(event, player, arena, arenaName);
                break;
            case 24: // Delete Arena
                player.closeInventory();
                plugin.getArenaManager().deleteArena(arenaName);
                player.sendMessage(plugin.getMessagesManager().getPrefix() + " §aArena §e" + arenaName + " §ahas been successfully deleted.");
                plugin.getGUIManager().openGUI(player, "arena_list");
                break;
            case 31: // Back Button
                player.closeInventory();
                plugin.getGUIManager().openGUI(player, "arena_list");
                break;
        }
    }

    private void handleMinPlayersClick(InventoryClickEvent event, Player player, Arena arena, String arenaName) {
        if (event.isLeftClick()) {
            int newMin = arena.getMinPlayers() + 1;
            if (newMin <= arena.getMaxPlayers()) {
                arena.setMinPlayers(newMin);
                plugin.getArenaManager().saveArenas();
                player.sendMessage(plugin.getMessagesManager().getPrefix() + " §aThe minimum number of players has been set to §e" + newMin + "§a.");
            } else {
                player.sendMessage(plugin.getMessagesManager().getPrefix() + " §cThe minimum number cannot exceed the maximum (§e" + arena.getMaxPlayers() + "§c).");
            }
        } else if (event.isRightClick()) {
            int newMin = arena.getMinPlayers() - 1;
            if (newMin >= 2) {
                arena.setMinPlayers(newMin);
                plugin.getArenaManager().saveArenas();
                player.sendMessage(plugin.getMessagesManager().getPrefix() + " §aThe minimum number of players has been set to §e" + newMin + "§a.");
            } else {
                player.sendMessage(plugin.getMessagesManager().getPrefix() + " §cThe minimum number cannot be less than 2.");
            }
        }
        plugin.getGUIManager().refreshGUI(player, arenaName);
    }

    private void handleMaxPlayersClick(InventoryClickEvent event, Player player, Arena arena, String arenaName) {
        if (event.isLeftClick()) {
            int newMax = arena.getMaxPlayers() + 1;
            arena.setMaxPlayers(newMax);
            plugin.getArenaManager().saveArenas();
            player.sendMessage(plugin.getMessagesManager().getPrefix() + " §aThe maximum number of players has been set to §e" + newMax + "§a.");
        } else if (event.isRightClick()) {
            int newMax = arena.getMaxPlayers() - 1;
            if (newMax >= arena.getMinPlayers()) {
                arena.setMaxPlayers(newMax);
                plugin.getArenaManager().saveArenas();
                player.sendMessage(plugin.getMessagesManager().getPrefix() + " §aThe maximum number of players has been set to §e" + newMax + "§a.");
            } else {
                player.sendMessage(plugin.getMessagesManager().getPrefix() + " §cThe maximum number cannot be less than the minimum (§e" + arena.getMinPlayers() + "§c).");
            }
        }
        plugin.getGUIManager().refreshGUI(player, arenaName);
    }

    private void handlePotatoTimerClick(InventoryClickEvent event, Player player, Arena arena, String arenaName) {
        if (event.isLeftClick()) {
            int newTimer = arena.getPotatoTimer() + 5;
            arena.setPotatoTimer(newTimer);
            plugin.getArenaManager().saveArenas();
            player.sendMessage(plugin.getMessagesManager().getPrefix() + " §aThe potato timer duration has been set to §e" + newTimer + "s§a.");
        } else if (event.isRightClick()) {
            int newTimer = arena.getPotatoTimer() - 5;
            if (newTimer >= 5) {
                arena.setPotatoTimer(newTimer);
                plugin.getArenaManager().saveArenas();
                player.sendMessage(plugin.getMessagesManager().getPrefix() + " §aThe potato timer duration has been set to §e" + newTimer + "s§a.");
            } else {
                player.sendMessage(plugin.getMessagesManager().getPrefix() + " §cThe potato timer duration cannot be less than 5 seconds.");
            }
        }
        plugin.getGUIManager().refreshGUI(player, arenaName);
    }

    private void handleSpawnManagementClick(InventoryClickEvent event, Player player, Arena arena, String arenaName) {
        if (event.isLeftClick()) {
            player.closeInventory();
            plugin.getGUIManager().openGUI(player, "spawn_manager", arenaName);
        } else if (event.isRightClick()) {
            arena.addSpawnLocation(player.getLocation());
            plugin.getArenaManager().saveArenas();
            player.sendMessage(plugin.getMessagesManager().getPrefix() + " §aA spawn point has been added to arena §e" + arenaName + "§a.");
            plugin.getGUIManager().refreshGUI(player, arenaName);
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