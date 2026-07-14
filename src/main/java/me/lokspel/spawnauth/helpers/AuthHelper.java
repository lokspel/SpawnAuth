package me.lokspel.spawnauth.helpers;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.nickuc.login.api.nLoginAPI;
import com.nickuc.openlogin.bukkit.OpenLoginBukkit;
import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.function.Predicate;

public final class AuthHelper {
    private static Predicate<String> authCheck = name -> false;

    private AuthHelper() {
    }

    public static void init(String authPluginName) {
        switch (authPluginName) {
            case "nLogin" -> authCheck = name -> nLoginAPI.getApi().isAuthenticated(name);
            case "OpenLogin" -> {
                Plugin olPlugin = Bukkit.getPluginManager().getPlugin("OpeNLogin");
                if (olPlugin instanceof OpenLoginBukkit openLogin) {
                    authCheck = openLogin.getLoginManagement()::isAuthenticated;
                }
            }
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

    public static boolean isAuthenticated(Player player) {
        return player != null && authCheck.test(player.getName());
    }
}
