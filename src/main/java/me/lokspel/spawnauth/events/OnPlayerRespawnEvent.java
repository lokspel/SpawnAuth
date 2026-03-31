package me.lokspel.spawnauth.events;

import me.lokspel.spawnauth.helpers.GameHelper;
import me.lokspel.spawnauth.helpers.SaveHelper;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class OnPlayerRespawnEvent implements Listener {
    private final GameHelper gameHelper;
    private final SaveHelper saveHelper;

    public OnPlayerRespawnEvent(GameHelper gameHelper, SaveHelper saveHelper) {
        this.gameHelper = gameHelper;
        this.saveHelper = saveHelper;
    }

    @EventHandler
    private void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!gameHelper.isAuthenticated(player)) {
            Location respawnLocation = event.getRespawnLocation();
            if (respawnLocation != null) {
                saveHelper.saveLocation(player.getName(), respawnLocation);
            }

            Location authSpawn = gameHelper.getAuthSpawnLocation();
            if (authSpawn != null) {
                event.setRespawnLocation(authSpawn);
                gameHelper.updateLimboCollision(player, authSpawn);
                gameHelper.updateLimboWeather(player, authSpawn);
                return;
            }
        }

        gameHelper.updateLimboCollision(player, event.getRespawnLocation());
        gameHelper.updateLimboWeather(player, event.getRespawnLocation());
    }
}
