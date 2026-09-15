package me.lokspel.spawnauth.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.lokspel.spawnauth.config.section.DatabaseSection;
import me.lokspel.spawnauth.database.connection.ConnectionProvider;
import me.lokspel.spawnauth.database.repository.SavedLocationRepository;
import me.lokspel.spawnauth.dependencies.DatabaseLibrary;

import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class Database implements AutoCloseable {

    private final HikariDataSource dataSource;
    private final ConnectionProvider connectionProvider;
    private final ExecutorService executor;
    private final SavedLocationRepository savedLocationRepository;

    private Database(ConnectionProvider connectionProvider, HikariDataSource dataSource,
                     String tablePrefix, ExecutorService executor, boolean isSqlite) {
        this.connectionProvider = connectionProvider;
        this.dataSource = dataSource;
        this.executor = executor;
        this.savedLocationRepository = new SavedLocationRepository(connectionProvider, tablePrefix, executor, isSqlite);
    }

    public static Database forSQLite(DatabaseSection section, Path libsDir, String tablePrefix) throws Exception {
        DatabaseLibrary.SQLITE.ensureLoaded(libsDir);
        String url = DatabaseLibrary.SQLITE.getJdbcUrl(section);

        return new Database(
                () -> DriverManager.getConnection(url),
                null,
                tablePrefix,
                Executors.newSingleThreadExecutor(),
                true
        );
    }

    public static Database forMySQL(DatabaseSection section, Path libsDir) throws Exception {
        DatabaseLibrary.MYSQL.ensureLoaded(libsDir);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DatabaseLibrary.MYSQL.getJdbcUrl(section));
        config.setUsername(section.getMySqlUser());
        config.setPassword(section.getMySqlPassword());
        config.setMaximumPoolSize(section.getMaxPoolSize());
        config.setConnectionTimeout(section.getConnectionTimeoutMs());
        config.setPoolName("spawnauth-mysql");

        HikariDataSource dataSource = new HikariDataSource(config);

        return new Database(
                dataSource::getConnection,
                dataSource,
                section.getTablePrefix(),
                Executors.newFixedThreadPool(Math.max(section.getMaxPoolSize(), 1)),
                false
        );
    }

    public static Database create(DatabaseSection section, Path libsDir, boolean cacheEnabled) throws Exception {
        return switch (section.getType()) {
            case SQLITE -> forSQLite(section, libsDir, section.getTablePrefix());
            case MYSQL -> forMySQL(section, libsDir);
            case NONE -> {
                if (!cacheEnabled) {
                    throw new IllegalStateException("Database type 'none' requires the in-memory cache to be enabled.");
                }
                yield null;
            }
        };
    }

    public SavedLocationRepository getSavedLocationRepository() {
        return savedLocationRepository;
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }

        if (dataSource != null) {
            dataSource.close();
        }
        connectionProvider.close();
    }
}