package fr.zeygal.zpotato;

import fr.zeygal.zpotato.arena.ArenaManager;
import fr.zeygal.zpotato.commands.CommandManager;
import fr.zeygal.zpotato.config.ConfigManager;
import fr.zeygal.zpotato.config.MessagesManager;
import fr.zeygal.zpotato.game.GameManager;
import fr.zeygal.zpotato.gui.GUIListener;
import fr.zeygal.zpotato.gui.GUIManager;
import fr.zeygal.zpotato.listeners.GameListener;
import fr.zeygal.zpotato.listeners.PlayerListener;
import fr.zeygal.zpotato.placeholders.ZPotatoPlaceholderExpansion;
import fr.zeygal.zpotato.player.PlayerManager;
import fr.zeygal.zpotato.scoreboard.ScoreboardManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main instance;
    private ConfigManager configManager;
    private MessagesManager messagesManager;
    private ArenaManager arenaManager;
    private PlayerManager playerManager;
    private GameManager gameManager;
    private CommandManager commandManager;
    private GUIManager guiManager;
    private ScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        instance = this;

        this.configManager = new ConfigManager(this);
        this.messagesManager = new MessagesManager(this);
        this.arenaManager = new ArenaManager(this);
        this.playerManager = new PlayerManager(this);
        this.gameManager = new GameManager(this);
        this.guiManager = new GUIManager(this);

        this.configManager.loadConfig();
        this.messagesManager.loadMessages();
        this.arenaManager.loadArenas();

        this.scoreboardManager = new ScoreboardManager(this);

        this.commandManager = new CommandManager(this);
        this.commandManager.registerCommands();

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new GameListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("PlaceholderAPI found, registering placeholders...");
            new ZPotatoPlaceholderExpansion(this).register();
            getLogger().info("ZPotato placeholders registered successfully!");
        } else {
            getLogger().info("PlaceholderAPI not found, placeholders will not be available.");
        }

        getLogger().info("ZPotato plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        if (gameManager != null) {
            gameManager.stopAllGames();
        }

        if (arenaManager != null) {
            arenaManager.saveArenas();
        }

        if (playerManager != null) {
            playerManager.savePlayers();
        }

        if (scoreboardManager != null) {
            scoreboardManager.shutdown();
        }

        getLogger().info("ZPotato plugin has been disabled!");
    }

    public static Main getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessagesManager getMessagesManager() {
        return messagesManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public GUIManager getGUIManager() {
        return guiManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
}