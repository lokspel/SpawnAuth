package me.lokspel.spawnauth.events.authme;

import fr.xephi.authme.events.LogoutEvent;
import me.lokspel.spawnauth.helpers.SaveHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AuthMeLogoutListener implements Listener {
    private final SaveHelper saveHelper;

    public AuthMeLogoutListener(SaveHelper saveHelper) {
        this.saveHelper = saveHelper;
    }

    @EventHandler
    private void onPlayerLogout(LogoutEvent event) {
        Player player = event.getPlayer();
        saveHelper.saveLocation(player.getName(), player.getLocation());
    }
}
