package me.lokspel.spawnauth.events.loginsecurity;

import com.lenis0012.bukkit.loginsecurity.events.AuthActionEvent;
import com.lenis0012.bukkit.loginsecurity.session.AuthActionType;
import me.lokspel.spawnauth.helpers.SaveHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class LoginSecurityLogoutListener implements Listener {
    private final SaveHelper saveHelper;

    public LoginSecurityLogoutListener(SaveHelper saveHelper) {
        this.saveHelper = saveHelper;
    }

    @EventHandler
    private void onAuthAction(AuthActionEvent event) {
        if (event.getType() != AuthActionType.LOGOUT) {
            return;
        }

        Player player = event.getPlayer();
        if (player != null && player.isOnline()) {
            saveHelper.saveLocation(player.getName(), player.getLocation());
        }
    }
}
