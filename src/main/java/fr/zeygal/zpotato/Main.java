package fr.zeygal.zpotato;

import fr.zeygal.zpotato.arena.ArenaManager;
import fr.zeygal.zpotato.commands.CommandManager;
import fr.zeygal.zpotato.config.ConfigManager;
import fr.zeygal.zpotato.config.MessagesManager;
import fr.zeygal.zpotato.game.GameManager;
import fr.zeygal.zpotato.listeners.GameListener;
import fr.zeygal.zpotato.listeners.PlayerListener;
import fr.zeygal.zpotato.player.PlayerManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main instance;
    private ConfigManager configManager;
    private MessagesManager messagesManager;
    private ArenaManager arenaManager;
    private PlayerManager playerManager;
    private GameManager gameManager;
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        instance = this;

        this.configManager = new ConfigManager(this);
        this.messagesManager = new MessagesManager(this);
        this.arenaManager = new ArenaManager(this);
        this.playerManager = new PlayerManager(this);
        this.gameManager = new GameManager(this);

        this.configManager.loadConfig();
        this.messagesManager.loadMessages();
        this.arenaManager.loadArenas();

        this.commandManager = new CommandManager(this);
        this.commandManager.registerCommands();

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new GameListener(this), this);

        getLogger().info("ZPotato plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        if (arenaManager != null) {
            arenaManager.saveArenas();
        }

        if (gameManager != null) {
            gameManager.stopAllGames();
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
}