package host.plas.justpoints.managers;

import host.plas.justpoints.JustPoints;
import host.plas.justpoints.data.PointPlayer;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

public class PointsManager {
    @Getter @Setter
    private static ConcurrentSkipListSet<PointPlayer> loadedPlayers = new ConcurrentSkipListSet<>();

    public static void loadPlayer(PointPlayer player) {
        loadedPlayers.add(player);
    }

    public static void unloadPlayer(String uuid, boolean save) {
        loadedPlayers.forEach(player -> {
            if (player.getIdentifier().equalsIgnoreCase(uuid)) {
                if (save) player.save();

                loadedPlayers.remove(player);
            }
        });
    }

    public static Optional<PointPlayer> getPlayer(String uuid) {
        return loadedPlayers.stream().filter(player -> player.getIdentifier().equalsIgnoreCase(uuid)).findFirst();
    }

    public static boolean isPlayerLoaded(String uuid) {
        return getPlayer(uuid).isPresent();
    }

    public static PointPlayer createNewPlayer(String uuid) {
        return new PointPlayer(uuid);
    }

    public static PointPlayer getOrGetPlayer(String uuid) {
        Optional<PointPlayer> player = getPlayer(uuid);
        if (player.isPresent()) {
            return player.get();
        }

        PointPlayer newPlayer = createNewPlayer(uuid);
        newPlayer = newPlayer.augment(JustPoints.getMainDatabase().loadPlayer(uuid));

        loadPlayer(newPlayer);

        return newPlayer;
    }
}
