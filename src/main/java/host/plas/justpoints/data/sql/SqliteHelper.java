package host.plas.justpoints.data.sql;

import io.streamlined.bukkit.lib.thebase.lib.hikari.HikariConfig;
import io.streamlined.bukkit.lib.thebase.lib.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;

@Getter @Setter
public class SqliteHelper extends AbstractSqlHelper {
    public SqliteHelper(ConnectorSet connectorSet) {
        super(connectorSet);
    }

    @Override
    public Connection getConnection() {
        try {
            if (getDataSource() == null) {
                Class.forName("org.sqlite.JDBC");

                ensureFileExists();

                HikariConfig hikariConfig = new HikariConfig();
                hikariConfig.setJdbcUrl(getConnectorSet().getSqliteConnectionString());
                hikariConfig.setConnectionTimeout(10000);
                hikariConfig.setLeakDetectionThreshold(10000);
                hikariConfig.setMaximumPoolSize(10);
                hikariConfig.setMinimumIdle(5);
                hikariConfig.setMaxLifetime(60000);
                hikariConfig.setPoolName("JustPoints");
                hikariConfig.addDataSourceProperty("allowMultiQueries", true); // Allows multiple queries to be executed in one statement
                hikariConfig.setDriverClassName("org.sqlite.JDBC");

                setDataSource(new HikariDataSource(hikariConfig));
            }

            return getDataSource().getConnection();
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }
}
