package host.plas.justpoints.data;

import host.plas.justpoints.JustPoints;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Getter @Setter
public class PointPlayer implements Comparable<PointPlayer> {
    private String uuid;

    private String username;
    private ConcurrentSkipListMap<String, Double> points;

    public PointPlayer(String uuid, String username, ConcurrentSkipListMap<String, Double> points) {
        this.uuid = uuid;
        this.username = username;
        this.points = points;
    }

    public PointPlayer(String uuid, String username) {
        this(uuid, username, new ConcurrentSkipListMap<>());
    }

    public PointPlayer(String uuid) {
        this(Bukkit.getOfflinePlayer(UUID.fromString(uuid)));
    }

    public PointPlayer(OfflinePlayer player) {
        this(player.getUniqueId().toString(), player.getName());
    }

    public PointPlayer(OfflinePlayer player, ConcurrentSkipListMap<String, Double> points) {
        this(player.getUniqueId().toString(), player.getName(), points);
    }

    @Override
    public int compareTo(@NotNull PointPlayer o) {
        return uuid.compareTo(o.getUuid());
    }

    public double getPoints(String type) {
        return points.getOrDefault(type, 0.0);
    }

    public void addPoints(String type, double points) {
        double currentPoints = getPoints(type);
        currentPoints += points;

        this.points.put(type, currentPoints);
    }

    public void removePoints(String type, double points) {
        double currentPoints = getPoints(type);
        currentPoints -= points;

        this.points.put(type, currentPoints);
    }

    public void setPointsSpecific(String type, double points) {
        this.points.put(type, points);
    }

    public void save() {
        JustPoints.getMySqlHelper().putPlayer(this);
    }

    public void register() {
        registerPlayer(this);
    }

    public void unregister() {
        unregisterPlayer(uuid);
    }

    @Getter @Setter
    private static ConcurrentSkipListSet<PointPlayer> players = new ConcurrentSkipListSet<>();

    public static void registerPlayer(PointPlayer player) {
        players.add(player);
    }

    public static void unregisterPlayer(String uuid) {
        players.removeIf(player -> player.getUuid().equalsIgnoreCase(uuid));
    }

    public static Optional<PointPlayer> getPlayer(String uuid) {
        return players.stream().filter(player -> player.getUuid().equalsIgnoreCase(uuid)).findFirst();
    }

    public static CompletableFuture<PointPlayer> getOrGetPlayer(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<PointPlayer> p = getPlayer(uuid);
            if (p.isPresent()) {
                return p.get();
            }

            p = JustPoints.getMySqlHelper().getPlayer(uuid).join();
            if (p.isPresent()) {
                registerPlayer(p.get());
                return p.get();
            } else {
                PointPlayer pl = new PointPlayer(uuid);
                registerPlayer(pl);
                JustPoints.getMySqlHelper().putPlayer(pl);

                return pl;
            }
        });
    }
}
