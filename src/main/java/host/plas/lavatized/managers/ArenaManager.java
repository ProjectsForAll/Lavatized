package host.plas.lavatized.managers;

import host.plas.lavatized.Lavatized;
import host.plas.lavatized.arenas.Arena;
import host.plas.lavatized.arenas.ArenaConfig;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

public class ArenaManager {
    @Getter @Setter
    private static int currentArenaIndex = 0;

    public static int getNextArenaIndex() {
        return currentArenaIndex ++;
    }

    @Getter @Setter
    private static ConcurrentSkipListSet<ArenaConfig> loadedConfigs = new ConcurrentSkipListSet<>();

    public static void loadConfig(ArenaConfig config) {
        loadedConfigs.add(config);
    }

    public static void unloadConfig(String identifier) {
        loadedConfigs.stream().filter(config -> config.getIdentifier().equalsIgnoreCase(identifier)).findFirst().ifPresent(loadedConfigs::remove);
    }

    public static Optional<ArenaConfig> getConfig(String identifier) {
        return loadedConfigs.stream().filter(config -> config.getIdentifier().equalsIgnoreCase(identifier)).findFirst();
    }

    public static boolean isConfigLoaded(String identifier) {
        return getConfig(identifier).isPresent();
    }

    @Getter @Setter
    private static ConcurrentSkipListSet<Arena> loadedArenas = new ConcurrentSkipListSet<>();

    public static void loadArena(Arena arena) {
        loadedArenas.add(arena);
    }

    public static void unloadArena(String identifier) {
        loadedArenas.stream().filter(arena -> arena.getIdentifier().equalsIgnoreCase(identifier)).findFirst().ifPresent(loadedArenas::remove);
    }

    public static Optional<Arena> getArena(String identifier) {
        return loadedArenas.stream().filter(arena -> arena.getIdentifier().equalsIgnoreCase(identifier)).findFirst();
    }

    public static boolean isArenaLoaded(String identifier) {
        return getArena(identifier).isPresent();
    }

    public static Arena wrap(String identifier) {
        return Arena.wrap(identifier);
    }

    public static Arena wrap(ArenaConfig config) {
        return Arena.wrap(config);
    }

    public static void init() {
        Lavatized.getMainConfig().getArenaConfigsFromConfig().forEach(ArenaManager::loadConfig);

        getLoadedConfigs().forEach(arenaConfig -> {
            Arena arena = Arena.wrap(arenaConfig);
            if (arena != null) {
                loadArena(arena);
            }
        });
    }
}
