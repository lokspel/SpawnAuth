package me.lokspel.spawnauth.dependencies;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

public class IsolatedDriver implements Driver {

    private final String initializer;
    private Driver original;

    public IsolatedDriver(String initializer) {
        this.initializer = initializer;
    }

    public String getInitializer() {
        return initializer;
    }

    public Driver getOriginal() {
        return original;
    }

    public void setOriginal(Driver driver) {
        this.original = driver;
    }

    public boolean isLoaded() {
        return original != null;
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (url.startsWith(initializer)) {
            return original.connect(url.substring(initializer.length()), info);
        }

        return null;
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        if (url.startsWith(initializer)) {
            if (original == null) {
                return false;
            }

            return original.acceptsURL(url.substring(initializer.length()));
        }

        return false;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        if (url.startsWith(initializer)) {
            return original.getPropertyInfo(url.substring(initializer.length()), info);
        }

        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return original.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return original.getMinorVersion();
    }

    @Override
    public boolean jdbcCompliant() {
        return original.jdbcCompliant();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return original.getParentLogger();
    }
}