package me.lokspel.spawnauth.config;

import me.lokspel.spawnauth.SpawnAuth;
import me.lokspel.spawnauth.config.section.LimboSection;

public record MainConfig(LimboSection limbo) {

    public MainConfig(SpawnAuth plugin) {
        this(new LimboSection(plugin));
        plugin.saveDefaultConfig();
    }
}