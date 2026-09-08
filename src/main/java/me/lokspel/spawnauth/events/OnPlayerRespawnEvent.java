package me.lokspel.spawnauth.events;

import me.lokspel.spawnauth.helpers.AuthHelper;
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
    private final String loginMode;
    private final String registerMode;

    public OnPlayerRespawnEvent(GameHelper gameHelper, SaveHelper saveHelper, String loginMode, String registerMode) {
        this.gameHelper = gameHelper;
        this.saveHelper = saveHelper;
        this.loginMode = loginMode;
        this.registerMode = registerMode;
    }

    @EventHandler
    private void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!gameHelper.isAuthenticated(player)) {
            saveHelper.saveLocation(player.getName(), event.getRespawnLocation());

            String mode = AuthHelper.isRegistered(player) ? loginMode : registerMode;
            Location authSpawn = gameHelper.getAuthSpawnLocation(mode);
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
