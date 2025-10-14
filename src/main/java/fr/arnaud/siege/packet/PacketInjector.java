package fr.arnaud.siege.packet;

import fr.arnaud.siege.Siege;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockChange;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class PacketInjector {

    public static void inject(Player player) {

        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();

        if(pipeline.get("PacketInjector") != null) return;

        pipeline.addBefore("packet_handler", "PacketInjector", new ChannelDuplexHandler() {

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

                    if (!Siege.getInstance().getNpcManager().getNpcs().isEmpty() &&
                            Siege.getInstance().getNpcManager().getNpcs().contains(entityId)) {
                        Siege.getInstance().getNpcManager().handleNpcClick(player, entityId);
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

                    if (Siege.getInstance().getBuildVisibilityManager().containsHiddenBlockAt(loc)) {
                        return;
                    }
                }

                super.write(ctx, msg, promise);
            }
        });
    }

    public static void uninject(Player player) {
        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();

        if (pipeline.get("PacketInjector") != null) {
            pipeline.remove("PacketInjector");
            Siege.getInstance().getLogger().info("Successfully uninjected " + player.getName());
        }
    }
}
