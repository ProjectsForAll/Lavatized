package host.plas.justpoints.data.sql;

import host.plas.justpoints.JustPoints;
import host.plas.justpoints.data.PointPlayer;
import io.streamlined.bukkit.lib.thebase.lib.hikari.HikariConfig;
import io.streamlined.bukkit.lib.thebase.lib.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;

@Getter @Setter
public class MySqlHelper {
    private ConnectorSet connectorSet;
    private Connection rawConnection;
    private HikariDataSource dataSource;

    public MySqlHelper(ConnectorSet connectorSet) {
        this.connectorSet = connectorSet;
    }

    public Connection buildConnection() {
        try {
            if (dataSource == null) {
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

                dataSource = new HikariDataSource(hikariConfig);
            }

            closeConnection();

            rawConnection = dataSource.getConnection();

            return rawConnection;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    public Connection getConnection() {
        if (rawConnection == null) {
            return buildConnection();
        } else {
            try {
                if (rawConnection.isClosed()) {
                    return buildConnection();
                }

                return rawConnection;
            } catch (Exception e) {
//                e.printStackTrace();
                return buildConnection();
            }
        }
    }

    public void closeConnection() {
        try {
            if (rawConnection != null && ! rawConnection.isClosed()) {
                rawConnection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void executeUpdate(String sqlStatement, String logMessage) {
        Connection connection = getConnection();
        if (connection == null) {
            JustPoints.getInstance().getLogger().severe("Could not connect, connection is null!");
            return;
        }

        try {
            if (connection.isClosed()) {
                connection = buildConnection();
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

        try (Statement statement = connection.createStatement()) {
            int rows = statement.executeUpdate(sqlStatement);

            if (rows > 0) {
//                JustPoints.getInstance().getLogger().info(logMessage);
            }
        } catch (Exception e) {
            JustPoints.getInstance().getLogger().severe("Error executing update!");
            e.printStackTrace();
        }
    }

    public void ensureTables() {
        String sqlStatement = Statements.MySql.CREATE_TABLE_POINTS.getStatement()
                .replace("%database%", getConnectorSet().getDatabase())
                .replace("%table_prefix%", getConnectorSet().getTablePrefix());

        executeUpdate(sqlStatement, "Created tables!");
    }

    public void putPlayer(PointPlayer player) {
        CompletableFuture.supplyAsync(() -> {
            ensureTables();

            String playerStatement = Statements.MySql.UPDATE_PLAYER.getStatement()
                    .replace("%database%", getConnectorSet().getDatabase())
                    .replace("%table_prefix%", getConnectorSet().getTablePrefix())
                    .replace("%uuid%", player.getUuid())
                    .replace("%username%", player.getUsername());

            executeUpdate(playerStatement, "Updated player for " + player.getUsername() + "!");

            player.getPoints().forEach((key, value) -> {
                String pointsStatement = Statements.MySql.UPDATE_POINTS.getStatement()
                        .replace("%database%", getConnectorSet().getDatabase())
                        .replace("%table_prefix%", getConnectorSet().getTablePrefix())
                        .replace("%uuid%", player.getUuid())
                        .replace("%key%", key)
                        .replace("%points%", String.valueOf(value));

                executeUpdate(pointsStatement, "Updated points for " + player.getUsername() + "!");
            });

            return true;
        });
    }

    public CompletableFuture<Optional<PointPlayer>> getPlayer(String uuid) {
       return CompletableFuture.supplyAsync(() -> {
           ensureTables();

           String playerStatement = Statements.MySql.GET_PLAYER.getStatement()
                   .replace("%database%", getConnectorSet().getDatabase())
                   .replace("%table_prefix%", getConnectorSet().getTablePrefix())
                   .replace("%uuid%", uuid);

           String pointsStatement = Statements.MySql.GET_POINTS.getStatement()
                   .replace("%database%", getConnectorSet().getDatabase())
                   .replace("%table_prefix%", getConnectorSet().getTablePrefix())
                   .replace("%uuid%", uuid);

           Connection connection = getConnection();
           if (connection == null) {
               JustPoints.getInstance().getLogger().severe("Could not connect, connection is null!");
               return Optional.empty();
           }

           try {
               if (connection.isClosed()) {
                   connection = buildConnection();
               }
           } catch (Exception e) {
               // TODO: handle exception
           }

           String username = null;

           try (Statement statement = connection.createStatement()) {
               ResultSet playerSet = statement.executeQuery(playerStatement);

               if (playerSet.next()) {
                   username = playerSet.getString("username");
               } else {
                   JustPoints.getInstance().getLogger().severe("Could not find player with uuid " + uuid + "!");
                   return Optional.empty();
               }
           } catch (Exception e) {
               JustPoints.getInstance().getLogger().severe("Error executing query!");
               e.printStackTrace();
           }

           try {
               if (connection.isClosed()) {
                   connection = buildConnection();
               }
           } catch (Exception e) {
               // TODO: handle exception
           }

           try (Statement statement = connection.createStatement()) {
               ResultSet pointsSet = statement.executeQuery(pointsStatement);

               ConcurrentSkipListMap<String, Double> points = new ConcurrentSkipListMap<>();
               while (pointsSet.next()) {
                   points.put(pointsSet.getString("key"), pointsSet.getDouble("points"));
               }

               PointPlayer player = new PointPlayer(uuid, username, points);

               return Optional.of(player);
           } catch (Exception e) {
               JustPoints.getInstance().getLogger().severe("Error executing query!");
               e.printStackTrace();
           }

           JustPoints.getInstance().getLogger().severe("Could not find player with uuid " + uuid + "!");
           return Optional.empty();
       });
    }
}
