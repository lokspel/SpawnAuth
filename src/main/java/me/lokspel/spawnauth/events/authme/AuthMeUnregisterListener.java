package me.lokspel.spawnauth.events.authme;

import fr.xephi.authme.events.UnregisterByAdminEvent;
import fr.xephi.authme.events.UnregisterByPlayerEvent;
import me.lokspel.spawnauth.helpers.SaveHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AuthMeUnregisterListener implements Listener {
    private final SaveHelper saveHelper;

    public AuthMeUnregisterListener(SaveHelper saveHelper) {
        this.saveHelper = saveHelper;
    }

    @EventHandler
    private void onPlayerUnregister(UnregisterByPlayerEvent event) {
        Player player = event.getPlayer();
        saveHelper.saveLocation(player.getName(), player.getLocation());
    }

    @EventHandler
    private void onPlayerUnregisterByAdmin(UnregisterByAdminEvent event) {
        Player player = event.getPlayer();
        if (player != null && player.isOnline()) {
            saveHelper.saveLocation(player.getName(), player.getLocation());
        }
    }
}
