package fr.zeygal.zpotato.commands;

import fr.zeygal.zpotato.Main;
import fr.zeygal.zpotato.arena.Arena;
import fr.zeygal.zpotato.arena.ArenaState;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CommandManager implements CommandExecutor, TabCompleter {

    private final Main plugin;
    private static final List<String> ADMIN_COMMANDS = Arrays.asList(
            "create", "delete", "addspawn", "setlobby", "setspectate",
            "settings", "start", "stop", "setmainlobby", "unsetmainlobby",
            "list", "reload", "gui", "admin"
    );
    private static final List<String> USER_COMMANDS = Arrays.asList(
            "join", "leave", "stats", "scoreboard"
    );

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

        switch (subCommand) {
            case "create":
                return handleCreateCommand(sender, args);
            case "delete":
                return handleDeleteCommand(sender, args);
            case "addspawn":
                return handleAddSpawnCommand(sender, args);
            case "setlobby":
                return handleSetLobbyCommand(sender, args);
            case "setspectate":
                return handleSetSpectateCommand(sender, args);
            case "settings":
                return handleSettingsCommand(sender, args);
            case "start":
                return handleStartCommand(sender, args);
            case "stop":
                return handleStopCommand(sender, args);
            case "setmainlobby":
                return handleSetMainLobbyCommand(sender, args);
            case "unsetmainlobby":
                return handleUnsetMainLobbyCommand(sender, args);
            case "list":
                return handleListCommand(sender, args);
            case "reload":
                return handleReloadCommand(sender, args);
            case "join":
                return handleJoinCommand(sender, args);
            case "leave":
                return handleLeaveCommand(sender, args);
            case "stats":
                return handleStatsCommand(sender, args);
            case "gui":
            case "admin":
                return handleGUICommand(sender, args);
            case "scoreboard":
                return handleScoreboardCommand(sender, args);
            default:
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
                subCommands.addAll(ADMIN_COMMANDS);
            }

            if (sender.hasPermission("hotpotato.user")) {
                subCommands.addAll(USER_COMMANDS);
            }

            if (sender.hasPermission("hotpotato.scoreboard.user")) {
                subCommands.add("scoreboard");
            }

            String prefix = args[0].toLowerCase();
            completions.addAll(subCommands.stream()
                    .filter(cmd -> cmd.startsWith(prefix))
                    .collect(Collectors.toList()));

        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            String prefix = args[1];

            if ((subCommand.equals("join") || subCommand.equals("start") || subCommand.equals("stop") ||
                    subCommand.equals("delete") || subCommand.equals("addspawn") || subCommand.equals("setlobby") ||
                    subCommand.equals("setspectate") || subCommand.equals("settings")) &&
                    (sender.hasPermission("hotpotato.admin") ||
                            (subCommand.equals("join") && sender.hasPermission("hotpotato.user")))) {

                completions.addAll(plugin.getArenaManager().getAllArenas().keySet().stream()
                        .filter(arena -> arena.startsWith(prefix))
                        .collect(Collectors.toList()));
            }

            if ((subCommand.equals("gui") || subCommand.equals("admin")) && sender.hasPermission("hotpotato.admin")) {
                List<String> guiSubCommands = Arrays.asList("arena", "spawns");
                completions.addAll(guiSubCommands.stream()
                        .filter(cmd -> cmd.startsWith(prefix.toLowerCase()))
                        .collect(Collectors.toList()));
            }

            if (subCommand.equals("scoreboard") && sender.hasPermission("hotpotato.scoreboard.user")) {
                List<String> scoreboardOptions = Arrays.asList("on", "off");
                completions.addAll(scoreboardOptions.stream()
                        .filter(option -> option.startsWith(prefix.toLowerCase()))
                        .collect(Collectors.toList()));
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            String subSubCommand = args[1].toLowerCase();
            String prefix = args[2];

            if ((subCommand.equals("gui") || subCommand.equals("admin")) &&
                    (subSubCommand.equals("arena") || subSubCommand.equals("spawns")) &&
                    sender.hasPermission("hotpotato.admin")) {

                completions.addAll(plugin.getArenaManager().getAllArenas().keySet().stream()
                        .filter(arena -> arena.startsWith(prefix))
                        .collect(Collectors.toList()));
            }
        }

        return completions;
    }

    private boolean handleScoreboardCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.player-only"));
            return true;
        }

        if (!sender.hasPermission("hotpotato.scoreboard.user")) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("permission.denied"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            sender.sendMessage(plugin.getMessagesManager().getPrefix() + " §cUsage: §e/hp scoreboard <on|off>");
            return true;
        }

        String option = args[1].toLowerCase();

        if (option.equals("on")) {
            if (plugin.getArenaManager().isPlayerInAnyArena(player.getUniqueId())) {
                Arena arena = plugin.getArenaManager().getPlayerArena(player.getUniqueId());
                plugin.getScoreboardManager().updateScoreboard(player, arena);
                sender.sendMessage(plugin.getMessagesManager().getPrefix() + " §aLe scoreboard a été activé.");
            } else {
                sender.sendMessage(plugin.getMessagesManager().getPrefix() + " §cVous devez être dans une arène pour activer le scoreboard.");
            }
            return true;
        } else if (option.equals("off")) {
            plugin.getScoreboardManager().removeScoreboard(player);
            sender.sendMessage(plugin.getMessagesManager().getPrefix() + " §aLe scoreboard a été désactivé.");
            return true;
        } else {
            sender.sendMessage(plugin.getMessagesManager().getPrefix() + " §cOption invalide. Utilisez §e/hp scoreboard <on|off>");
            return true;
        }
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

            if (guiSubCommand.equals("arena") || guiSubCommand.equals("spawns")) {
                if (args.length < 3) {
                    sender.sendMessage(plugin.getMessagesManager().getPrefix() + " §cUsage: §e/hp " + args[0] + " " + guiSubCommand + " <arena_name>");
                    return true;
                }

                String arenaName = args[2];
                Arena arena = plugin.getArenaManager().getArena(arenaName);

                if (arena == null) {
                    sender.sendMessage(plugin.getMessagesManager().getMessage("command.arena-not-found").replace("{arena}", arenaName));
                    return true;
                }

                String guiType = guiSubCommand.equals("arena") ? "arena_settings" : "spawn_manager";
                plugin.getGUIManager().openGUI(player, guiType, arenaName);
                return true;
            }
        }

        plugin.getGUIManager().openGUI(player, "arena_list");
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.getMessagesManager().getPrefix() + " §6Hot Potato Commands");

        if (sender.hasPermission("hotpotato.user")) {
            sender.sendMessage("§e/hp join <arena> §7- Join an arena");
            sender.sendMessage("§e/hp leave §7- Leave the current arena");
            sender.sendMessage("§e/hp stats §7- View your stats");
            sender.sendMessage("§e/hp scoreboard <on|off> §7- Toggle scoreboard visibility");
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
        sender.sendMessage(plugin.getMessagesManager().getMessage(
                        created ? "command.create.success" : "command.create.already-exists")
                .replace("{arena}", arenaName));

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

        sender.sendMessage(plugin.getMessagesManager().getMessage(
                        deleted ? "command.delete.success" : "command.delete.not-found")
                .replace("{arena}", arenaName));

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
        Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.arena-not-found").replace("{arena}", arenaName));
            return true;
        }

        arena.addSpawnLocation(player.getLocation());
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
        Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.arena-not-found").replace("{arena}", arenaName));
            return true;
        }

        arena.setLobby(player.getLocation());
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
        Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.arena-not-found").replace("{arena}", arenaName));
            return true;
        }

        arena.setSpectatorLocation(player.getLocation());
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
        Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.arena-not-found").replace("{arena}", arenaName));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(plugin.getMessagesManager().getPrefix() + " §6Arena Settings: §e" + arenaName);
            sender.sendMessage("§7Min Players: §e" + arena.getMinPlayers());
            sender.sendMessage("§7Max Players: §e" + arena.getMaxPlayers());
            sender.sendMessage("§7Potato Timer: §e" + arena.getPotatoTimer() + "s");

            sender.sendMessage("§6Available settings:");
            sender.sendMessage("§e/hp settings " + arenaName + " minplayers <amount>");
            sender.sendMessage("§e/hp settings " + arenaName + " maxplayers <amount>");
            sender.sendMessage("§e/hp settings " + arenaName + " potatotimer <seconds>");

            return true;
        }

        String setting = args[2].toLowerCase();

        switch (setting) {
            case "minplayers":
                return handleMinPlayersSettings(sender, args, arena, arenaName);
            case "maxplayers":
                return handleMaxPlayersSettings(sender, args, arena, arenaName);
            case "potatotimer":
                return handlePotatoTimerSettings(sender, args, arena, arenaName);
            default:
                sender.sendMessage(plugin.getMessagesManager().getMessage("command.settings.unknown"));
                return true;
        }
    }

    private boolean handleMinPlayersSettings(CommandSender sender, String[] args, Arena arena, String arenaName) {
        if (args.length < 4) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.settings.minplayers.usage"));
            return true;
        }

        try {
            int minPlayers = Integer.parseInt(args[3]);

            if (minPlayers <= 0 || minPlayers > arena.getMaxPlayers()) {
                sender.sendMessage(plugin.getMessagesManager().getMessage("command.settings.minplayers.invalid"));
                return true;
            }

            arena.setMinPlayers(minPlayers);
            plugin.getArenaManager().saveArenas();

            sender.sendMessage(plugin.getMessagesManager().getMessage("command.settings.minplayers.success")
                    .replace("{arena}", arenaName)
                    .replace("{amount}", String.valueOf(minPlayers)));

        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.settings.minplayers.invalid"));
        }

        return true;
    }

    private boolean handleMaxPlayersSettings(CommandSender sender, String[] args, Arena arena, String arenaName) {
        if (args.length < 4) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.settings.maxplayers.usage"));
            return true;
        }

        try {
            int maxPlayers = Integer.parseInt(args[3]);

            if (maxPlayers <= 0 || maxPlayers < arena.getMinPlayers()) {
                sender.sendMessage(plugin.getMessagesManager().getMessage("command.settings.maxplayers.invalid"));
                return true;
            }

            arena.setMaxPlayers(maxPlayers);
            plugin.getArenaManager().saveArenas();

            sender.sendMessage(plugin.getMessagesManager().getMessage("command.settings.maxplayers.success")
                    .replace("{arena}", arenaName)
                    .replace("{amount}", String.valueOf(maxPlayers)));

        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.settings.maxplayers.invalid"));
        }

        return true;
    }

    private boolean handlePotatoTimerSettings(CommandSender sender, String[] args, Arena arena, String arenaName) {
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

            arena.setPotatoTimer(potatoTimer);
            plugin.getArenaManager().saveArenas();

            sender.sendMessage(plugin.getMessagesManager().getMessage("command.settings.potatotimer.success")
                    .replace("{arena}", arenaName)
                    .replace("{time}", String.valueOf(potatoTimer)));

        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.settings.potatotimer.invalid"));
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
        sender.sendMessage(plugin.getMessagesManager().getMessage(
                        started ? "command.start.success" : "command.start.failed")
                .replace("{arena}", arenaName));

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
        sender.sendMessage(plugin.getMessagesManager().getMessage(
                        stopped ? "command.stop.success" : "command.stop.not-running")
                .replace("{arena}", arenaName));

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
            Arena arena = plugin.getArenaManager().getArena(arenaName);
            StringBuilder arenaInfo = new StringBuilder("§e" + arenaName + " §7- ");

            String stateColor = arena.getState() == ArenaState.RUNNING ? "a" : "c";
            arenaInfo.append("State: §").append(stateColor).append(arena.getState().name());

            arenaInfo.append("§7, Players: §e")
                    .append(arena.getPlayerCount())
                    .append("/")
                    .append(arena.getMaxPlayers());

            arenaInfo.append("§7, Valid: §")
                    .append(arena.isValid() ? "a✓" : "c✗");

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

        Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.arena-not-found").replace("{arena}", arenaName));
            return true;
        }

        if (plugin.getArenaManager().isPlayerInAnyArena(player.getUniqueId())) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.join.already-in-arena"));
            return true;
        }

        if (!arena.isValid()) {
            if (player.hasPermission("hotpotato.admin")) {
                StringBuilder details = new StringBuilder();
                if (arena.getLobby() == null) details.append("lobby");
                if (arena.getSpectatorLocation() == null) {
                    if (details.length() > 0) details.append(", ");
                    details.append("spectator");
                }
                if (arena.getSpawnLocations().isEmpty()) {
                    if (details.length() > 0) details.append(", ");
                    details.append("spawns");
                }

                sender.sendMessage(plugin.getMessagesManager().getMessage("command.join.invalid-arena-admin")
                        .replace("{arena}", arenaName)
                        .replace("{details}", details.toString()));
            } else {
                sender.sendMessage(plugin.getMessagesManager().getMessage("command.join.invalid-arena")
                        .replace("{arena}", arenaName));
            }
            return true;
        }

        if (arena.getState() != ArenaState.WAITING && arena.getState() != ArenaState.STARTING) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.join.failed")
                    .replace("{arena}", arenaName));
            return true;
        }

        if (arena.getPlayerCount() >= arena.getMaxPlayers()) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.join.failed")
                    .replace("{arena}", arenaName));
            return true;
        }

        boolean added = arena.addPlayer(player.getUniqueId());

        if (added && arena.getLobby() != null) {
            player.teleport(arena.getLobby());

            for (UUID playerId : arena.getPlayers()) {
                Player arenaPlayer = Bukkit.getPlayer(playerId);
                if (arenaPlayer != null) {
                    arenaPlayer.sendMessage(plugin.getMessagesManager().getPrefix() + " §e" + player.getName() +
                            " §7has joined arena §e" + arena.getName() +
                            " §7(" + arena.getPlayerCount() + "/" + arena.getMaxPlayers() + ")");
                }
            }

            player.sendMessage(plugin.getMessagesManager().getMessage("command.join.success")
                    .replace("{arena}", arenaName));

            if (arena.canStart() && arena.getState() == ArenaState.WAITING) {
                plugin.getGameManager().startGame(arenaName);
            }
        } else {
            sender.sendMessage(plugin.getMessagesManager().getMessage("command.join.failed")
                    .replace("{arena}", arenaName));
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