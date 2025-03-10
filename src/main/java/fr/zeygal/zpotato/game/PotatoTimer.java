package fr.zeygal.zpotato.game;

import fr.zeygal.zpotato.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class PotatoTimer {

    private final Main plugin;
    private final UUID playerId;
    private final int initialTime;
    private int timeLeft;
    private int taskId = -1;
    private final Consumer<UUID> onExplosion;

    public PotatoTimer(Main plugin, UUID playerId, int time, Consumer<UUID> onExplosion) {
        this.plugin = plugin;
        this.playerId = playerId;
        this.initialTime = time;
        this.timeLeft = time;
        this.onExplosion = onExplosion;
    }

    public void start() {
        stop();

        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            Map<String, String> replacements = new HashMap<>();
            replacements.put("time", String.valueOf(timeLeft));
            player.sendMessage(plugin.getMessagesManager().getMessage("game.you-have-potato", replacements));
            player.playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1.0f, 1.0f);
        }

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            timeLeft--;

            updateExperienceBar();

            if (timeLeft <= 5 && timeLeft > 0) {
                Player potatoPlayer = Bukkit.getPlayer(playerId);
                if (potatoPlayer != null) {
                    potatoPlayer.playSound(potatoPlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);

                    Map<String, String> replacements = new HashMap<>();
                    replacements.put("time", String.valueOf(timeLeft));
                    potatoPlayer.sendMessage(ChatColor.RED + plugin.getMessagesManager().getMessage("game.potato-warning", replacements));
                }
            }

            if (timeLeft <= 0) {
                explode();
            }
        }, 0L, 20L);
    }

    private void updateExperienceBar() {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            float ratio = (float) timeLeft / initialTime;

            player.setLevel(timeLeft);
            player.setExp(ratio);
        }
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;

            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.setLevel(0);
                player.setExp(0);
            }
        }
    }

    public void reset() {
        stop();
        timeLeft = initialTime;
        start();
    }

    private void explode() {
        stop();

        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            player.getWorld().createExplosion(player.getLocation(), 0.0f, false, false);
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        }

        if (onExplosion != null) {
            onExplosion.accept(playerId);
        }
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public boolean isRunning() {
        return taskId != -1;
    }
}