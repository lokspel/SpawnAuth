package me.lokspel.spawnauth.world;

import me.lokspel.spawnauth.SpawnAuth;
import me.lokspel.spawnauth.config.section.LimboSection;
import me.lokspel.spawnauth.helpers.GameHelper;
import me.lokspel.spawnauth.utils.FoliaAPI;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import me.lokspel.spawnauth.helpers.LogHelper;

public class LimboWorldManager {
    private final SpawnAuth plugin;
    private final GameHelper gameHelper;
    private final LimboSection config;

    public LimboWorldManager(SpawnAuth plugin, GameHelper gameHelper, LimboSection config) {
        this.plugin = plugin;
        this.gameHelper = gameHelper;
        this.config = config;
    }

    public World createLimboWorld() {
        String worldName = config.getWorldName();
        boolean generateWorld = config.isGenerateWorld();
        int spawnX = (int) config.getFixedSpawnX();
        int spawnY = (int) config.getFixedSpawnY();
        int spawnZ = (int) config.getFixedSpawnZ();
        float spawnYaw = config.getFixedSpawnYaw();
        float spawnPitch = config.getFixedSpawnPitch();
        int platformRadius = config.getPlatformRadius();

        World world = plugin.getServer().getWorld(worldName);
        if (world != null) {
            gameHelper.setAuthWorld(world);
            return world;
        }

        if (!generateWorld) {
            LogHelper.LOGGER.severe(() -> "The configured limbo world '" + worldName + "' was not found.");
            LogHelper.LOGGER.severe("Automatic world generation is disabled at config.yml -> limbo.create.");
            LogHelper.LOGGER.severe("Create that world manually or set limbo.create to true.");
            return null;
        }

        if (FoliaAPI.isFolia()) {
            LogHelper.LOGGER.severe("Automatic limbo world creation is not supported on Folia.");
            LogHelper.LOGGER.severe("Create the world manually first, then point config.yml -> limbo.world to that world.");
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
