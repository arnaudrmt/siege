package fr.arnaud.siege.game.state;

import fr.arnaud.siege.Siege;
import fr.arnaud.siege.game.GameState;
import fr.arnaud.siege.game.TeamManager;
import fr.arnaud.siege.marker.MarkerType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.List;
import java.util.Random;

public class FightingState implements GameState, Listener {

    private final Siege plugin;

    public FightingState(Siege plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnter() {

        plugin.getLogger().info("Entering Fighting State...");

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void onUpdate(long elapsedTime) {}

    private void checkWinCondition() {

        if (plugin.getTeamManager().getKingUUID() == null) {
            plugin.getLogger().info("Attackers win!");
            plugin.getGameManager().changeState(new EndState(plugin));
        } else if (plugin.getTeamManager().getAttackers().isEmpty()) {
            plugin.getLogger().info("Defenders win!");
            plugin.getGameManager().changeState(new EndState(plugin));
        }
    }

    @EventHandler
    public void onDeath(EntityDamageByEntityEvent event) {

        if((event.getDamager() instanceof Player) && (event.getEntity() instanceof Player)) {

            Player entity = (Player) event.getEntity();

            if(event.getDamage() > entity.getHealth()) {

                event.setCancelled(true);

                TeamManager teamManager = plugin.getTeamManager();
                List<Location> defenderSpawnPoints = plugin.getMarkerManager().getMarkers(MarkerType.DEFENDERS_SPAWN_POINT);

                if (defenderSpawnPoints.isEmpty()) {
                    plugin.getLogger().warning("No defender spawn points set! Cannot respawn defender.");
                    return;
                }

                if (teamManager.isAttacker(entity) || teamManager.isKing(entity)) {

                    entity.setGameMode(GameMode.SPECTATOR);
                    entity.setHealth(entity.getMaxHealth());
                    entity.getInventory().clear();

                    teamManager.removePlayer(entity);

                    Bukkit.broadcastMessage(entity.getName() + " has been eliminated.");

                    checkWinCondition();

                } else if (teamManager.isDefender(entity)) {

                    Location spawnLocation = defenderSpawnPoints.stream()
                            .skip(new Random().nextInt(defenderSpawnPoints.size()))
                            .findFirst()
                            .orElse(null);

                    entity.setHealth(entity.getMaxHealth());
                    entity.teleport(spawnLocation);
                }
            }
        } else {
            event.setCancelled(true);
        }
    }

    @Override
    public void onExit() {

        HandlerList.unregisterAll(this);
        plugin.getLogger().info("Exiting Fighting State");
    }

    @Override public boolean canBreakBlocks() { return true; }
    @Override public boolean canPlaceBlocks() { return true; }
    @Override public boolean canReceiveDamage() { return true; }

    @Override
    public String getName() {
        return "FIGHTING";
    }
}
