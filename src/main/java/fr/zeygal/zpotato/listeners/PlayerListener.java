package fr.zeygal.zpotato.listeners;

import fr.zeygal.zpotato.Main;
import fr.zeygal.zpotato.arena.Arena;
import fr.zeygal.zpotato.arena.ArenaState;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener {

    private final Main plugin;

    public PlayerListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        plugin.getPlayerManager().getPlayer(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (plugin.getArenaManager().isPlayerInAnyArena(player.getUniqueId())) {
            Arena arena = plugin.getArenaManager().getPlayerArena(player.getUniqueId());

            if (arena.getState() == ArenaState.RUNNING) {
                if (plugin.getGameManager().getGame(arena.getName()) != null &&
                        plugin.getGameManager().getGame(arena.getName()).getPotatoHolder() != null &&
                        plugin.getGameManager().getGame(arena.getName()).getPotatoHolder().equals(player.getUniqueId())) {

                    plugin.getGameManager().eliminatePlayer(player.getUniqueId(), arena.getName());
                } else {
                    plugin.getArenaManager().removePlayerFromArena(player);
                }
            } else {
                plugin.getArenaManager().removePlayerFromArena(player);
            }
        }

        plugin.getPlayerManager().savePlayers();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if (plugin.getArenaManager().isPlayerInAnyArena(player.getUniqueId())) {
            Arena arena = plugin.getArenaManager().getPlayerArena(player.getUniqueId());

            if (arena.getState() == ArenaState.RUNNING) {
                Location to = event.getTo();

                if (event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN) {
                    boolean isValidDestination = isLocationInArena(to, arena);

                    if (!isValidDestination) {
                        event.setCancelled(true);
                        player.sendMessage(plugin.getMessagesManager().getMessage("game.cannot-teleport"));
                    }
                } else {
                    event.setCancelled(true);
                    player.sendMessage(plugin.getMessagesManager().getMessage("game.cannot-teleport"));
                }
            }
        }
    }

    private boolean isLocationInArena(Location location, Arena arena) {
        if (arena.getLobby() != null && isSameLocation(location, arena.getLobby())) {
            return true;
        }

        if (arena.getSpectatorLocation() != null && isSameLocation(location, arena.getSpectatorLocation())) {
            return true;
        }

        for (Location spawn : arena.getSpawnLocations()) {
            if (isSameLocation(location, spawn)) {
                return true;
            }
        }

        return false;
    }

    private boolean isSameLocation(Location loc1, Location loc2) {
        return loc1.getWorld().equals(loc2.getWorld()) &&
                loc1.getBlockX() == loc2.getBlockX() &&
                loc1.getBlockY() == loc2.getBlockY() &&
                loc1.getBlockZ() == loc2.getBlockZ();
    }
}