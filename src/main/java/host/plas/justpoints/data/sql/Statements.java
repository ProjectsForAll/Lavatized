package host.plas.justpoints.data.sql;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Statements {
    @Getter
    public enum MySql {
        CREATE_DATABASE("CREATE DATABASE IF NOT EXISTS `%database%`;"),
        CREATE_PLAYERS_TABLE("CREATE TABLE IF NOT EXISTS `%database%`.`%table_prefix%players` (\n" +
                "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                "  `uuid` nvarchar(36) NOT NULL,\n" +
                "  `username` nvarchar(255) NOT NULL,\n" +
                "  PRIMARY KEY (`id`),\n" +
                "  UNIQUE KEY `uuid` (`uuid`)\n" +
                ");"),
        CREATE_POINTS_TABLE("CREATE TABLE IF NOT EXISTS `%database%`.`%table_prefix%points` (\n" +
                "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                "  `uuid` nvarchar(36) NOT NULL,\n" +
                "  `key` nvarchar(255) NOT NULL,\n" +
                "  `points` double NOT NULL,\n" +
                "  PRIMARY KEY (`id`),\n" +
                "  UNIQUE KEY `uuid_and_key` (`uuid`, `key`)" +
                ");"),
        UPDATE_PLAYER("INSERT INTO `%database%`.`%table_prefix%players` (`uuid`, `username`) VALUES ('%uuid%', " +
                "'%username%') ON DUPLICATE KEY UPDATE `uuid` = '%uuid%', `username` = '%username%';"),
        GET_PLAYER("SELECT * FROM `%database%`.`%table_prefix%players` WHERE `uuid` = '%uuid%';"),
        UPDATE_POINTS("INSERT INTO `%database%`.`%table_prefix%points` (`uuid`, `key`, `points`) VALUES ('%uuid%', " +
                "'%key%', %points%) ON DUPLICATE KEY UPDATE `points` = %points%;"),
        GET_POINTS("SELECT * FROM `%database%`.`%table_prefix%points` WHERE `uuid` = '%uuid%';"),
        ;

        private final String statement;

        MySql(String statement) {
            this.statement = statement;
        }
    }

    @Getter
    public enum SQLite {
        CREATE_DATABASE(""),
        CREATE_PLAYERS_TABLE("CREATE TABLE IF NOT EXISTS `%table_prefix%players` (" +
                "  `id` INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  `uuid` TEXT NOT NULL," +
                "  `username` TEXT NOT NULL," +
                "  UNIQUE(`uuid`)" +
                ");"),
        CREATE_POINTS_TABLE("CREATE TABLE IF NOT EXISTS `%table_prefix%points` (" +
                "  `id` INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  `uuid` TEXT NOT NULL," +
                "  `key` TEXT NOT NULL," +
                "  `points` REAL NOT NULL," +
                "  UNIQUE(`uuid`, `key`)" +
                ");"),
        UPDATE_PLAYER("INSERT OR REPLACE INTO `%table_prefix%players` (`uuid`, `username`) VALUES ('%uuid%', '%username%');"),
        GET_PLAYER("SELECT * FROM `%table_prefix%players` WHERE `uuid` = '%uuid%';"),
        UPDATE_POINTS("INSERT OR REPLACE INTO `%table_prefix%points` (`uuid`, `key`, `points`) VALUES ('%uuid%', " +
                "'%key%', %points%);"),
        GET_POINTS("SELECT * FROM `%table_prefix%points` WHERE `uuid` = '%uuid%';"),
        ;

        private final String statement;

        SQLite(String statement) {
            this.statement = statement;
        }
    }

    public enum StatementKey {
        CREATE_DATABASE,
        CREATE_PLAYERS_TABLE,
        CREATE_POINTS_TABLE,
        UPDATE_PLAYER,
        GET_PLAYER,
        UPDATE_POINTS,
        GET_POINTS
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
