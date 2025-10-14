package fr.arnaud.siege.marker;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;

import java.util.*;

public class MarkerManager {

    private final Map<MarkerType, List<Location>> markers = new HashMap<>();

    public void scanMarkers(World world) {
        markers.clear();

        Arrays.stream(MarkerType.values()).forEach(type -> markers.put(type, new ArrayList<>()));

        world.getEntities().forEach(entity -> {
            if(entity instanceof ArmorStand && entity.getCustomName() != null) {
                ArmorStand armorStand = (ArmorStand) entity;
                String name = armorStand.getCustomName();

                try {
                    MarkerType type = MarkerType.valueOf(name);
                    markers.get(type).add(armorStand.getLocation());

                } catch (IllegalArgumentException ignored) {

                }
            }
        });

        markers.entrySet().stream().filter(entry -> entry.getValue().isEmpty()).forEach(entry -> {
            MarkerType type = entry.getKey();
            Bukkit.getLogger().warning("No " + type + " markers found! The game may malfunction.");
        });
    }

    public List<Location> getMarkers(MarkerType type) {
        return markers.getOrDefault(type, Collections.emptyList());
    }
}
