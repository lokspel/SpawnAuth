package me.lokspel.spawnauth.config.section;

import org.bukkit.configuration.file.FileConfiguration;

public class LimboSection {

    private final FileConfiguration config;
    private final String path;

    public LimboSection(FileConfiguration config, String path) {
        this.config = config;
        this.path = path;
    }

    public String getWorldName() {
        return config.getString(path + ".world", "limbo");
    }

    public boolean isGenerateWorld() {
        return config.getBoolean(path + ".create", true);
    }

    public String getSpawnMode() {
        return config.getString(path + ".spawn-mode", "vanilla");
    }

    public double getFixedSpawnX() {
        return config.getDouble(path + ".fixed-spawn.x", 7);
    }

    public double getFixedSpawnY() {
        return config.getDouble(path + ".fixed-spawn.y", 70);
    }

    public double getFixedSpawnZ() {
        return config.getDouble(path + ".fixed-spawn.z", 7);
    }

    public float getFixedSpawnYaw() {
        return (float) config.getDouble(path + ".fixed-spawn.yaw", 0);
    }

    public float getFixedSpawnPitch() {
        return (float) config.getDouble(path + ".fixed-spawn.pitch", 0);
    }

    public int getPlatformRadius() {
        return config.getInt(path + ".platform-radius", 1);
    }
}
