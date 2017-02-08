package com.waves_rsp.ikb4stream.core.metrics;

import java.util.Objects;

/**
 * Define properties to connect to the influx database for metrics
 */
public class MetricsProperties {
    private final String host;
    private final String user;
    private final String password;
    private final String dbName;

    private MetricsProperties(String host, String user, String password, String dbName) {
        Objects.requireNonNull(host);
        Objects.requireNonNull(user);
        Objects.requireNonNull(password);
        Objects.requireNonNull(dbName);

        this.host = checkArgument(host);
        this.user = checkArgument(user);
        this.password = password;
        this.dbName = checkArgument(dbName);
    }

    /**
     * Singleton to get properties from influx database
     *
     * @param host url to connect to the database
     * @param user the user who access to the database
     * @param password the user password
     * @param dbName the database name which requested
     *
     * @return the object instance for MetricsProperties
     */
    public static MetricsProperties create(String host, String user, String password, String dbName) {
        return new MetricsProperties(host, user, password, dbName);
    }

    public String getHost() {
        return host;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getDbName() {
        return dbName;
    }

    private static String checkArgument(String argument) {
        if(argument.isEmpty())
            throw new IllegalArgumentException(argument+" can't be empty");
        return argument;
    }
}
