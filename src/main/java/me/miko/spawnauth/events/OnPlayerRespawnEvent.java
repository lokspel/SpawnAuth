package me.miko.spawnauth.events;

import me.miko.spawnauth.helpers.GameHelper;
import me.miko.spawnauth.helpers.SaveHelper;
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
        Location respawnLocation = event.getRespawnLocation();

        if (respawnLocation != null) {
            saveHelper.saveLocation(player.getName(), respawnLocation);
        }

        if (!gameHelper.authMeApi.isAuthenticated(player)) {
            Location authSpawn = gameHelper.getAuthSpawnLocation();
            if (authSpawn != null) {
                event.setRespawnLocation(authSpawn);
            }
        }
    }
}
