package me.lokspel.spawnauth.helpers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.WeatherType;

public class GameHelper {
    private final String authWorldName;
    private World authWorld;
    private Location authSpawnLocation;

    public GameHelper(String authWorldName) {
        this.authWorldName = authWorldName;
    }

    public Location getSpawnLocation(World world) {
        return world != null ? world.getSpawnLocation().toCenterLocation() : null;
    }

    public World getAuthWorld() {
        if (authWorld == null) {
            authWorld = Bukkit.getWorld(authWorldName);
        }
        return authWorld;
    }

    public Location getAuthSpawnLocation() {
        if (authSpawnLocation == null) {
            authSpawnLocation = getSpawnLocation(getAuthWorld());
        }
        return authSpawnLocation != null ? authSpawnLocation.clone() : null;
    }

    public boolean isInAuthWorld(Location location) {
        World authWorld = getAuthWorld();
        return location != null && location.getWorld() != null && authWorld != null && location.getWorld().equals(authWorld);
    }

    public boolean isAtAuthSpawn(Location location) {
        Location authSpawn = getAuthSpawnLocation();
        if (location == null || authSpawn == null || location.getWorld() == null || authSpawn.getWorld() == null) {
            return false;
        }

        if (!location.getWorld().equals(authSpawn.getWorld())) {
            return false;
        }

        return location.distanceSquared(authSpawn) <= 4.0;
    }

    public void teleport(Player player, Location location) {
        if (player == null) {
            LogHelper.LOGGER.warning("Tried to teleport a null player.");
            return;
        }

        if (location == null || location.getWorld() == null) {
            LogHelper.LOGGER.warning(() -> "Tried to teleport " + player.getName() + " but location was null.");
            return;
        }

        player.teleport(location);
    }

    public boolean isAuthenticated(Player player) {
        return AuthHelper.isAuthenticated(player);
    }

    public void updateLimboCollision(Player player) {
        if (player == null) {
            return;
        }

        updateLimboCollision(player, player.getLocation());
    }

    public void updateLimboCollision(Player player, Location location) {
        if (player == null) {
            return;
        }

        player.setCollidable(!isInAuthWorld(location));
    }

    public void updateLimboWeather(Player player) {
        updateLimboWeather(player, player != null ? player.getLocation() : null);
    }

    public void updateLimboWeather(Player player, Location location) {
        if (player == null) {
            return;
        }

        if (isInAuthWorld(location)) {
            player.setPlayerWeather(WeatherType.CLEAR);
            return;
        }

        player.resetPlayerWeather();
    }

    public void resetCollision(Player player) {
        if (player == null) {
            return;
        }

        player.setCollidable(true);
    }

    public void resetWeather(Player player) {
        if (player == null) {
            return;
        }

        player.resetPlayerWeather();
    }

    public void setAuthWorld(World authWorld) {
        this.authWorld = authWorld;
        this.authSpawnLocation = getSpawnLocation(authWorld);
    }
}
