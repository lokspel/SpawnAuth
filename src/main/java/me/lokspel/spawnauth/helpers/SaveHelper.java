package me.lokspel.spawnauth.helpers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;

public class SaveHelper {
    //noinspection SqlNoDataSourceInspection
    private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS PlayerLocations (name TEXT NOT NULL PRIMARY KEY, x REAL NOT NULL, y REAL NOT NULL, z REAL NOT NULL, world TEXT NOT NULL)";
    //noinspection SqlNoDataSourceInspection
    private static final String CREATE_INDEX_SQL = "CREATE UNIQUE INDEX IF NOT EXISTS idx_player_locations_name ON PlayerLocations(name)";
    //noinspection SqlNoDataSourceInspection
    private static final String UPSERT_LOCATION_SQL = "INSERT INTO PlayerLocations (name, x, y, z, world) VALUES (?, ?, ?, ?, ?) ON CONFLICT(name) DO UPDATE SET x = excluded.x, y = excluded.y, z = excluded.z, world = excluded.world";
    //noinspection SqlNoDataSourceInspection
    private static final String DELETE_LOCATION_SQL = "DELETE FROM PlayerLocations WHERE name = ?";
    //noinspection SqlNoDataSourceInspection
    private static final String SELECT_LOCATION_SQL = "SELECT * FROM PlayerLocations WHERE name = ?";
    //noinspection SqlNoDataSourceInspection
    private static final String SELECT_ALL_LOCATIONS_SQL = "SELECT * FROM PlayerLocations";
    //noinspection SqlNoDataSourceInspection
    private static final String SELECT_AND_DELETE_LOCATION_SQL = "DELETE FROM PlayerLocations WHERE name = ? RETURNING x, y, z, world";

    private final String dataBaseURL;

    public SaveHelper(File dataBaseFolder) {
        this.dataBaseURL = "jdbc:sqlite:" + dataBaseFolder + File.separator + "SpawnAuth.db";
    }

    public void setupDataBase() {
        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(CREATE_TABLE_SQL)) {
                preparedStatement.executeUpdate();
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(CREATE_INDEX_SQL)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException exception) {
            LogHelper.LOGGER.warning(() -> "Failed to setup database: " + exception.getMessage());
        }
    }

    public void saveLocation(String name, Location location) {
        if (location == null || location.getWorld() == null) {
            return;
        }

        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(UPSERT_LOCATION_SQL)) {
                preparedStatement.setString(1, name);
                preparedStatement.setDouble(2, location.getX());
                preparedStatement.setDouble(3, location.getY());
                preparedStatement.setDouble(4, location.getZ());
                preparedStatement.setString(5, location.getWorld().getName());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException exception) {
            LogHelper.LOGGER.warning(() -> "Failed to save location for " + name + ": " + exception.getMessage());
        }
    }

    public void removeLocation(String name) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(DELETE_LOCATION_SQL)) {
                preparedStatement.setString(1, name);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException exception) {
            LogHelper.LOGGER.warning(() -> "Failed to remove location for " + name + ": " + exception.getMessage());
        }
    }

    public Location getLocation(String name) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_LOCATION_SQL)) {
                preparedStatement.setString(1, name);
                try (ResultSet result = preparedStatement.executeQuery()) {
                    if (result.next()) {
                        return readLocation(result);
                    }
                }
            }
        } catch (SQLException exception) {
            LogHelper.LOGGER.warning(() -> "Failed to load location for " + name + ": " + exception.getMessage());
        }
        return null;
    }

    public Location takeLocation(String name) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_AND_DELETE_LOCATION_SQL)) {
                preparedStatement.setString(1, name);
                try (ResultSet result = preparedStatement.executeQuery()) {
                    if (result.next()) {
                        return readLocation(result);
                    }
                }
            }
        } catch (SQLException exception) {
            LogHelper.LOGGER.warning(() -> "Failed to take location for " + name + ": " + exception.getMessage());
        }
        return null;
    }

    public void handleDisable(GameHelper gameHelper) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_LOCATIONS_SQL)) {
                try (ResultSet result = preparedStatement.executeQuery()) {
                    while (result.next()) {
                        try {
                            Location location = readLocation(result);
                            Player player = Bukkit.getPlayer(result.getString("name"));

                            if (location != null && player != null && player.isOnline()) {
                                gameHelper.teleport(player, location);
                                removeLocation(player.getName());
                            }
                        } catch (Exception exception) {
                    LogHelper.LOGGER.warning(() -> "Failed to restore a player location on disable: " + exception.getMessage());
                        }
                    }
                }
            }
        } catch (SQLException exception) {
            LogHelper.LOGGER.warning(() -> "Failed to handle disable: " + exception.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(dataBaseURL);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA journal_mode=WAL");
            statement.execute("PRAGMA synchronous=NORMAL");
            statement.execute("PRAGMA busy_timeout=5000");
        }
        return connection;
    }

    private Location readLocation(ResultSet result) throws SQLException {
        var world = Bukkit.getWorld(result.getString("world"));
        if (world == null) {
            return null;
        }

        return new Location(world, result.getDouble("x"), result.getDouble("y"), result.getDouble("z"));
    }
}
