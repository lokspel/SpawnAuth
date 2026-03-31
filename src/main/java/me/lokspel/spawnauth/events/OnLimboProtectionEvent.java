package me.lokspel.spawnauth.events;

import me.lokspel.spawnauth.helpers.GameHelper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class OnLimboProtectionEvent implements Listener {
    private final GameHelper gameHelper;

    public OnLimboProtectionEvent(GameHelper gameHelper) {
        this.gameHelper = gameHelper;
    }

    @EventHandler
    private void onWeatherChange(WeatherChangeEvent event) {
        if (event.getWorld().equals(gameHelper.getAuthWorld()) && event.toWeatherState()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onThunderChange(ThunderChangeEvent event) {
        if (event.getWorld().equals(gameHelper.getAuthWorld()) && event.toThunderState()) {
            event.setCancelled(true);
        }
    }
}
