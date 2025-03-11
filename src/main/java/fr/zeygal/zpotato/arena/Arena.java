package fr.zeygal.zpotato.arena;

import fr.zeygal.zpotato.utils.LocationSerializer;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Arena {

    private final String name;
    private int minPlayers;
    private int maxPlayers;
    private Location lobby;
    private Location spectatorLocation;
    private final List<Location> spawnLocations;
    private int potatoTimer;
    private ArenaState state;
    private final List<UUID> players;

    public Arena(String name) {
        this.name = name;
        this.minPlayers = 2;
        this.maxPlayers = 12;
        this.spawnLocations = new ArrayList<>();
        this.potatoTimer = 20;
        this.state = ArenaState.WAITING;
        this.players = new ArrayList<>();
    }

    public void saveToConfig(FileConfiguration config) {
        String path = "arenas." + name;
        config.set(path + ".minPlayers", minPlayers);
        config.set(path + ".maxPlayers", maxPlayers);
        config.set(path + ".potatoTimer", potatoTimer);

        if (lobby != null) {
            config.set(path + ".lobby", LocationSerializer.locationToString(lobby));
        }

        if (spectatorLocation != null) {
            config.set(path + ".spectator", LocationSerializer.locationToString(spectatorLocation));
        }

        List<String> spawns = spawnLocations.stream()
                .map(LocationSerializer::locationToString)
                .collect(Collectors.toList());

        config.set(path + ".spawns", spawns);
    }

    public static Arena loadFromConfig(ConfigurationSection section, String arenaName) {
        Arena arena = new Arena(arenaName);

        arena.setMinPlayers(section.getInt("minPlayers", 2));
        arena.setMaxPlayers(section.getInt("maxPlayers", 12));
        arena.setPotatoTimer(section.getInt("potatoTimer", 20));

        String lobbyStr = section.getString("lobby");
        if (lobbyStr != null) {
            arena.setLobby(LocationSerializer.stringToLocation(lobbyStr));
        }

        String spectatorStr = section.getString("spectator");
        if (spectatorStr != null) {
            arena.setSpectatorLocation(LocationSerializer.stringToLocation(spectatorStr));
        }

        section.getStringList("spawns").stream()
                .map(LocationSerializer::stringToLocation)
                .filter(location -> location != null)
                .forEach(arena::addSpawnLocation);

        return arena;
    }

    public boolean addPlayer(UUID playerId) {
        if ((state == ArenaState.WAITING || state == ArenaState.STARTING) && players.size() < maxPlayers) {
            players.add(playerId);
            return true;
        }
        return false;
    }

    public boolean removePlayer(UUID playerId) {
        return players.remove(playerId);
    }

    public boolean isPlayerInArena(UUID playerId) {
        return players.contains(playerId);
    }

    public boolean canStart() {
        return players.size() >= minPlayers && state == ArenaState.WAITING &&
                lobby != null && spectatorLocation != null && !spawnLocations.isEmpty();
    }

    public boolean isValid() {
        return lobby != null && spectatorLocation != null && !spawnLocations.isEmpty();
    }

    public String getName() {
        return name;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public Location getLobby() {
        return lobby;
    }

    public void setLobby(Location lobby) {
        this.lobby = lobby;
    }

    public Location getSpectatorLocation() {
        return spectatorLocation;
    }

    public void setSpectatorLocation(Location spectatorLocation) {
        this.spectatorLocation = spectatorLocation;
    }

    public List<Location> getSpawnLocations() {
        return Collections.unmodifiableList(spawnLocations);
    }

    public void addSpawnLocation(Location spawn) {
        this.spawnLocations.add(spawn);
    }

    public int getPotatoTimer() {
        return potatoTimer;
    }

    public void setPotatoTimer(int potatoTimer) {
        this.potatoTimer = potatoTimer;
    }

    public ArenaState getState() {
        return state;
    }

    public void setState(ArenaState state) {
        this.state = state;
    }

    public List<UUID> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public int getPlayerCount() {
        return players.size();
    }
}