package host.plas.justpoints.data;

import host.plas.justpoints.JustPoints;
import host.plas.justpoints.managers.PointsManager;
import host.plas.justpoints.utils.MessageUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import tv.quaint.objects.Identifiable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;

@Getter @Setter
public class PointPlayer implements Identifiable {
    private String identifier;

    private String username;
    private ConcurrentSkipListMap<String, Double> points;

    private long lastEditedMillis;

    private boolean loadedAtLeastOnce;

    public PointPlayer(String identifier, String username, ConcurrentSkipListMap<String, Double> points) {
        this.identifier = identifier;
        this.username = username;
        this.points = points;
        this.loadedAtLeastOnce = false;
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

    public void saveAndUnload() {
        PointsManager.unloadPlayer(getIdentifier(), true);
    }

    public void save() {
        JustPoints.getMainDatabase().savePlayer(this);
        setLastEditedMillis(System.currentTimeMillis());
    }

    public PointPlayer augment(CompletableFuture<Optional<PointPlayer>> future) {
        CompletableFuture.runAsync(() -> {
            try {
                Optional<PointPlayer> p = future.join();
                if (p.isEmpty()) return;
                PointPlayer player = p.get();

                if (loadedAtLeastOnce) {
                    player.getPoints().forEach((key, value) -> {
                        this.points.compute(key, (k, current) -> value);
                    });
                } else {
                    player.getPoints().forEach((key, value) -> {
                        this.points.compute(key, (k, current) -> {
                            if (current == null) return value;
                            return current + value;
                        });
                    });
                }

                this.username = player.getUsername();
                this.lastEditedMillis = player.getLastEditedMillis();

                this.loadedAtLeastOnce = true;
            } catch (Exception e) {
                MessageUtils.logError("Error augmenting player data for " + getIdentifier());
                e.printStackTrace();
            }
        });

        return this;
    }

    public void load() {
        PointsManager.loadPlayer(this);
    }

    public void unload() {
        PointsManager.unloadPlayer(getIdentifier(), false);
    }

    public void reset(String key) {
        points.remove(key);

        JustPoints.getMainDatabase().resetPoints(key, this);

        save();
    }

    public void action(Consumer<PointPlayer> action) {
        action.accept(this);
    }
}
