package fr.zeygal.zpotato.gui;

import fr.zeygal.zpotato.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GUIListener implements Listener {

    private final Main plugin;

    public GUIListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (!title.contains("§6§l")) {
            return;
        }

        event.setCancelled(true);

        GUI gui = plugin.getGUIManager().getOpenedGUI(player.getUniqueId());

        if (gui == null) {
            if (title.equals("§6§lArena Management")) {
                gui = new ArenaListGUI(plugin);
                plugin.getGUIManager().setGUI(player.getUniqueId(), gui);
            }
            else if (title.startsWith("§6§lSettings: §e")) {
                gui = new ArenaSettingsGUI(plugin);
                plugin.getGUIManager().setGUI(player.getUniqueId(), gui);
            }
        }

        if (gui != null) {
            String[] args = new String[0];

            if (title.startsWith("§6§lSettings: §e")) {
                String arenaName = title.substring(title.lastIndexOf("§e") + 2);
                args = new String[] { arenaName };
            }
            else if (title.startsWith("§6§lSpawn Points: §e")) {
                String arenaName = title.substring(title.lastIndexOf("§e") + 2);
                args = new String[] { arenaName };
            }

            gui.handleClick(event, player, args);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        GUI gui = plugin.getGUIManager().getOpenedGUI(player.getUniqueId());

        if (gui != null) {
            gui.onClose(player);

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!player.getOpenInventory().getTitle().contains("§6§l")) {
                    plugin.getGUIManager().removePlayer(player.getUniqueId());
                }
            }, 2L);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getGUIManager().removePlayer(event.getPlayer().getUniqueId());
    }
}