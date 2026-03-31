package me.lokspel.spawnauth.events;

import me.lokspel.spawnauth.helpers.GameHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class OnPlayerQuitEvent implements Listener {
    private final GameHelper gameHelper;

    public OnPlayerQuitEvent(GameHelper gameHelper) {
        this.gameHelper = gameHelper;
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        gameHelper.resetCollision(player);
        gameHelper.resetWeather(player);

        if (player.isInsideVehicle()) {
            player.leaveVehicle();
        }
    }
}
