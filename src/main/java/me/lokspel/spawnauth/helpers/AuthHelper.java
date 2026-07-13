package me.lokspel.spawnauth.helpers;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.nickuc.login.api.nLoginAPI;
import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.function.Predicate;

public final class AuthHelper {
    private static Predicate<String> authCheck = name -> false;

    private AuthHelper() {
    }

    public static void init(String authPluginName) {
        switch (authPluginName) {
            case "nLogin" -> authCheck = name -> nLoginAPI.getApi().isAuthenticated(name);
            case "OpenLogin" -> initOpenLogin();
            case "LoginSecurity" -> authCheck = name -> {
                Player player = Bukkit.getPlayer(name);
                if (player == null) return false;
                var session = LoginSecurity.getSessionManager().getPlayerSession(player);
                return session != null && session.isLoggedIn();
            };
            case "AuthMe" -> authCheck = name -> {
                Player player = Bukkit.getPlayer(name);
                return player != null && AuthMeApi.getInstance().isAuthenticated(player);
            };
        }
    }

    private static void initOpenLogin() {
        try {
            Plugin olPlugin = Bukkit.getPluginManager().getPlugin("OpeNLogin");
            assert olPlugin != null;
            Method getLM = olPlugin.getClass().getMethod("getLoginManagement");
            Object loginManagement = getLM.invoke(olPlugin);
            Method isAuth = loginManagement.getClass().getMethod("isAuthenticated", String.class);
            authCheck = name -> {
                try {
                    return (boolean) isAuth.invoke(loginManagement, name);
                } catch (Exception e) {
                    return false;
                }
            };
        } catch (Exception e) {
            LogHelper.LOGGER.warning("Failed to set up OpenLogin auth check: " + e.getMessage());
        }
    }

    public static boolean isAuthenticated(Player player) {
        return player != null && authCheck.test(player.getName());
    }
}
