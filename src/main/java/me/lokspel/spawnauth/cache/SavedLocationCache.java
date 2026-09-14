package me.lokspel.spawnauth.cache;

import me.lokspel.spawnauth.database.model.SavedLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SavedLocationCache {

    private final Map<String, SavedLocation> cache = new ConcurrentHashMap<>();

    public SavedLocation get(String name) {
        return cache.get(name);
    }

    public void put(SavedLocation location) {
        cache.put(location.name(), location);
    }

    public SavedLocation remove(String name) {
        return cache.remove(name);
    }

    public Collection<SavedLocation> values() {
        return new ArrayList<>(cache.values());
    }

    public void clear() {
        cache.clear();
    }
}