package host.plas.justpoints.commands;

import host.plas.justpoints.JustPoints;
import host.plas.justpoints.data.PointPlayer;
import io.streamlined.bukkit.commands.CommandContext;
import io.streamlined.bukkit.commands.SimplifiedCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PointsCMD extends SimplifiedCommand {
    public PointsCMD() {
        super("points", JustPoints.getInstance());
    }

    @Override
    public boolean command(CommandContext commandContext) {
        String action = commandContext.getStringArg(0);

        switch (action) {
            case "add":
                if (commandContext.getArgs().size() < 4) {
                    commandContext.sendMessage("&cUsage: /points add <player> <type> <points>");
                    return true;
                }

                String aplayer = commandContext.getStringArg(1);
                String atype = commandContext.getStringArg(2);
                Optional<Double> apoints = commandContext.getDoubleArg(3);
                if (apoints.isEmpty()) {
                    commandContext.sendMessage("&cInvalid points.");
                    return true;
                }

                OfflinePlayer aofflinePlayer = Bukkit.getOfflinePlayer(aplayer);
                PointPlayer.getOrGetPlayer(aofflinePlayer.getUniqueId().toString()).whenComplete((pointPlayer, throwable) -> {
                    if (throwable != null) {
                        throwable.printStackTrace();
                        return;
                    }

                    if (pointPlayer == null) {
                        commandContext.sendMessage("&cPlayer not found.");
                        return;
                    }

                    pointPlayer.addPoints(atype, apoints.get());
                    pointPlayer.save();

                    commandContext.sendMessage("&eAdded &f" + apoints.get() + " &cpoints &eto &d" + aplayer + "&8.");
                });
                return true;
            case "remove":
                if (commandContext.getArgs().size() < 4) {
                    commandContext.sendMessage("&cUsage: /points remove <player> <type> <points>");
                    return true;
                }

                String rplayer = commandContext.getStringArg(1);
                String rtype = commandContext.getStringArg(2);
                Optional<Double> rpoints = commandContext.getDoubleArg(3);
                if (rpoints.isEmpty()) {
                    commandContext.sendMessage("&cInvalid points.");
                    return true;
                }

                OfflinePlayer rofflinePlayer = Bukkit.getOfflinePlayer(rplayer);
                PointPlayer.getOrGetPlayer(rofflinePlayer.getUniqueId().toString()).whenComplete((pointPlayer, throwable) -> {
                    if (throwable != null) {
                        throwable.printStackTrace();
                        commandContext.sendMessage("&cError getting player.");
                        return;
                    }

                    if (pointPlayer == null) {
                        commandContext.sendMessage("&cPlayer not found.");
                        return;
                    }

                    pointPlayer.removePoints(rtype, rpoints.get());
                    pointPlayer.save();

                    commandContext.sendMessage("&eRemoved &f" + rpoints.get() + " &cpoints &efrom &d" + rplayer + "&8.");
                });
                return true;
            case "set":
                if (commandContext.getArgs().size() < 4) {
                    commandContext.sendMessage("&cUsage: /points set <player> <type> <points>");
                    return true;
                }

                String splayer = commandContext.getStringArg(1);
                String stype = commandContext.getStringArg(2);
                Optional<Double> spoints = commandContext.getDoubleArg(3);
                if (spoints.isEmpty()) {
                    commandContext.sendMessage("&cInvalid points.");
                    return true;
                }

                OfflinePlayer sofflinePlayer = Bukkit.getOfflinePlayer(splayer);
                PointPlayer.getOrGetPlayer(sofflinePlayer.getUniqueId().toString()).whenComplete((pointPlayer, throwable) -> {
                    if (throwable != null) {
                        throwable.printStackTrace();
                        commandContext.sendMessage("&cError getting player.");
                        return;
                    }

                    if (pointPlayer == null) {
                        commandContext.sendMessage("&cPlayer not found.");
                        return;
                    }

                    pointPlayer.setPointsSpecific(stype, spoints.get());
                    pointPlayer.save();

                    commandContext.sendMessage("&eSet &cpoints &efor &d" + splayer + " &eto &f" + spoints.get() + "&8.");
                });
                return true;
            case "get":
                if (commandContext.getArgs().size() < 3) {
                    commandContext.sendMessage("&cUsage: /points get <player> <type>");
                    return true;
                }

                String gplayer = commandContext.getStringArg(1);
                String gtype = commandContext.getStringArg(2);

                OfflinePlayer gofflinePlayer = Bukkit.getOfflinePlayer(gplayer);
                PointPlayer.getOrGetPlayer(gofflinePlayer.getUniqueId().toString()).whenComplete((pointPlayer, throwable) -> {
                    if (throwable != null) {
                        throwable.printStackTrace();
                        commandContext.sendMessage("&cError getting player.");
                        return;
                    }

                    if (pointPlayer == null) {
                        commandContext.sendMessage("&cPlayer not found.");
                        return;
                    }

                    commandContext.sendMessage("&d" + gplayer + " &ehas &f" + pointPlayer.getPoints(gtype) + " &cpoints&8.");
                });
                return true;
            default:
                commandContext.sendMessage("&cInvalid action.");
                return true;
        }
    }

    @Override
    public ConcurrentSkipListSet<String> tabComplete(CommandContext commandContext) {
        ConcurrentSkipListSet<String> tab = new ConcurrentSkipListSet<>();

        Optional<CommandSender> optSender = commandContext.getSender().getCommandSender();
        if (optSender.isEmpty()) {
            return tab;
        }
        CommandSender s = optSender.get();

        if (commandContext.getArgs().size() == 1) {
            tab.add("add");
            tab.add("remove");
            tab.add("set");
            tab.add("get");
        } else if (commandContext.getArgs().size() == 2) {
            tab.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
        } else if (commandContext.getArgs().size() == 3) {
            if (s.hasPermission("justpoints.command.points.add") || s.hasPermission("justpoints.command.points.remove") ||
                    s.hasPermission("justpoints.command.points.set") || s.hasPermission("justpoints.command.points.get")) {
                tab.add("<key>");

                if (commandContext.getStringArg(1).equalsIgnoreCase("get") && s.hasPermission("justpoints.command.points.get")) {
                    OfflinePlayer target = Bukkit.getOfflinePlayer(commandContext.getStringArg(2));
                    PointPlayer pointPlayer = PointPlayer.getOrGetPlayer(target.getUniqueId().toString()).completeOnTimeout(null, 77, TimeUnit.MILLISECONDS).join();

                    if (pointPlayer == null) {
                        return tab;
                    }

                    tab.addAll(pointPlayer.getPoints().keySet());
                }
            }
        }

        return tab;
    }
}
