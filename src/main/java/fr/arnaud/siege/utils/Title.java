package fr.arnaud.siege.utils;

import net.minecraft.server.v1_8_R3.*;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class Title {

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {

        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();
        PlayerConnection playerConnection = entityPlayer.playerConnection;

        IChatBaseComponent iChatBaseComponentTitle = IChatBaseComponent.
                ChatSerializer.a("{\"text\": \"" + title + "\"}");

        IChatBaseComponent iChatBaseComponentSubTitle = IChatBaseComponent.
                ChatSerializer.a("{\"text\": \"" + subtitle + "\"}");

        PacketPlayOutTitle packetPlayOutTiming;
        if (stay != 0) {
            packetPlayOutTiming = new PacketPlayOutTitle(
                    PacketPlayOutTitle.EnumTitleAction.TIMES, null, fadeIn, stay, fadeOut);
        } else {
            packetPlayOutTiming = new PacketPlayOutTitle(
                    PacketPlayOutTitle.EnumTitleAction.TIMES, null, 10, 100, 10);
        }
        playerConnection.sendPacket(packetPlayOutTiming);

        PacketPlayOutTitle packetPlayOutTitle = new PacketPlayOutTitle(
                PacketPlayOutTitle.EnumTitleAction.TITLE, iChatBaseComponentTitle);
        playerConnection.sendPacket(packetPlayOutTitle);

        if(subtitle != null) {
            PacketPlayOutTitle packetPlayOutSubTitle = new PacketPlayOutTitle(
                    PacketPlayOutTitle.EnumTitleAction.SUBTITLE, iChatBaseComponentSubTitle);
            playerConnection.sendPacket(packetPlayOutSubTitle);
        }
    }

    public static void sendActionBar(Player player, String message){
        IChatBaseComponent cbc = ChatSerializer.a("{\"text\": \"" + message + "\"}");
        PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc,(byte) 2);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(ppoc);
    }
}