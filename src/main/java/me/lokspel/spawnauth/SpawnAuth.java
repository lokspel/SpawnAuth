package me.lokspel.spawnauth;

import dev.faststats.ErrorTracker;
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
import me.lokspel.spawnauth.database.Database;
import com.tcoded.folialib.FoliaLib;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import dev.faststats.bukkit.BukkitContext;
import me.lokspel.spawnauth.world.LimboWorldManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;


public final class SpawnAuth extends JavaPlugin {
    public static final ErrorTracker ERROR_TRACKER = ErrorTracker.contextAware();

    private final Map<String, Boolean> pluginEnabledCache = new ConcurrentHashMap<>();
    private FoliaLib foliaLib;
    private SaveHelper saveHelper;
    private GameHelper gameHelper;
    private BukkitContext fastStatsContext;

    @Override
    public void onEnable() {

        // Setup dataFolder
        if (!getDataFolder().mkdirs() && !getDataFolder().exists()) {
            LogHelper.LOGGER.severe("Failed to create the plugin folder.");
            getServer().getPluginManager().disablePlugin(this);
            return;
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
        metrics.addCustomChart(new SimplePie("database_type", config.database().getType()::name));

        // FastStats
        fastStatsContext = new BukkitContext.Factory(this, "d78bc9e16b230262d97d101ae00b77d4")
                .errorTrackerService(ERROR_TRACKER)
                .metrics(dev.faststats.Metrics.Factory::create)
                .create();
        fastStatsContext.ready();

        saveHelper = initSaveHelper(config);
        if (saveHelper == null) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        gameHelper = new GameHelper(this, config.limbo());

        String loginMode = config.limbo().getLoginSpawnMode();
        String registerMode = config.limbo().getRegisterSpawnMode();

        if (config.limbo().usesFixedSpawn()) {
            if (new LimboWorldManager(this, gameHelper, config.limbo()).createLimboWorld() == null) {
                LogHelper.LOGGER.severe("Unable to load the limbo world.");
                getServer().getPluginManager().disablePlugin(this);
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
        if (fastStatsContext != null) {
            fastStatsContext.shutdown();
        }

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

    private SaveHelper initSaveHelper(MainConfig config) {
        boolean cache = config.database().useCache();
        var libsDir = getDataFolder().toPath().resolve("libraries");
        String loginMode = config.limbo().getLoginSpawnMode();
        String registerMode = config.limbo().getRegisterSpawnMode();

        try {
            Database database = Database.create(config.database(), libsDir, cache);
            return SaveHelper.create(database, cache, loginMode, registerMode);
        } catch (Exception e) {
            if (cache) {
                LogHelper.LOGGER.log(
                        Level.SEVERE,
                        "Database is unavailable, falling back to in-memory cache",
                        e
                );
                return new SaveHelper(null, true, loginMode, registerMode);
            }

            LogHelper.LOGGER.log(
                    Level.SEVERE,
                    "Database is unavailable.",
                    e
            );
            return null;
        }
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
