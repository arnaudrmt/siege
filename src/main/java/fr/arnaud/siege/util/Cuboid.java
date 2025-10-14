package fr.arnaud.siege.util;

import org.bukkit.Location;

public class Cuboid {

    private final Location corner1;
    private final Location corner2;

    public Cuboid(Location corner1, Location corner2) {
        this.corner1 = corner1;
        this.corner2 = corner2;
    }

    public int getMinX() {
        return Math.min(corner1.getBlockX(), corner2.getBlockX());
    }

    public int getMaxX() {
        return Math.max(corner1.getBlockX(), corner2.getBlockX());
    }

    public int getMinY() {
        return Math.min(corner1.getBlockY(), corner2.getBlockY());
    }

    public int getMaxY() {
        return Math.max(corner1.getBlockY(), corner2.getBlockY());
    }

    public int getMinZ() {
        return Math.min(corner1.getBlockZ(), corner2.getBlockZ());
    }

    public int getMaxZ() {
        return Math.max(corner1.getBlockZ(), corner2.getBlockZ());
    }

    public Location getCorner1() {
        return corner1;
    }

    public Location getCorner2() {
        return corner2;
    }

    public boolean contains(Location loc) {

        if (!loc.getWorld().equals(corner1.getWorld())) return false;

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        return x >= getMinX() && x <= getMaxX()
                && y >= getMinY() && y <= getMaxY()
                && z >= getMinZ() && z <= getMaxZ();
    }

    public Location getCenter() {
        double x = (getMinX() + getMaxX()) / 2.0 + 0.5;
        double y = (getMinY() + getMaxY()) / 2.0 + 0.5;
        double z = (getMinZ() + getMaxZ()) / 2.0 + 0.5;
        return new Location(corner1.getWorld(), x, y, z);
    }

    public int getVolume() {
        return (getMaxX() - getMinX() + 1)
                * (getMaxY() - getMinY() + 1)
                * (getMaxZ() - getMinZ() + 1);
    }
}