package host.plas.justpoints.data.sql;

import host.plas.justpoints.JustPoints;
import host.plas.justpoints.data.PointPlayer;
import host.plas.justpoints.utils.MessageUtils;
import io.streamlined.bukkit.lib.thebase.lib.hikari.HikariConfig;
import io.streamlined.bukkit.lib.thebase.lib.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;

@Getter @Setter
public abstract class AbstractSqlHelper {
    private ConnectorSet connectorSet;
    private HikariDataSource dataSource;

    public AbstractSqlHelper(ConnectorSet connectorSet) {
        this.connectorSet = connectorSet;
    }

    public abstract Connection getConnection();
    
    public File getDatabaseFile() {
        if (! getConnectorSet().isSqlite()) return null;
        
        File file = new File(JustPoints.getInstance().getDataFolder(), getConnectorSet().getSqliteFile());
        
        if (! file.exists()) {
            try {
                file.createNewFile();
                MessageUtils.logInfo("Created database file: " + file.getName());
            } catch (Exception e) {
                MessageUtils.logError("Could not create database file!");
                e.printStackTrace();
            }
        }
        
        return file;
    }
    
    public void ensureFileExists() {
        if (! getConnectorSet().isSqlite()) return;
        
        File file = getDatabaseFile();
        if (file == null) return;

//        MessageUtils.logDebug("Using Database File: " + file.getAbsolutePath());
    }

    public void ensureReady() {
        ensureFileExists();
        ensureDatabase();
        ensureTables();
    }

    public void executeUpdate(String sqlStatement, String logMessage) {
        try (Connection connection = getConnection()) {
            if (connection == null) {
                MessageUtils.logError("Could not connect, connection is null!");
                return;
            }

            try {
                if (connection.isClosed()) {
                    MessageUtils.logError("Could not connect, connection is closed!");
                    return;
                }
            } catch (Exception e) {
                // TODO: handle exception
            }

            try (Statement statement = connection.createStatement()) {
                int rows = statement.executeUpdate(sqlStatement);

                if (rows > 0) {
//                    MessageUtils.logInfo(logMessage);
                } else {
//                    MessageUtils.logError("Error executing update! Rows: " + rows + " SQL: " + sqlStatement);
                }
            } catch (Exception e) {
                MessageUtils.logError("Error executing update!");
                e.printStackTrace();
            }
        } catch (Exception e) {
            MessageUtils.logError("Error executing update!");
            e.printStackTrace();
        }
    }

    public void ensureDatabase() {
        String sqlStatement = Statements.getStatement(getConnectorSet(), Statements.StatementKey.CREATE_DATABASE);
        if (sqlStatement.isBlank() || sqlStatement.isEmpty()) return;

        executeUpdate(sqlStatement, "Created database!");
    }

    public void ensureTable(Statements.StatementKey key) {
        String sqlStatement = Statements.getStatement(getConnectorSet(), key);
        if (sqlStatement.isBlank() || sqlStatement.isEmpty()) return;

        executeUpdate(sqlStatement, "Created tables!");
    }

    public void ensureTables() {
        ensureTable(Statements.StatementKey.CREATE_PLAYERS_TABLE);
        ensureTable(Statements.StatementKey.CREATE_POINTS_TABLE);
    }

    public void putPlayer(PointPlayer player) {
        CompletableFuture.supplyAsync(() -> {
            ensureReady();

            String playerStatement = Statements.getStatement(getConnectorSet(), Statements.StatementKey.UPDATE_PLAYER)
                    .replace("%uuid%", player.getUuid())
                    .replace("%username%", player.getUsername());

            executeUpdate(playerStatement, "Updated player for " + player.getUsername() + "!");

            player.getPoints().forEach((key, value) -> {
                String pointsStatement = Statements.getStatement(getConnectorSet(), Statements.StatementKey.UPDATE_POINTS)
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
           ensureReady();

           String playerStatement = Statements.getStatement(getConnectorSet(), Statements.StatementKey.GET_PLAYER)
                   .replace("%uuid%", uuid);

           String pointsStatement = Statements.getStatement(getConnectorSet(), Statements.StatementKey.GET_POINTS)
                   .replace("%uuid%", uuid);

           try (Connection connection = getConnection()) {
               if (connection == null) {
                   MessageUtils.logError("Could not connect, connection is null!");
                   return Optional.empty();
               }

               try {
                   if (connection.isClosed()) {
                       MessageUtils.logError("Could not connect, connection is closed!");
                       return Optional.empty();
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
                       MessageUtils.logError("Could not find player with uuid " + uuid + "!");
                       return Optional.empty();
                   }
               } catch (Exception e) {
                   MessageUtils.logError("Error executing query!");
                   e.printStackTrace();
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
                   MessageUtils.logError("Error executing query!");
                   e.printStackTrace();
               }

               MessageUtils.logError("Could not find player with uuid " + uuid + "!");
               return Optional.empty();
           } catch (Exception e) {
               MessageUtils.logError("Error getting connection!");
               e.printStackTrace();

               return Optional.empty();
           }
       });
    }
}
