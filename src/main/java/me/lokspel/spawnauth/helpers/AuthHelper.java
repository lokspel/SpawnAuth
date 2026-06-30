package me.lokspel.spawnauth.helpers;

import com.nickuc.login.api.nLoginAPI;
import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AuthHelper {
    private static final Map<String, Boolean> PLUGIN_ENABLED_CACHE = new ConcurrentHashMap<>();

    private AuthHelper() {
    }

    public static boolean isAuthenticated(Player player) {
        if (player == null) {
            return false;
        }

        if (isPluginEnabled()) {
            return isAuthenticatedWithNLogin(player);
        }

        return isAuthenticatedWithAuthMe(player);
    }

    private static boolean isAuthenticatedWithNLogin(Player player) {
        return nLoginAPI.getApi().isAuthenticated(player.getName());
    }

    private static boolean isAuthenticatedWithAuthMe(Player player) {
        AuthMeApi authMeApi = AuthMeApi.getInstance();
        return authMeApi != null && authMeApi.isAuthenticated(player);
    }

    private static boolean isPluginEnabled() {
        return PLUGIN_ENABLED_CACHE.computeIfAbsent("nLogin", name -> {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
            return plugin != null && plugin.isEnabled();
        });
    }
}
