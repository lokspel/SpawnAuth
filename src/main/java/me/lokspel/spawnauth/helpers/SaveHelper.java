package me.lokspel.spawnauth.helpers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;

public class SaveHelper {

    //noinspection SqlNoDataSourceInspection
    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS PlayerLocations (" +
                    "name TEXT PRIMARY KEY," +
                    "x REAL NOT NULL," +
                    "y REAL NOT NULL," +
                    "z REAL NOT NULL," +
                    "world TEXT NOT NULL" +
                    ")";

    //noinspection SqlNoDataSourceInspection
    private static final String UPSERT_LOCATION_SQL =
            "INSERT INTO PlayerLocations (name, x, y, z, world) VALUES (?, ?, ?, ?, ?) " +
                    "ON CONFLICT(name) DO UPDATE SET " +
                    "x = excluded.x, " +
                    "y = excluded.y, " +
                    "z = excluded.z, " +
                    "world = excluded.world";

    //noinspection SqlNoDataSourceInspection
    private static final String DELETE_LOCATION_SQL =
            "DELETE FROM PlayerLocations WHERE name = ?";

    //noinspection SqlNoDataSourceInspection
    private static final String SELECT_LOCATION_SQL =
            "SELECT x, y, z, world FROM PlayerLocations WHERE name = ?";

    //noinspection SqlNoDataSourceInspection
    private static final String SELECT_ALL_LOCATIONS_SQL =
            "SELECT name, x, y, z, world FROM PlayerLocations";

    //noinspection SqlNoDataSourceInspection
    private static final String SELECT_AND_DELETE_LOCATION_SQL =
            "DELETE FROM PlayerLocations WHERE name = ? RETURNING x, y, z, world";

    private final String dataBaseURL;

    public SaveHelper(File dataBaseFolder) {
        this.dataBaseURL = "jdbc:sqlite:" + dataBaseFolder + File.separator + "SpawnAuth.db";
    }

    public void setupDataBase() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(CREATE_TABLE_SQL);

        } catch (SQLException exception) {
            LogHelper.LOGGER.warning(() ->
                    "Failed to initialize the SQLite database: " + exception.getMessage());
        }
    }

    public void saveLocation(String name, Location location) {
        if (location == null || location.getWorld() == null) {
            return;
        }

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPSERT_LOCATION_SQL)) {

            statement.setString(1, name);
            statement.setDouble(2, location.getX());
            statement.setDouble(3, location.getY());
            statement.setDouble(4, location.getZ());
            statement.setString(5, location.getWorld().getName());

            statement.executeUpdate();

        } catch (SQLException exception) {
            LogHelper.LOGGER.warning(() ->
                    "Failed to save location for '" + name + "': " + exception.getMessage());
        }
    }

    public void removeLocation(String name) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_LOCATION_SQL)) {

            statement.setString(1, name);
            statement.executeUpdate();

        } catch (SQLException exception) {
            LogHelper.LOGGER.warning(() ->
                    "Failed to remove location for '" + name + "': " + exception.getMessage());
        }
    }

    public Location getLocation(String name) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_LOCATION_SQL)) {

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

    public Location takeLocation(String name) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_AND_DELETE_LOCATION_SQL)) {

            statement.setString(1, name);

            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? readLocation(result) : null;
            }

        } catch (SQLException exception) {
            LogHelper.LOGGER.warning(() ->
                    "Failed to load and remove location for '" + name + "': " + exception.getMessage());
        }

        return null;
    }

    public void handleDisable(GameHelper gameHelper) {
        try (Connection connection = getConnection();
             PreparedStatement select = connection.prepareStatement(SELECT_ALL_LOCATIONS_SQL);
             PreparedStatement delete = connection.prepareStatement(DELETE_LOCATION_SQL);
             ResultSet result = select.executeQuery()) {

            while (result.next()) {
                try {
                    Location location = readLocation(result);
                    Player player = Bukkit.getPlayer(result.getString("name"));

                    if (location != null && player != null && player.isOnline()) {
                        gameHelper.teleport(player, location);

                        delete.setString(1, player.getName());
                        delete.executeUpdate();
                    }
                } catch (Exception exception) {
                    LogHelper.LOGGER.warning(() ->
                            "Failed to restore player location: " + exception.getMessage());
                }
            }

        } catch (SQLException exception) {
            LogHelper.LOGGER.warning(() ->
                    "Failed to process stored locations: " + exception.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dataBaseURL);
    }

    private Location readLocation(ResultSet result) throws SQLException {
        var world = Bukkit.getWorld(result.getString("world"));
        if (world == null) {
            return null;
        }

        return new Location(
                world,
                result.getDouble("x"),
                result.getDouble("y"),
                result.getDouble("z")
        );
    }
}