package fr.arnaud.siege.game.setup;

import fr.arnaud.siege.marker.MarkerManager;
import fr.arnaud.siege.marker.MarkerType;
import org.bukkit.Location;

import java.util.List;

public final class MapValidationService {

    private MapValidationService() {}

    public static void validateMarkers(MarkerManager markerManager) throws IllegalStateException {

        if (markerManager.getMarkers(MarkerType.ATTACKERS_SPAWN_POINT).isEmpty()) {
            throw new IllegalStateException("Map validation failed: No ATTACKERS_SPAWN_POINT markers found.");
        }
        if (markerManager.getMarkers(MarkerType.DEFENDERS_SPAWN_POINT).isEmpty()) {
            throw new IllegalStateException("Map validation failed: No DEFENDERS_SPAWN_POINT markers found.");
        }

        List<Location> eastMarkers = markerManager.getMarkers(MarkerType.WALL_DELIMITATION_EAST);
        List<Location> northMarkers = markerManager.getMarkers(MarkerType.WALL_DELIMITATION_NORTH);

        if (!eastMarkers.isEmpty() && !northMarkers.isEmpty()) {
            throw new IllegalStateException("Map validation failed: Both EAST and NORTH wall markers were found." +
                    "Only one type is allowed.");
        }

        if (eastMarkers.isEmpty() && northMarkers.isEmpty()) {
            throw new IllegalStateException("Map validation failed: No wall delimitation marker" +
                    "(NORTH or EAST) was found.");
        }
    }
}