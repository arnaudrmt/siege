package fr.arnaud.siege.wall;

import fr.arnaud.siege.util.Cuboid;
import fr.arnaud.siege.util.EffectUtil;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;

public class WallBoundaryVisualizer {

    private static final int WALLS_HEIGHT = 10;

    public static void displayFullBoundary(Location markerLoc, Cuboid wallCuboid, Collection<? extends Player> players) {

        World world = markerLoc.getWorld();
        Location tempLoc = new Location(world, 0, 0, 0);

        // Spawn bottom face fill (y = minY)
        for (int x = wallCuboid.getMinX(); x <= wallCuboid.getMaxX(); x++) {
            for (int z = wallCuboid.getMinZ(); z <= wallCuboid.getMaxZ(); z++) {
                tempLoc.setX(x + 0.5);
                tempLoc.setY(wallCuboid.getMinY() + 0.5);
                tempLoc.setZ(z + 0.5);
                players.forEach(player -> EffectUtil.sendParticles(player, EnumParticle.REDSTONE, tempLoc, 0f, 0f, 0f, 0f, 1, true));
            }
        }

        // Spawn top face fill (y = minY + verticalHeight)
        int topY = wallCuboid.getMinY() + WALLS_HEIGHT;
        for (int x = wallCuboid.getMinX(); x <= wallCuboid.getMaxX(); x++) {
            for (int z = wallCuboid.getMinZ(); z <= wallCuboid.getMaxZ(); z++) {
                tempLoc.setX(x + 0.5);
                tempLoc.setY(topY + 0.5);
                tempLoc.setZ(z + 0.5);
                players.forEach(player -> EffectUtil.sendParticles(player, EnumParticle.REDSTONE, tempLoc, 0f, 0f, 0f, 0f, 1, true));
            }
        }

        // Spawn vertical edges as before:
        for (int x = wallCuboid.getMinX(); x <= wallCuboid.getMaxX(); x++) {
            for (int z = wallCuboid.getMinZ(); z <= wallCuboid.getMaxZ(); z++) {
                int edgeCount = 0;
                if (x == wallCuboid.getMinX() || x == wallCuboid.getMaxX()) edgeCount++;
                if (z == wallCuboid.getMinZ() || z == wallCuboid.getMaxZ()) edgeCount++;

                // Vertical edges only along perimeter blocks:
                if (edgeCount > 0) {
                    for (int yOffset = 0; yOffset <= WALLS_HEIGHT; yOffset++) {
                        tempLoc.setX(x + 0.5);
                        tempLoc.setY(wallCuboid.getMinY() + yOffset + 0.5);
                        tempLoc.setZ(z + 0.5);
                        players.forEach(player -> EffectUtil.sendParticles(player, EnumParticle.REDSTONE, tempLoc, 0f, 0f, 0f, 0f, 1, true));
                    }
                }
            }
        }
    }
}
