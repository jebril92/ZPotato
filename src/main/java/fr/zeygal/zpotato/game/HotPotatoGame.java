package fr.zeygal.zpotato.game;

import fr.zeygal.zpotato.Main;
import fr.zeygal.zpotato.arena.Arena;
import fr.zeygal.zpotato.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HotPotatoGame {

    private final Main plugin;
    private final Arena arena;
    private UUID potatoHolder;
    private int potatoTimerTaskId = -1;
    private int potatoTimeLeft;
    private int actionBarTaskId = -1;

    public HotPotatoGame(Main plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
        this.potatoTimeLeft = arena.getPotatoTimer();
    }

    public void startPotatoTimer() {
        this.potatoTimeLeft = arena.getPotatoTimer();

        stopPotatoTimer();

        Player player = Bukkit.getPlayer(potatoHolder);
        if (player != null) {
            Map<String, String> replacements = new HashMap<>();
            replacements.put("time", String.valueOf(potatoTimeLeft));
            player.sendMessage(plugin.getMessagesManager().getMessage("game.you-have-potato", replacements));
            player.playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1.0f, 1.0f);

            for (UUID playerId : arena.getPlayers()) {
                if (!playerId.equals(potatoHolder)) {
                    Player otherPlayer = Bukkit.getPlayer(playerId);
                    if (otherPlayer != null) {
                        replacements.put("player", player.getName());
                        otherPlayer.sendMessage(plugin.getMessagesManager().getMessage("game.player-has-potato", replacements));
                    }
                }
            }

            plugin.getPlayerManager().setHotPotato(potatoHolder, true);
        }

        startActionBarTimer();

        potatoTimerTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            potatoTimeLeft--;

            if (potatoTimeLeft <= 5 && potatoTimeLeft > 0) {
                Player potatoPlayer = Bukkit.getPlayer(potatoHolder);
                if (potatoPlayer != null) {
                    potatoPlayer.playSound(potatoPlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);

                    Map<String, String> replacements = new HashMap<>();
                    replacements.put("time", String.valueOf(potatoTimeLeft));
                    potatoPlayer.sendMessage(ChatColor.RED + plugin.getMessagesManager().getMessage("game.potato-warning", replacements));
                }
            }

            if (potatoTimeLeft <= 0) {
                stopPotatoTimer();
                explodePotato();
            }
        }, 0L, 20L);
    }

    private void startActionBarTimer() {
        if (actionBarTaskId != -1) {
            Bukkit.getScheduler().cancelTask(actionBarTaskId);
        }

        actionBarTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            updateActionBar();
        }, 0L, 5L);
    }

    private void updateActionBar() {
        Player player = Bukkit.getPlayer(potatoHolder);
        if (player != null) {
            float percentage = (float) potatoTimeLeft / arena.getPotatoTimer();

            ChatColor color;
            if (percentage > 0.66) {
                color = ChatColor.GREEN;
            } else if (percentage > 0.33) {
                color = ChatColor.YELLOW;
            } else {
                color = ChatColor.RED;
            }

            StringBuilder progressBar = new StringBuilder();
            progressBar.append("▃▄▅▆▇");

            progressBar.append(" " + potatoTimeLeft + " ");

            progressBar.append("▇▆▅▄▃");

            String message = color + progressBar.toString();

            if (potatoTimeLeft <= 5) {
                message = ChatColor.RED + "" + ChatColor.BOLD + message;
            }

            MessageUtils.sendActionBar(player, message);
        }
    }

    public void restartPotatoTimer() {
        if (potatoHolder != null) {
            plugin.getPlayerManager().setHotPotato(potatoHolder, false);
        }

        startPotatoTimer();
    }

    public void stopPotatoTimer() {
        if (potatoTimerTaskId != -1) {
            Bukkit.getScheduler().cancelTask(potatoTimerTaskId);
            potatoTimerTaskId = -1;
        }

        if (actionBarTaskId != -1) {
            Bukkit.getScheduler().cancelTask(actionBarTaskId);
            actionBarTaskId = -1;
        }

        if (potatoHolder != null) {
            plugin.getPlayerManager().setHotPotato(potatoHolder, false);

            Player player = Bukkit.getPlayer(potatoHolder);
            if (player != null) {
                player.setLevel(0);
                player.setExp(0);
                MessageUtils.sendActionBar(player, "");
            }
        }
    }

    private void explodePotato() {
        if (potatoHolder != null) {
            Player player = Bukkit.getPlayer(potatoHolder);

            if (player != null) {
                player.getWorld().createExplosion(player.getLocation(), 0.0f, false, false);
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

                plugin.getPlayerManager().incrementExplosionsProvoked(potatoHolder);
            }

            plugin.getGameManager().eliminatePlayer(potatoHolder, arena.getName());
        }
    }

    public Arena getArena() {
        return arena;
    }

    public UUID getPotatoHolder() {
        return potatoHolder;
    }

    public void setPotatoHolder(UUID potatoHolder) {
        if (this.potatoHolder != null) {
            plugin.getPlayerManager().setHotPotato(this.potatoHolder, false);

            Player oldPlayer = Bukkit.getPlayer(this.potatoHolder);
            if (oldPlayer != null) {
                oldPlayer.setLevel(0);
                oldPlayer.setExp(0);
                MessageUtils.sendActionBar(oldPlayer, "");
            }
        }

        this.potatoHolder = potatoHolder;
    }

    public int getPotatoTimeLeft() {
        return potatoTimeLeft;
    }
}