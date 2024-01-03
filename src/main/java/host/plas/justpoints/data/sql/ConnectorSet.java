package host.plas.justpoints.data.sql;

import lombok.Getter;

@Getter
public class ConnectorSet {
    private String host;
    private String port;
    private String database;
    private String tablePrefix;
    private String username;
    private String password;
    private String options;

    public ConnectorSet(String host, String port, String database, String tablePrefix, String username,
                        String password, String options) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.tablePrefix = tablePrefix;
        this.username = username;
        this.password = password;
        this.options = options;
    }

    public ConnectorSet() {
        this("localhost", "3306", "points", "pnts_", "username", "password", "useSSL=false");
    }

    public ConnectorSet setHost(String host) {
        this.host = host;
        return this;
    }

    public ConnectorSet setPort(String port) {
        this.port = port;
        return this;
    }

    public ConnectorSet setDatabase(String database) {
        this.database = database;
        return this;
    }

    public ConnectorSet setTablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
        return this;
    }

    public ConnectorSet setUsername(String username) {
        this.username = username;
        return this;
    }

    public ConnectorSet setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getMySqlConnectionString() {
        return "jdbc:mysql://" + host + ":" + port + "/" + database + "?" + options;
    }
}
