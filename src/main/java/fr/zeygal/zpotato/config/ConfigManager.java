package fr.zeygal.zpotato.config;

import fr.zeygal.zpotato.Main;
import fr.zeygal.zpotato.utils.LocationSerializer;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final Main plugin;
    private FileConfiguration config;
    private Location mainLobby;
    private int defaultPotatoTimer;
    private int countdownTime;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();

        plugin.reloadConfig();
        config = plugin.getConfig();

        String mainLobbyStr = config.getString("main-lobby");
        if (mainLobbyStr != null) {
            mainLobby = LocationSerializer.stringToLocation(mainLobbyStr);
        }

        defaultPotatoTimer = config.getInt("default-potato-timer", 20);
        countdownTime = config.getInt("countdown-time", 30);

    }

    public void saveConfig() {
        if (mainLobby != null) {
            config.set("main-lobby", LocationSerializer.locationToString(mainLobby));
        }

        config.set("default-potato-timer", defaultPotatoTimer);
        config.set("countdown-time", countdownTime);

        plugin.saveConfig();
    }

    public void setMainLobby(Location location) {
        this.mainLobby = location;
        saveConfig();
    }

    public void unsetMainLobby() {
        this.mainLobby = null;
        config.set("main-lobby", null);
        saveConfig();
    }

    public Location getMainLobby() {
        return mainLobby;
    }

    public int getDefaultPotatoTimer() {
        return defaultPotatoTimer;
    }

    public void setDefaultPotatoTimer(int defaultPotatoTimer) {
        this.defaultPotatoTimer = defaultPotatoTimer;
        saveConfig();
    }

    public int getCountdownTime() {
        return countdownTime;
    }

    public void setCountdownTime(int countdownTime) {
        this.countdownTime = countdownTime;
        saveConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }
}