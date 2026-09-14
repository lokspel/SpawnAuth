package me.lokspel.spawnauth.config.section;

import me.lokspel.spawnauth.SpawnAuth;
import me.lokspel.spawnauth.database.DatabaseType;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.Locale;

public final class DatabaseSection {

    private final SpawnAuth plugin;

    public DatabaseSection(SpawnAuth plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration config() {
        return plugin.getConfig();
    }

    public DatabaseType getType() {
        String value = config().getString("database.type", "sqlite");
        try {
            return DatabaseType.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return DatabaseType.SQLITE;
        }
    }

    public String getTablePrefix() {
        return config().getString("database.table-prefix", "spawnauth");
    }

    public boolean isCacheEnabled() {
        return config().getBoolean("database.cache", true);
    }

    public String getMySqlHost() {
        return config().getString("database.mysql.host", "localhost");
    }

    public int getMySqlPort() {
        return config().getInt("database.mysql.port", 3306);
    }

    public String getMySqlDatabase() {
        return config().getString("database.mysql.database", "spawnauth");
    }

    public String getMySqlUser() {
        return config().getString("database.mysql.user", "root");
    }

    public String getMySqlPassword() {
        return config().getString("database.mysql.password", "");
    }

    public String getMySqlUrlParameters() {
        return config().getString("database.mysql.url-parameters", "useSSL=false&allowPublicKeyRetrieval=true");
    }

    public int getMaxPoolSize() {
        return config().getInt("database.pool.max-pool-size", 10);
    }

    public long getConnectionTimeoutMs() {
        return config().getLong("database.pool.connection-timeout-ms", 30000);
    }

    public File getDatabaseFile() {
        return new File(plugin.getDataFolder(), "SpawnAuth.db");
    }
}