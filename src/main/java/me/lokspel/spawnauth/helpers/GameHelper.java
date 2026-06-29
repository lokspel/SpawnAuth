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

    public GameHelper(LimboSection config) {
        this.config = config;
    }

    public World getAuthWorld() {
        return getAuthWorld(World.Environment.NORMAL);
    }

    public World getAuthWorld(World.Environment environment) {
        String name = worldNameFor(environment);
        if (name != null) {
            World world = Bukkit.getWorld(name);
            if (world != null) return world;
        }

        if (environment != World.Environment.NORMAL && config.isFallbackEnabled()) {
            World world = Bukkit.getWorld(config.getOverworldName());
            if (world != null) return world;
        }

        authWorld = null;
        authSpawnLocation = null;
        return null;
    }

    private String worldNameFor(World.Environment environment) {
        return switch (environment) {
            case NETHER -> {
                String n = config.getNetherName();
                yield n != null ? n : (config.isFallbackEnabled() ? config.getOverworldName() : null);
            }
            case THE_END -> {
                String e = config.getEndName();
                yield e != null ? e : (config.isFallbackEnabled() ? config.getOverworldName() : null);
            }
            default -> config.getOverworldName();
        };
    }

    public Location getAuthSpawnLocation() {
        return getAuthSpawnLocation(World.Environment.NORMAL);
    }

    public Location getAuthSpawnLocation(Player player) {
        return getAuthSpawnLocation(player.getWorld().getEnvironment());
    }

    public Location getAuthSpawnLocation(World.Environment environment) {
        if (!"vanilla".equals(config.getSpawnMode())) {
            if (authSpawnLocation == null) {
                World world = Bukkit.getWorld(config.getGenerationWorldName());
                if (world == null) return null;
                authSpawnLocation = new Location(world, config.getFixedSpawnX() + 0.5, config.getFixedSpawnY(), config.getFixedSpawnZ() + 0.5, config.getFixedSpawnYaw(), config.getFixedSpawnPitch());
            }
            return authSpawnLocation.clone();
        }

        World world = getAuthWorld(environment);
        if (world == null) return null;

        return getServerSpawnLocation(world);
    }

    private Location getServerSpawnLocation(World world) {
        Location worldSpawn = world.getSpawnLocation();
        Integer radius = world.getGameRuleValue(GameRules.RESPAWN_RADIUS);
        if (radius == null || radius <= 0) {
            return worldSpawn;
        }
        int dx = (int) (Math.random() * (radius * 2 + 1)) - radius;
        int dz = (int) (Math.random() * (radius * 2 + 1)) - radius;
        int x = (int) worldSpawn.getX() + dx;
        int z = (int) worldSpawn.getZ() + dz;
        Integer y = getSafeSpawnY(world, x, z, worldSpawn.getBlockY());
        if (y == null) {
            return worldSpawn;
        }
        double spawnX = x + 0.5;
        double spawnZ = z + 0.5;
        float yaw = (float) Math.toDegrees(Math.atan2(-(worldSpawn.getX() - spawnX), worldSpawn.getZ() - spawnZ));
        return new Location(world, spawnX, y, spawnZ, yaw, 0.0f);
    }

    private Integer getSafeSpawnY(World world, int x, int z, int baseY) {
        if (isPassable(world, x, baseY, z) && isPassable(world, x, baseY + 1, z)) {
            return baseY;
        }
        int margin = 10;
        if (world.getBlockAt(x, baseY, z).isPassable()) {
            for (int dy = 1; dy <= margin; dy++) {
                int y = baseY - dy;
                if (isPassable(world, x, y, z) && isPassable(world, x, y + 1, z)) {
                    return y;
                }
            }
        } else {
            for (int dy = 1; dy <= margin; dy++) {
                int y = baseY + dy;
                if (isPassable(world, x, y, z) && isPassable(world, x, y + 1, z)) {
                    return y;
                }
            }
        }
        return null;
    }

    private boolean isPassable(World world, int x, int y, int z) {
        return world.getBlockAt(x, y, z).isPassable();
    }

    public boolean isInAuthWorld(Location location) {
        if (location == null || location.getWorld() == null) return false;
        String name = location.getWorld().getName();
        return name.equals(config.getOverworldName())
                || name.equals(config.getNetherName())
                || name.equals(config.getEndName());
    }

    public boolean isAtAuthSpawn(Location location) {
        if (location == null || location.getWorld() == null) return false;
        Location authSpawn = getAuthSpawnLocation(location.getWorld().getEnvironment());
        if (authSpawn == null || authSpawn.getWorld() == null) return false;
        if (!location.getWorld().equals(authSpawn.getWorld())) return false;
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
