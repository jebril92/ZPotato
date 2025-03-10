package fr.zeygal.zpotato.config;

import fr.zeygal.zpotato.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MessagesManager {

    private final Main plugin;
    private FileConfiguration messagesConfig;
    private final File messagesFile;
    private final Map<String, String> messages;

    public MessagesManager(Main plugin) {
        this.plugin = plugin;
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        this.messages = new HashMap<>();
    }

    public void loadMessages() {
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        for (String key : messagesConfig.getKeys(true)) {
            if (messagesConfig.isString(key)) {
                messages.put(key, ChatColor.translateAlternateColorCodes('&', messagesConfig.getString(key)));
            }
        }
    }

    public String getMessage(String key) {
        String message = messages.getOrDefault(key, "Message not found: " + key);

        message = message.replace("{prefix}", getPrefix());

        return message;
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
        return messages.getOrDefault("prefix", "&6[&eZPotato&6]&r");
    }
}