package fr.zeygal.zpotato.arena;

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

public class ArenaManager {

    private final Main plugin;
    private final Map<String, Arena> arenas;
    private final File arenasFile;
    private FileConfiguration arenasConfig;

    public ArenaManager(Main plugin) {
        this.plugin = plugin;
        this.arenas = new HashMap<>();
        this.arenasFile = new File(plugin.getDataFolder(), "arenas.yml");
    }

    public void loadArenas() {
        if (!arenasFile.exists()) {
            plugin.saveResource("arenas.yml", false);
        }

        arenasConfig = YamlConfiguration.loadConfiguration(arenasFile);
        ConfigurationSection arenasSection = arenasConfig.getConfigurationSection("arenas");

        if (arenasSection != null) {
            arenasSection.getKeys(false).forEach(arenaName -> {
                ConfigurationSection arenaSection = arenasSection.getConfigurationSection(arenaName);
                if (arenaSection != null) {
                    Arena arena = Arena.loadFromConfig(arenaSection, arenaName);
                    arenas.put(arena.getName(), arena);
                    plugin.getLogger().info("Loaded arena: " + arena.getName());
                }
            });
        }
    }

    public void saveArenas() {
        if (arenasConfig == null) {
            arenasConfig = new YamlConfiguration();
        }

        arenasConfig.set("arenas", null);

        arenas.values().forEach(arena -> arena.saveToConfig(arenasConfig));

        try {
            arenasConfig.save(arenasFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save arenas: " + e.getMessage());
        }
    }

    public boolean createArena(String name, int minPlayers, int maxPlayers) {
        if (arenas.containsKey(name)) {
            return false;
        }

        Arena arena = new Arena(name);
        arena.setMinPlayers(minPlayers);
        arena.setMaxPlayers(maxPlayers);

        arenas.put(name, arena);
        saveArenas();

        return true;
    }

    public boolean deleteArena(String name) {
        if (!arenas.containsKey(name)) {
            return false;
        }

        arenas.remove(name);
        saveArenas();

        return true;
    }

    public Arena getArena(String name) {
        return arenas.get(name);
    }

    public Arena getPlayerArena(UUID playerId) {
        return arenas.values().stream()
                .filter(arena -> arena.isPlayerInArena(playerId))
                .findFirst()
                .orElse(null);
    }

    public Map<String, Arena> getAllArenas() {
        return new HashMap<>(arenas);
    }

    public boolean isPlayerInAnyArena(UUID playerId) {
        return arenas.values().stream()
                .anyMatch(arena -> arena.isPlayerInArena(playerId));
    }

    public boolean addPlayerToArena(Player player, String arenaName) {
        if (isPlayerInAnyArena(player.getUniqueId())) {
            return false;
        }

        Arena arena = getArena(arenaName);
        if (arena == null || !arena.isValid() ||
                (arena.getState() != ArenaState.WAITING && arena.getState() != ArenaState.STARTING) ||
                arena.getPlayerCount() >= arena.getMaxPlayers()) {
            return false;
        }

        boolean added = arena.addPlayer(player.getUniqueId());

        if (added && arena.getLobby() != null) {
            player.teleport(arena.getLobby());

            if (arena.canStart() && arena.getState() == ArenaState.WAITING) {
                plugin.getGameManager().startGame(arenaName);
            }
        }

        return added;
    }

    public boolean removePlayerFromArena(Player player) {
        Arena arena = getPlayerArena(player.getUniqueId());
        return arena != null && arena.removePlayer(player.getUniqueId());
    }
}