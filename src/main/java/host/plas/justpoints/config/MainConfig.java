package host.plas.justpoints.config;

import host.plas.justpoints.JustPoints;
import host.plas.justpoints.data.sql.ConnectorSet;
import tv.quaint.storage.resources.flat.simple.SimpleConfiguration;

import java.io.File;

public class MainConfig extends SimpleConfiguration {
    public MainConfig() {
        super("config.yml", JustPoints.getInstance(), true);
    }

    @Override
    public void init() {
        getDatabaseHost();
        getDatabasePort();
        getDatabaseName();
        getDatabaseTablePrefix();
        getDatabaseUsername();
        getDatabasePassword();
        getDatabaseOptions();

        getPointsDefault();
        getPointsOnJoinLoad();
        getPointsOnQuitSave();
        getPointsOnQuitDispose();
        getPointsSaveInterval();
    }

    public String getDatabaseHost() {
        reloadResource();

        return getResource().getString("database.host");
    }

    public String getDatabasePort() {
        reloadResource();

        return getResource().getString("database.port");
    }

    public String getDatabaseName() {
        reloadResource();

        return getResource().getString("database.database");
    }

    public String getDatabaseTablePrefix() {
        reloadResource();

        return getResource().getString("database.table-prefix");
    }

    public String getDatabaseUsername() {
        reloadResource();

        return getResource().getString("database.username");
    }

    public String getDatabasePassword() {
        reloadResource();

        return getResource().getString("database.password");
    }

    public String getDatabaseOptions() {
        reloadResource();

        return getResource().getString("database.options");
    }

    public ConnectorSet buildConnectorSet() {
        return new ConnectorSet(
                getDatabaseHost(),
                getDatabasePort(),
                getDatabaseName(),
                getDatabaseTablePrefix(),
                getDatabaseUsername(),
                getDatabasePassword(),
                getDatabaseOptions()
        );
    }

    public double getPointsDefault() {
        reloadResource();

        return getResource().getDouble("points.default");
    }

    public boolean getPointsOnJoinLoad() {
        reloadResource();

        return getResource().getBoolean("points.on-join.load");
    }

    public boolean getPointsOnQuitSave() {
        reloadResource();

        return getResource().getBoolean("points.on-quit.save");
    }

    public boolean getPointsOnQuitDispose() {
        reloadResource();

        return getResource().getBoolean("points.on-quit.dispose");
    }

    public int getPointsSaveInterval() {
        reloadResource();

        return getResource().getInt("points.save-interval");
    }
}
