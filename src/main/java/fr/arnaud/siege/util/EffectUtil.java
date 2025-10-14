package fr.arnaud.siege.util;

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class EffectUtil {

    /**
     * Sends a particle to a player with full customization.
     *
     * @param player   Target player
     * @param particle Particle type (EnumParticle)
     * @param loc      Location of particle
     * @param offsetX  X-axis offset (spread)
     * @param offsetY  Y-axis offset (spread)
     * @param offsetZ  Z-axis offset (spread)
     * @param speed    Speed of particle
     * @param count    Number of particles
     * @param force    If true, particle is visible from far away
     */
    public static void sendParticles(Player player, EnumParticle particle, Location loc,
                                     float offsetX, float offsetY, float offsetZ,
                                     float speed, int count, boolean force) {

        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
                particle,
                force,
                (float) loc.getX(),
                (float) loc.getY(),
                (float) loc.getZ(),
                offsetX,
                offsetY,
                offsetZ,
                speed,
                count
        );

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    public static void playSound(Player player, Location loc, Sound sound, float volume, float pitch) {
        player.playSound(loc, sound, volume, pitch);
    }
}