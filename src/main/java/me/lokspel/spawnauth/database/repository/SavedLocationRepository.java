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
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

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
        return submit(() -> {
            createTable();
            return null;
        });
    }

    public CompletableFuture<Void> upsert(SavedLocation location) {
        return submit(() -> {
            doUpsert(location);
            return null;
        });
    }

    public CompletableFuture<Void> delete(String name) {
        return submit(() -> {
            doDelete(name);
            return null;
        });
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

    private <T> CompletableFuture<T> submit(Callable<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();

        try {
            executor.submit(() -> {
                try {
                    future.complete(task.call());
                } catch (Exception exception) {
                    LogHelper.LOGGER.log(
                            Level.SEVERE,
                            "Unexpected error in database executor",
                            exception
                    );

                    future.completeExceptionally(exception);
                }
            });
        } catch (RuntimeException exception) {
            future.completeExceptionally(exception);
        }

        return future;
    }

    private void createTable() throws SQLException {
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
        }
    }

    private void addColumnIfMissing(Connection connection, String column, String definition) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition
            );
        } catch (SQLException exception) {
            if (!isColumnAlreadyExists(exception)) {
                throw exception;
            }
        }
    }

    private boolean isColumnAlreadyExists(SQLException exception) {
        String sqlState = exception.getSQLState();

        if ("42S21".equals(sqlState)) {
            return true;
        }

        if (exception.getErrorCode() == 1060) {
            return true;
        }

        String message = exception.getMessage();

        if (message == null) {
            return false;
        }

        String lowerMessage = message.toLowerCase(Locale.ROOT);

        return lowerMessage.contains("duplicate column")
                || lowerMessage.contains("column already exists");
    }

    private void doUpsert(SavedLocation location) throws SQLException {
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
            throw new SQLException(
                    "Failed to save location for '" + location.name() + "': " + exception.getMessage(),
                    exception
            );
        }
    }

    private void doDelete(String name) throws SQLException {
        try (Connection connection = connectionProvider.getConnection()) {
            delete(connection, name);
        } catch (SQLException exception) {
            throw new SQLException(
                    "Failed to delete location for '" + name + "': " + exception.getMessage(),
                    exception
            );
        }
    }

    private SavedLocation doGet(String name) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM " + table + " WHERE name = ?";

        try (Connection connection = connectionProvider.getConnection()) {
            return select(connection, sql, name);
        } catch (SQLException exception) {
            throw new SQLException(
                    "Failed to load location for '" + name + "': " + exception.getMessage(),
                    exception
            );
        }
    }

    private SavedLocation doTake(String name) throws SQLException {
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
                try {
                    connection.setAutoCommit(previousAutoCommit);
                } catch (SQLException restoreException) {
                    LogHelper.LOGGER.log(
                            Level.WARNING,
                            "Failed to restore auto-commit mode",
                            restoreException
                    );
                }
            }
        } catch (SQLException exception) {
            throw new SQLException(
                    "Failed to load and delete location for '" + name + "': " + exception.getMessage(),
                    exception
            );
        }
    }

    private Collection<SavedLocation> doGetAll() throws SQLException {
        List<SavedLocation> locations = new ArrayList<>();

        String sql = "SELECT " + COLUMNS + " FROM " + table;

        try (Connection connection = connectionProvider.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            while (result.next()) {
                locations.add(readLocation(result));
            }

        } catch (SQLException exception) {
            throw new SQLException(
                    "Failed to load all locations: " + exception.getMessage(),
                    exception
            );
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