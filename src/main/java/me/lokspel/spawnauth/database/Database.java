package me.lokspel.spawnauth.database;

import me.lokspel.spawnauth.config.section.DatabaseSection;
import me.lokspel.spawnauth.database.connection.ConnectionProvider;
import me.lokspel.spawnauth.database.connection.MySqlConnectionProvider;
import me.lokspel.spawnauth.database.connection.SqliteConnectionProvider;
import me.lokspel.spawnauth.database.repository.SavedLocationRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class Database implements AutoCloseable {

    private final ConnectionProvider connectionProvider;
    private final ExecutorService executor;
    private final SavedLocationRepository savedLocationRepository;

    private Database(ConnectionProvider connectionProvider, String tablePrefix, ExecutorService executor) {
        this.connectionProvider = connectionProvider;
        this.executor = executor;
        this.savedLocationRepository = new SavedLocationRepository(connectionProvider, tablePrefix, executor);
    }

    public static Database forSQLite(DatabaseSection section, String tablePrefix) throws Exception {
        return new Database(
                new SqliteConnectionProvider(section),
                tablePrefix,
                Executors.newSingleThreadExecutor()
        );
    }

    public static Database forMySQL(DatabaseSection section) throws Exception {
        return new Database(
                new MySqlConnectionProvider(section),
                section.getTablePrefix(),
                Executors.newFixedThreadPool(Math.max(section.getMaxPoolSize(), 1))
        );
    }

    public static Database create(DatabaseSection section, boolean cacheEnabled) throws Exception {
        return switch (section.getType()) {
            case SQLITE -> forSQLite(section, section.getTablePrefix());
            case MYSQL -> forMySQL(section);
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
        connectionProvider.close();
    }
}