package fr.zeygal.zpotato.player;

import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;

public class HPPlayer {

    private final UUID playerId;
    private int gamesPlayed;
    private int wins;
    private int explosionsProvoked;
    private boolean hasHotPotato;

    public HPPlayer(UUID playerId) {
        this.playerId = playerId;
        this.gamesPlayed = 0;
        this.wins = 0;
        this.explosionsProvoked = 0;
        this.hasHotPotato = false;
    }

    public void saveToConfig(ConfigurationSection section) {
        section.set("gamesPlayed", gamesPlayed);
        section.set("wins", wins);
        section.set("explosionsProvoked", explosionsProvoked);
    }

    public static HPPlayer loadFromConfig(ConfigurationSection section, UUID playerId) {
        HPPlayer hpPlayer = new HPPlayer(playerId);

        hpPlayer.setGamesPlayed(section.getInt("gamesPlayed", 0));
        hpPlayer.setWins(section.getInt("wins", 0));
        hpPlayer.setExplosionsProvoked(section.getInt("explosionsProvoked", 0));

        return hpPlayer;
    }

    public void incrementGamesPlayed() {
        this.gamesPlayed++;
    }

    public void incrementWins() {
        this.wins++;
    }

    public void incrementExplosionsProvoked() {
        this.explosionsProvoked++;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getExplosionsProvoked() {
        return explosionsProvoked;
    }

    public void setExplosionsProvoked(int explosionsProvoked) {
        this.explosionsProvoked = explosionsProvoked;
    }

    public boolean hasHotPotato() {
        return hasHotPotato;
    }

    public void setHasHotPotato(boolean hasHotPotato) {
        this.hasHotPotato = hasHotPotato;
    }
}