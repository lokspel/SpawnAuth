package me.lokspel.spawnauth.helpers;

import me.lokspel.spawnauth.utils.FoliaAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.WeatherType;

public class GameHelper {
    private final String authWorldName;
    private final double authSpawnX;
    private final double authSpawnY;
    private final double authSpawnZ;
    private final float authSpawnYaw;
    private final float authSpawnPitch;
    private World authWorld;
    private Location authSpawnLocation;

    public GameHelper(String authWorldName, double authSpawnX, double authSpawnY, double authSpawnZ, float authSpawnYaw, float authSpawnPitch) {
        this.authWorldName = authWorldName;
        this.authSpawnX = authSpawnX;
        this.authSpawnY = authSpawnY;
        this.authSpawnZ = authSpawnZ;
        this.authSpawnYaw = authSpawnYaw;
        this.authSpawnPitch = authSpawnPitch;
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
            World authWorld = getAuthWorld();
            if (authWorld != null) {
                authSpawnLocation = new Location(authWorld, authSpawnX + 0.5, authSpawnY, authSpawnZ + 0.5, authSpawnYaw, authSpawnPitch);
            }
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
            LogHelper.LOGGER.warning("Teleport was skipped because the target player reference was null.");
            return;
        }

        if (location == null || location.getWorld() == null) {
            LogHelper.LOGGER.warning(() -> "Teleport was skipped for player '" + player.getName()
                    + "' because the destination location or world was null.");
            return;
        }

        FoliaAPI.teleportPlayer(player, location, FoliaAPI.isFolia());
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
        this.authSpawnLocation = authWorld != null ? new Location(authWorld, authSpawnX + 0.5, authSpawnY, authSpawnZ + 0.5, authSpawnYaw, authSpawnPitch) : null;
    }

    public void setAuthSpawnLocation(Location authSpawnLocation) {
        this.authSpawnLocation = authSpawnLocation != null ? authSpawnLocation.clone() : null;
    }
}
