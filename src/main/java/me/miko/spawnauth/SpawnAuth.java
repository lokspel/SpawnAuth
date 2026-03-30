package me.miko.spawnauth;

import me.miko.spawnauth.events.*;
import fr.xephi.authme.api.v3.AuthMeApi;
import me.miko.spawnauth.helpers.LogHelper;
import org.bukkit.plugin.java.JavaPlugin;
import me.miko.spawnauth.helpers.GameHelper;
import me.miko.spawnauth.helpers.SaveHelper;
import me.miko.spawnauth.world.LimboWorldManager;

public final class SpawnAuth extends JavaPlugin {
    public SaveHelper saveHelper;
    public GameHelper gameHelper;
    @Override
    public void onEnable() {
        // Setup dataFolder
        if (!getDataFolder().mkdirs() && !getDataFolder().exists()) {
            LogHelper.LOGGER.severe("[SpawnAuth] DataBase folder failed to create.");
            getServer().shutdown();
        }

        saveDefaultConfig();

        // Create classes
        saveHelper = new SaveHelper(getDataFolder());
        gameHelper = new GameHelper(AuthMeApi.getInstance(), getConfig().getString("limbo.name", "limbo"));

        // Setup data base
        saveHelper.setupDataBase();
        gameHelper.setSaveHelper(saveHelper);
        new LimboWorldManager(this).createLimboWorld();

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
