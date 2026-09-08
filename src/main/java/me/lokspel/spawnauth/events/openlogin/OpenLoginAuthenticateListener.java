package me.lokspel.spawnauth.events.openlogin;

import com.nickuc.openlogin.bukkit.api.events.AsyncLoginEvent;
import com.nickuc.openlogin.bukkit.api.events.AsyncRegisterEvent;
import me.lokspel.spawnauth.SpawnAuth;
import me.lokspel.spawnauth.helpers.GameHelper;
import me.lokspel.spawnauth.helpers.SaveHelper;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class OpenLoginAuthenticateListener implements Listener {
    private final SpawnAuth plugin;
    private final SaveHelper saveHelper;
    private final GameHelper gameHelper;
    private final String loginMode;
    private final String registerMode;

    public OpenLoginAuthenticateListener(SpawnAuth plugin, GameHelper gameHelper, SaveHelper saveHelper, String loginMode, String registerMode) {
        this.plugin = plugin;
        this.saveHelper = saveHelper;
        this.gameHelper = gameHelper;
        this.loginMode = loginMode;
        this.registerMode = registerMode;
    }

    @EventHandler
    private void onAsyncLogin(AsyncLoginEvent event) {
        Player player = event.getPlayer();
        plugin.getFoliaLib().getScheduler().runAtEntity(player, unused -> handleAuthenticatedPlayer(player, loginMode));
    }

    @EventHandler
    private void onAsyncRegister(AsyncRegisterEvent event) {
        Player player = event.getPlayer();
        plugin.getFoliaLib().getScheduler().runAtEntity(player, unused -> handleAuthenticatedPlayer(player, registerMode));
    }

    private void handleAuthenticatedPlayer(Player player, String mode) {
        String name = player.getName();
        gameHelper.teleportAuthenticated(player, saveHelper, mode);

        gameHelper.updateLimboCollision(player);
        gameHelper.updateLimboWeather(player);

        plugin.getFoliaLib().getScheduler().runAtEntity(player, unused -> {
            if (!player.isOnline() || !gameHelper.isAuthenticated(player) || gameHelper.isNotAtAuthSpawn(player.getLocation(), mode)) {
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
