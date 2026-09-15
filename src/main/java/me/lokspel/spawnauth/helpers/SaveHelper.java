package me.lokspel.spawnauth.helpers;

import me.lokspel.spawnauth.cache.SavedLocationCache;
import me.lokspel.spawnauth.database.Database;
import me.lokspel.spawnauth.database.model.SavedLocation;
import me.lokspel.spawnauth.database.repository.SavedLocationRepository;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class SaveHelper {

    private final Database database;
    private final SavedLocationRepository repository;
    private final SavedLocationCache cache;
    private final String loginMode;
    private final String registerMode;

    public SaveHelper(Database database, boolean cacheEnabled, String loginMode, String registerMode) {
        this.database = database;
        this.repository = database != null ? database.getSavedLocationRepository() : null;
        this.cache = cacheEnabled ? new SavedLocationCache() : null;
        this.loginMode = loginMode;
        this.registerMode = registerMode;
    }

    public static SaveHelper create(Database database, boolean cacheEnabled,
                                    String loginMode, String registerMode) {
        SaveHelper saveHelper = new SaveHelper(database, cacheEnabled, loginMode, registerMode);

        try {
            saveHelper.setupDataBase();
            return saveHelper;
        } catch (RuntimeException exception) {
            saveHelper.closeDatabase();
            throw exception;
        }
    }

    public boolean usePersistence(Player player) {
        if (player == null) {
            return false;
        }
        String mode = AuthHelper.isRegistered(player) ? loginMode : registerMode;
        return !"disabled".equalsIgnoreCase(mode);
    }

    private void setupDataBase() {
        if (repository != null) {
            try {
                repository.init().join();
            } catch (CompletionException exception) {
                throw new IllegalStateException(
                        "Failed to initialize the locations table",
                        exception.getCause()
                );
            }
        }
    }

    private void closeDatabase() {
        if (database != null) {
            database.close();
        }
    }

    public void saveLocation(String name, Location location) {
        if (location == null || location.getWorld() == null) {
            return;
        }

        SavedLocation saved = new SavedLocation(
                name,
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );

        if (cache != null) {
            cache.put(saved);
        }
        if (repository != null) {
            repository.upsert(saved);
        }
    }

    public void removeLocation(String name) {
        if (cache != null) {
            cache.remove(name);
        }
        if (repository != null) {
            repository.delete(name);
        }
    }

    public CompletableFuture<Location> getLocation(String name) {
        SavedLocation saved = cache != null ? cache.get(name) : null;
        if (saved != null) {
            return CompletableFuture.completedFuture(toLocation(saved));
        }

        if (repository != null) {
            return repository.get(name).thenApply(dbSaved -> {
                if (dbSaved != null && cache != null) {
                    cache.put(dbSaved);
                }
                return toLocation(dbSaved);
            });
        }

        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Location> takeLocation(String name) {
        if (cache != null) {
            SavedLocation cached = cache.remove(name);
            if (cached != null) {
                if (repository != null) {
                    repository.delete(name);
                }
                return CompletableFuture.completedFuture(toLocation(cached));
            }
        }

        if (repository != null) {
            return repository.take(name).thenApply(this::toLocation);
        }

        return CompletableFuture.completedFuture(null);
    }

    // Cache-only fallback for restoring online players before shutdown.
    public void handleDisable(GameHelper gameHelper) {
        Collection<SavedLocation> savedLocations;
        try {
            savedLocations = repository != null
                    ? repository.getAll().join()
                    : (cache != null ? cache.values() : List.of());
        } catch (CompletionException exception) {
            LogHelper.LOGGER.warning(() ->
                    "Failed to load saved locations during disable: " + exception.getCause());
            savedLocations = List.of();
        }

        for (SavedLocation saved : savedLocations) {
            try {
                Location location = toLocation(saved);
                Player player = Bukkit.getPlayer(saved.name());

                if (location != null && player != null && player.isOnline()) {
                    Boolean success = gameHelper.teleport(player, location).join();
                    if (Boolean.TRUE.equals(success)) {
                        removeLocation(saved.name());
                    }
                }
            } catch (Exception exception) {
                LogHelper.LOGGER.warning(() ->
                        "Failed to restore player location: " + exception.getMessage());
            }
        }

        if (cache != null) {
            cache.clear();
        }
        closeDatabase();
    }

    private Location toLocation(SavedLocation saved) {
        if (saved == null) {
            return null;
        }

        World world = Bukkit.getWorld(saved.world());
        if (world == null) {
            return null;
        }

        return new Location(
                world,
                saved.x(),
                saved.y(),
                saved.z(),
                saved.yaw(),
                saved.pitch()
        );
    }
}