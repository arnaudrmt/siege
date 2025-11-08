package fr.arnaud.siege.nms.v1_8_R3;

import fr.arnaud.siege.Siege;
import fr.arnaud.siege.nms.NMSHandler;
import fr.arnaud.siege.nms.v1_8_R3.entity.NpcEntity_R8;
import fr.arnaud.siege.npc.api.INpc;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class NMSHandler_v1_8_R3 implements NMSHandler {

    private final Siege plugin;

    public NMSHandler_v1_8_R3(Siege plugin) {
        this.plugin = plugin;
    }

    @Override
    public void inject(Player player) {

        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

                if(msg instanceof PacketPlayInUseEntity) {

                    PacketPlayInUseEntity packet = (PacketPlayInUseEntity) msg;
                    int entityId = -1;

                    try {
                        Field entityIdField = packet.getClass().getDeclaredField("a");
                        entityIdField.setAccessible(true);
                        entityId = entityIdField.getInt(packet);
                    } catch (Exception e) {
                        Bukkit.getLogger().warning("The entityIdField could not be found.");
                    }

                    if (!plugin.getNpcManager().getNpcs().isEmpty() &&
                            plugin.getNpcManager().getNpcIds().contains(entityId)) {
                        plugin.getNpcManager().handleNpcClick(player, entityId);
                    }
                }

                super.channelRead(ctx, msg);
            }

            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

                if (msg instanceof PacketPlayOutBlockChange) {

                    PacketPlayOutBlockChange packet = (PacketPlayOutBlockChange) msg;

                    Field positionField = packet.getClass().getDeclaredField("a");
                    positionField.setAccessible(true);
                    BlockPosition position = (BlockPosition) positionField.get(packet);

                    Location loc = new Location(Bukkit.getWorlds().get(0), position.getX(), position.getY(), position.getZ());

                    if (plugin.getBuildVisibilityManager().containsHiddenBlockAt(loc)) {
                        return;
                    }
                }

                if (msg instanceof PacketPlayOutMapChunk) {
                    PacketPlayOutMapChunk packet = (PacketPlayOutMapChunk) msg;

                    Field chunkXField = packet.getClass().getDeclaredField("a");
                    chunkXField.setAccessible(true);
                    int chunkX = chunkXField.getInt(packet);

                    Field chunkZField = packet.getClass().getDeclaredField("b");
                    chunkZField.setAccessible(true);
                    int chunkZ = chunkZField.getInt(packet);

                    if (!plugin.getBuildVisibilityManager().chunkContainsHiddenBlocks(chunkX, chunkZ)) {
                        super.write(ctx, msg, promise);
                        return;
                    }

                    org.bukkit.Chunk bukkitChunk = player.getWorld().getChunkAt(chunkX, chunkZ);
                    Chunk nmsChunk = ((CraftChunk) bukkitChunk).getHandle();

                    Chunk clonedNmsChunk = new Chunk(nmsChunk.getWorld(), nmsChunk.locX, nmsChunk.locZ);

                    for (int i = 0; i < nmsChunk.getSections().length; i++) {
                        ChunkSection originalSection = nmsChunk.getSections()[i];
                        if (originalSection != null) {

                            char[] blockIdsClone = originalSection.getIdArray().clone();
                            ChunkSection clonedSection = new ChunkSection(originalSection.getYPosition(), true, blockIdsClone);

                            byte[] emittedLightData = originalSection.getEmittedLightArray().a();
                            System.arraycopy(emittedLightData, 0, clonedSection.getEmittedLightArray().a(), 0, emittedLightData.length);

                            if (originalSection.getSkyLightArray() != null) {
                                byte[] skyLightData = originalSection.getSkyLightArray().a();
                                System.arraycopy(skyLightData, 0, clonedSection.getSkyLightArray().a(), 0, skyLightData.length);
                            }

                            clonedNmsChunk.getSections()[i] = clonedSection;
                        }
                    }

                    plugin.getBuildVisibilityManager().getHiddenBlocksInChunk(chunkX, chunkZ).forEach(ghostBlock -> {
                        Location loc = ghostBlock.getLocation();
                        BlockPosition pos = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                        clonedNmsChunk.a(pos, Blocks.AIR.getBlockData());
                    });

                    PacketPlayOutMapChunk newPacket = new PacketPlayOutMapChunk(clonedNmsChunk, true, 65535);

                    super.write(ctx, newPacket, promise);
                    return;
                }

                super.write(ctx, msg, promise);
            }
        };

        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();
        if (pipeline.get("SiegePacketInjector") == null) {
            pipeline.addBefore("packet_handler", "SiegePacketInjector", channelDuplexHandler);
        }
    }

    @Override
    public void uninject(Player player) {
        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();

        if (pipeline.get("SiegePacketInjector") != null) {
            pipeline.remove("packet_handler");
            plugin.getLogger().info("Successfully uninjected " + player.getName());
        }
    }

    @Override
    public void changeBlock(Location location, Material material, Player player) {

        WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

        Block realNmsBlock = CraftMagicNumbers.getBlock(material);
        IBlockData realBlockData = realNmsBlock.getBlockData();
        PacketPlayOutBlockChange realPacket = new PacketPlayOutBlockChange(worldServer, blockPosition);
        realPacket.block = realBlockData;
        playerConnection.sendPacket(realPacket);
    }

    @Override
    public INpc createNpc(String name, String skinValue, String skinSignature, Location location) {
        return new NpcEntity_R8(plugin, name, skinValue, skinSignature, location);
    }

    @Override
    public void sendParticles(Player player, EnumParticle particle, Location loc,
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
}
