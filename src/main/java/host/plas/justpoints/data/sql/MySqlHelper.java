package host.plas.justpoints.data.sql;

import lombok.Getter;
import lombok.Setter;
import tv.quaint.thebase.lib.hikari.HikariConfig;
import tv.quaint.thebase.lib.hikari.HikariDataSource;

import java.sql.Connection;

@Getter @Setter
public class MySqlHelper extends AbstractSqlHelper {
    public MySqlHelper(ConnectorSet connectorSet) {
        super(connectorSet);
    }

    public Connection getConnection() {
        try {
            if (getDataSource() == null) {
                Class.forName("com.mysql.cj.jdbc.Driver");

                HikariConfig hikariConfig = new HikariConfig();
                hikariConfig.setJdbcUrl(getConnectorSet().getMySqlConnectionString());
                hikariConfig.setUsername(getConnectorSet().getUsername());
                hikariConfig.setPassword(getConnectorSet().getPassword());
                hikariConfig.setConnectionTimeout(10000);
                hikariConfig.setLeakDetectionThreshold(10000);
                hikariConfig.setMaximumPoolSize(10);
                hikariConfig.setMinimumIdle(5);
                hikariConfig.setMaxLifetime(60000);
                hikariConfig.setPoolName("JustPoints");
                hikariConfig.addDataSourceProperty("allowMultiQueries", true); // Allows multiple queries to be executed in one statement
                // new driver class
                hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");

                setDataSource(new HikariDataSource(hikariConfig));
            }

            return getDataSource().getConnection();
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }
}
