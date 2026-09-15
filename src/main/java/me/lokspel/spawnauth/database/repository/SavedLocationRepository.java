package me.lokspel.spawnauth.database.repository;

import me.lokspel.spawnauth.database.connection.ConnectionProvider;
import me.lokspel.spawnauth.database.model.SavedLocation;
import me.lokspel.spawnauth.helpers.LogHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class SavedLocationRepository {

    private static final String COLUMNS = "name, x, y, z, yaw, pitch, world";

    private static final String UPSERT_SQLITE =
            "INSERT INTO %s (name, x, y, z, yaw, pitch, world) VALUES (?, ?, ?, ?, ?, ?, ?) " +
            "ON CONFLICT(name) DO UPDATE SET x = excluded.x, y = excluded.y, z = excluded.z, " +
            "yaw = excluded.yaw, pitch = excluded.pitch, world = excluded.world";

    private static final String UPSERT_MYSQL =
            "INSERT INTO %s (name, x, y, z, yaw, pitch, world) VALUES (?, ?, ?, ?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE x = VALUES(x), y = VALUES(y), z = VALUES(z), " +
            "yaw = VALUES(yaw), pitch = VALUES(pitch), world = VALUES(world)";

    private final ConnectionProvider connectionProvider;
    private final ExecutorService executor;
    private final String table;
    private final boolean isSqlite;

    public SavedLocationRepository(ConnectionProvider connectionProvider, String tablePrefix,
                                   ExecutorService executor, boolean isSqlite) {
        this.connectionProvider = connectionProvider;
        this.executor = executor;
        this.isSqlite = isSqlite;
        this.table = tablePrefix + "_PlayerLocations";
    }

    public CompletableFuture<Void> init() {
        return submit(this::createTable);
    }

    public CompletableFuture<Void> upsert(SavedLocation location) {
        return submit(() -> doUpsert(location));
    }

    public CompletableFuture<Void> delete(String name) {
        return submit(() -> doDelete(name));
    }

    public CompletableFuture<SavedLocation> get(String name) {
        return submit(() -> doGet(name));
    }

    public CompletableFuture<SavedLocation> take(String name) {
        return submit(() -> doTake(name));
    }

    public CompletableFuture<Collection<SavedLocation>> getAll() {
        return submit(this::doGetAll);
    }

    private CompletableFuture<Void> submit(Runnable task) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        executor.submit(() -> {
            try {
                task.run();
                future.complete(null);
            } catch (Exception exception) {
                LogHelper.LOGGER.severe(() ->
                        "Unexpected error in database executor: " + exception.getMessage());
                future.completeExceptionally(exception);
            }
        });
        return future;
    }

    private <T> CompletableFuture<T> submit(Callable<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();
        executor.submit(() -> {
            try {
                future.complete(task.call());
            } catch (Exception exception) {
                LogHelper.LOGGER.severe(() ->
                        "Unexpected error in database executor: " + exception.getMessage());
                future.completeExceptionally(exception);
            }
        });
        return future;
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS " + table + " (" +
                "name VARCHAR(64) PRIMARY KEY," +
                "x DOUBLE NOT NULL," +
                "y DOUBLE NOT NULL," +
                "z DOUBLE NOT NULL," +
                "yaw FLOAT NOT NULL DEFAULT 0," +
                "pitch FLOAT NOT NULL DEFAULT 0," +
                "world VARCHAR(128) NOT NULL" +
                ")";

        try (Connection connection = connectionProvider.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);

            addColumnIfMissing(connection, "yaw", "FLOAT NOT NULL DEFAULT 0");
            addColumnIfMissing(connection, "pitch", "FLOAT NOT NULL DEFAULT 0");

        } catch (SQLException exception) {
            LogHelper.LOGGER.severe(() ->
                    "Failed to create/update the locations table: " + exception.getMessage());
        }
    }

    private void addColumnIfMissing(Connection connection, String column, String definition) {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition
            );
        } catch (SQLException ignored) {
        }
    }

    private void doUpsert(SavedLocation location) {
        String sql = String.format(isSqlite ? UPSERT_SQLITE : UPSERT_MYSQL, table);

        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, location.name());
            statement.setDouble(2, location.x());
            statement.setDouble(3, location.y());
            statement.setDouble(4, location.z());
            statement.setFloat(5, location.yaw());
            statement.setFloat(6, location.pitch());
            statement.setString(7, location.world());

            statement.executeUpdate();

        } catch (SQLException exception) {
            LogHelper.LOGGER.warning(() ->
                    "Failed to save location for '" + location.name() + "': " + exception.getMessage());
        }
    }

    private void doDelete(String name) {
        try (Connection connection = connectionProvider.getConnection()) {
            delete(connection, name);
        } catch (SQLException exception) {
            LogHelper.LOGGER.warning(() ->
                    "Failed to delete location for '" + name + "': " + exception.getMessage());
        }
    }

    private SavedLocation doGet(String name) {
        String sql = "SELECT " + COLUMNS + " FROM " + table + " WHERE name = ?";

        try (Connection connection = connectionProvider.getConnection()) {
            return select(connection, sql, name);
        } catch (SQLException exception) {
            LogHelper.LOGGER.warning(() ->
                    "Failed to load location for '" + name + "': " + exception.getMessage());
        }

        return null;
    }

    private SavedLocation doTake(String name) {
        String sql = "SELECT " + COLUMNS + " FROM " + table + " WHERE name = ?"
                + (isSqlite ? "" : " FOR UPDATE");

        try (Connection connection = connectionProvider.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                SavedLocation location = select(connection, sql, name);
                if (location != null) {
                    delete(connection, name);
                }
                connection.commit();
                return location;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        } catch (SQLException exception) {
            LogHelper.LOGGER.warning(() ->
                    "Failed to load and delete location for '" + name + "': " + exception.getMessage());
        }

        return null;
    }

    private Collection<SavedLocation> doGetAll() {
        List<SavedLocation> locations = new ArrayList<>();

        String sql = "SELECT " + COLUMNS + " FROM " + table;

        try (Connection connection = connectionProvider.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            while (result.next()) {
                locations.add(readLocation(result));
            }

        } catch (SQLException exception) {
            LogHelper.LOGGER.warning(() ->
                    "Failed to load all locations: " + exception.getMessage());
        }

        return locations;
    }

    private SavedLocation select(Connection connection, String sql, String name) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);

            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? readLocation(result) : null;
            }
        }
    }

    private void delete(Connection connection, String name) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM " + table + " WHERE name = ?")) {

            statement.setString(1, name);
            statement.executeUpdate();
        }
    }

    private SavedLocation readLocation(ResultSet result) throws SQLException {
        return new SavedLocation(
                result.getString("name"),
                result.getString("world"),
                result.getDouble("x"),
                result.getDouble("y"),
                result.getDouble("z"),
                result.getFloat("yaw"),
                result.getFloat("pitch")
        );
    }
}