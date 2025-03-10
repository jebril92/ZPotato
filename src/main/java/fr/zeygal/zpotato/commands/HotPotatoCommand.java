package fr.zeygal.zpotato.commands;

import fr.zeygal.zpotato.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import java.util.List;

public class HotPotatoCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public HotPotatoCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return plugin.getCommand("hp").getExecutor().onCommand(sender, command, label, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return plugin.getCommand("hp").getTabCompleter().onTabComplete(sender, command, alias, args);
    }
}