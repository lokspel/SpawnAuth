package me.lokspel.spawnauth.config;

import me.lokspel.spawnauth.SpawnAuth;
import me.lokspel.spawnauth.config.section.DatabaseSection;
import me.lokspel.spawnauth.config.section.LimboSection;

public record MainConfig(LimboSection limbo, DatabaseSection database) {

    public MainConfig(SpawnAuth plugin) {
        this(new LimboSection(plugin), new DatabaseSection(plugin));
        plugin.saveDefaultConfig();
    }
}