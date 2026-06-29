package me.lokspel.spawnauth.config;

import me.lokspel.spawnauth.config.section.LimboSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ConfigManager {

    private final JavaPlugin plugin;
    private LimboSection limbo;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        if (!plugin.getDataFolder().exists()) {
            if (!plugin.getDataFolder().mkdirs()) {
                plugin.getLogger().warning("Failed to create plugin data folder.");
            }
        }

        File configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        limbo = new LimboSection(config, "limbo");
    }

    public LimboSection getLimbo() {
        return limbo;
    }
}
