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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class SavedLocationRepository {

    private final ConnectionProvider connectionProvider;
    private final ExecutorService executor;
    private final String table;

    public SavedLocationRepository(ConnectionProvider connectionProvider, String tablePrefix, ExecutorService executor) {
        this.connectionProvider = connectionProvider;
        this.executor = executor;
        this.table = tablePrefix + "_PlayerLocations";
    }

    public void init() {
        execute(this::createTable);
    }

    public void upsert(SavedLocation location) {
        execute(() -> doUpsert(location));
    }

    public void delete(String name) {
        execute(() -> doDelete(name));
    }

    public SavedLocation get(String name) {
        return query(() -> doGet(name));
    }

    public SavedLocation take(String name) {
        return query(() -> doTake(name));
    }

    public Collection<SavedLocation> getAll() {
        return query(this::doGetAll);
    }

    private void execute(Runnable task) {
        try {
            executor.submit(task).get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException exception) {
            LogHelper.LOGGER.severe(() ->
                    "Unexpected error in database executor: " + exception.getCause());
        }
    }

    private <T> T query(Callable<T> task) {
        try {
            return executor.submit(task).get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException exception) {
            LogHelper.LOGGER.severe(() ->
                    "Unexpected error in database executor: " + exception.getCause());
            return null;
        }
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS " + table + " (" +
                "name VARCHAR(64) PRIMARY KEY," +
                "x DOUBLE NOT NULL," +
                "y DOUBLE NOT NULL," +
                "z DOUBLE NOT NULL," +
                "world VARCHAR(128) NOT NULL" +
                ")";
        try (Connection connection = connectionProvider.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException exception) {
            LogHelper.LOGGER.severe(() -> "Failed to create the locations table: " + exception.getMessage());
        }
    }

    private void doUpsert(SavedLocation location) {
        try (Connection connection = connectionProvider.getConnection()) {
            if (exists(connection, location.name())) {
                update(connection, location);
            } else {
                insert(connection, location);
            }
        } catch (SQLException exception) {
            LogHelper.LOGGER.warning(() ->
                    "Failed to save location for '" + location.name() + "': " + exception.getMessage());
        }
    }

    private void doDelete(String name) {
        String sql = "DELETE FROM " + table + " WHERE name = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.executeUpdate();
        } catch (SQLException exception) {
            LogHelper.LOGGER.warning(() ->
                    "Failed to delete location for '" + name + "': " + exception.getMessage());
        }
    }

    private SavedLocation doGet(String name) {
        String sql = "SELECT name, x, y, z, world FROM " + table + " WHERE name = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? readLocation(result) : null;
            }
        } catch (SQLException exception) {
            LogHelper.LOGGER.warning(() ->
                    "Failed to load location for '" + name + "': " + exception.getMessage());
        }
        return null;
    }

    private SavedLocation doTake(String name) {
        try (Connection connection = connectionProvider.getConnection()) {
            SavedLocation location = null;
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT name, x, y, z, world FROM " + table + " WHERE name = ?")) {
                statement.setString(1, name);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        location = readLocation(result);
                    }
                }
            }
            if (location != null) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM " + table + " WHERE name = ?")) {
                    statement.setString(1, name);
                    statement.executeUpdate();
                }
            }
            return location;
        } catch (SQLException exception) {
            LogHelper.LOGGER.warning(() ->
                    "Failed to load and delete location for '" + name + "': " + exception.getMessage());
        }
        return null;
    }

    private Collection<SavedLocation> doGetAll() {
        List<SavedLocation> locations = new ArrayList<>();
        String sql = "SELECT name, x, y, z, world FROM " + table;
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

    private boolean exists(Connection connection, String name) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT name FROM " + table + " WHERE name = ?")) {
            statement.setString(1, name);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private void insert(Connection connection, SavedLocation location) throws SQLException {
        String sql = "INSERT INTO " + table + " (name, x, y, z, world) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, location);
            statement.executeUpdate();
        }
    }

    private void update(Connection connection, SavedLocation location) throws SQLException {
        String sql = "UPDATE " + table + " SET x = ?, y = ?, z = ?, world = ? WHERE name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, location.x());
            statement.setDouble(2, location.y());
            statement.setDouble(3, location.z());
            statement.setString(4, location.world());
            statement.setString(5, location.name());
            statement.executeUpdate();
        }
    }

    private void bind(PreparedStatement statement, SavedLocation location) throws SQLException {
        statement.setString(1, location.name());
        statement.setDouble(2, location.x());
        statement.setDouble(3, location.y());
        statement.setDouble(4, location.z());
        statement.setString(5, location.world());
    }

    private SavedLocation readLocation(ResultSet result) throws SQLException {
        return new SavedLocation(
                result.getString("name"),
                result.getString("world"),
                result.getDouble("x"),
                result.getDouble("y"),
                result.getDouble("z")
        );
    }
}