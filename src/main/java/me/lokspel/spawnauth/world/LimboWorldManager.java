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
        int spawnX = plugin.getConfig().getInt("limbo.spawn.x", 7);
        int spawnY = plugin.getConfig().getInt("limbo.spawn.y", 70);
        int spawnZ = plugin.getConfig().getInt("limbo.spawn.z", 7);
        int platformRadius = plugin.getConfig().getInt("limbo.platform.radius", 1);

        World world = plugin.getServer().getWorld(worldName);
        if (world != null) {
            gameHelper.setAuthWorld(world);
            return world;
        }

        if (FoliaAPI.isFolia()) {
            LogHelper.LOGGER.severe("On Folia, automatic world creation is not supported.");
            LogHelper.LOGGER.severe("Create the limbo world manually and set its name in config.yml at limbo.name.");
            LogHelper.LOGGER.severe("Current limbo world name: " + worldName);
            return null;
        }

        WorldCreator worldCreator = new WorldCreator(worldName);
        worldCreator.environment(World.Environment.NORMAL);
        worldCreator.generator(new VoidWorldGenerator(spawnX, spawnY - 1, spawnZ, platformRadius));
        worldCreator.generateStructures(false);

        world = plugin.getServer().createWorld(worldCreator);
        if (world == null) {
            LogHelper.LOGGER.severe("Failed to create limbo world.");
            return null;
        }

        configureLimboWorld(world, spawnX, spawnY, spawnZ);
        gameHelper.setAuthWorld(world);
        gameHelper.setAuthSpawnLocation(new Location(world, spawnX + 0.5, spawnY, spawnZ + 0.5));
        return world;
    }

    private void configureLimboWorld(World world, int spawnX, int spawnY, int spawnZ) {
        Location spawnLocation = new Location(world, spawnX + 0.5, spawnY, spawnZ + 0.5);

        if (FoliaAPI.isFolia()) {
            LogHelper.LOGGER.warning("Skipping limbo world gamerule configuration on Folia. Configure world settings manually for: " + world.getName());
            return;
        }

        world.setSpawnLocation(spawnLocation);
        LimboWorldConfigurator.configure(world);
    }
}
