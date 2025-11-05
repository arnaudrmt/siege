package fr.arnaud.siege.nms.v1_8_R3.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import fr.arnaud.siege.Siege;
import fr.arnaud.siege.npc.api.INpc;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

public class NpcEntity_R8 implements INpc {

    private final Siege plugin;

    private final EntityPlayer npc;
    private final Location location;

    public NpcEntity_R8(Siege plugin, String name, String skinValue, String skinSignature, Location spawnLocation) {

        this.plugin = plugin;
        this.location = spawnLocation;

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();

        GameProfile profile = new GameProfile(UUID.randomUUID(), name);
        profile.getProperties().put("textures", new Property("textures", skinValue, skinSignature));

        this.npc = new EntityPlayer(server, world, profile, new PlayerInteractManager(world));
        this.npc.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    @Override
    public void spawn(Collection<Player> players) {
        players.forEach(this::sendSpawnPackets);
    }

    @Override
    public void destroy(Collection<Player> players) {
        players.forEach(this::sendDestroyPackets);
    }

    private void sendSpawnPackets(Player player) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,
                npc));
        connection.sendPacket(new PacketPlayOutNamedEntitySpawn(npc));

        Bukkit.getScheduler().runTaskLater(plugin, () ->
            connection.sendPacket(
                    new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,
                    npc)), 10L);
    }

    private void sendDestroyPackets(Player player) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.sendPacket(new PacketPlayOutEntityDestroy(npc.getId()));
    }

    @Override
    public void lookAt(Player target) {
        Location npcLoc = npc.getBukkitEntity().getLocation();
        Location targetLoc = target.getLocation();
        double trackRange = 5.0;

        if (npcLoc.getWorld() != targetLoc.getWorld() ||
                npcLoc.distanceSquared(targetLoc) > trackRange * trackRange) {
            return;
        }

        double dx = targetLoc.getX() - npcLoc.getX();
        double dy = (targetLoc.getY() + 1.62) - (npcLoc.getY() + 1.62);
        double dz = targetLoc.getZ() - npcLoc.getZ();

        double distanceXZ = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) Math.toDegrees(-Math.atan2(dy, distanceXZ));

        byte yawByte = (byte) ((yaw + 360) % 360 * 256 / 360);
        byte pitchByte = (byte) ((pitch + 360) % 360 * 256 / 360);

        PacketPlayOutEntity.PacketPlayOutEntityLook lookPacket = new PacketPlayOutEntity.PacketPlayOutEntityLook(npc.getId(), yawByte, pitchByte, true);
        PacketPlayOutEntityHeadRotation headPacket = new PacketPlayOutEntityHeadRotation(npc, yawByte);

        PlayerConnection conn = ((CraftPlayer) target).getHandle().playerConnection;
        conn.sendPacket(lookPacket);
        conn.sendPacket(headPacket);
    }

    @Override
    public int getEntityId() {
        return npc.getId();
    }

    @Override
    public UUID getUniqueId() {
        return npc.getUniqueID();
    }

    @Override
    public Location getLocation() {
        return location.clone();
    }
}
