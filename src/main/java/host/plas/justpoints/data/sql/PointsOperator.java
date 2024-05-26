package host.plas.justpoints.data.sql;

import host.plas.justpoints.data.PointPlayer;
import host.plas.justpoints.utils.MessageUtils;
import lombok.Getter;
import lombok.Setter;
import tv.quaint.objects.AtomicString;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

@Getter @Setter
public class PointsOperator extends DBOperator {
    public PointsOperator(ConnectorSet connectorSet) {
        super(connectorSet, "JustPoints");
    }

    @Override
    public void ensureDatabase() {
        String s1 = Statements.getStatement(getConnectorSet(), Statements.StatementKey.CREATE_DATABASE);

        execute(s1, stmt -> {});
    }

    @Override
    public void ensureTables() {
        String s1 = Statements.getStatement(getConnectorSet(), Statements.StatementKey.CREATE_TABLES);

        execute(s1, stmt -> {});
    }

    public void savePlayer(PointPlayer player) {
        savePlayer(player, true);
    }

    public void savePlayer(PointPlayer player, boolean async) {
        if (async) {
            CompletableFuture.runAsync(() -> savePlayerAsync(player).join());
        } else {
            savePlayerAsync(player).join();
        }
    }

    public CompletableFuture<Boolean> savePlayerAsync(PointPlayer player) {
        return CompletableFuture.supplyAsync(() -> {
            ensureUsable();

            String playerStatement = Statements.getStatement(getConnectorSet(), Statements.StatementKey.UPDATE_PLAYER);

            execute(playerStatement, stmt -> {
                try {
                    stmt.setString(1, player.getIdentifier());
                    stmt.setString(2, player.getUsername());

                    if (getType() == DatabaseType.MYSQL) {
                        stmt.setString(3, player.getUsername());
                    }
                } catch (Exception e) {
                    MessageUtils.logError("Error setting statement values!");
                    e.printStackTrace();
                }
            });

            player.getPoints().forEach((key, value) -> {
                String pointsStatement = Statements.getStatement(getConnectorSet(), Statements.StatementKey.UPDATE_POINTS);

                execute(pointsStatement, stmt -> {
                    try {
                        stmt.setString(1, player.getIdentifier());
                        stmt.setString(2, key);
                        stmt.setDouble(3, value);

                        if (getType() == DatabaseType.MYSQL) {
                            stmt.setDouble(4, value);
                        }
                    } catch (Exception e) {
                        MessageUtils.logError("Error setting statement values!");
                        e.printStackTrace();
                    }
                });
            });

            String syncingStatement = Statements.getStatement(getConnectorSet(), Statements.StatementKey.UPDATE_SYNCING);

            long lastEdited = System.currentTimeMillis();

            execute(syncingStatement, stmt -> {
                try {
                    stmt.setString(1, player.getIdentifier());
                    stmt.setLong(2, lastEdited);

                    if (getType() == DatabaseType.MYSQL) {
                        stmt.setLong(3, lastEdited);
                    }
                } catch (Exception e) {
                    MessageUtils.logError("Error setting statement values!");
                    e.printStackTrace();
                }
            });

            player.setLastEditedMillis(lastEdited);

            return true;
        });
    }

    public CompletableFuture<Optional<PointPlayer>> loadPlayer(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            ensureUsable();

            String s1 = Statements.getStatement(getConnectorSet(), Statements.StatementKey.GET_PLAYER);

            String s2 = Statements.getStatement(getConnectorSet(), Statements.StatementKey.GET_POINTS);

            Optional<PointPlayer> player = Optional.empty();
            ConcurrentSkipListMap<String, Double> points = new ConcurrentSkipListMap<>();
            AtomicString username = new AtomicString();

            executeQuery(s1, stmt -> {
                try {
                    stmt.setString(1, uuid);
                } catch (Exception e) {
                    MessageUtils.logError("Error setting statement values!");
                    e.printStackTrace();
                }
            }, set -> {
                try {
                    if (set.next()) {
                        String user = set.getString("Username");

                        username.set(user);
                    } else {
                        MessageUtils.logError("Could not find username with uuid " + uuid + "!");
                    }
                } catch (Exception e) {
                    MessageUtils.logError("Error executing query!");
                    e.printStackTrace();
                }
            });

            if (username.get() == null) return player;

            executeQuery(s2, stmt -> {
                try {
                    stmt.setString(1, uuid);
                } catch (Exception e) {
                    MessageUtils.logError("Error setting statement values!");
                    e.printStackTrace();
                }
            }, set -> {
                try {
                    while (set.next()) {
                        try {
                            String key = set.getString("Key");
                            double pts = set.getDouble("Points");
                            points.put(key, pts);
                        } catch (Exception e) {
                            MessageUtils.logError("Error parsing points!");
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    MessageUtils.logError("Error executing query!");
                    e.printStackTrace();
                }
            });

            PointPlayer pointPlayer = new PointPlayer(uuid, username.get(), points);
            pointPlayer.setLastEditedMillis(System.currentTimeMillis());

            player = Optional.of(pointPlayer);

            return player;
        });
    }

    public CompletableFuture<Long> getLastEditedMillis(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            ensureUsable();

            String s1 = Statements.getStatement(getConnectorSet(), Statements.StatementKey.GET_SYNCING);

            AtomicLong lastEdited = new AtomicLong();

            executeQuery(s1, stmt -> {
                try {
                    stmt.setString(1, uuid);
                } catch (Exception e) {
                    MessageUtils.logError("Error setting statement values!");
                    e.printStackTrace();
                }
            }, set -> {
                try {
                    if (set.next()) {
                        long last = set.getLong("lastEdited");

                        lastEdited.set(last);
                    } else {
                        MessageUtils.logError("Could not find player with uuid " + uuid + "!");
                    }
                } catch (Exception e) {
                    MessageUtils.logError("Error executing query!");
                    e.printStackTrace();
                }
            });

            return lastEdited.get();
        });
    }

    public void dropPoints(String key) {
        String s1 = Statements.getStatement(getConnectorSet(), Statements.StatementKey.DROP_POINTS);

        execute(s1, stmt -> {
            try {
                stmt.setString(1, key);
            } catch (Exception e) {
                MessageUtils.logError("Error setting statement values!");
                e.printStackTrace();
            }
        });
    }

    public void resetPoints(String key, PointPlayer player) {
        String s1 = Statements.getStatement(getConnectorSet(), Statements.StatementKey.RESET_POINTS);

        execute(s1, stmt -> {
            try {
                stmt.setString(1, key);
                stmt.setString(2, player.getIdentifier());
            } catch (Exception e) {
                MessageUtils.logError("Error setting statement values!");
                e.printStackTrace();
            }
        });
    }
}
