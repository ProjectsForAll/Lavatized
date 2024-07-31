package host.plas.lavatized.config;

import host.plas.bou.sql.ConnectorSet;
import host.plas.bou.sql.DatabaseType;
import host.plas.lavatized.Lavatized;
import host.plas.lavatized.arenas.ArenaConfig;
import tv.quaint.storage.resources.flat.simple.SimpleConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

public class MainConfig extends SimpleConfiguration {
    public MainConfig() {
        super("config.yml", Lavatized.getInstance(), true);
    }

    @Override
    public void init() {
        getDatabaseType();
        getDatabaseHost();
        getDatabasePort();
        getDatabaseName();
        getDatabaseTablePrefix();
        getDatabaseUsername();
        getDatabasePassword();
        getSqliteFile();
    }

    public DatabaseType getDatabaseType() {
        reloadResource();

        return DatabaseType.valueOf(getOrSetDefault("database.type", DatabaseType.SQLITE.name()));
    }

    public String getDatabaseHost() {
        reloadResource();

        return getOrSetDefault("database.host", "localhost");
    }

    public int getDatabasePort() {
        reloadResource();

        return getOrSetDefault("database.port", 3306);
    }

    public String getDatabaseName() {
        reloadResource();

        return getOrSetDefault("database.database", "database");
    }

    public String getDatabaseUsername() {
        reloadResource();

        return getOrSetDefault("database.username", "username");
    }

    public String getDatabasePassword() {
        reloadResource();

        return getOrSetDefault("database.password", "password");
    }

    public String getDatabaseTablePrefix() {
        reloadResource();

        return getOrSetDefault("database.table-prefix", "pnts_");
    }

    public String getSqliteFile() {
        reloadResource();

        return getOrSetDefault("database.sqlite-file", "points.db");
    }

    public ConnectorSet buildConnectorSet() {
        return new ConnectorSet(
                getDatabaseType(),
                getDatabaseHost(),
                getDatabasePort(),
                getDatabaseName(),
                getDatabaseUsername(),
                getDatabasePassword(),
                getDatabaseTablePrefix(),
                getSqliteFile()
        );
    }

    public ConcurrentSkipListSet<ArenaConfig> getArenaConfigsFromConfig() {
        ConcurrentSkipListSet<ArenaConfig> configs = new ConcurrentSkipListSet<>();

        singleLayerKeySet("arenas").forEach(s -> {
            String path = "arenas." + s;

            String name = getOrSetDefault(path + ".name", "Arena " + s);
            double centerX = getOrSetDefault(path + ".radius.center.x", 0.0);
            double centerZ = getOrSetDefault(path + ".radius.center.z", 0.0);
            double maxRadius = getOrSetDefault(path + ".radius.start", 249.5);
            double minRadius = getOrSetDefault(path + ".radius.end", 9.5);
            long shrinkTime = getOrSetDefault(path + ".radius.shrink-time", 36000L);
            String lobbyWorld = getOrSetDefault(path + ".lobby.world", "world");
            double lobbyX = getOrSetDefault(path + ".lobby.x", 0.0);
            double lobbyY = getOrSetDefault(path + ".lobby.y", 0.0);
            double lobbyZ = getOrSetDefault(path + ".lobby.z", 0.0);
            float lobbyYaw = getOrSetDefault(path + ".lobby.yaw", 0.0f);
            float lobbyPitch = getOrSetDefault(path + ".lobby.pitch", 0.0f);
            long gracePeriod = getOrSetDefault(path + ".grace-period", 0L);
            List<String> itemStrings = getOrSetDefault(path + ".items", new ArrayList<>());
            List<String> possibleSeeds = getOrSetDefault(path + ".seeds", new ArrayList<>());
            long ticksPerLayer = getOrSetDefault(path + ".ticks-per-layer", 0L);
            boolean seed = getOrSetDefault(path + ".reset.on-unload", true);

            configs.add(new ArenaConfig(s, name, lobbyWorld, minRadius, centerX, centerZ, maxRadius, shrinkTime,
                    lobbyX, lobbyY, lobbyZ, lobbyYaw, lobbyPitch, gracePeriod, ticksPerLayer, itemStrings, possibleSeeds, seed));
        });

        return configs;
    }

    public void saveArenaConfigToConfig(ArenaConfig config) {
        String path = "arenas." + config.getIdentifier();

        write(path + ".name", config.getName());
        write(path + ".radius.center.x", config.getCenterX());
        write(path + ".radius.center.z", config.getCenterZ());
        write(path + ".radius.start", config.getMaxRadius());
        write(path + ".radius.end", config.getMinRadius());
        write(path + ".radius.shrink-time", config.getShrinkTime());
        write(path + ".lobby.world", config.getLobbyWorld());
        write(path + ".lobby.x", config.getLobbyX());
        write(path + ".lobby.y", config.getLobbyY());
        write(path + ".lobby.z", config.getLobbyZ());
        write(path + ".lobby.yaw", config.getLobbyYaw());
        write(path + ".lobby.pitch", config.getLobbyPitch());
        write(path + ".grace-period", config.getGracePeriodMaxTicks());
        write(path + ".ticks-per-layer", config.getTicksPerLayer());
        write(path + ".items", config.getItemStrings());
        write(path + ".seeds", config.getPossibleSeeds());
        write(path + ".reset.on-unload", config.isResetOnUnload());
    }
}
