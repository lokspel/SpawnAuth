package me.lokspel.spawnauth.config.section;

import org.bukkit.configuration.file.FileConfiguration;

public class LimboSection {

    private final FileConfiguration config;
    private final String path;

    public LimboSection(FileConfiguration config, String path) {
        this.config = config;
        this.path = path;
    }

    private String key(String suffix) {
        return path.isEmpty() ? suffix.substring(1) : path + suffix;
    }

    public String getOverworldName() {
        return config.getString(key(".worlds.overworld"), "world");
    }

    public String getNetherName() {
        return config.getString(key(".worlds.nether"));
    }

    public String getEndName() {
        return config.getString(key(".worlds.end"));
    }

    public boolean isFallbackEnabled() {
        return config.getBoolean(key(".worlds.fallback"), true);
    }

    public String getGenerationWorldName() {
        return config.getString(key(".generation.world"), "limbo");
    }

    public boolean isGenerateWorld() {
        return config.getBoolean(key(".generation.create"), false);
    }

    public String getSpawnMode() {
        return config.getString(key(".spawn-mode"), "vanilla");
    }

    public double getFixedSpawnX() {
        return config.getDouble(key(".generation.fixed-spawn.x"), 7);
    }

    public double getFixedSpawnY() {
        return config.getDouble(key(".generation.fixed-spawn.y"), 70);
    }

    public double getFixedSpawnZ() {
        return config.getDouble(key(".generation.fixed-spawn.z"), 7);
    }

    public float getFixedSpawnYaw() {
        return (float) config.getDouble(key(".generation.fixed-spawn.yaw"), 0);
    }

    public float getFixedSpawnPitch() {
        return (float) config.getDouble(key(".generation.fixed-spawn.pitch"), 0);
    }

    public int getPlatformRadius() {
        return config.getInt(key(".generation.platform-radius"), 1);
    }
}
