package host.plas.lavatized.commands;

import host.plas.lavatized.Lavatized;
import host.plas.lavatized.arenas.Arena;
import host.plas.lavatized.arenas.ArenaConfig;
import host.plas.lavatized.arenas.players.ArenaPlayer;
import host.plas.bou.commands.CommandContext;
import host.plas.bou.commands.SimplifiedCommand;
import host.plas.lavatized.managers.ArenaManager;

import java.util.Arrays;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

public class LavaRisingCMD extends SimplifiedCommand {
    public LavaRisingCMD() {
        super("lavatized", Lavatized.getInstance());
    }

    @Override
    public boolean command(CommandContext commandContext) {
        String action = commandContext.getStringArg(0);

        switch (action) {
            case "create":
                if (! commandContext.getCommandSender().hasPermission("lavatized.command.create")) {
                    commandContext.sendMessage("&cYou do not have permission to do this.");
                    return false;
                }

                if (! commandContext.isArgUsable(1)) {
                    commandContext.sendMessage("&cInvalid action.");
                    return true;
                }

                String eIdentifier = commandContext.getStringArg(1);
                if (ArenaManager.isArenaLoaded(eIdentifier)) {
                    commandContext.sendMessage("&cArena already exists.");
                    return true;
                }

                Arena eArena = Arena.wrap(eIdentifier);
                if (eArena == null) {
                    commandContext.sendMessage("&cArena could not be created.");
                    return true;
                }

                commandContext.sendMessage("&eCreated arena &d" + eArena.getIdentifier() + "&8.");
                return true;
            case "reset":
                if (! commandContext.getCommandSender().hasPermission("lavatized.command.reset")) {
                    commandContext.sendMessage("&cYou do not have permission to do this.");
                    return false;
                }

                if (! commandContext.isArgUsable(1)) {
                    commandContext.sendMessage("&cInvalid action.");
                    return true;
                }

                String rIdentifier = commandContext.getStringArg(1);
                if (! ArenaManager.isArenaLoaded(rIdentifier)) {
                    commandContext.sendMessage("&cArena is not created yet.");
                    return true;
                }

                Arena rArena = Arena.wrap(rIdentifier);
                if (rArena == null) {
                    commandContext.sendMessage("&cArena could not be reset.");
                    return true;
                }

                rArena.reset();

                commandContext.sendMessage("&eReset arena &d" + rArena.getIdentifier() + "&8.");
                return true;
            case "set-lobby":
                if (! commandContext.getCommandSender().hasPermission("lavatized.command.set-lobby")) {
                    commandContext.sendMessage("&cYou do not have permission to do this.");
                    return false;
                }

                if (! commandContext.isArgUsable(1)) {
                    commandContext.sendMessage("&cInvalid action.");
                    return true;
                }

                String sIdentifier = commandContext.getStringArg(1);
                if (! ArenaManager.isArenaLoaded(sIdentifier)) {
                    commandContext.sendMessage("&cArena is not created yet.");
                    return true;
                }

                Arena sArena = Arena.wrap(sIdentifier);
                if (sArena == null) {
                    commandContext.sendMessage("&cArena could not be set.");
                    return true;
                }

                sArena.setLobby(commandContext.getPlayer().get().getLocation());
                sArena.saveToConfig();

                commandContext.sendMessage("&eSet lobby for arena &d" + sArena.getIdentifier() + "&8.");
                return true;
            case "start":
                if (! commandContext.getCommandSender().hasPermission("lavatized.command.start")) {
                    commandContext.sendMessage("&cYou do not have permission to do this.");
                    return false;
                }

                if (! commandContext.isArgUsable(1)) {
                    commandContext.sendMessage("&cInvalid action.");
                    return true;
                }

                String s2Identifier = commandContext.getStringArg(1);
                if (! ArenaManager.isArenaLoaded(s2Identifier)) {
                    commandContext.sendMessage("&cArena is not created yet.");
                    return true;
                }

                Arena s2Arena = Arena.wrap(s2Identifier);
                if (s2Arena == null) {
                    commandContext.sendMessage("&cArena could not be started.");
                    return true;
                }

                if (s2Arena.isRunning()) {
                    commandContext.sendMessage("&cArena is already started.");
                    return true;
                }

                s2Arena.onStart();

                commandContext.sendMessage("&eStarted arena &d" + s2Arena.getIdentifier() + "&8.");
                return true;
            case "join":
                if (! commandContext.getCommandSender().hasPermission("lavatized.command.join")) {
                    commandContext.sendMessage("&cYou do not have permission to do this.");
                    return false;
                }

                if (! commandContext.isArgUsable(1)) {
                    commandContext.sendMessage("&cInvalid action.");
                    return true;
                }

                String jIdentifier = commandContext.getStringArg(1);
                if (! ArenaManager.isArenaLoaded(jIdentifier)) {
                    commandContext.sendMessage("&cArena is not created yet.");
                    return true;
                }

                Arena jArena = Arena.wrap(jIdentifier);
                if (jArena == null) {
                    commandContext.sendMessage("&cArena could not be joined.");
                    return true;
                }

                ArenaPlayer jPlayer = ArenaPlayer.wrap(commandContext.getPlayer().get());

                if (jPlayer.getPlayState().isPresent()) {
                    commandContext.sendMessage("&cYou are already in an arena.");
                    return true;
                }

                jArena.onJoin(jPlayer);

                commandContext.sendMessage("&eJoined arena &d" + jArena.getIdentifier() + "&8.");
                return true;
            case "leave":
                if (! commandContext.getCommandSender().hasPermission("lavatized.command.leave")) {
                    commandContext.sendMessage("&cYou do not have permission to do this.");
                    return false;
                }

                if (! commandContext.isArgUsable(1)) {
                    ArenaPlayer lPlayer = ArenaPlayer.wrap(commandContext.getPlayer().get());

                    if (lPlayer.getPlayState().isEmpty()) {
                        commandContext.sendMessage("&cYou are not in an arena.");
                        return true;
                    }

                    Arena lArena = lPlayer.getPlayState().get().getCurrentArena();
                    if (lArena == null) {
                        commandContext.sendMessage("&cArena could not be left.");
                        return true;
                    }

                    if (! lArena.ownsPlayer(lPlayer.getIdentifier())) {
                        commandContext.sendMessage("&cYou are not in this arena.");
                        return true;
                    }

                    lArena.onLeave(lPlayer);

                    commandContext.sendMessage("&eLeft arena &d" + lArena.getIdentifier() + "&8.");
                    return true;
                }

                String lIdentifier = commandContext.getStringArg(1);
                if (! ArenaManager.isArenaLoaded(lIdentifier)) {
                    commandContext.sendMessage("&cArena is not created yet.");
                    return true;
                }

                Arena lArena = Arena.wrap(lIdentifier);
                if (lArena == null) {
                    commandContext.sendMessage("&cArena could not be left.");
                    return true;
                }

                ArenaPlayer lPlayer = ArenaPlayer.wrap(commandContext.getPlayer().get());

                if (! lArena.ownsPlayer(lPlayer.getIdentifier())) {
                    commandContext.sendMessage("&cYou are not in this arena.");
                    return true;
                }

                lArena.onLeave(lPlayer);

                commandContext.sendMessage("&eLeft arena &d" + lArena.getIdentifier() + "&8.");
                return true;
            default:
                commandContext.sendMessage("&cInvalid action.");
                return true;
        }
    }

    @Override
    public ConcurrentSkipListSet<String> tabComplete(CommandContext commandContext) {
        if (commandContext.getArgs().size() == 1) {
            return new ConcurrentSkipListSet<>(Arrays.asList("create", "reset", "set-lobby", "start", "join", "leave"));
        }

        if (commandContext.getArgs().size() == 2) {
            switch (commandContext.getStringArg(0)) {
                case "create":
                    return ArenaManager.getLoadedConfigs().stream().map(ArenaConfig::getIdentifier).collect(Collectors.toCollection(ConcurrentSkipListSet::new));
                case "reset":
                case "set-lobby":
                case "start":
                case "join":
                case "leave":
                    return ArenaManager.getLoadedArenas().stream().map(Arena::getIdentifier).collect(Collectors.toCollection(ConcurrentSkipListSet::new));
            }
        }

        return new ConcurrentSkipListSet<>();
    }
}
