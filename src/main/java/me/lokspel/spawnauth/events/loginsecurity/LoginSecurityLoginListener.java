package me.lokspel.spawnauth.events.loginsecurity;

import com.lenis0012.bukkit.loginsecurity.events.AuthModeChangedEvent;
import com.lenis0012.bukkit.loginsecurity.session.AuthMode;
import me.lokspel.spawnauth.helpers.GameHelper;
import me.lokspel.spawnauth.helpers.SaveHelper;
import me.lokspel.spawnauth.utils.FoliaAPI;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
public class LoginSecurityLoginListener implements Listener {
    private final SaveHelper saveHelper;
    private final GameHelper gameHelper;

    public LoginSecurityLoginListener(GameHelper gameHelper, SaveHelper saveHelper) {
        this.saveHelper = saveHelper;
        this.gameHelper = gameHelper;
    }

    @EventHandler
    private void onAuthModeChanged(AuthModeChangedEvent event) {
        if (event.getCurrentMode() != AuthMode.AUTHENTICATED) {
            return;
        }

        Player player = event.getSession().getPlayer();
        if (player == null) {
            return;
        }

        handleAuthenticatedPlayer(player);
    }

    private void handleAuthenticatedPlayer(Player player) {
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
