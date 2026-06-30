package me.lokspel.spawnauth.helpers;

import me.lokspel.spawnauth.config.section.LimboSection;
import me.lokspel.spawnauth.utils.FoliaAPI;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public class GameHelper {
    private final LimboSection config;
    private Location authSpawnLocation;

    public GameHelper(LimboSection config) {
        this.config = config;
    }

    public World getAuthWorld() {
        World world = Bukkit.getWorld(config.getOverworldName());
        if (world == null) {
            authSpawnLocation = null;
        }
        return world;
    }

    public Location getAuthSpawnLocation() {
        if (!"vanilla".equals(config.getSpawnMode())) {
            if (authSpawnLocation == null) {
                World world = Bukkit.getWorld(config.getGenerationWorldName());
                if (world == null) return null;
                authSpawnLocation = new Location(world, config.getFixedSpawnX() + 0.5, config.getFixedSpawnY(), config.getFixedSpawnZ() + 0.5, config.getFixedSpawnYaw(), config.getFixedSpawnPitch());
            }
            return authSpawnLocation.clone();
        }

        World world = getAuthWorld();
        if (world == null) return null;

        return getServerSpawnLocation(world);
    }

    private Location getServerSpawnLocation(World world) {
        Location spawn = world.getSpawnLocation();
        int radius = world.getGameRuleValue(GameRules.RESPAWN_RADIUS);

        if (radius <= 0) {
            return spawn;
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();

        return new Location(
                world,
                spawn.getBlockX() + random.nextInt(-radius, radius + 1) + 0.5,
                spawn.getY(),
                spawn.getBlockZ() + random.nextInt(-radius, radius + 1) + 0.5
        );
    }

    public boolean isInAuthWorld(Location location) {
        if (location == null || location.getWorld() == null) return false;
        return location.getWorld().getName().equals(config.getOverworldName());
    }

    public boolean isNotAtAuthSpawn(Location location) {
        if (location == null || location.getWorld() == null) return true;
        Location authSpawn = getAuthSpawnLocation();
        if (authSpawn == null || authSpawn.getWorld() == null) return true;
        if (!location.getWorld().equals(authSpawn.getWorld())) return true;
        return location.distanceSquared(authSpawn) > 4.0;
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

        if (!FoliaAPI.isFolia() && Bukkit.isPrimaryThread()) {
            player.teleport(location);
        } else {
            player.teleportAsync(location);
        }
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

    public void setAuthWorld(World world) {
        this.authSpawnLocation = world != null && !"vanilla".equals(config.getSpawnMode())
                ? new Location(world, config.getFixedSpawnX() + 0.5, config.getFixedSpawnY(), config.getFixedSpawnZ() + 0.5, config.getFixedSpawnYaw(), config.getFixedSpawnPitch())
                : null;
    }

    public void setAuthSpawnLocation(Location authSpawnLocation) {
        this.authSpawnLocation = authSpawnLocation != null ? authSpawnLocation.clone() : null;
    }
}
