package host.plas.justpoints.data.sql;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum DatabaseType {
    MYSQL("jdbc:mysql://"),
    SQLITE("jdbc:sqlite:"),
    ;

    private String connectionUrlPrefix;

    DatabaseType(String connectionUrlPrefix) {
        this.connectionUrlPrefix = connectionUrlPrefix;
    }
}
