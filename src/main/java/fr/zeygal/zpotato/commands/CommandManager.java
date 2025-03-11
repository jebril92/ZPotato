package fr.zeygal.zpotato.commands;

import fr.zeygal.zpotato.Main;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import org.bukkit.command.Command;

import fr.zeygal.zpotato.Main;
import fr.zeygal.zpotato.arena.Arena;
import fr.zeygal.zpotato.arena.ArenaState;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import org.bukkit.command.Command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandManager implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public CommandManager(Main plugin) {
        this.plugin = plugin;
    }

    public void registerCommands() {
        plugin.getCommand("hp").setExecutor(this);
        plugin.getCommand("hp").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("create")) {
            return handleCreateCommand(sender, args);
        } else if (subCommand.equals("delete")) {
            return handleDeleteCommand(sender, args);
        } else if (subCommand.equals("addspawn")) {
            return handleAddSpawnCommand(sender, args);
        } else if (subCommand.equals("setlobby")) {
            return handleSetLobbyCommand(sender, args);
        } else if (subCommand.equals("setspectate")) {
            return handleSetSpectateCommand(sender, args);
        } else if (subCommand.equals("settings")) {
            return handleSettingsCommand(sender, args);
        } else if (subCommand.equals("start")) {
            return handleStartCommand(sender, args);
        } else if (subCommand.equals("stop")) {
            return handleStopCommand(sender, args);
        } else if (subCommand.equals("setmainlobby")) {
            return handleSetMainLobbyCommand(sender, args);
        } else if (subCommand.equals("unsetmainlobby")) {
            return handleUnsetMainLobbyCommand(sender, args);
        } else if (subCommand.equals("list")) {
            return handleListCommand(sender, args);
        } else if (subCommand.equals("reload")) {
            return handleReloadCommand(sender, args);
        }

        else if (subCommand.equals("join")) {
            return handleJoinCommand(sender, args);
        } else if (subCommand.equals("leave")) {
            return handleLeaveCommand(sender, args);
        } else if (subCommand.equals("stats")) {
            return handleStatsCommand(sender, args);
        }

        else if (subCommand.equals("gui") || subCommand.equals("admin")) {
            return handleGUICommand(sender, args);
        }

        else {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.unknown"));
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>();

            if (sender.hasPermission("hotpotato.admin")) {
                subCommands.addAll(Arrays.asList(
                        "create", "delete", "addspawn", "setlobby", "setspectate",
                        "settings", "start", "stop", "setmainlobby", "unsetmainlobby",
                        "list", "reload", "gui", "admin"  // Ajout des nouvelles commandes
                ));
            }

            if (sender.hasPermission("hotpotato.user")) {
                subCommands.addAll(Arrays.asList("join", "leave", "stats"));
            }

            for (String subCommand : subCommands) {
                if (subCommand.startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if ((subCommand.equals("join") || subCommand.equals("start") || subCommand.equals("stop") ||
                    subCommand.equals("delete") || subCommand.equals("addspawn") || subCommand.equals("setlobby") ||
                    subCommand.equals("setspectate") || subCommand.equals("settings")) &&
                    (sender.hasPermission("hotpotato.admin") ||
                            (subCommand.equals("join") && sender.hasPermission("hotpotato.user")))) {

                List<String> arenaNames = new ArrayList<>(plugin.getArenaManager().getAllArenas().keySet());
                for (String arenaName : arenaNames) {
                    if (arenaName.startsWith(args[1])) {
                        completions.add(arenaName);
                    }
                }
            }

            // Autocomplétion pour les sous-commandes GUI
            if ((subCommand.equals("gui") || subCommand.equals("admin")) && sender.hasPermission("hotpotato.admin")) {
                List<String> guiSubCommands = Arrays.asList("arena", "spawns");
                for (String guiSubCommand : guiSubCommands) {
                    if (guiSubCommand.startsWith(args[1].toLowerCase())) {
                        completions.add(guiSubCommand);
                    }
                }
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            String subSubCommand = args[1].toLowerCase();

            // Autocomplétion pour les arènes dans les commandes GUI
            if ((subCommand.equals("gui") || subCommand.equals("admin")) &&
                    (subSubCommand.equals("arena") || subSubCommand.equals("spawns")) &&
                    sender.hasPermission("hotpotato.admin")) {

                List<String> arenaNames = new ArrayList<>(plugin.getArenaManager().getAllArenas().keySet());
                for (String arenaName : arenaNames) {
                    if (arenaName.startsWith(args[2])) {
                        completions.add(arenaName);
                    }
                }
            }
        }

        return completions;
    }

    private boolean handleGUICommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.player-only"));
            return true;
        }

        if (!sender.hasPermission("hotpotato.admin")) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("permission.denied"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length >= 2) {
            String guiSubCommand = args[1].toLowerCase();

            if (guiSubCommand.equals("arena")) {
                if (args.length >= 3) {
                    String arenaName = args[2];
                    Arena arena = plugin.getArenaManager().getArena(arenaName);

                    if (arena == null) {
                        sender.sendMessage(plugin.getMessagesManager().getMessage("command.arena-not-found").replace("{arena}", arenaName));
                        return true;
                    }

                    plugin.getGUIManager().openGUI(player, "arena_settings", arenaName);
                    return true;
                } else {
                    sender.sendMessage(plugin.getMessagesManager().getPrefix() + " §cUtilisation: §e/hp " + args[0] + " arena <nom_arène>");
                    return true;
                }
            } else if (guiSubCommand.equals("spawns")) {
                if (args.length >= 3) {
                    String arenaName = args[2];
                    Arena arena = plugin.getArenaManager().getArena(arenaName);

                    if (arena == null) {
                        sender.sendMessage(plugin.getMessagesManager().getMessage("command.arena-not-found").replace("{arena}", arenaName));
                        return true;
                    }

                    plugin.getGUIManager().openGUI(player, "spawn_manager", arenaName);
                    return true;
                } else {
                    sender.sendMessage(plugin.getMessagesManager().getPrefix() + " §cUtilisation: §e/hp " + args[0] + " spawns <nom_arène>");
                    return true;
                }
            }
        }

        // Par défaut, ouvrir la liste des arènes
        plugin.getGUIManager().openGUI(player, "arena_list");
        return true;
    }



    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.getMessagesManager().getPrefix() + " §6Hot Potato Commands");

        if (sender.hasPermission("hotpotato.user")) {
            sender.sendMessage("§e/hp join <arena> §7- Join an arena");
            sender.sendMessage("§e/hp leave §7- Leave the current arena");
            sender.sendMessage("§e/hp stats §7- View your stats");
        }

        if (sender.hasPermission("hotpotato.admin")) {
            sender.sendMessage("§c/hp create <arena> [min] [max] §7- Create a new arena");
            sender.sendMessage("§c/hp delete <arena> §7- Delete an arena");
            sender.sendMessage("§c/hp addspawn <arena> §7- Add a spawn point to an arena");
            sender.sendMessage("§c/hp setlobby <arena> §7- Set the lobby for an arena");
            sender.sendMessage("§c/hp setspectate <arena> §7- Set the spectator area for an arena");
            sender.sendMessage("§c/hp settings <arena> §7- Configure arena settings");
            sender.sendMessage("§c/hp start [arena] §7- Start a game");
            sender.sendMessage("§c/hp stop [arena] §7- Stop a game");
            sender.sendMessage("§c/hp setmainlobby §7- Set the main lobby");
            sender.sendMessage("§c/hp unsetmainlobby §7- Unset the main lobby");
            sender.sendMessage("§c/hp list §7- List all arenas");
            sender.sendMessage("§c/hp reload §7- Reload the plugin");
            sender.sendMessage("§c/hp gui §7- Open the GUI arena manager");
            sender.sendMessage("§c/hp admin §7- Alias for gui command");
        }
    }


    private boolean handleCreateCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hotpotato.admin")) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("permission.denied"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.create.usage"));
            return true;
        }

        String arenaName = args[1];
        int minPlayers = 2;
        int maxPlayers = 12;

        if (args.length >= 3) {
            try {
                minPlayers = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getMessagesManager().getMessage("command.create.invalid-min"));
                return true;
            }
        }

        if (args.length >= 4) {
            try {
                maxPlayers = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getMessagesManager().getMessage("command.create.invalid-max"));
                return true;
            }
        }

        if (minPlayers <= 0 || maxPlayers <= 0 || minPlayers > maxPlayers) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.create.invalid-range"));
            return true;
        }

        boolean created = plugin.getArenaManager().createArena(arenaName, minPlayers, maxPlayers);

        if (created) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.create.success").replace("{arena}", arenaName));
        } else {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.create.already-exists").replace("{arena}", arenaName));
        }

        return true;
    }

    private boolean handleDeleteCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hotpotato.admin")) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("permission.denied"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.delete.usage"));
            return true;
        }

        String arenaName = args[1];

        boolean deleted = plugin.getArenaManager().deleteArena(arenaName);

        if (deleted) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.delete.success").replace("{arena}", arenaName));
        } else {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.delete.not-found").replace("{arena}", arenaName));
        }

        return true;
    }

    private boolean handleAddSpawnCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.player-only"));
            return true;
        }

        if (!sender.hasPermission("hotpotato.admin")) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("permission.denied"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.addspawn.usage"));
            return true;
        }

        String arenaName = args[1];
        Player player = (Player) sender;

        if (plugin.getArenaManager().getArena(arenaName) == null) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.arena-not-found").replace("{arena}", arenaName));
            return true;
        }

        plugin.getArenaManager().getArena(arenaName).addSpawnLocation(player.getLocation());
        plugin.getArenaManager().saveArenas();

        sender.sendMessage(plugin.getMessagesManager().getMessage("command.addspawn.success").replace("{arena}", arenaName));

        return true;
    }

    private boolean handleSetLobbyCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.player-only"));
            return true;
        }

        if (!sender.hasPermission("hotpotato.admin")) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("permission.denied"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.setlobby.usage"));
            return true;
        }

        String arenaName = args[1];
        Player player = (Player) sender;

        if (plugin.getArenaManager().getArena(arenaName) == null) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.arena-not-found").replace("{arena}", arenaName));
            return true;
        }

        plugin.getArenaManager().getArena(arenaName).setLobby(player.getLocation());
        plugin.getArenaManager().saveArenas();

        sender.sendMessage(plugin.getMessagesManager().getMessage("command.setlobby.success").replace("{arena}", arenaName));

        return true;
    }

    private boolean handleSetSpectateCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.player-only"));
            return true;
        }

        if (!sender.hasPermission("hotpotato.admin")) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("permission.denied"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.setspectate.usage"));
            return true;
        }

        String arenaName = args[1];
        Player player = (Player) sender;

        if (plugin.getArenaManager().getArena(arenaName) == null) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.arena-not-found").replace("{arena}", arenaName));
            return true;
        }

        plugin.getArenaManager().getArena(arenaName).setSpectatorLocation(player.getLocation());
        plugin.getArenaManager().saveArenas();

        sender.sendMessage(plugin.getMessagesManager().getMessage("command.setspectate.success").replace("{arena}", arenaName));

        return true;
    }

    private boolean handleSettingsCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hotpotato.admin")) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("permission.denied"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.settings.usage"));
            return true;
        }

        String arenaName = args[1];

        if (plugin.getArenaManager().getArena(arenaName) == null) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.arena-not-found").replace("{arena}", arenaName));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(plugin.getMessagesManager().getPrefix() + " §6Arena Settings: §e" + arenaName);
            sender.sendMessage("§7Min Players: §e" + plugin.getArenaManager().getArena(arenaName).getMinPlayers());
            sender.sendMessage("§7Max Players: §e" + plugin.getArenaManager().getArena(arenaName).getMaxPlayers());
            sender.sendMessage("§7Potato Timer: §e" + plugin.getArenaManager().getArena(arenaName).getPotatoTimer() + "s");

            sender.sendMessage("§6Available settings:");
            sender.sendMessage("§e/hp settings " + arenaName + " minplayers <amount>");
            sender.sendMessage("§e/hp settings " + arenaName + " maxplayers <amount>");
            sender.sendMessage("§e/hp settings " + arenaName + " potatotimer <seconds>");

            return true;
        }

        String setting = args[2].toLowerCase();

        if (setting.equals("minplayers")) {
            if (args.length < 4) {
                sender.sendMessage(plugin.getMessagesManager().getMessage("command.settings.minplayers.usage"));
                return true;
            }

            try {
                int minPlayers = Integer.parseInt(args[3]);

                if (minPlayers <= 0 || minPlayers > plugin.getArenaManager().getArena(arenaName).getMaxPlayers()) {
                    sender.sendMessage(plugin.getMessagesManager().getMessage("command.settings.minplayers.invalid"));
                    return true;
                }

                plugin.getArenaManager().getArena(arenaName).setMinPlayers(minPlayers);
                plugin.getArenaManager().saveArenas();

                sender.sendMessage(plugin.getMessagesManager().getMessage("command.settings.minplayers.success")
                        .replace("{arena}", arenaName)
                        .replace("{amount}", String.valueOf(minPlayers)));

            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getMessagesManager().getMessage("command.settings.minplayers.invalid"));
            }

        } else if (setting.equals("maxplayers")) {
            if (args.length < 4) {
                sender.sendMessage(plugin.getMessagesManager().getMessage("command.settings.maxplayers.usage"));
                return true;
            }

            try {
                int maxPlayers = Integer.parseInt(args[3]);

                if (maxPlayers <= 0 || maxPlayers < plugin.getArenaManager().getArena(arenaName).getMinPlayers()) {
                    sender.sendMessage(plugin.getMessagesManager().getMessage("command.settings.maxplayers.invalid"));
                    return true;
                }

                plugin.getArenaManager().getArena(arenaName).setMaxPlayers(maxPlayers);
                plugin.getArenaManager().saveArenas();

                sender.sendMessage(plugin.getMessagesManager().getMessage("command.settings.maxplayers.success")
                        .replace("{arena}", arenaName)
                        .replace("{amount}", String.valueOf(maxPlayers)));

            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getMessagesManager().getMessage("command.settings.maxplayers.invalid"));
            }

        } else if (setting.equals("potatotimer")) {
            if (args.length < 4) {
                sender.sendMessage(plugin.getMessagesManager().getMessage("command.settings.potatotimer.usage"));
                return true;
            }

            try {
                int potatoTimer = Integer.parseInt(args[3]);

                if (potatoTimer <= 0) {
                    sender.sendMessage(plugin.getMessagesManager().getMessage("command.settings.potatotimer.invalid"));
                    return true;
                }

                plugin.getArenaManager().getArena(arenaName).setPotatoTimer(potatoTimer);
                plugin.getArenaManager().saveArenas();

                sender.sendMessage(plugin.getMessagesManager().getMessage("command.settings.potatotimer.success")
                        .replace("{arena}", arenaName)
                        .replace("{time}", String.valueOf(potatoTimer)));

            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getMessagesManager().getMessage("command.settings.potatotimer.invalid"));
            }

        } else {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.settings.unknown"));
        }

        return true;
    }

    private boolean handleStartCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hotpotato.admin")) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("permission.denied"));
            return true;
        }

        String arenaName;

        if (args.length < 2) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getMessagesManager().getMessage("command.start.specify-arena"));
                return true;
            }

            Player player = (Player) sender;
            if (plugin.getArenaManager().isPlayerInAnyArena(player.getUniqueId())) {
                arenaName = plugin.getArenaManager().getPlayerArena(player.getUniqueId()).getName();
            } else {
                sender.sendMessage(plugin.getMessagesManager().getMessage("command.start.not-in-arena"));
                return true;
            }
        } else {
            arenaName = args[1];
        }

        if (plugin.getArenaManager().getArena(arenaName) == null) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.arena-not-found").replace("{arena}", arenaName));
            return true;
        }

        boolean started = plugin.getGameManager().startGame(arenaName);

        if (started) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.start.success").replace("{arena}", arenaName));
        } else {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.start.failed").replace("{arena}", arenaName));
        }

        return true;
    }

    private boolean handleStopCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hotpotato.admin")) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("permission.denied"));
            return true;
        }

        String arenaName;

        if (args.length < 2) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getMessagesManager().getMessage("command.stop.specify-arena"));
                return true;
            }

            Player player = (Player) sender;
            if (plugin.getArenaManager().isPlayerInAnyArena(player.getUniqueId())) {
                arenaName = plugin.getArenaManager().getPlayerArena(player.getUniqueId()).getName();
            } else {
                sender.sendMessage(plugin.getMessagesManager().getMessage("command.stop.not-in-arena"));
                return true;
            }
        } else {
            arenaName = args[1];
        }

        if (plugin.getArenaManager().getArena(arenaName) == null) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.arena-not-found").replace("{arena}", arenaName));
            return true;
        }

        boolean stopped = plugin.getGameManager().stopGame(arenaName);

        if (stopped) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.stop.success").replace("{arena}", arenaName));
        } else {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.stop.not-running").replace("{arena}", arenaName));
        }

        return true;
    }

    private boolean handleSetMainLobbyCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.player-only"));
            return true;
        }

        if (!sender.hasPermission("hotpotato.admin")) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("permission.denied"));
            return true;
        }

        Player player = (Player) sender;
        plugin.getConfigManager().setMainLobby(player.getLocation());

        sender.sendMessage(plugin.getMessagesManager().getMessage("command.setmainlobby.success"));

        return true;
    }

    private boolean handleUnsetMainLobbyCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hotpotato.admin")) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("permission.denied"));
            return true;
        }

        plugin.getConfigManager().unsetMainLobby();

        sender.sendMessage(plugin.getMessagesManager().getMessage("command.unsetmainlobby.success"));

        return true;
    }

    private boolean handleListCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hotpotato.admin")) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("permission.denied"));
            return true;
        }

        sender.sendMessage(plugin.getMessagesManager().getPrefix() + " §6Arena List:");

        if (plugin.getArenaManager().getAllArenas().isEmpty()) {
            sender.sendMessage("§7No arenas found.");
            return true;
        }

        for (String arenaName : plugin.getArenaManager().getAllArenas().keySet()) {
            StringBuilder arenaInfo = new StringBuilder("§e" + arenaName + " §7- ");

            arenaInfo.append("State: §")
                    .append(plugin.getArenaManager().getArena(arenaName).getState() == fr.zeygal.zpotato.arena.ArenaState.RUNNING ? "a" : "c")
                    .append(plugin.getArenaManager().getArena(arenaName).getState().name());

            arenaInfo.append("§7, Players: §e")
                    .append(plugin.getArenaManager().getArena(arenaName).getPlayerCount())
                    .append("/")
                    .append(plugin.getArenaManager().getArena(arenaName).getMaxPlayers());

            arenaInfo.append("§7, Valid: §")
                    .append(plugin.getArenaManager().getArena(arenaName).isValid() ? "a✓" : "c✗");

            sender.sendMessage(arenaInfo.toString());
        }

        return true;
    }

    private boolean handleReloadCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hotpotato.admin")) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("permission.denied"));
            return true;
        }

        plugin.getGameManager().stopAllGames();

        plugin.getConfigManager().loadConfig();
        plugin.getMessagesManager().loadMessages();
        plugin.getArenaManager().loadArenas();

        sender.sendMessage(plugin.getMessagesManager().getMessage("command.reload.success"));

        return true;
    }


    private boolean handleJoinCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.player-only"));
            return true;
        }

        if (!sender.hasPermission("hotpotato.user")) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("permission.denied"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.join.usage"));
            return true;
        }

        String arenaName = args[1];
        Player player = (Player) sender;

        if (plugin.getArenaManager().getArena(arenaName) == null) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.arena-not-found").replace("{arena}", arenaName));
            return true;
        }

        if (plugin.getArenaManager().isPlayerInAnyArena(player.getUniqueId())) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.join.already-in-arena"));
            return true;
        }

        boolean joined = plugin.getArenaManager().addPlayerToArena(player, arenaName);

        if (joined) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.join.success").replace("{arena}", arenaName));
        } else {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.join.failed").replace("{arena}", arenaName));
        }

        return true;
    }

    private boolean handleLeaveCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.player-only"));
            return true;
        }

        if (!sender.hasPermission("hotpotato.user")) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("permission.denied"));
            return true;
        }

        Player player = (Player) sender;

        if (!plugin.getArenaManager().isPlayerInAnyArena(player.getUniqueId())) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.leave.not-in-arena"));
            return true;
        }

        String arenaName = plugin.getArenaManager().getPlayerArena(player.getUniqueId()).getName();
        boolean left = plugin.getArenaManager().removePlayerFromArena(player);

        if (left) {
            if (plugin.getConfigManager().getMainLobby() != null) {
                player.teleport(plugin.getConfigManager().getMainLobby());
            }

            sender.sendMessage(plugin.getMessagesManager().getMessage("command.leave.success").replace("{arena}", arenaName));
        } else {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.leave.failed"));
        }

        return true;
    }

    private boolean handleStatsCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.player-only"));
            return true;
        }

        if (!sender.hasPermission("hotpotato.user")) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("permission.denied"));
            return true;
        }

        Player player = (Player) sender;

        int gamesPlayed = plugin.getPlayerManager().getGamesPlayed(player.getUniqueId());
        int wins = plugin.getPlayerManager().getWins(player.getUniqueId());
        int explosionsProvoked = plugin.getPlayerManager().getExplosionsProvoked(player.getUniqueId());

        sender.sendMessage(plugin.getMessagesManager().getPrefix() + " §6Your Stats:");
        sender.sendMessage("§7Games Played: §e" + gamesPlayed);
        sender.sendMessage("§7Wins: §e" + wins);
        sender.sendMessage("§7Explosions Provoked: §e" + explosionsProvoked);

        if (gamesPlayed > 0) {
            double winRate = (double) wins / gamesPlayed * 100;
            sender.sendMessage("§7Win Rate: §e" + String.format("%.2f", winRate) + "%");
        }

        return true;
    }
}