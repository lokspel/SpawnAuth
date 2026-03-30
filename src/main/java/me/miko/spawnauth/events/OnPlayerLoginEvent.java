package me.miko.spawnauth.events;

import me.miko.spawnauth.helpers.GameHelper;
import me.miko.spawnauth.helpers.SaveHelper;
import fr.xephi.authme.events.LoginEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class OnPlayerLoginEvent implements Listener {
    private final SaveHelper saveHelper;
    private final GameHelper gameHelper;
    private final JavaPlugin plugin;

    public OnPlayerLoginEvent(JavaPlugin plugin, GameHelper gameHelper, SaveHelper saveHelper) {
        this.plugin = plugin;
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

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline() || !gameHelper.isAuthenticated(player) || !gameHelper.isAtAuthSpawn(player.getLocation())) {
                return;
            }

            Location fallbackLocation = saveHelper.takeLocation(name);
            if (fallbackLocation != null) {
                gameHelper.teleport(player, fallbackLocation);
            }
        });
    }
}
