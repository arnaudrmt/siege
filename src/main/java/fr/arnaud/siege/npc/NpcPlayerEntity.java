package fr.arnaud.siege.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

public class NpcPlayerEntity {

    private final EntityPlayer npc;
    private final Location location;

    public NpcPlayerEntity(String name, String skinValue, String skinSignature, Location spawnLocation) {

        this.location = spawnLocation;

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();

        GameProfile profile = new GameProfile(UUID.randomUUID(), name);

        profile.getProperties().put("textures", new Property("textures", skinValue, skinSignature));

        EntityPlayer npc = new EntityPlayer(server, world, profile, new PlayerInteractManager(world));
        npc.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        this.npc = npc;
    }

    public void bulkSpawn(Collection<Player> players) {
        System.out.println(players.size());
        players.forEach(this::spawnFor);
    }

    public void bulkDelete(Collection<Player> players) {

        players.forEach(this::deleteFor);
    }

    public void spawnFor(Player player) {
        PlayerConnection conn = ((CraftPlayer) player).getHandle().playerConnection;
        conn.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc));
        conn.sendPacket(new PacketPlayOutNamedEntitySpawn(npc));
    }

    public void deleteFor(Player player) {
        PlayerConnection conn = ((CraftPlayer) player).getHandle().playerConnection;
        conn.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, npc));
        conn.sendPacket(new PacketPlayOutEntityDestroy(npc.getId()));
    }

    public void lookAtPlayer(Player target) {
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

    public EntityPlayer getHandle() {
        return npc;
    }

    public int getEntityId() {
        return npc.getId();
    }

    public Location getLocation() {
        return location;
    }
}
