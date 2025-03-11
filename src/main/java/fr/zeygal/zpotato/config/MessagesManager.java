package fr.zeygal.zpotato.config;

import fr.zeygal.zpotato.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessagesManager {

    private final Main plugin;
    private FileConfiguration messagesConfig;
    private final File messagesFile;
    private final Map<String, String> messages;
    private String prefix;

    public MessagesManager(Main plugin) {
        this.plugin = plugin;
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        this.messages = new ConcurrentHashMap<>();
    }

    public void loadMessages() {
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        messages.clear();

        messagesConfig.getKeys(true).stream()
                .filter(key -> messagesConfig.isString(key))
                .forEach(key -> messages.put(key, ChatColor.translateAlternateColorCodes('&', messagesConfig.getString(key))));

        prefix = messages.getOrDefault("prefix", "&6[&eZPotato&6]&r");
    }

    public String getMessage(String key) {
        String message = messages.getOrDefault(key, "Message not found: " + key);
        return message.replace("{prefix}", getPrefix());
    }

    public String getMessage(String key, Map<String, String> replacements) {
        String message = getMessage(key);

        if (replacements != null) {
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        return message;
    }

    public void setMessage(String key, String value) {
        messagesConfig.set(key, value);
        messages.put(key, ChatColor.translateAlternateColorCodes('&', value));

        try {
            messagesConfig.save(messagesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save messages: " + e.getMessage());
        }
    }

    public String getPrefix() {
        return prefix;
    }
}