package host.plas.justpoints.data.sql;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Statements {
    @Getter
    public enum MySql {
        CREATE_DATABASE("CREATE DATABASE IF NOT EXISTS `%database%`;"),
        CREATE_TABLES("CREATE TABLE IF NOT EXISTS `%database%`.`%table_prefix%players` (\n" +
                "  `Uuid` nvarchar(36) NOT NULL PRIMARY KEY,\n" +
                "  `Username` nvarchar(255) NOT NULL\n" +
                ");;" +
                "CREATE TABLE IF NOT EXISTS `%database%`.`%table_prefix%points` (\n" +
                "  `Uuid` nvarchar(36) NOT NULL,\n" +
                "  `Key` nvarchar(255) NOT NULL,\n" +
                "  `Points` double NOT NULL,\n" +
                "  PRIMARY KEY (`Uuid`, `Key`)\n" +
                ");;" +
                "CREATE TABLE IF NOT EXISTS `%database%`.`%table_prefix%syncing` (\n" +
                "  `Uuid` nvarchar(36) NOT NULL,\n" +
                "  `LastEdited` bigint NOT NULL,\n" +
                "  PRIMARY KEY (`Uuid`)\n" +
                ");;"),
        UPDATE_PLAYER("INSERT INTO `%database%`.`%table_prefix%players` (`Uuid`, `Username`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `Username` = ?;"),
        GET_PLAYER("SELECT * FROM `%database%`.`%table_prefix%players` WHERE `Uuid` = ?;"),
        UPDATE_POINTS("INSERT INTO `%database%`.`%table_prefix%points` (`Uuid`, `Key`, `Points`) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE `Points` = ?;"),
        GET_POINTS("SELECT * FROM `%database%`.`%table_prefix%points` WHERE `Uuid` = ?;"),
        UPDATE_SYNCING("INSERT INTO `%database%`.`%table_prefix%syncing` (`Uuid`, `LastEdited`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `LastEdited` = ?;"),
        GET_SYNCING("SELECT * FROM `%database%`.`%table_prefix%syncing` WHERE `Uuid` = ?;"),
        DROP_POINTS("DELETE FROM `%database%`.`%table_prefix%points` WHERE `Key` = ?;"),
        RESET_POINTS("DELETE FROM `%table_prefix%points` WHERE `Key` = ? AND `Uuid` = ?;"),
        ;

        private final String statement;

        MySql(String statement) {
            this.statement = statement;
        }
    }

    @Getter
    public enum SQLite {
        CREATE_DATABASE(""),
        CREATE_TABLES("CREATE TABLE IF NOT EXISTS `%table_prefix%players` (" +
                "  `Uuid` TEXT NOT NULL PRIMARY KEY," +
                "  `Username` TEXT NOT NULL" +
                ");;" +
                "CREATE TABLE IF NOT EXISTS `%table_prefix%points` (" +
                "  `Uuid` TEXT NOT NULL," +
                "  `Key` TEXT NOT NULL," +
                "  `Points` REAL NOT NULL," +
                "  PRIMARY KEY (`Uuid`, `Key`)" +
                ");;" +
                "CREATE TABLE IF NOT EXISTS `%table_prefix%syncing` (" +
                "  `Uuid` TEXT NOT NULL," +
                "  `LastEdited` INTEGER NOT NULL," +
                "  PRIMARY KEY (`Uuid`)" +
                ");;"),
        UPDATE_PLAYER("INSERT OR REPLACE INTO `%table_prefix%players` (`Uuid`, `Username`) VALUES (?, ?);"),
        GET_PLAYER("SELECT * FROM `%table_prefix%players` WHERE `Uuid` = ?;"),
        UPDATE_POINTS("INSERT OR REPLACE INTO `%table_prefix%points` (`Uuid`, `Key`, `Points`) VALUES (?, ?, ?);"),
        GET_POINTS("SELECT * FROM `%table_prefix%points` WHERE `Uuid` = ?;"),
        UPDATE_SYNCING("INSERT OR REPLACE INTO `%table_prefix%syncing` (`Uuid`, `LastEdited`) VALUES (?, ?);"),
        GET_SYNCING("SELECT * FROM `%table_prefix%syncing` WHERE `Uuid` = ?;"),
        DROP_POINTS("DELETE FROM `%table_prefix%points` WHERE `Key` = ?;"),
        RESET_POINTS("DELETE FROM `%table_prefix%points` WHERE `Key` = ? AND `Uuid` = ?;"),
        ;

        private final String statement;

        SQLite(String statement) {
            this.statement = statement;
        }
    }

    public enum StatementKey {
        CREATE_DATABASE,
        CREATE_TABLES,
        UPDATE_PLAYER,
        GET_PLAYER,
        UPDATE_POINTS,
        GET_POINTS,
        UPDATE_SYNCING,
        GET_SYNCING,
        DROP_POINTS,
        RESET_POINTS,
        ;
    }

    public static String getStatement(ConnectorSet connectorSet, String key) {
        switch (connectorSet.getType()) {
            case MYSQL:
                return MySql.valueOf(key).getStatement()
                        .replace("%database%", connectorSet.getDatabase())
                        .replace("%table_prefix%", connectorSet.getTablePrefix());
            case SQLITE:
                return SQLite.valueOf(key).getStatement()
                        .replace("%table_prefix%", connectorSet.getTablePrefix());
            default:
                return null;
        }
    }

    public static String getStatement(ConnectorSet connectorSet, StatementKey key) {
        return getStatement(connectorSet, key.name());
    }
}
