package fr.zeygal.zpotato.listeners;

import fr.zeygal.zpotato.Main;
import fr.zeygal.zpotato.arena.Arena;
import fr.zeygal.zpotato.arena.ArenaState;
import fr.zeygal.zpotato.game.HotPotatoGame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class GameListener implements Listener {

    private final Main plugin;

    public GameListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();

            if (!plugin.getArenaManager().isPlayerInAnyArena(attacker.getUniqueId()) ||
                    !plugin.getArenaManager().isPlayerInAnyArena(victim.getUniqueId())) {
                return;
            }

            Arena attackerArena = plugin.getArenaManager().getPlayerArena(attacker.getUniqueId());
            Arena victimArena = plugin.getArenaManager().getPlayerArena(victim.getUniqueId());

            if (attackerArena != victimArena || attackerArena.getState() != ArenaState.RUNNING) {
                return;
            }

            HotPotatoGame game = plugin.getGameManager().getGame(attackerArena.getName());
            if (game != null && game.getPotatoHolder() != null &&
                    game.getPotatoHolder().equals(attacker.getUniqueId())) {

                plugin.getGameManager().transferPotato(attacker.getUniqueId(), victim.getUniqueId(), attackerArena.getName());

                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (plugin.getArenaManager().isPlayerInAnyArena(player.getUniqueId())) {
                if (event instanceof EntityDamageByEntityEvent) {
                    EntityDamageByEntityEvent damageByEntityEvent = (EntityDamageByEntityEvent) event;
                    if (damageByEntityEvent.getDamager() instanceof Player) {
                        return;
                    }
                }

                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (plugin.getArenaManager().isPlayerInAnyArena(player.getUniqueId())) {
                event.setCancelled(true);
                event.setFoodLevel(20);
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (plugin.getArenaManager().isPlayerInAnyArena(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (plugin.getArenaManager().isPlayerInAnyArena(player.getUniqueId())) {
            Arena arena = plugin.getArenaManager().getPlayerArena(player.getUniqueId());

            if (arena.getState() == ArenaState.RUNNING) {

            }
        }
    }
}