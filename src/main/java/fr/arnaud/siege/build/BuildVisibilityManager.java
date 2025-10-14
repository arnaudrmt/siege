package fr.arnaud.siege.build;

import fr.arnaud.siege.Siege;
import fr.arnaud.siege.util.EffectUtil;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class BuildVisibilityManager {

    private final Siege plugin;
    private final NavigableMap<Integer, List<GhostBlock>> hiddenBlocksByLayer = new TreeMap<>();

    private BukkitRunnable revealingTask;

    public BuildVisibilityManager(Siege plugin) {
        this.plugin = plugin;
    }

    public void addGhostBlock(Location location, Material type) {
        int y = location.getBlockY();
        List<GhostBlock> layer = hiddenBlocksByLayer.computeIfAbsent(y, k -> new ArrayList<>());

        if (layer.stream().noneMatch(block -> block.getLocation().equals(location))) {
            layer.add(new GhostBlock(location, type));
        }
    }

    public void removeGhostBlock(Location location) {
        int y = location.getBlockY();

        List<GhostBlock> layerBlocks = hiddenBlocksByLayer.get(y);
        if (layerBlocks == null) return;

        layerBlocks.removeIf(block -> {
            block.getLocation().setPitch(0);
            block.getLocation().setYaw(0);
            location.setPitch(0);
            location.setYaw(0);
            return block.getLocation().equals(location);
        });

        if (layerBlocks.isEmpty()) {
            hiddenBlocksByLayer.remove(y);
        }
    }

    public void revealBlockToAttackers(GhostBlock ghostBlock, List<Player> onlineAttackers) {

        World world = ghostBlock.getLocation().getWorld();
        WorldServer worldServer = ((CraftWorld) world).getHandle();

        Location location = ghostBlock.getLocation();
        Material material = ghostBlock.getMaterial();

        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Block nmsBlock = CraftMagicNumbers.getBlock(material);
        IBlockData blockData = nmsBlock.getBlockData();

        PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(worldServer, blockPosition);
        packet.block = blockData;

        onlineAttackers.forEach(player -> {

                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);

                    EffectUtil.sendParticles(player, EnumParticle.CRIT,
                            location,
                            0.3f, 0.3f, 0.3f,
                            0.1f, 5, false);
                });
    }

    public void startLayeredReveal() {

        if (hiddenBlocksByLayer.isEmpty() || revealingTask != null) return;

        Iterator<Map.Entry<Integer, List<GhostBlock>>> layerIterator = hiddenBlocksByLayer.entrySet().iterator();

        revealingTask = new BukkitRunnable() {

            @Override
            public void run() {
                if (!layerIterator.hasNext()) {
                    hiddenBlocksByLayer.clear();
                    cancel();
                    revealingTask = null;
                    return;
                }

                Map.Entry<Integer, List<GhostBlock>> entry = layerIterator.next();
                List<GhostBlock> layerBlocks = entry.getValue();

                List<Player> onlineAttackers = plugin.getTeamManager().getAttackers().stream()
                        .map(Bukkit::getPlayer)
                        .filter(Objects::nonNull)
                        .filter(Player::isOnline)
                        .collect(Collectors.toList());

                layerBlocks.forEach(block -> revealBlockToAttackers(block, onlineAttackers));
            }
        };

        revealingTask.runTaskTimer(plugin, 20L, 0L);
    }

    public void resetReveal() {
        if (revealingTask != null) {
            revealingTask.cancel();
            revealingTask = null;
        }
        hiddenBlocksByLayer.clear();
    }

    public boolean containsHiddenBlockAt(Location targetLocation) {
        return hiddenBlocksByLayer.values().stream()
                .flatMap(List::stream)
                .anyMatch(ghostBlock -> ghostBlock.getLocation().equals(targetLocation));
    }
}
