package me.lokspel.spawnauth;

import me.lokspel.spawnauth.config.MainConfig;
import me.lokspel.spawnauth.events.OnPlayerJoinEvent;
import me.lokspel.spawnauth.events.OnPlayerQuitEvent;
import me.lokspel.spawnauth.events.OnPlayerRespawnEvent;
import me.lokspel.spawnauth.events.authme.AuthMeLoginListener;
import me.lokspel.spawnauth.events.authme.AuthMeLogoutListener;
import me.lokspel.spawnauth.events.authme.AuthMeUnregisterListener;
import me.lokspel.spawnauth.events.nlogin.NLoginLoginListener;
import me.lokspel.spawnauth.events.nlogin.NLoginUnregisterListener;
import me.lokspel.spawnauth.events.loginsecurity.LoginSecurityLoginListener;
import me.lokspel.spawnauth.events.loginsecurity.LoginSecurityLogoutListener;
import me.lokspel.spawnauth.events.loginsecurity.LoginSecurityUnregisterListener;
import me.lokspel.spawnauth.events.openlogin.OpenLoginAuthenticateListener;
import me.lokspel.spawnauth.helpers.AuthHelper;
import me.lokspel.spawnauth.helpers.GameHelper;
import me.lokspel.spawnauth.helpers.LogHelper;
import me.lokspel.spawnauth.helpers.SaveHelper;
import com.tcoded.folialib.FoliaLib;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import me.lokspel.spawnauth.world.LimboWorldManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public final class SpawnAuth extends JavaPlugin {
    private final Map<String, Boolean> pluginEnabledCache = new ConcurrentHashMap<>();
    private FoliaLib foliaLib;
    private SaveHelper saveHelper;
    private GameHelper gameHelper;

    @Override
    public void onEnable() {

        // Setup dataFolder
        if (!getDataFolder().mkdirs() && !getDataFolder().exists()) {
            LogHelper.LOGGER.severe("DataBase folder failed to create.");
            getServer().shutdown();
        }

        foliaLib = new FoliaLib(this);

        MainConfig config = new MainConfig(this);

        String provider = getProvider();
        if (provider == null) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // bStats
        int pluginId = 33136;
        Metrics metrics = new Metrics(this, pluginId);
        metrics.addCustomChart(new SimplePie("auth_provider", () -> provider));

        saveHelper = new SaveHelper(getDataFolder());
        gameHelper = new GameHelper(this, config.limbo());

        // Setup data base
        saveHelper.setupDataBase();

        String loginMode = config.limbo().getLoginSpawnMode();
        String registerMode = config.limbo().getRegisterSpawnMode();

        if (config.limbo().usesFixedSpawn()) {
            if (new LimboWorldManager(this, gameHelper, config.limbo()).createLimboWorld() == null) {
                return;
            }
        }

        // Register events
        getServer().getPluginManager().registerEvents(new OnPlayerJoinEvent(this, gameHelper, saveHelper, loginMode, registerMode), this);
        getServer().getPluginManager().registerEvents(new OnPlayerRespawnEvent(gameHelper, saveHelper, loginMode, registerMode), this);
        getServer().getPluginManager().registerEvents(new OnPlayerQuitEvent(gameHelper, saveHelper), this);
        AuthHelper.init(provider);

        if ("nLogin".equals(provider)) {
            getServer().getPluginManager().registerEvents(new NLoginLoginListener(this, gameHelper, saveHelper, loginMode, registerMode), this);
            getServer().getPluginManager().registerEvents(new NLoginUnregisterListener(saveHelper), this);
        }

        if ("OpenLogin".equals(provider)) {
            getServer().getPluginManager().registerEvents(new OpenLoginAuthenticateListener(this, gameHelper, saveHelper, loginMode, registerMode), this);
        }

        if ("AuthMe".equals(provider)) {
            getServer().getPluginManager().registerEvents(new AuthMeLoginListener(this, gameHelper, saveHelper, loginMode, registerMode), this);
            getServer().getPluginManager().registerEvents(new AuthMeLogoutListener(saveHelper), this);
            getServer().getPluginManager().registerEvents(new AuthMeUnregisterListener(saveHelper), this);
        }

        if ("LoginSecurity".equals(provider)) {
            getServer().getPluginManager().registerEvents(new LoginSecurityLoginListener(this, gameHelper, saveHelper, loginMode, registerMode), this);
            getServer().getPluginManager().registerEvents(new LoginSecurityLogoutListener(saveHelper), this);
            getServer().getPluginManager().registerEvents(new LoginSecurityUnregisterListener(saveHelper), this);
        }
    }

    @Override
    public void onDisable() {
        if (foliaLib != null) {
            foliaLib.getScheduler().cancelAllTasks();
        }

        if (saveHelper != null && gameHelper != null) {
            saveHelper.handleDisable(gameHelper);
        }
    }

    public FoliaLib getFoliaLib() {
        return foliaLib;
    }

    private String getProvider() {
        if (isPluginEnabled("nLogin")) {
            LogHelper.LOGGER.info("Using nLogin as the authentication provider.");
            return "nLogin";
        }

        if (isPluginEnabled("OpeNLogin")) {
            LogHelper.LOGGER.info("Using OpenLogin as the authentication provider.");
            return "OpenLogin";
        }

        if (isPluginEnabled("LoginSecurity")) {
            LogHelper.LOGGER.info("Using LoginSecurity as the authentication provider.");
            return "LoginSecurity";
        }

        if (isPluginEnabled("AuthMe")) {
            LogHelper.LOGGER.info("Using AuthMe as the authentication provider.");
            return "AuthMe";
        }

        LogHelper.LOGGER.severe("No supported authentication plugin found. Install nLogin, OpenLogin, LoginSecurity, or AuthMe.");
        return null;
    }

    private boolean isPluginEnabled(String pluginName) {
        return pluginEnabledCache.computeIfAbsent(pluginName, name -> {
            Plugin plugin = getServer().getPluginManager().getPlugin(name);
            return plugin != null && plugin.isEnabled();
        });
    }
}
