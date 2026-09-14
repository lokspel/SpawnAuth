package me.lokspel.spawnauth.database.connection;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class SqliteConnectionProvider implements ConnectionProvider {

    private final String url;

    public SqliteConnectionProvider(File file) {
        this.url = "jdbc:sqlite:" + file.getAbsolutePath();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    @Override
    public void close() {
    }
}