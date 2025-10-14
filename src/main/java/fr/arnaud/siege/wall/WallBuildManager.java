package fr.arnaud.siege.wall;

import fr.arnaud.siege.Siege;
import fr.arnaud.siege.marker.MarkerType;
import fr.arnaud.siege.util.Cuboid;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class WallBuildManager {

    private final Siege plugin;

    private static final int WALL_RADIUS = 5;
    private static final int MAX_BUILD_DISTANCE = 50;

    private BukkitRunnable boundaryTask;

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

    public WallBuildManager(Siege plugin) {
        this.plugin = plugin;
    }

    public boolean isAllowed(Player player, Location checkLocation, ActionType type) {

        if (type == ActionType.BLOCK && plugin.getTeamManager().isAttacker(player)) {
            return false;
        }

        if (type == ActionType.MOVE && plugin.getTeamManager().isDefender(player)) {
            return false;
        }

        MarkerData markerData = getWallMarker();

        if (markerData == null) {
            return true;
        }

        return isWithinAllowedArea(checkLocation, markerData.location, markerData.direction);
    }

    private MarkerData getWallMarker() {
        List<Location> eastMarkers = plugin.getMarkerManager().getMarkers(MarkerType.WALL_DELIMITATION_EAST);
        List<Location> northMarkers = plugin.getMarkerManager().getMarkers(MarkerType.WALL_DELIMITATION_NORTH);

        if (!eastMarkers.isEmpty() && !northMarkers.isEmpty()) {
            plugin.getLogger().warning("Both EAST and NORTH wall markers found, defaulting to EAST.");
        }

        if (!eastMarkers.isEmpty()) {
            return new MarkerData(eastMarkers.get(0), WallDirection.EAST);
        }
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

        boundaryTask = new BukkitRunnable() {
            @Override
            public void run() {
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
            }
        };

        boundaryTask.runTaskTimer(plugin, 0L, 20L);
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