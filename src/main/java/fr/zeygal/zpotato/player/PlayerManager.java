package fr.zeygal.zpotato.player;

import fr.zeygal.zpotato.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {

    private final Main plugin;
    private final Map<UUID, HPPlayer> players;
    private final File playersFile;
    private FileConfiguration playersConfig;

    public PlayerManager(Main plugin) {
        this.plugin = plugin;
        this.players = new ConcurrentHashMap<>();
        this.playersFile = new File(plugin.getDataFolder(), "players.yml");

        loadPlayers();
    }

    public void loadPlayers() {
        if (!playersFile.exists()) {
            try {
                if (!plugin.getDataFolder().exists()) {
                    plugin.getDataFolder().mkdirs();
                }
                playersFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create players file: " + e.getMessage());
                return;
            }
        }

        playersConfig = YamlConfiguration.loadConfiguration(playersFile);
        ConfigurationSection playersSection = playersConfig.getConfigurationSection("players");

        if (playersSection != null) {
            playersSection.getKeys(false).forEach(uuidStr -> {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    ConfigurationSection playerSection = playersSection.getConfigurationSection(uuidStr);
                    if (playerSection != null) {
                        HPPlayer hpPlayer = HPPlayer.loadFromConfig(playerSection, uuid);
                        players.put(uuid, hpPlayer);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in players file: " + uuidStr);
                }
            });
        }
    }

    public void savePlayers() {
        if (playersConfig == null) {
            playersConfig = new YamlConfiguration();
        }

        playersConfig.set("players", null);
        ConfigurationSection playersSection = playersConfig.createSection("players");

        players.forEach((uuid, player) -> {
            ConfigurationSection playerSection = playersSection.createSection(uuid.toString());
            player.saveToConfig(playerSection);
        });

        try {
            playersConfig.save(playersFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save players data: " + e.getMessage());
        }
    }

    public HPPlayer getPlayer(UUID playerId) {
        return players.computeIfAbsent(playerId, HPPlayer::new);
    }

    public HPPlayer getPlayer(Player player) {
        return getPlayer(player.getUniqueId());
    }

    public boolean hasHotPotato(UUID playerId) {
        return players.containsKey(playerId) && players.get(playerId).hasHotPotato();
    }

    public void setHotPotato(UUID playerId, boolean hasHotPotato) {
        getPlayer(playerId).setHasHotPotato(hasHotPotato);
    }

    public void incrementGamesPlayed(UUID playerId) {
        getPlayer(playerId).incrementGamesPlayed();
    }

    public void incrementWins(UUID playerId) {
        getPlayer(playerId).incrementWins();
    }

    public void incrementExplosionsProvoked(UUID playerId) {
        getPlayer(playerId).incrementExplosionsProvoked();
    }

    public int getGamesPlayed(UUID playerId) {
        return getPlayer(playerId).getGamesPlayed();
    }

    public int getWins(UUID playerId) {
        return getPlayer(playerId).getWins();
    }

    public int getExplosionsProvoked(UUID playerId) {
        return getPlayer(playerId).getExplosionsProvoked();
    }
}