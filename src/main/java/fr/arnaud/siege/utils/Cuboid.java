package fr.arnaud.siege.utils;

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
}