package me.miko.spawnauth.events;

import me.miko.spawnauth.helpers.GameHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class OnLimboProtectionEvent implements Listener {
    private final GameHelper gameHelper;

    public OnLimboProtectionEvent(GameHelper gameHelper) {
        this.gameHelper = gameHelper;
    }

    @EventHandler
    private void onBlockBreak(BlockBreakEvent event) {
        if (gameHelper.isInAuthWorld(event.getPlayer().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onBlockPlace(BlockPlaceEvent event) {
        if (gameHelper.isInAuthWorld(event.getPlayer().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        if (gameHelper.isInAuthWorld(event.getPlayer().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (gameHelper.isInAuthWorld(event.getPlayer().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (gameHelper.isInAuthWorld(event.getPlayer().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player && gameHelper.isInAuthWorld(player.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onDrop(PlayerDropItemEvent event) {
        if (gameHelper.isInAuthWorld(event.getPlayer().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player && gameHelper.isInAuthWorld(player.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onSwapHand(PlayerSwapHandItemsEvent event) {
        if (gameHelper.isInAuthWorld(event.getPlayer().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player && gameHelper.isInAuthWorld(player.getLocation())) {
            event.setCancelled(true);
        }
    }
}
