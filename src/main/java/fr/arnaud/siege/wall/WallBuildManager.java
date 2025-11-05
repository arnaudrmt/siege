package fr.arnaud.siege.wall;

import fr.arnaud.siege.Siege;
import fr.arnaud.siege.game.TeamManager;
import fr.arnaud.siege.marker.MarkerManager;
import fr.arnaud.siege.marker.MarkerType;
import fr.arnaud.siege.utils.Cuboid;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class WallBuildManager {

    private final Siege plugin;
    private final TeamManager teamManager;
    private final MarkerManager markerManager;

    private static final int WALL_RADIUS = 5;
    private static final int MAX_BUILD_DISTANCE = 50;

    private BukkitTask boundaryTask;

    public enum WallDirection {
        NORTH, EAST
    }

    public enum ActionType {
        BLOCK, MOVE
    }

    private static class MarkerData {
        Location location;
        WallDirection direction;

        MarkerData(Location location, WallDirection direction) {
            this.location = location;
            this.direction = direction;
        }
    }

    public WallBuildManager(Siege plugin, TeamManager teamManager, MarkerManager markerManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
        this.markerManager = markerManager;
    }

    public boolean isAllowed(Player player, Location location, ActionType type) {
        MarkerData markerData = getWallMarker();
        if (markerData == null) return true;

        boolean isWithinBuildZone = isWithinAllowedArea(location, markerData.location, markerData.direction);
        boolean isAttacker = plugin.getTeamManager().isAttacker(player);

        if (isAttacker && type == ActionType.BLOCK) {
            return false;
        }

        if (!isAttacker && type == ActionType.BLOCK) {
            return isWithinBuildZone;
        }

        if (isAttacker && type == ActionType.MOVE) {
            return !isWithinBuildZone;
        }

        if(!isAttacker && type == ActionType.MOVE) {
            return true;
        }

        return true;
    }

    private MarkerData getWallMarker() {
        List<Location> eastMarkers = plugin.getMarkerManager().getMarkers(MarkerType.WALL_DELIMITATION_EAST);
        if (!eastMarkers.isEmpty()) {
            return new MarkerData(eastMarkers.get(0), WallDirection.EAST);
        }

        List<Location> northMarkers = plugin.getMarkerManager().getMarkers(MarkerType.WALL_DELIMITATION_NORTH);
        if (!northMarkers.isEmpty()) {
            return new MarkerData(northMarkers.get(0), WallDirection.NORTH);
        }

        return null;
    }

    public boolean isWithinAllowedArea(Location loc, Location markerLoc, WallDirection direction) {
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        int markerX = markerLoc.getBlockX();
        int markerZ = markerLoc.getBlockZ();

        if (direction.equals(WallDirection.NORTH)) {
            boolean xInRange = Math.abs(x - markerX) <= WALL_RADIUS;
            boolean zInRange = Math.abs(z - markerZ) <= MAX_BUILD_DISTANCE;
            return xInRange && zInRange;
        } else if (direction.equals(WallDirection.EAST)) {
            boolean zInRange = Math.abs(z - markerZ) <= WALL_RADIUS;
            boolean xInRange = Math.abs(x - markerX) <= MAX_BUILD_DISTANCE;
            return xInRange && zInRange;
        }

        return false;
    }

    public void showBoundaries() {

        if (boundaryTask != null) {
            boundaryTask.cancel();
        }

        boundaryTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Location markerLoc = getCurrentMarkerLocation();
            WallDirection direction = getCurrentMarkerDirection();

            if (markerLoc == null || direction == null) {
                plugin.getLogger().warning("Wall marker not found, cannot show boundaries.");
                return;
            }

            Location corner1 = calculateFirstCorner(markerLoc, direction);
            Location corner2 = calculateSecondCorner(markerLoc, direction);

            Cuboid wallCuboid = new Cuboid(corner1, corner2);

            WallBoundaryVisualizer.displayFullBoundary(markerLoc, wallCuboid, new ArrayList<>(Bukkit.getOnlinePlayers()));

        }, 0L, 20);
    }

    public void stopShowingBoundaries() {
        if (boundaryTask != null) {
            boundaryTask.cancel();
            boundaryTask = null;
        }
    }

    public Location calculateFirstCorner(Location markerLoc, WallDirection direction) {
        Location corner = markerLoc.clone();

        if (direction.equals(WallBuildManager.WallDirection.NORTH)) {
            corner.add(-5, 0, -50);
        } else if (direction.equals(WallBuildManager.WallDirection.EAST)) {
            corner.add(-50, 0, -5);
        }
        return corner;
    }

    public Location calculateSecondCorner(Location markerLoc, WallDirection direction) {
        Location corner = markerLoc.clone();

        if (direction.equals(WallBuildManager.WallDirection.NORTH)) {
            corner.add(5, 0, 50);
        } else if (direction.equals(WallBuildManager.WallDirection.EAST)) {
            corner.add(50, 0, 5);
        }
        return corner;
    }

    public Location getCurrentMarkerLocation() {
        MarkerData markerData = getWallMarker();
        return markerData != null ? markerData.location.clone() : null;
    }

    public WallDirection getCurrentMarkerDirection() {
        MarkerData markerData = getWallMarker();
        return markerData != null ? markerData.direction : null;
    }
}