package me.lokspel.spawnauth.config.section;

import me.lokspel.spawnauth.SpawnAuth;
import org.bukkit.configuration.file.FileConfiguration;

public final class LimboSection {

    private final SpawnAuth plugin;

    public LimboSection(SpawnAuth plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration config() {
        return plugin.getConfig();
    }

    public String getOverworldName() {
        return config().getString("worlds.overworld", "world");
    }

    public String getGenerationWorldName() {
        return config().getString("fixed.world", "limbo");
    }

    public boolean isGenerateWorld() {
        return config().getBoolean("fixed.create", false);
    }

    public String getLoginSpawnMode() {
        return config().getString("login.spawn-mode", "vanilla");
    }

    public String getRegisterSpawnMode() {
        return config().getString("register.spawn-mode", "vanilla");
    }

    public boolean usesFixedSpawn() {
        return "fixed".equalsIgnoreCase(getLoginSpawnMode())
                || "fixed".equalsIgnoreCase(getRegisterSpawnMode());
    }

    public double getFixedSpawnX() {
        return config().getDouble("fixed.spawn.x", 7);
    }

    public double getFixedSpawnY() {
        return config().getDouble("fixed.spawn.y", 70);
    }

    public double getFixedSpawnZ() {
        return config().getDouble("fixed.spawn.z", 7);
    }

    public float getFixedSpawnYaw() {
        return (float) config().getDouble("fixed.spawn.yaw", 0);
    }

    public float getFixedSpawnPitch() {
        return (float) config().getDouble("fixed.spawn.pitch", 0);
    }

    public int getPlatformRadius() {
        return config().getInt("fixed.platform-radius", 1);
    }
}