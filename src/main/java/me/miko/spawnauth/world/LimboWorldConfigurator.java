package me.miko.spawnauth.world;

import org.bukkit.GameRules;
import org.bukkit.World;

public final class LimboWorldConfigurator {
    private LimboWorldConfigurator() {
    }

    public static void configure(World world) {
        world.setGameRule(GameRules.ADVANCE_TIME, false);
        world.setGameRule(GameRules.SPAWN_PHANTOMS, false);
        world.setGameRule(GameRules.ADVANCE_WEATHER, false);
        world.setGameRule(GameRules.SPAWN_MOBS, false);
        world.setGameRule(GameRules.FALL_DAMAGE, false);
        world.setGameRule(GameRules.KEEP_INVENTORY, true);
        world.setTime(6000L);
        world.setStorm(false);
        world.setThundering(false);
    }
}
