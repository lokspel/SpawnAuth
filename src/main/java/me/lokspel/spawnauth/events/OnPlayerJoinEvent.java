package me.lokspel.spawnauth.events;

import me.lokspel.spawnauth.SpawnAuth;
import me.lokspel.spawnauth.helpers.AuthHelper;
import me.lokspel.spawnauth.helpers.GameHelper;
import me.lokspel.spawnauth.helpers.SaveHelper;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
public class OnPlayerJoinEvent implements Listener {
    private final SpawnAuth plugin;
    private final GameHelper gameHelper;
    private final SaveHelper saveHelper;
    private final String loginMode;
    private final String registerMode;

    public OnPlayerJoinEvent(SpawnAuth plugin, GameHelper gameHelper, SaveHelper saveHelper, String loginMode, String registerMode) {
        this.plugin = plugin;
        this.saveHelper = saveHelper;
        this.gameHelper = gameHelper;
        this.loginMode = loginMode;
        this.registerMode = registerMode;
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.isDead()) {
            saveHelper.removeLocation(player.getName());
            player.spigot().respawn();
            plugin.getFoliaLib().getScheduler().runAtEntity(player, unused -> handlePostJoin(player));
            return;
        }

        handlePostJoin(player);
    }

    private void handlePostJoin(Player player) {
        if (!player.isOnline()) {
            return;
        }

        String playerName = player.getName();
        var pendingLocation = saveHelper.getLocation(playerName);

        if (gameHelper.isAuthenticated(player)) {
            if (pendingLocation != null) {
                if (gameHelper.isInAuthWorld(player.getLocation())) {
                    gameHelper.teleport(player, pendingLocation);
                }
                saveHelper.removeLocation(playerName);
            }
            gameHelper.updateLimboCollision(player);
            gameHelper.updateLimboWeather(player);
            return;
        }

        if (pendingLocation != null) {
            Location authSpawn = gameHelper.getAuthSpawnLocation(modeFor(player));
            if (authSpawn != null) {
                gameHelper.teleport(player, authSpawn);
            }
            gameHelper.updateLimboCollision(player);
            gameHelper.updateLimboWeather(player);
            return;
        }

        saveHelper.saveLocation(player.getName(), player.getLocation());
        Location authSpawn = gameHelper.getAuthSpawnLocation(modeFor(player));
        if (authSpawn != null) {
            gameHelper.teleport(player, authSpawn);
        }
        gameHelper.updateLimboCollision(player);
        gameHelper.updateLimboWeather(player);
    }

    private String modeFor(Player player) {
        return AuthHelper.isRegistered(player) ? loginMode : registerMode;
    }
}
