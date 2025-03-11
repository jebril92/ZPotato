package fr.zeygal.zpotato.game;

import fr.zeygal.zpotato.Main;
import fr.zeygal.zpotato.arena.Arena;
import fr.zeygal.zpotato.arena.ArenaState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class GameManager {

    private final Main plugin;
    private final Map<String, HotPotatoGame> activeGames;
    private final Random random;
    private final Map<String, Integer> countdownTasks;
    private final Map<String, List<UUID>> spectators;

    public GameManager(Main plugin) {
        this.plugin = plugin;
        this.activeGames = new HashMap<>();
        this.random = new Random();
        this.countdownTasks = new HashMap<>();
        this.spectators = new HashMap<>();
    }

    public boolean startGame(String arenaName) {
        Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null || arena.getState() != ArenaState.WAITING || !arena.canStart()) {
            return false;
        }

        if (arena.getPlayerCount() < arena.getMinPlayers()) {
            return false;
        }

        spectators.put(arenaName, new ArrayList<>());

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
                arena.getPlayers().stream()
                        .map(Bukkit::getPlayer)
                        .filter(Objects::nonNull)
                        .forEach(player -> {
                            Map<String, String> replacements = new HashMap<>();
                            replacements.put("time", String.valueOf(countdown[0]));
                            replacements.put("arena", arena.getName());

                            String message = plugin.getMessagesManager().getMessage("countdown.message", replacements);
                            player.sendMessage(message);
                        });
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
        Collections.shuffle(shuffledPlayers, random);

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

        Location mainLobby = plugin.getConfigManager().getMainLobby();
        if (mainLobby == null) {
            plugin.getLogger().warning("Main lobby not set! Players will not be teleported.");
        }

        UUID lastEliminatedPlayer = null;
        List<UUID> spectatorsList = spectators.getOrDefault(arenaName, new ArrayList<>());
        if (!spectatorsList.isEmpty()) {
            lastEliminatedPlayer = spectatorsList.get(spectatorsList.size() - 1);
        }

        List<UUID> allActivePlayers = new ArrayList<>(arena.getPlayers());

        allActivePlayers.forEach(playerId -> {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                teleportToMainLobby(player, arena, mainLobby);
                arena.removePlayer(playerId);
            }
        });

        spectatorsList.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(player -> teleportToMainLobby(player, arena, mainLobby));

        if (lastEliminatedPlayer != null) {
            Player lastEliminated = Bukkit.getPlayer(lastEliminatedPlayer);
            if (lastEliminated != null) {
                teleportToMainLobby(lastEliminated, arena, mainLobby);
            }
        }

        spectators.remove(arenaName);

        arena.setState(ArenaState.WAITING);
        activeGames.remove(arenaName);

        return true;
    }

    private void teleportToMainLobby(Player player, Arena arena, Location mainLobby) {
        if (mainLobby != null) {
            player.teleport(mainLobby);

            Map<String, String> replacements = new HashMap<>();
            replacements.put("arena", arena.getName());
            player.sendMessage(plugin.getMessagesManager().getMessage("game.stop", replacements));

            player.sendMessage(plugin.getMessagesManager().getMessage("game.teleported-to-main-lobby"));
        } else {
            Location lobbyLocation = arena.getLobby();
            if (lobbyLocation != null) {
                player.teleport(lobbyLocation);
            }

            Map<String, String> replacements = new HashMap<>();
            replacements.put("arena", arena.getName());
            player.sendMessage(plugin.getMessagesManager().getMessage("game.stop", replacements));
        }
    }

    public void stopAllGames() {
        new ArrayList<>(activeGames.keySet()).forEach(this::stopGame);

        countdownTasks.forEach((arenaName, taskId) -> Bukkit.getScheduler().cancelTask(taskId));
        countdownTasks.clear();
        spectators.clear();
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

            arena.getPlayers().stream()
                    .filter(id -> !id.equals(playerId))
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .forEach(otherPlayer ->
                            otherPlayer.sendMessage(plugin.getMessagesManager().getMessage("game.player-eliminated", replacements))
                    );

            spectators.computeIfAbsent(arenaName, k -> new ArrayList<>()).add(playerId);
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

            game.getArena().getPlayers().stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .forEach(player ->
                            player.sendMessage(plugin.getMessagesManager().getMessage("game.potato-transferred", replacements))
                    );

            spectators.getOrDefault(arenaName, new ArrayList<>()).stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .forEach(player ->
                            player.sendMessage(plugin.getMessagesManager().getMessage("game.potato-transferred", replacements))
                    );
        }

        game.restartPotatoTimer();
    }

    public HotPotatoGame getGame(String arenaName) {
        return activeGames.get(arenaName);
    }

    public boolean isGameActive(String arenaName) {
        return activeGames.containsKey(arenaName);
    }

    public boolean isSpectator(UUID playerId, String arenaName) {
        List<UUID> arenaSpectators = spectators.getOrDefault(arenaName, new ArrayList<>());
        return arenaSpectators.contains(playerId);
    }

    public List<UUID> getSpectators(String arenaName) {
        return new ArrayList<>(spectators.getOrDefault(arenaName, new ArrayList<>()));
    }
}