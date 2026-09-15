package me.lokspel.spawnauth.events;

import me.lokspel.spawnauth.helpers.GameHelper;
import me.lokspel.spawnauth.helpers.SaveHelper;
import org.bukkit.Location;
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
        Location currentLocation = player.getLocation();

        if (saveHelper.usePersistence(player)) {
            saveHelper.getLocation(name).thenAccept(location -> {
                if (location == null) {
                    saveHelper.saveLocation(name, currentLocation);
                }
            });
        }

        gameHelper.resetCollision(player);
        gameHelper.resetWeather(player);

        if (player.isInsideVehicle()) {
            player.leaveVehicle();
        }
    }
}
