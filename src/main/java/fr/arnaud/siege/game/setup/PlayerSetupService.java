package fr.arnaud.siege.game.setup;

import fr.arnaud.siege.Siege;
import fr.arnaud.siege.game.TeamManager;
import fr.arnaud.siege.marker.MarkerManager;
import fr.arnaud.siege.marker.MarkerType;
import fr.arnaud.siege.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class PlayerSetupService {

    private PlayerSetupService() {}

    public static void assignRolesAndTeleport(Siege plugin) {
        TeamManager teamManager = plugin.getTeamManager();
        MarkerManager markerManager = plugin.getMarkerManager();
        Logger logger = plugin.getLogger();

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        teamManager.assignRoles(players);

        List<Location> defendersSpawns = markerManager.getMarkers(MarkerType.DEFENDERS_SPAWN_POINT);
        List<Location> attackersSpawns = markerManager.getMarkers(MarkerType.ATTACKERS_SPAWN_POINT);

        if (defendersSpawns.isEmpty() || attackersSpawns.isEmpty()) {
            logger.severe("Missing spawn point markers! Cannot teleport players.");
            return;
        }

        teamManager.getOnlineAttackers()
                .forEach(player -> {
                    Location spawn = Utils.pickRandom(attackersSpawns);
                    player.teleport(spawn.clone().add(0, 1, 0));
                    player.setGameMode(GameMode.SURVIVAL);
                    player.getInventory().clear();
                    player.setHealth(player.getMaxHealth());
                    player.setFoodLevel(20);
                    player.setSaturation(20);
                    plugin.getNmsHandler().inject(player);
                });

        teamManager.getOnlineDefenders()
                .forEach(player -> {
                    Location spawn = Utils.pickRandom(defendersSpawns);
                    player.setGameMode(GameMode.SURVIVAL);
                    player.getInventory().clear();
                    player.setHealth(player.getMaxHealth());
                    player.setFoodLevel(20);
                    player.setSaturation(20);
                    player.teleport(spawn);
                });

        logger.info("Successfully assigned roles and teleported players.");
    }
}