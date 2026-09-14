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

public class SaveHelper {

    private final Database database;
    private final SavedLocationRepository repository;
    private final SavedLocationCache cache;

    public SaveHelper(Database database, boolean cacheEnabled) {
        this.database = database;
        this.repository = database != null ? database.getSavedLocationRepository() : null;
        this.cache = cacheEnabled ? new SavedLocationCache() : null;
    }

    public void setupDataBase() {
        if (repository != null) {
            repository.init();
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
                location.getZ()
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

    public Location getLocation(String name) {
        SavedLocation saved = cache != null ? cache.get(name) : null;
        if (saved == null && repository != null) {
            saved = repository.get(name);
            if (saved != null && cache != null) {
                cache.put(saved);
            }
        }

        return toLocation(saved);
    }

    public Location takeLocation(String name) {
        SavedLocation saved = null;
        if (cache != null) {
            saved = cache.remove(name);
        }

        if (repository != null) {
            if (saved != null) {
                repository.delete(name);
            } else {
                saved = repository.take(name);
            }
        }

        return toLocation(saved);
    }

    public void handleDisable(GameHelper gameHelper) {
        Collection<SavedLocation> savedLocations = repository != null
                ? repository.getAll()
                : (cache != null ? cache.values() : List.of());

        for (SavedLocation saved : savedLocations) {
            try {
                Location location = toLocation(saved);
                Player player = Bukkit.getPlayer(saved.name());

                if (location != null && player != null && player.isOnline()) {
                    gameHelper.teleport(player, location);
                    removeLocation(saved.name());
                }
            } catch (Exception exception) {
                LogHelper.LOGGER.warning(() ->
                        "Failed to restore player location: " + exception.getMessage());
            }
        }

        if (cache != null) {
            cache.clear();
        }
        if (database != null) {
            database.close();
        }
    }

    private Location toLocation(SavedLocation saved) {
        if (saved == null) {
            return null;
        }

        World world = Bukkit.getWorld(saved.world());
        if (world == null) {
            return null;
        }

        return new Location(world, saved.x(), saved.y(), saved.z());
    }
}