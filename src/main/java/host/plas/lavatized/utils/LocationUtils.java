package host.plas.lavatized.utils;

import host.plas.bou.scheduling.TaskManager;
import host.plas.bou.utils.ClassHelper;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListMap;

public class LocationUtils {
    public static String locationToString(Location location) {
        return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
    }

    public static Location stringToLocation(String string) {
        String[] parts = string.split(",");
        return new org.bukkit.Location(Bukkit.getWorld(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), Float.parseFloat(parts[4]), Float.parseFloat(parts[5]));
    }

    public static Location getLocationOf(String world, double x, double y, double z, float yaw, float pitch) {
        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }

    public static Location getLocationOf(String world, double x, double y, double z) {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    public static Location getLocationOf(String world, int x, int y, int z) {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    public static Location getRandomLocation(Location min, Location max) {
        Random RNG = new Random();
        double x = RNG.nextDouble() * (max.getX() - min.getX()) + min.getX();
        double y = RNG.nextDouble() * (max.getY() - min.getY()) + min.getY();
        double z = RNG.nextDouble() * (max.getZ() - min.getZ()) + min.getZ();

        return new Location(min.getWorld(), x, y, z);
    }

    public static Location getRandomTopLocation(Location min, Location max) {
        Location location = getRandomLocation(min, max);
        Location previousLocation = location.clone();

        for (int y = location.getWorld().getMaxHeight(); y > 0; y--) {
            location.setY(y);
            if (!(location.getBlock().getType() == Material.AIR)) {
                return previousLocation;
            }
            previousLocation = location.clone();
        }

        return previousLocation;
    }

    public static void teleport(Player player, Location location) {
        if (ClassHelper.isFolia()) TaskManager.getScheduler().teleport(player, location);
        else {
            TaskManager.getScheduler().runTask(() -> player.teleport(location));
        }
    }

    @Getter @Setter
    private static ConcurrentSkipListMap<Date, World> generatedWorlds = new ConcurrentSkipListMap<>();

    public static void createWorld(String name, String seed) {
        try {
            long seedLong;
            try {
                seedLong = Long.parseLong(seed);
            } catch (NumberFormatException e) {
                seedLong = seed.hashCode();
            }

            WorldCreator creator = new WorldCreator(name);
            creator.seed(seedLong);

            final Date now = new Date();
            TaskManager.getScheduler().runTask(() -> {
                try {
                    World world = Bukkit.createWorld(creator);

                    generatedWorlds.put(now, world);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
