package me.lokspel.spawnauth.events;

import me.lokspel.spawnauth.helpers.GameHelper;
import me.lokspel.spawnauth.helpers.SaveHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class OnPlayerJoinEvent implements Listener {
    private final JavaPlugin plugin;
    private final GameHelper gameHelper;
    private final SaveHelper saveHelper;

    public OnPlayerJoinEvent(JavaPlugin plugin, GameHelper gameHelper, SaveHelper saveHelper) {
        this.plugin = plugin;
        this.saveHelper = saveHelper;
        this.gameHelper = gameHelper;
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.isDead()) {
            player.spigot().respawn();
            Bukkit.getScheduler().runTask(plugin, () -> handlePostJoin(player));
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
                saveHelper.removeLocation(playerName);
                gameHelper.teleport(player, pendingLocation);
            }
            gameHelper.updateLimboCollision(player);
            gameHelper.updateLimboWeather(player);
            return;
        }

        if (pendingLocation != null) {
            if (!gameHelper.isInAuthWorld(player.getLocation())) {
                gameHelper.teleport(player, gameHelper.getAuthSpawnLocation());
            }
            gameHelper.updateLimboCollision(player);
            gameHelper.updateLimboWeather(player);
            return;
        }

        saveHelper.saveLocation(player.getName(), player.getLocation());
        gameHelper.teleport(player, gameHelper.getAuthSpawnLocation());
        gameHelper.updateLimboCollision(player);
        gameHelper.updateLimboWeather(player);
    }
}
