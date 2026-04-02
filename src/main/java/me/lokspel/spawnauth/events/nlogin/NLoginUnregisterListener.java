package me.lokspel.spawnauth.events.nlogin;

import com.nickuc.login.api.event.bukkit.command.UnregisterEvent;
import me.lokspel.spawnauth.helpers.SaveHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NLoginUnregisterListener implements Listener {
    private final SaveHelper saveHelper;

    public NLoginUnregisterListener(SaveHelper saveHelper) {
        this.saveHelper = saveHelper;
    }

    @EventHandler
    private void onPlayerUnregister(UnregisterEvent event) {
        Player player = event.getPlayer();
        if (player != null && player.isOnline()) {
            saveHelper.saveLocation(player.getName(), player.getLocation());
        }
    }
}
