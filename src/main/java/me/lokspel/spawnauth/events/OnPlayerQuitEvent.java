package me.lokspel.spawnauth.events;

import me.lokspel.spawnauth.helpers.GameHelper;
import me.lokspel.spawnauth.helpers.SaveHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class OnPlayerQuitEvent implements Listener {
    private final GameHelper gameHelper;
    private final SaveHelper saveHelper;

    public OnPlayerQuitEvent(GameHelper gameHelper, SaveHelper saveHelper) {
        this.gameHelper = gameHelper;
        this.saveHelper = saveHelper;
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String name = player.getName();

        if (saveHelper.getLocation(name) == null) {
            saveHelper.saveLocation(name, player.getLocation());
        }

        gameHelper.resetCollision(player);
        gameHelper.resetWeather(player);

        if (player.isInsideVehicle()) {
            player.leaveVehicle();
        }
    }
}
