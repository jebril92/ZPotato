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

public class PlayerManager {

    private final Main plugin;
    private final Map<UUID, HPPlayer> players;
    private final File playersFile;
    private FileConfiguration playersConfig;

    public PlayerManager(Main plugin) {
        this.plugin = plugin;
        this.players = new HashMap<>();
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
            for (String uuidStr : playersSection.getKeys(false)) {
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
            }
        }
    }

    public void savePlayers() {
        if (playersConfig == null) {
            playersConfig = new YamlConfiguration();
        }

        playersConfig.set("players", null);
        ConfigurationSection playersSection = playersConfig.createSection("players");

        for (Map.Entry<UUID, HPPlayer> entry : players.entrySet()) {
            ConfigurationSection playerSection = playersSection.createSection(entry.getKey().toString());
            entry.getValue().saveToConfig(playerSection);
        }

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
        HPPlayer hpPlayer = players.get(playerId);
        return hpPlayer != null && hpPlayer.hasHotPotato();
    }

    public void setHotPotato(UUID playerId, boolean hasHotPotato) {
        HPPlayer hpPlayer = getPlayer(playerId);
        hpPlayer.setHasHotPotato(hasHotPotato);
    }

    public void incrementGamesPlayed(UUID playerId) {
        HPPlayer hpPlayer = getPlayer(playerId);
        hpPlayer.incrementGamesPlayed();
    }

    public void incrementWins(UUID playerId) {
        HPPlayer hpPlayer = getPlayer(playerId);
        hpPlayer.incrementWins();
    }

    public void incrementExplosionsProvoked(UUID playerId) {
        HPPlayer hpPlayer = getPlayer(playerId);
        hpPlayer.incrementExplosionsProvoked();
    }

    public int getGamesPlayed(UUID playerId) {
        HPPlayer hpPlayer = getPlayer(playerId);
        return hpPlayer.getGamesPlayed();
    }

    public int getWins(UUID playerId) {
        HPPlayer hpPlayer = getPlayer(playerId);
        return hpPlayer.getWins();
    }

    public int getExplosionsProvoked(UUID playerId) {
        HPPlayer hpPlayer = getPlayer(playerId);
        return hpPlayer.getExplosionsProvoked();
    }
}