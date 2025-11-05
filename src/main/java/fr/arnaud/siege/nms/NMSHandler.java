package fr.arnaud.siege.nms;

import fr.arnaud.siege.npc.api.INpc;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public interface NMSHandler {

    void inject(Player player);

    void uninject(Player player);

    void changeBlock(Location location, Material material, Player player);

    INpc createNpc(String name, String skinValue, String skinSignature, Location location);

    void sendParticles(Player player, EnumParticle particle, Location loc,
                  float offsetX, float offsetY, float offsetZ, float speed, int count, boolean force);
}
