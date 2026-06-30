package me.lokspel.spawnauth;

import me.lokspel.spawnauth.events.OnPlayerJoinEvent;
import me.lokspel.spawnauth.events.OnPlayerQuitEvent;
import me.lokspel.spawnauth.events.OnPlayerRespawnEvent;
import me.lokspel.spawnauth.events.authme.AuthMeLoginListener;
import me.lokspel.spawnauth.events.authme.AuthMeLogoutListener;
import me.lokspel.spawnauth.events.authme.AuthMeUnregisterListener;
import me.lokspel.spawnauth.events.nlogin.NLoginLoginListener;
import me.lokspel.spawnauth.events.nlogin.NLoginUnregisterListener;
import me.lokspel.spawnauth.config.ConfigManager;
import me.lokspel.spawnauth.helpers.LogHelper;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import me.lokspel.spawnauth.helpers.GameHelper;
import me.lokspel.spawnauth.helpers.SaveHelper;
import me.lokspel.spawnauth.utils.FoliaAPI;
import me.lokspel.spawnauth.world.LimboWorldManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public final class SpawnAuth extends JavaPlugin {
    private static SpawnAuth instance;
    private final Map<String, Boolean> pluginEnabledCache = new ConcurrentHashMap<>();
    private SaveHelper saveHelper;
    private GameHelper gameHelper;

    @Override
    public void onEnable() {
        instance = this;

        // Setup dataFolder
        if (!getDataFolder().mkdirs() && !getDataFolder().exists()) {
            LogHelper.LOGGER.severe("DataBase folder failed to create.");
            getServer().shutdown();
        }

        FoliaAPI.init(this);

        ConfigManager configManager = new ConfigManager(this);
        configManager.loadConfig();

        String authPluginName = getAuthPluginName();
        if (authPluginName == null) {
            return;
        }

        saveHelper = new SaveHelper(getDataFolder());
        gameHelper = new GameHelper(configManager.getLimbo());

        // Setup data base
        saveHelper.setupDataBase();

        if (new LimboWorldManager(this, gameHelper, configManager.getLimbo()).createLimboWorld() == null) {
            return;
        }

        // Register events
        getServer().getPluginManager().registerEvents(new OnPlayerJoinEvent(this, gameHelper, saveHelper), this);
        getServer().getPluginManager().registerEvents(new OnPlayerRespawnEvent(gameHelper, saveHelper), this);
        getServer().getPluginManager().registerEvents(new OnPlayerQuitEvent(gameHelper), this);

        if ("nLogin".equals(authPluginName)) {
            getServer().getPluginManager().registerEvents(new NLoginLoginListener(this, gameHelper, saveHelper), this);
            getServer().getPluginManager().registerEvents(new NLoginUnregisterListener(saveHelper), this);
        }

        if ("AuthMe".equals(authPluginName)) {
            getServer().getPluginManager().registerEvents(new AuthMeLoginListener(gameHelper, saveHelper), this);
            getServer().getPluginManager().registerEvents(new AuthMeLogoutListener(saveHelper), this);
            getServer().getPluginManager().registerEvents(new AuthMeUnregisterListener(saveHelper), this);
        }
    }

    @Override
    public void onDisable() {
        if (saveHelper != null && gameHelper != null) {
            saveHelper.handleDisable(gameHelper);
        }
    }

    public static SpawnAuth getInstance() {
        return instance;
    }

    private String getAuthPluginName() {
        if (isPluginEnabled("nLogin")) {
            LogHelper.LOGGER.info("Using nLogin as the authentication provider.");
            return "nLogin";
        }

        if (isPluginEnabled("AuthMe")) {
            LogHelper.LOGGER.info("Using AuthMe as the authentication provider.");
            return "AuthMe";
        }

        LogHelper.LOGGER.severe("No supported authentication plugin found. Install nLogin or AuthMe.");
        return null;
    }

    private boolean isPluginEnabled(String pluginName) {
        return pluginEnabledCache.computeIfAbsent(pluginName, name -> {
            Plugin plugin = getServer().getPluginManager().getPlugin(name);
            return plugin != null && plugin.isEnabled();
        });
    }
}
