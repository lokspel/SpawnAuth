package me.lokspel.spawnauth.database.connection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.lokspel.spawnauth.config.section.DatabaseSection;
import me.lokspel.spawnauth.dependencies.DatabaseLibrary;

import java.sql.Connection;
import java.sql.SQLException;

public final class MySqlConnectionProvider implements ConnectionProvider {

    private final HikariDataSource dataSource;

    public MySqlConnectionProvider(DatabaseSection section) throws Exception {
        DatabaseLibrary.MYSQL.ensureLoaded();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DatabaseLibrary.MYSQL.getJdbcUrl(section));
        config.setUsername(section.getMySqlUser());
        config.setPassword(section.getMySqlPassword());
        config.setMaximumPoolSize(section.getMaxPoolSize());
        config.setConnectionTimeout(section.getConnectionTimeoutMs());
        config.setPoolName("spawnauth-mysql");

        this.dataSource = new HikariDataSource(config);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void close() {
        dataSource.close();
    }
}