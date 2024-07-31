package host.plas.lavatized.managers;

import host.plas.lavatized.arenas.players.ArenaPlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;

import java.util.concurrent.ConcurrentSkipListSet;

public class PlayerManager {
    @Getter @Setter
    private static ConcurrentSkipListSet<ArenaPlayer> loadedPlayers = new ConcurrentSkipListSet<>();

    public static void loadPlayer(ArenaPlayer player) {
        loadedPlayers.add(player);
    }

    public static void unloadPlayer(String identifier) {
        loadedPlayers.stream().filter(player -> player.getIdentifier().equalsIgnoreCase(identifier)).findFirst().ifPresent(loadedPlayers::remove);
    }

    public static ArenaPlayer getPlayer(String identifier) {
        return loadedPlayers.stream().filter(player -> player.getIdentifier().equalsIgnoreCase(identifier)).findFirst().orElse(null);
    }

    public static boolean isPlayerLoaded(String identifier) {
        return getPlayer(identifier) != null;
    }

    public static ArenaPlayer wrap(String identifier) {
        return isPlayerLoaded(identifier) ? getPlayer(identifier) : new ArenaPlayer(identifier);
    }

    public static ArenaPlayer wrap(OfflinePlayer player) {
        return wrap(player.getUniqueId().toString());
    }
}
