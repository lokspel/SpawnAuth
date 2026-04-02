package me.lokspel.spawnauth.helpers;

import com.nickuc.login.api.nLoginAPI;
import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class AuthHelper {
    private AuthHelper() {
    }

    public static boolean isAuthenticated(Player player) {
        if (player == null) {
            return false;
        }

        if (isPluginEnabled("nLogin")) {
            return isAuthenticatedWithNLogin(player);
        }

        return isAuthenticatedWithAuthMe(player);
    }

    private static boolean isAuthenticatedWithNLogin(Player player) {
        nLoginAPI nLoginApi = nLoginAPI.getApi();
        return nLoginApi != null && nLoginApi.isAuthenticated(player.getName());
    }

    private static boolean isAuthenticatedWithAuthMe(Player player) {
        AuthMeApi authMeApi = AuthMeApi.getInstance();
        return authMeApi != null && authMeApi.isAuthenticated(player);
    }

    private static boolean isPluginEnabled(String pluginName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        return plugin != null && plugin.isEnabled();
    }
}
