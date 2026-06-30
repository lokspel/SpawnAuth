package me.lokspel.spawnauth.events.authme;

import fr.xephi.authme.events.LoginEvent;
import me.lokspel.spawnauth.helpers.GameHelper;
import me.lokspel.spawnauth.helpers.SaveHelper;
import me.lokspel.spawnauth.utils.FoliaAPI;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
public class AuthMeLoginListener implements Listener {
    private final SaveHelper saveHelper;
    private final GameHelper gameHelper;

    public AuthMeLoginListener(GameHelper gameHelper, SaveHelper saveHelper) {
        this.saveHelper = saveHelper;
        this.gameHelper = gameHelper;
    }

    @EventHandler
    private void onPlayerLogin(LoginEvent event) {
        Player player = event.getPlayer();
        String name = player.getName();
        Location location = saveHelper.takeLocation(name);

        if (location != null) {
            gameHelper.teleport(player, location);
        }

        gameHelper.updateLimboCollision(player);
        gameHelper.updateLimboWeather(player);

        FoliaAPI.runTaskForEntity(player, () -> {
            if (!player.isOnline() || !gameHelper.isAuthenticated(player) || gameHelper.isNotAtAuthSpawn(player.getLocation())) {
                return;
            }

            Location fallbackLocation = saveHelper.takeLocation(name);
            if (fallbackLocation != null) {
                gameHelper.teleport(player, fallbackLocation);
            }

            gameHelper.updateLimboCollision(player);
            gameHelper.updateLimboWeather(player);
        });
    }
}
