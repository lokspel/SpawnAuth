package me.lokspel.spawnauth.helpers;

import me.lokspel.spawnauth.config.section.LimboSection;
import me.lokspel.spawnauth.utils.FoliaAPI;
import org.bukkit.Bukkit;
import org.bukkit.GameRules;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.WeatherType;

public class GameHelper {
    private final LimboSection config;
    private World authWorld;
    private Location authSpawnLocation;

    public String getAuthWorldName() {
        return config.getWorldName();
    }

    public GameHelper(LimboSection config) {
        this.config = config;
    }

    public World getAuthWorld() {
        World current = Bukkit.getWorld(config.getWorldName());
        if (current == null) {
            authWorld = null;
            authSpawnLocation = null;
        } else {
            authWorld = current;
        }
        return authWorld;
    }

    public Location getAuthSpawnLocation() {
        if (!"vanilla".equals(config.getSpawnMode())) {
            if (authSpawnLocation == null) {
                World world = getAuthWorld();
                if (world == null) return null;
                authSpawnLocation = new Location(world, config.getFixedSpawnX() + 0.5, config.getFixedSpawnY(), config.getFixedSpawnZ() + 0.5, config.getFixedSpawnYaw(), config.getFixedSpawnPitch());
            }
            return authSpawnLocation.clone();
        }

        World authWorld = getAuthWorld();
        if (authWorld == null) return null;

        Location spawn = authWorld.getSpawnLocation();
        int radius = authWorld.getGameRuleValue(GameRules.RESPAWN_RADIUS);
        if (radius > 0) {
            spawn.add(
                    (Math.random() - 0.5) * 2 * radius,
                    0,
                    (Math.random() - 0.5) * 2 * radius
            );
            spawn.setY(authWorld.getHighestBlockYAt(spawn.getBlockX(), spawn.getBlockZ()));
        }
        return spawn;
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

        boolean inLimbo = isInAuthWorld(location) && !isAuthenticated(player);
        player.setCollidable(!inLimbo);
    }

    public void updateLimboWeather(Player player) {
        updateLimboWeather(player, player != null ? player.getLocation() : null);
    }

    public void updateLimboWeather(Player player, Location location) {
        if (player == null) {
            return;
        }

        if (isInAuthWorld(location) && !isAuthenticated(player)) {
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
        this.authSpawnLocation = authWorld != null && !"vanilla".equals(config.getSpawnMode())
                ? new Location(authWorld, config.getFixedSpawnX() + 0.5, config.getFixedSpawnY(), config.getFixedSpawnZ() + 0.5, config.getFixedSpawnYaw(), config.getFixedSpawnPitch())
                : null;
    }

    public void setAuthSpawnLocation(Location authSpawnLocation) {
        this.authSpawnLocation = authSpawnLocation != null ? authSpawnLocation.clone() : null;
    }
}
