package me.lokspel.spawnauth.events.nlogin;

import com.nickuc.login.api.event.bukkit.auth.LoginEvent;
import com.nickuc.login.api.event.bukkit.auth.RegisterEvent;
import me.lokspel.spawnauth.helpers.GameHelper;
import me.lokspel.spawnauth.helpers.SaveHelper;
import me.lokspel.spawnauth.utils.FoliaAPI;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class NLoginLoginListener implements Listener {
    private final SaveHelper saveHelper;
    private final GameHelper gameHelper;
    private final JavaPlugin plugin;

    public NLoginLoginListener(JavaPlugin plugin, GameHelper gameHelper, SaveHelper saveHelper) {
        this.plugin = plugin;
        this.saveHelper = saveHelper;
        this.gameHelper = gameHelper;
    }

    @EventHandler
    private void onPlayerLogin(LoginEvent event) {
        FoliaAPI.runTaskForEntity(event.getPlayer(), () -> handleAuthenticatedPlayer(event.getPlayer()));
    }

    @EventHandler
    private void onPlayerRegister(RegisterEvent event) {
        FoliaAPI.runTaskForEntity(event.getPlayer(), () -> handleAuthenticatedPlayer(event.getPlayer()));
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
