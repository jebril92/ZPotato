package fr.zeygal.zpotato.placeholders;

import fr.zeygal.zpotato.Main;
import fr.zeygal.zpotato.player.HPPlayer;
import fr.zeygal.zpotato.player.PlayerManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ZPotatoPlaceholderExpansion extends PlaceholderExpansion {

    private final Main plugin;
    private final DecimalFormat decimalFormat = new DecimalFormat("#0.00");

    public ZPotatoPlaceholderExpansion(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "zpotato";
    }

    @Override
    public String getAuthor() {
        return "ZEYGAL";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        PlayerManager playerManager = plugin.getPlayerManager();

        if (player != null) {
            UUID playerId = player.getUniqueId();

            if (identifier.equals("games_played")) {
                return String.valueOf(playerManager.getGamesPlayed(playerId));
            }

            if (identifier.equals("wins")) {
                return String.valueOf(playerManager.getWins(playerId));
            }

            if (identifier.equals("explosions")) {
                return String.valueOf(playerManager.getExplosionsProvoked(playerId));
            }

            if (identifier.equals("win_rate")) {
                int games = playerManager.getGamesPlayed(playerId);
                int wins = playerManager.getWins(playerId);

                if (games > 0) {
                    double winRate = (double) wins / games * 100;
                    return decimalFormat.format(winRate);
                }
                return "0.00";
            }

            if (identifier.equals("has_potato")) {
                return playerManager.hasHotPotato(playerId) ? "yes" : "no";
            }
        }

        if (identifier.startsWith("top_games_played_")) {
            return getTopPlayerByGamesPlayed(identifier);
        }

        if (identifier.startsWith("top_wins_")) {
            return getTopPlayerByWins(identifier);
        }

        if (identifier.startsWith("top_explosions_")) {
            return getTopPlayerByExplosions(identifier);
        }

        return null;
    }

    private String getTopPlayerByGamesPlayed(String identifier) {
        try {
            int position = Integer.parseInt(identifier.split("_")[3]);
            return getTopPlayerInfo(position, Comparator.comparingInt(Map.Entry::getValue), "games_played");
        } catch (Exception e) {
            return null;
        }
    }

    private String getTopPlayerByWins(String identifier) {
        try {
            int position = Integer.parseInt(identifier.split("_")[2]);
            return getTopPlayerInfo(position, Comparator.comparingInt(Map.Entry::getValue), "wins");
        } catch (Exception e) {
            return null;
        }
    }

    private String getTopPlayerByExplosions(String identifier) {
        try {
            int position = Integer.parseInt(identifier.split("_")[2]);
            return getTopPlayerInfo(position, Comparator.comparingInt(Map.Entry::getValue), "explosions");
        } catch (Exception e) {
            return null;
        }
    }

    private String getTopPlayerInfo(int position, Comparator<Map.Entry<UUID, Integer>> comparator, String statType) {
        if (position < 1 || position > 10) {
            return null;
        }

        Map<UUID, Integer> statsMap = getStatsMap(statType);
        if (statsMap.isEmpty()) {
            return "None";
        }

        List<Map.Entry<UUID, Integer>> sortedStats = statsMap.entrySet()
                .stream()
                .sorted(comparator.reversed())
                .collect(Collectors.toList());

        if (position > sortedStats.size()) {
            return "None";
        }

        Map.Entry<UUID, Integer> playerEntry = sortedStats.get(position - 1);
        String playerName = Bukkit.getOfflinePlayer(playerEntry.getKey()).getName();

        if (playerName == null) {
            playerName = "Unknown";
        }

        return playerName + " (" + playerEntry.getValue() + ")";
    }

    private Map<UUID, Integer> getStatsMap(String statType) {
        PlayerManager playerManager = plugin.getPlayerManager();
        Map<UUID, HPPlayer> players = playerManager.getAllPlayers();

        Map<UUID, Integer> statsMap;

        switch (statType) {
            case "games_played":
                statsMap = players.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().getGamesPlayed()
                        ));
                break;
            case "wins":
                statsMap = players.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().getWins()
                        ));
                break;
            case "explosions":
                statsMap = players.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().getExplosionsProvoked()
                        ));
                break;
            default:
                statsMap = Map.of();
        }

        return statsMap;
    }
}