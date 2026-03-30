package me.lokspel.spawnauth.world;

import me.lokspel.spawnauth.SpawnAuth;
import me.lokspel.spawnauth.helpers.GameHelper;
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

        WorldCreator worldCreator = new WorldCreator(worldName);
        worldCreator.environment(World.Environment.NORMAL);
        worldCreator.generator(new VoidWorldGenerator(spawnX, spawnY - 1, spawnZ, platformRadius));
        worldCreator.generateStructures(false);

        World world = plugin.getServer().createWorld(worldCreator);
        if (world == null) {
            LogHelper.LOGGER.severe("[SpawnAuth] Failed to create limbo world.");
            plugin.getServer().shutdown();
            return null;
        }

        configureLimboWorld(world, spawnX, spawnZ);
        gameHelper.setAuthWorld(world);
        return world;
    }

    private void configureLimboWorld(World world, int spawnX, int spawnZ) {
        Location spawnLocation = new Location(world, spawnX + 0.5, world.getHighestBlockYAt(spawnX, spawnZ) + 1, spawnZ + 0.5);

        world.setSpawnLocation(spawnLocation);
        LimboWorldConfigurator.configure(world);
    }
}
