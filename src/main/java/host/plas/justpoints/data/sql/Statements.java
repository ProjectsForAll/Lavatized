package host.plas.justpoints.data.sql;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Statements {
    @Getter
    public enum MySql {
        CREATE_TABLE_POINTS("CREATE TABLE IF NOT EXISTS `%database%`.`%table_prefix%players` (\n" +
                "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                "  `uuid` nvarchar(36) NOT NULL,\n" +
                "  `username` nvarchar(255) NOT NULL,\n" +
                "  PRIMARY KEY (`id`),\n" +
                "  UNIQUE KEY `uuid` (`uuid`)\n" +
                ");" +
                "CREATE TABLE IF NOT EXISTS `%database%`.`%table_prefix%points` (\n" +
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
}
