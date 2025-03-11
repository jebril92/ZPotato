package fr.zeygal.zpotato.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MessageUtils {

    public static String colorize(String message) {
        if (message == null) {
            return null;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static List<String> colorize(List<String> messages) {
        return messages.stream()
                .map(MessageUtils::colorize)
                .collect(Collectors.toList());
    }

    public static String format(String message, Map<String, String> replacements) {
        if (message == null || replacements == null) {
            return message;
        }

        String formatted = message;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            formatted = formatted.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return formatted;
    }

    public static void sendMessage(Player player, String message) {
        if (player != null && message != null) {
            player.sendMessage(colorize(message));
        }
    }

    public static void sendMessage(Player player, String message, Map<String, String> replacements) {
        if (player != null && message != null) {
            player.sendMessage(colorize(format(message, replacements)));
        }
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (player != null) {
            player.sendTitle(
                    colorize(title),
                    colorize(subtitle),
                    fadeIn,
                    stay,
                    fadeOut
            );
        }
    }

    public static void sendActionBar(Player player, String message) {
        if (player != null && message != null) {
            player.spigot().sendMessage(
                    net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                    net.md_5.bungee.api.chat.TextComponent.fromLegacyText(colorize(message))
            );
        }
    }
}