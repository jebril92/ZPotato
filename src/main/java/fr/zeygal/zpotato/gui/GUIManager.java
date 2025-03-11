package fr.zeygal.zpotato.gui;

import fr.zeygal.zpotato.Main;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GUIManager {

    private final Main plugin;
    private final Map<String, GUI> registeredGUIs;
    private final Map<UUID, GUI> playerGUIs;

    public GUIManager(Main plugin) {
        this.plugin = plugin;
        this.registeredGUIs = new HashMap<>();
        this.playerGUIs = new ConcurrentHashMap<>();

        registerGUIs();
    }

    private void registerGUIs() {
        registeredGUIs.put("arena_list", new ArenaListGUI(plugin));
        registeredGUIs.put("arena_settings", new ArenaSettingsGUI(plugin));
    }

    public boolean openGUI(Player player, String guiId, String... args) {
        GUI gui = registeredGUIs.get(guiId);

        if (gui == null) {
            return false;
        }

        Inventory inventory = gui.getInventory(player, args);
        if (inventory == null) {
            return false;
        }

        playerGUIs.put(player.getUniqueId(), gui);
        player.openInventory(inventory);
        return true;
    }

    public GUI getOpenedGUI(UUID uuid) {
        return playerGUIs.get(uuid);
    }

    public void removePlayer(UUID uuid) {
        playerGUIs.remove(uuid);
    }

    public void closeGUI(Player player) {
        player.closeInventory();
        playerGUIs.remove(player.getUniqueId());
    }

    public void refreshGUI(Player player, String... args) {
        UUID uuid = player.getUniqueId();
        GUI gui = playerGUIs.get(uuid);

        if (gui != null) {
            Inventory inventory = gui.getInventory(player, args);
            if (inventory != null) {
                player.openInventory(inventory);
            }
        }
    }

    public void setGUI(UUID uuid, GUI gui) {
        playerGUIs.put(uuid, gui);
    }

    public void cleanupGUIs() {
        playerGUIs.clear();
    }
}