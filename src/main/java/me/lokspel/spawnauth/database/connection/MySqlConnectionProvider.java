package me.lokspel.spawnauth.database.connection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.lokspel.spawnauth.config.section.DatabaseSection;

import java.sql.Connection;
import java.sql.SQLException;

public final class MySqlConnectionProvider implements ConnectionProvider {

    private final HikariDataSource dataSource;

    public MySqlConnectionProvider(DatabaseSection section) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + section.getMySqlHost() + ":" + section.getMySqlPort() +
                "/" + section.getMySqlDatabase() + "?" + section.getMySqlUrlParameters());
        config.setUsername(section.getMySqlUser());
        config.setPassword(section.getMySqlPassword());
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
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