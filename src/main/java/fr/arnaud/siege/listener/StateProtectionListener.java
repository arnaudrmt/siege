package fr.arnaud.siege.listener;

import fr.arnaud.siege.game.GameStateManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class StateProtectionListener implements Listener {

    private final GameStateManager gameStateManager;

    public StateProtectionListener(GameStateManager gameStateManager) {
        this.gameStateManager = gameStateManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (gameStateManager.getCurrentState() instanceof fr.arnaud.siege.game.state.PreparationState) {
            return;
        }

        if (!gameStateManager.getCurrentState().canBreakBlocks()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot break blocks right now!");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (gameStateManager.getCurrentState() instanceof fr.arnaud.siege.game.state.PreparationState) {
            return;
        }

        if (!gameStateManager.getCurrentState().canPlaceBlocks()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot place blocks right now!");
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        if (!gameStateManager.getCurrentState().canReceiveDamage()) {
            event.setCancelled(true);
        }
    }
}