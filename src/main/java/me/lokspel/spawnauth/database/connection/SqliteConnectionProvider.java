package me.lokspel.spawnauth.database.connection;

import me.lokspel.spawnauth.config.section.DatabaseSection;
import me.lokspel.spawnauth.dependencies.DatabaseLibrary;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class SqliteConnectionProvider implements ConnectionProvider {

    private final String url;

    public SqliteConnectionProvider(DatabaseSection section, Path libsDir) throws Exception {
        DatabaseLibrary.SQLITE.ensureLoaded(libsDir);
        this.url = DatabaseLibrary.SQLITE.getJdbcUrl(section);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    @Override
    public void close() {
    }
}