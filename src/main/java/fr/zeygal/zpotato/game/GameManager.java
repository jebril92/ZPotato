package fr.zeygal.zpotato.game;

import fr.zeygal.zpotato.Main;
import fr.zeygal.zpotato.arena.Arena;
import fr.zeygal.zpotato.arena.ArenaState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class GameManager {

    private final Main plugin;
    private final Map<String, HotPotatoGame> activeGames;
    private final Random random;
    private final Map<String, Integer> countdownTasks;

    public GameManager(Main plugin) {
        this.plugin = plugin;
        this.activeGames = new HashMap<>();
        this.random = new Random();
        this.countdownTasks = new HashMap<>();
    }

    public boolean startGame(String arenaName) {
        Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null || arena.getState() != ArenaState.WAITING || !arena.canStart()) {
            return false;
        }

        if (arena.getPlayerCount() < arena.getMinPlayers()) {
            return false;
        }

        HotPotatoGame game = new HotPotatoGame(plugin, arena);
        activeGames.put(arenaName, game);

        startCountdown(arenaName);

        return true;
    }

    private void startCountdown(String arenaName) {
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) return;

        arena.setState(ArenaState.STARTING);

        int countdownTime = plugin.getConfigManager().getCountdownTime();
        final int[] countdown = {countdownTime};

        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (countdown[0] <= 0) {
                if (countdownTasks.containsKey(arenaName)) {
                    Bukkit.getScheduler().cancelTask(countdownTasks.get(arenaName));
                    countdownTasks.remove(arenaName);
                }

                actuallyStartGame(arenaName);
                return;
            }

            if (countdown[0] <= 5 || countdown[0] == 10 || countdown[0] == 20 || countdown[0] == 30) {
                for (UUID playerId : arena.getPlayers()) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null) {
                        Map<String, String> replacements = new HashMap<>();
                        replacements.put("time", String.valueOf(countdown[0]));
                        replacements.put("arena", arena.getName());

                        String message = plugin.getMessagesManager().getMessage("countdown.message", replacements);
                        player.sendMessage(message);
                    }
                }
            }

            countdown[0]--;
        }, 0L, 20L);

        countdownTasks.put(arenaName, taskId);
    }

    private void actuallyStartGame(String arenaName) {
        HotPotatoGame game = activeGames.get(arenaName);
        if (game == null) return;

        Arena arena = game.getArena();
        arena.setState(ArenaState.RUNNING);

        List<Location> spawnLocations = arena.getSpawnLocations();
        List<UUID> playerIds = arena.getPlayers();

        if (spawnLocations.size() < playerIds.size()) {
            plugin.getLogger().warning("Not enough spawn points in arena " + arenaName);
            stopGame(arenaName);
            return;
        }

        List<UUID> shuffledPlayers = new ArrayList<>(playerIds);
        for (int i = 0; i < shuffledPlayers.size(); i++) {
            int j = random.nextInt(shuffledPlayers.size());
            UUID temp = shuffledPlayers.get(i);
            shuffledPlayers.set(i, shuffledPlayers.get(j));
            shuffledPlayers.set(j, temp);
        }

        for (int i = 0; i < shuffledPlayers.size(); i++) {
            UUID playerId = shuffledPlayers.get(i);
            Player player = Bukkit.getPlayer(playerId);

            if (player != null) {
                player.teleport(spawnLocations.get(i % spawnLocations.size()));

                Map<String, String> replacements = new HashMap<>();
                replacements.put("arena", arena.getName());
                player.sendMessage(plugin.getMessagesManager().getMessage("game.start", replacements));

                plugin.getPlayerManager().incrementGamesPlayed(playerId);
            }
        }

        UUID potatoHolder = shuffledPlayers.get(random.nextInt(shuffledPlayers.size()));
        game.setPotatoHolder(potatoHolder);

        game.startPotatoTimer();
    }

    public boolean stopGame(String arenaName) {
        HotPotatoGame game = activeGames.get(arenaName);
        if (game == null) return false;

        Arena arena = game.getArena();
        arena.setState(ArenaState.ENDING);

        game.stopPotatoTimer();

        if (countdownTasks.containsKey(arenaName)) {
            Bukkit.getScheduler().cancelTask(countdownTasks.get(arenaName));
            countdownTasks.remove(arenaName);
        }

        for (UUID playerId : arena.getPlayers()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                Location lobbyLocation = arena.getLobby();
                if (lobbyLocation == null) {
                    lobbyLocation = plugin.getConfigManager().getMainLobby();
                }

                if (lobbyLocation != null) {
                    player.teleport(lobbyLocation);
                }

                Map<String, String> replacements = new HashMap<>();
                replacements.put("arena", arena.getName());
                player.sendMessage(plugin.getMessagesManager().getMessage("game.stop", replacements));
            }
        }

        arena.setState(ArenaState.WAITING);
        activeGames.remove(arenaName);

        return true;
    }

    public void stopAllGames() {
        for (String arenaName : new ArrayList<>(activeGames.keySet())) {
            stopGame(arenaName);
        }

        for (String arenaName : new ArrayList<>(countdownTasks.keySet())) {
            Bukkit.getScheduler().cancelTask(countdownTasks.get(arenaName));
        }
        countdownTasks.clear();
    }

    public void eliminatePlayer(UUID playerId, String arenaName) {
        HotPotatoGame game = activeGames.get(arenaName);
        if (game == null) return;

        Arena arena = game.getArena();

        Player player = Bukkit.getPlayer(playerId);
        if (player != null && arena.getSpectatorLocation() != null) {
            player.teleport(arena.getSpectatorLocation());

            Map<String, String> replacements = new HashMap<>();
            replacements.put("player", player.getName());
            player.sendMessage(plugin.getMessagesManager().getMessage("game.eliminated", replacements));

            for (UUID otherPlayerId : arena.getPlayers()) {
                if (!otherPlayerId.equals(playerId)) {
                    Player otherPlayer = Bukkit.getPlayer(otherPlayerId);
                    if (otherPlayer != null) {
                        otherPlayer.sendMessage(plugin.getMessagesManager().getMessage("game.player-eliminated", replacements));
                    }
                }
            }
        }

        arena.removePlayer(playerId);

        if (arena.getPlayerCount() <= 1) {
            if (arena.getPlayerCount() == 1) {
                UUID winnerId = arena.getPlayers().get(0);
                Player winner = Bukkit.getPlayer(winnerId);

                if (winner != null) {
                    Map<String, String> replacements = new HashMap<>();
                    replacements.put("player", winner.getName());
                    replacements.put("arena", arena.getName());

                    Bukkit.broadcastMessage(plugin.getMessagesManager().getMessage("game.winner", replacements));

                    plugin.getPlayerManager().incrementWins(winnerId);
                }
            }

            stopGame(arenaName);
        } else {
            if (game.getPotatoHolder() != null && game.getPotatoHolder().equals(playerId)) {
                List<UUID> remainingPlayers = arena.getPlayers();
                UUID newHolder = remainingPlayers.get(random.nextInt(remainingPlayers.size()));
                game.setPotatoHolder(newHolder);

                game.restartPotatoTimer();
            }
        }
    }

    public void transferPotato(UUID fromPlayerId, UUID toPlayerId, String arenaName) {
        HotPotatoGame game = activeGames.get(arenaName);
        if (game == null) return;

        if (!fromPlayerId.equals(game.getPotatoHolder())) {
            return;
        }

        game.setPotatoHolder(toPlayerId);

        Player fromPlayer = Bukkit.getPlayer(fromPlayerId);
        Player toPlayer = Bukkit.getPlayer(toPlayerId);

        if (fromPlayer != null && toPlayer != null) {
            Map<String, String> replacements = new HashMap<>();
            replacements.put("from", fromPlayer.getName());
            replacements.put("to", toPlayer.getName());

            for (UUID playerId : game.getArena().getPlayers()) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                    player.sendMessage(plugin.getMessagesManager().getMessage("game.potato-transferred", replacements));
                }
            }
        }

        game.restartPotatoTimer();
    }

    public HotPotatoGame getGame(String arenaName) {
        return activeGames.get(arenaName);
    }

    public boolean isGameActive(String arenaName) {
        return activeGames.containsKey(arenaName);
    }
}