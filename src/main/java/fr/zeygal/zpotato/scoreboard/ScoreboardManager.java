package fr.zeygal.zpotato.scoreboard;

import fr.zeygal.zpotato.Main;
import fr.zeygal.zpotato.arena.Arena;
import fr.zeygal.zpotato.arena.ArenaState;
import fr.zeygal.zpotato.game.HotPotatoGame;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardManager {

    private final Main plugin;
    private final FileConfiguration scoreboardConfig;
    private final File configFile;
    private final org.bukkit.scoreboard.ScoreboardManager bukkitScoreboardManager;
    private final Map<UUID, Scoreboard> playerScoreboards;
    private final Map<UUID, Integer> updateTasks;
    private final boolean enabled;
    private final boolean placeholderAPIEnabled;

    private final boolean lobbyScoreboardEnabled;
    private final String lobbyTitle;
    private final List<String> lobbyLines;
    private final int lobbyUpdateInterval;

    private final boolean gameScoreboardEnabled;
    private final String gameTitle;
    private final List<String> gameLines;
    private final int gameUpdateInterval;

    public ScoreboardManager(Main plugin) {
        this.plugin = plugin;
        this.bukkitScoreboardManager = Bukkit.getScoreboardManager();
        this.playerScoreboards = new ConcurrentHashMap<>();
        this.updateTasks = new ConcurrentHashMap<>();
        this.configFile = new File(plugin.getDataFolder(), "scoreboards.yml");

        if (!configFile.exists()) {
            saveDefaultConfig();
        }

        this.scoreboardConfig = YamlConfiguration.loadConfiguration(configFile);

        this.placeholderAPIEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");

        this.enabled = scoreboardConfig.getBoolean("enabled", true);

        ConfigurationSection lobbySection = scoreboardConfig.getConfigurationSection("lobby-scoreboard");
        this.lobbyScoreboardEnabled = lobbySection != null && lobbySection.getBoolean("enabled", true);
        this.lobbyTitle = lobbySection != null ? ChatColor.translateAlternateColorCodes('&', lobbySection.getString("title", "&6&lHot Potato")) : "&6&lHot Potato";
        this.lobbyLines = loadLines(lobbySection);
        this.lobbyUpdateInterval = lobbySection != null ? lobbySection.getInt("update-interval", 20) : 20;

        ConfigurationSection gameSection = scoreboardConfig.getConfigurationSection("game-scoreboard");
        this.gameScoreboardEnabled = gameSection != null && gameSection.getBoolean("enabled", true);
        this.gameTitle = gameSection != null ? ChatColor.translateAlternateColorCodes('&', gameSection.getString("title", "&c&lHot Potato")) : "&c&lHot Potato";
        this.gameLines = loadLines(gameSection);
        this.gameUpdateInterval = gameSection != null ? gameSection.getInt("update-interval", 10) : 10;
    }

    private List<String> loadLines(ConfigurationSection section) {
        List<String> lines = new ArrayList<>();
        if (section != null && section.isList("lines")) {
            for (String line : section.getStringList("lines")) {
                lines.add(ChatColor.translateAlternateColorCodes('&', line));
            }
        } else {
            lines.add("&7---------------");
            lines.add("&fPlayers: &e{arena_player_count}/{arena_max_players}");
            lines.add("&fStatus: &e{arena_state}");
            lines.add("&7---------------");
            lines.add("&eLine 5");
            lines.add("&eLine 6");
            lines.add("&7---------------");
        }
        return lines;
    }

    private void saveDefaultConfig() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            InputStream defaultConfig = plugin.getResource("scoreboards.yml");
            if (defaultConfig != null) {
                Files.copy(defaultConfig, configFile.toPath());
                plugin.getLogger().info("Created default scoreboards.yml");
            } else {
                FileConfiguration config = new YamlConfiguration();
                ConfigurationSection mainSection = config.createSection("enabled");
                mainSection.set("enabled", true);

                ConfigurationSection lobbySection = config.createSection("lobby-scoreboard");
                lobbySection.set("enabled", true);
                lobbySection.set("title", "&6&lHot Potato - Lobby");
                lobbySection.set("update-interval", 20);
                List<String> lobbyLines = new ArrayList<>();
                lobbyLines.add("&7---------------");
                lobbyLines.add("&fArena: &e{arena_name}");
                lobbyLines.add("&fPlayers: &e{arena_player_count}/{arena_max_players}");
                lobbyLines.add("&fMin Players: &e{arena_min_players}");
                lobbyLines.add("&fStatus: &e{arena_state}");
                lobbyLines.add("");
                lobbyLines.add("&eWaiting for players...");
                lobbyLines.add("&7---------------");
                lobbySection.set("lines", lobbyLines);

                ConfigurationSection gameSection = config.createSection("game-scoreboard");
                gameSection.set("enabled", true);
                gameSection.set("title", "&c&lHot Potato - Game");
                gameSection.set("update-interval", 10);
                List<String> gameLines = new ArrayList<>();
                gameLines.add("&7---------------");
                gameLines.add("&fArena: &e{arena_name}");
                gameLines.add("&fPlayers: &e{arena_player_count}");
                gameLines.add("&fPotato Holder: &c{potato_holder}");
                gameLines.add("&fTime Left: &e{potato_time}s");
                gameLines.add("");
                gameLines.add("&cDon't get caught with");
                gameLines.add("&cthe potato!");
                gameLines.add("&7---------------");
                gameSection.set("lines", gameLines);

                config.save(configFile);
                plugin.getLogger().info("Created basic scoreboards.yml");
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Could not create scoreboards.yml: " + e.getMessage());
        }
    }

    public void updateScoreboardsForArena(String arenaName) {
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) return;

        for (UUID playerId : arena.getPlayers()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                updateScoreboard(player, arena);
            }
        }
    }

    public void updateScoreboard(Player player, Arena arena) {
        if (!enabled) return;

        UUID playerId = player.getUniqueId();

        if (arena == null) {
            removeScoreboard(player);
            return;
        }

        boolean shouldShowLobbyScoreboard = (arena.getState() == ArenaState.WAITING ||
                arena.getState() == ArenaState.STARTING) &&
                lobbyScoreboardEnabled;

        boolean shouldShowGameScoreboard = arena.getState() == ArenaState.RUNNING &&
                gameScoreboardEnabled;

        if (!shouldShowLobbyScoreboard && !shouldShowGameScoreboard) {
            removeScoreboard(player);
            return;
        }

        if (!playerScoreboards.containsKey(playerId)) {
            createScoreboard(player, arena);
            return;
        }

        Scoreboard board = playerScoreboards.get(playerId);
        Objective objective = board.getObjective(DisplaySlot.SIDEBAR);

        if (objective == null) {
            createScoreboard(player, arena);
            return;
        }

        if (shouldShowLobbyScoreboard) {
            objective.setDisplayName(lobbyTitle);
        } else {
            objective.setDisplayName(gameTitle);
        }

        List<String> lines = shouldShowLobbyScoreboard ? lobbyLines : gameLines;

        for (int i = 0; i < 15; i++) {
            Team team = board.getTeam("line" + i);
            if (team != null) {
                if (i < lines.size()) {
                    String line = replacePlaceholders(lines.get(i), player, arena);
                    String entry = ChatColor.values()[i].toString();

                    if (line.length() > 32) {
                        team.setPrefix(line.substring(0, 16));
                        team.setSuffix(line.substring(16, Math.min(32, line.length())));
                    } else {
                        team.setPrefix(line);
                        team.setSuffix("");
                    }
                } else {
                    team.setPrefix("");
                    team.setSuffix("");
                }
            }
        }
    }

    private void createScoreboard(Player player, Arena arena) {
        if (bukkitScoreboardManager == null) return;

        UUID playerId = player.getUniqueId();

        if (updateTasks.containsKey(playerId)) {
            Bukkit.getScheduler().cancelTask(updateTasks.get(playerId));
            updateTasks.remove(playerId);
        }

        boolean showLobbyScoreboard = (arena.getState() == ArenaState.WAITING ||
                arena.getState() == ArenaState.STARTING) &&
                lobbyScoreboardEnabled;

        boolean showGameScoreboard = arena.getState() == ArenaState.RUNNING &&
                gameScoreboardEnabled;

        if (!showLobbyScoreboard && !showGameScoreboard) {
            return;
        }

        Scoreboard board = bukkitScoreboardManager.getNewScoreboard();
        Objective objective = board.registerNewObjective("hotpotato", "dummy",
                showLobbyScoreboard ? lobbyTitle : gameTitle);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<String> lines = showLobbyScoreboard ? lobbyLines : gameLines;

        for (int i = 0; i < 15; i++) {
            Team team = board.registerNewTeam("line" + i);
            String entry = ChatColor.values()[i].toString();
            team.addEntry(entry);
            objective.getScore(entry).setScore(15 - i);

            if (i < lines.size()) {
                String line = replacePlaceholders(lines.get(i), player, arena);

                if (line.length() > 32) {
                    team.setPrefix(line.substring(0, 16));
                    team.setSuffix(line.substring(16, Math.min(32, line.length())));
                } else {
                    team.setPrefix(line);
                    team.setSuffix("");
                }
            }
        }

        player.setScoreboard(board);
        playerScoreboards.put(playerId, board);

        int updateInterval = showLobbyScoreboard ? lobbyUpdateInterval : gameUpdateInterval;
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (plugin.getArenaManager().isPlayerInAnyArena(playerId)) {
                Arena currentArena = plugin.getArenaManager().getPlayerArena(playerId);
                updateScoreboard(player, currentArena);
            } else {
                removeScoreboard(player);
            }
        }, updateInterval, updateInterval);

        updateTasks.put(playerId, taskId);
    }

    public void removeScoreboard(Player player) {
        UUID playerId = player.getUniqueId();

        if (updateTasks.containsKey(playerId)) {
            Bukkit.getScheduler().cancelTask(updateTasks.get(playerId));
            updateTasks.remove(playerId);
        }

        if (playerScoreboards.containsKey(playerId)) {
            playerScoreboards.remove(playerId);

            if (bukkitScoreboardManager != null) {
                player.setScoreboard(bukkitScoreboardManager.getNewScoreboard());
            }
        }
    }

    private String replacePlaceholders(String text, Player player, Arena arena) {
        String result = text;

        result = result.replace("{arena_name}", arena.getName());
        result = result.replace("{arena_player_count}", String.valueOf(arena.getPlayerCount()));
        result = result.replace("{arena_max_players}", String.valueOf(arena.getMaxPlayers()));
        result = result.replace("{arena_min_players}", String.valueOf(arena.getMinPlayers()));
        result = result.replace("{arena_state}", arena.getState().toString());

        if (arena.getState() == ArenaState.RUNNING) {
            HotPotatoGame game = plugin.getGameManager().getGame(arena.getName());
            if (game != null) {
                UUID potatoHolder = game.getPotatoHolder();
                String holderName = potatoHolder != null ?
                        Bukkit.getPlayer(potatoHolder) != null ?
                                Bukkit.getPlayer(potatoHolder).getName() : "Unknown" : "None";

                result = result.replace("{potato_holder}", holderName);
                result = result.replace("{potato_time}", String.valueOf(game.getPotatoTimeLeft()));
                result = result.replace("{is_potato_holder}",
                        player.getUniqueId().equals(potatoHolder) ? "YES" : "NO");
            }
        }

        if (arena.getState() == ArenaState.STARTING) {

        }

        result = result.replace("{player_name}", player.getName());
        result = result.replace("{player_games}", String.valueOf(plugin.getPlayerManager().getGamesPlayed(player.getUniqueId())));
        result = result.replace("{player_wins}", String.valueOf(plugin.getPlayerManager().getWins(player.getUniqueId())));
        result = result.replace("{player_explosions}", String.valueOf(plugin.getPlayerManager().getExplosionsProvoked(player.getUniqueId())));

        if (placeholderAPIEnabled) {
            result = PlaceholderAPI.setPlaceholders(player, result);
        }

        return result;
    }

    public void onPlayerJoin(Player player, Arena arena) {
        updateScoreboard(player, arena);
    }

    public void onPlayerQuit(Player player) {
        removeScoreboard(player);
    }

    public void onArenaStateChange(Arena arena) {
        updateScoreboardsForArena(arena.getName());
    }

    public void shutdown() {
        for (Integer taskId : updateTasks.values()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        updateTasks.clear();
        playerScoreboards.clear();
    }
}