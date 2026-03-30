package me.miko.spawnauth;

import me.miko.spawnauth.events.*;
import fr.xephi.authme.api.v3.AuthMeApi;
import me.miko.spawnauth.helpers.LogHelper;
import org.bukkit.plugin.java.JavaPlugin;
import me.miko.spawnauth.helpers.GameHelper;
import me.miko.spawnauth.helpers.SaveHelper;
import me.miko.spawnauth.world.LimboWorldManager;

public final class SpawnAuth extends JavaPlugin {
    private SaveHelper saveHelper;
    private GameHelper gameHelper;
    @Override
    public void onEnable() {
        // Setup dataFolder
        if (!getDataFolder().mkdirs() && !getDataFolder().exists()) {
            LogHelper.LOGGER.severe("[SpawnAuth] DataBase folder failed to create.");
            getServer().shutdown();
        }

        saveDefaultConfig();

        // Create classes
        AuthMeApi authMeApi = AuthMeApi.getInstance();
        if (authMeApi == null) {
            LogHelper.LOGGER.severe("[SpawnAuth] AuthMe API is unavailable. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveHelper = new SaveHelper(getDataFolder());
        gameHelper = new GameHelper(authMeApi, getConfig().getString("limbo.name", "limbo"));

        // Setup data base
        saveHelper.setupDataBase();

        new LimboWorldManager(this, gameHelper).createLimboWorld();

        // Register events
        getServer().getPluginManager().registerEvents(new OnPlayerJoinEvent(this, gameHelper, saveHelper), this);
        getServer().getPluginManager().registerEvents(new OnLimboProtectionEvent(gameHelper), this);
        getServer().getPluginManager().registerEvents(new OnPlayerRespawnEvent(gameHelper, saveHelper), this);
        getServer().getPluginManager().registerEvents(new OnPlayerLoginEvent(this, gameHelper, saveHelper), this);
        getServer().getPluginManager().registerEvents(new OnPlayerQuitEvent(gameHelper, saveHelper), this);
        getServer().getPluginManager().registerEvents(new OnPlayerLogoutEvent(saveHelper), this);
        getServer().getPluginManager().registerEvents(new OnPlayerUnregisterEvent(saveHelper), this);
    }

    @Override
    public void onDisable() {
        saveHelper.handleDisable(gameHelper);
    }
}
