package me.lokspel.spawnauth.events.nlogin;

import com.nickuc.login.api.event.bukkit.auth.LoginEvent;
import com.nickuc.login.api.event.bukkit.auth.RegisterEvent;
import me.lokspel.spawnauth.SpawnAuth;
import me.lokspel.spawnauth.helpers.GameHelper;
import me.lokspel.spawnauth.helpers.SaveHelper;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NLoginLoginListener implements Listener {
    private final SpawnAuth plugin;
    private final SaveHelper saveHelper;
    private final GameHelper gameHelper;
    private final String loginMode;
    private final String registerMode;

    public NLoginLoginListener(SpawnAuth plugin, GameHelper gameHelper, SaveHelper saveHelper, String loginMode, String registerMode) {
        this.plugin = plugin;
        this.saveHelper = saveHelper;
        this.gameHelper = gameHelper;
        this.loginMode = loginMode;
        this.registerMode = registerMode;
    }

    @EventHandler
    private void onPlayerLogin(LoginEvent event) {
        handleAuthenticatedPlayer(event.getPlayer(), loginMode);
    }

    @EventHandler
    private void onPlayerRegister(RegisterEvent event) {
        handleAuthenticatedPlayer(event.getPlayer(), registerMode);
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
