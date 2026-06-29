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
        World overworld = requireWorld(config.getOverworldName(), World.Environment.NORMAL);
        if (overworld == null) return null;

        if (config.getNetherName() != null) {
            requireWorld(config.getNetherName(), World.Environment.NETHER);
        }
        if (config.getEndName() != null) {
            requireWorld(config.getEndName(), World.Environment.THE_END);
        }

        return overworld;
    }

    private World requireWorld(String worldName, World.Environment environment) {
        if (worldName == null) return null;
        World world = plugin.getServer().getWorld(worldName);
        if (world != null) {
            return world;
        }

        if (!config.isGenerateWorld()) {
            LogHelper.LOGGER.warning(() -> "Limbo world '" + worldName + "' was not found. "
                    + "Create it manually or set limbo.create to true.");
            return null;
        }

        if (environment != World.Environment.NORMAL) {
            LogHelper.LOGGER.warning(() -> "Auto-creation is only supported for the overworld. "
                    + "Create '" + worldName + "' manually.");
            return null;
        }

        if (FoliaAPI.isFolia()) {
            LogHelper.LOGGER.severe("Automatic limbo world creation is not supported on Folia.");
            LogHelper.LOGGER.severe(() -> "Create '" + worldName + "' manually.");
            return null;
        }

        int spawnX = (int) config.getFixedSpawnX();
        int spawnY = (int) config.getFixedSpawnY();
        int spawnZ = (int) config.getFixedSpawnZ();
        float spawnYaw = config.getFixedSpawnYaw();
        float spawnPitch = config.getFixedSpawnPitch();
        int platformRadius = config.getPlatformRadius();

        WorldCreator worldCreator = new WorldCreator(worldName);
        worldCreator.environment(World.Environment.NORMAL);
        worldCreator.generator(new VoidWorldGenerator(spawnX, spawnY - 1, spawnZ, platformRadius));
        worldCreator.generateStructures(false);

        world = plugin.getServer().createWorld(worldCreator);
        if (world == null) {
            LogHelper.LOGGER.severe(() -> "The server returned null while creating the limbo world '" + worldName + "'.");
            return null;
        }

        Location spawnLocation = new Location(world, spawnX + 0.5, spawnY, spawnZ + 0.5, spawnYaw, spawnPitch);

        if (!FoliaAPI.isFolia()) {
            world.setSpawnLocation(spawnLocation);
            LimboWorldConfigurator.configure(world);
        }

        gameHelper.setAuthWorld(world);
        gameHelper.setAuthSpawnLocation(spawnLocation);
        return world;
    }
}
