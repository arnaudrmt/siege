package fr.arnaud.siege.protection;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class StateProtectionListener implements Listener {

    private boolean blockBreakProtection;
    private boolean blockPlaceProtection;
    private boolean playerDamageProtection;

    public StateProtectionListener() {
        this.blockBreakProtection = false;
        this.blockPlaceProtection = false;
        this.playerDamageProtection = false;
    }

    public void setProperties(boolean blockBreak, boolean blockPlace, boolean playerDamage) {
        this.blockBreakProtection = blockBreak;
        this.blockPlaceProtection = blockPlace;
        this.playerDamageProtection = playerDamage;
    }

    public void disableAllProtections() {
        this.blockBreakProtection = false;
        this.blockPlaceProtection = false;
        this.playerDamageProtection = false;
    }

    public void enableAllProtections() {
        this.blockBreakProtection = true;
        this.blockPlaceProtection = true;
        this.playerDamageProtection = true;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (blockBreakProtection) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Block breaking is disabled right now!");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (blockPlaceProtection) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Block placing is disabled right now!");
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (playerDamageProtection && event.getEntity() instanceof Player) {
            event.setCancelled(true);
            event.getEntity().sendMessage(ChatColor.RED + "Player damage is disabled right now!");
        }
    }
}