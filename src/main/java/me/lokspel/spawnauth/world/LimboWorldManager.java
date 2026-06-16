package me.lokspel.spawnauth.world;

import me.lokspel.spawnauth.SpawnAuth;
import me.lokspel.spawnauth.helpers.GameHelper;
import me.lokspel.spawnauth.utils.FoliaAPI;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import me.lokspel.spawnauth.helpers.LogHelper;

public class LimboWorldManager {
    private final SpawnAuth plugin;
    private final GameHelper gameHelper;

    public LimboWorldManager(SpawnAuth plugin, GameHelper gameHelper) {
        this.plugin = plugin;
        this.gameHelper = gameHelper;
    }

    public World createLimboWorld() {
        String worldName = plugin.getConfig().getString("limbo.name", "limbo");
        boolean generateWorld = plugin.getConfig().getBoolean("limbo.generate-world", true);
        int spawnX = plugin.getConfig().getInt("limbo.spawn.x", 7);
        int spawnY = plugin.getConfig().getInt("limbo.spawn.y", 70);
        int spawnZ = plugin.getConfig().getInt("limbo.spawn.z", 7);
        float spawnYaw = (float) plugin.getConfig().getDouble("limbo.spawn.yaw", 0);
        float spawnPitch = (float) plugin.getConfig().getDouble("limbo.spawn.pitch", 0);
        int platformRadius = plugin.getConfig().getInt("limbo.platform.radius", 1);

        World world = plugin.getServer().getWorld(worldName);
        if (world != null) {
            gameHelper.setAuthWorld(world);
            return world;
        }

        if (!generateWorld) {
            LogHelper.LOGGER.severe(() -> "The configured limbo world '" + worldName + "' was not found.");
            LogHelper.LOGGER.severe("Automatic world generation is disabled at config.yml -> limbo.generate-world.");
            LogHelper.LOGGER.severe("Create that world manually or set limbo.generate-world to true.");
            return null;
        }

        if (FoliaAPI.isFolia()) {
            LogHelper.LOGGER.severe("Automatic limbo world creation is not supported on Folia.");
            LogHelper.LOGGER.severe("Create the world manually first, then point config.yml -> limbo.name to that world.");
            LogHelper.LOGGER.severe(() -> "Configured limbo world name: " + worldName);
            return null;
        }

        WorldCreator worldCreator = new WorldCreator(worldName);
        worldCreator.environment(World.Environment.NORMAL);
        worldCreator.generator(new VoidWorldGenerator(spawnX, spawnY - 1, spawnZ, platformRadius));
        worldCreator.generateStructures(false);

        world = plugin.getServer().createWorld(worldCreator);
        if (world == null) {
            LogHelper.LOGGER.severe(() -> "The server returned null while creating the limbo world '" + worldName + "'.");
            return null;
        }

        configureLimboWorld(world, spawnX, spawnY, spawnZ, spawnYaw, spawnPitch);
        gameHelper.setAuthWorld(world);
        gameHelper.setAuthSpawnLocation(new Location(world, spawnX + 0.5, spawnY, spawnZ + 0.5, spawnYaw, spawnPitch));
        return world;
    }

    private void configureLimboWorld(World world, int spawnX, int spawnY, int spawnZ, float spawnYaw, float spawnPitch) {
        Location spawnLocation = new Location(world, spawnX + 0.5, spawnY, spawnZ + 0.5, spawnYaw, spawnPitch);

        if (FoliaAPI.isFolia()) {
            LogHelper.LOGGER.warning(() -> "Skipped automatic gamerule setup for limbo world '" + world.getName()
                    + "' because Folia requires this world to be configured manually.");
            return;
        }

        world.setSpawnLocation(spawnLocation);
        LimboWorldConfigurator.configure(world);
    }
}
