package me.miko.spawnauth.helpers;

import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class GameHelper {
    public AuthMeApi authMeApi;
    public SaveHelper saveHelper;
    private final String authWorldName;

    public GameHelper(AuthMeApi authMeApi, String authWorldName) {
        this.authMeApi = authMeApi;
        this.authWorldName = authWorldName;
    }

    public Location getSpawnLocation(World world) {
        return world != null ? world.getSpawnLocation().toCenterLocation() : null;
    }

    public World getAuthWorld() {
        return org.bukkit.Bukkit.getWorld(authWorldName);
    }

    public Location getAuthSpawnLocation() {
        return getSpawnLocation(getAuthWorld());
    }

    public boolean isInAuthWorld(Location location) {
        World authWorld = getAuthWorld();
        return location != null && location.getWorld() != null && authWorld != null && location.getWorld().equals(authWorld);
    }

    public boolean isAtAuthSpawn(Location location) {
        Location authSpawn = getAuthSpawnLocation();
        if (location == null || authSpawn == null || location.getWorld() == null || authSpawn.getWorld() == null) {
            return false;
        }

        if (!location.getWorld().equals(authSpawn.getWorld())) {
            return false;
        }

        return location.distanceSquared(authSpawn) <= 4.0;
    }

    public void teleport(Player player, Location location) {
        if (player == null) {
            LogHelper.LOGGER.warning("[SpawnAuth] Tried to teleport a null player.");
            return;
        }

        if (location == null || location.getWorld() == null) {
            LogHelper.LOGGER.warning("[SpawnAuth] Tried to teleport " + player.getName() + " but location was null.");
            return;
        }

        player.teleport(location);
    }

    public void setSaveHelper(SaveHelper saveHelper) {
        this.saveHelper = saveHelper;
    }
}
