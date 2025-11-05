package fr.arnaud.siege.build;


import org.bukkit.Location;
import org.bukkit.Material;

public class GhostBlock {

    private final Location location;
    private final Material material;

    public GhostBlock(Location location, Material material) {
        this.location = location;
        this.material = material;
    }

    public Location getLocation() {
        return location.clone();
    }

    public Material getMaterial() {
        return material;
    }
}
