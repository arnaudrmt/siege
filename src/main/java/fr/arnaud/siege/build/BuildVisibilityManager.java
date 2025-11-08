package fr.arnaud.siege.build;

import fr.arnaud.siege.Siege;
import fr.arnaud.siege.game.TeamManager;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;

public class BuildVisibilityManager {

    private final TeamManager teamManager;
    private final NavigableMap<Integer, List<GhostBlock>> hiddenBlocksByLayer = new TreeMap<>();

    private BukkitTask revealingTask;

    public BuildVisibilityManager(TeamManager teamManager) {
        this.teamManager = teamManager;
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

        layerBlocks.removeIf(block ->
                block.getLocation().getBlockX() == location.getBlockX() &&
                        block.getLocation().getBlockY() == location.getBlockY() &&
                        block.getLocation().getBlockZ() == location.getBlockZ()
        );

        if (layerBlocks.isEmpty()) {
            hiddenBlocksByLayer.remove(y);
        }
    }

    public void revealBlockToAttackers(GhostBlock ghostBlock, List<Player> onlineAttackers) {

        Location location = ghostBlock.getLocation();
        Material type = ghostBlock.getMaterial();

        onlineAttackers.forEach(player -> {
            Siege.getInstance().getNmsHandler().changeBlock(location, type, player);
            Siege.getInstance().getNmsHandler().sendParticles(player, EnumParticle.CRIT,
                    location, 0.3f, 0.3f, 0.3f, 0.1f, 5, false);
        });
    }

    public void startLayeredReveal() {

        Iterator<Map.Entry<Integer, List<GhostBlock>>> layerIterator = hiddenBlocksByLayer.entrySet().iterator();

        final Set<Chunk> chunksToRefresh = new HashSet<>();

        revealingTask = Bukkit.getScheduler().runTaskTimer(Siege.getInstance(), () -> {
            if (!layerIterator.hasNext()) {
                revealingTask.cancel();
                revealingTask = null;

                List<Player> onlineAttackers = teamManager.getOnlineAttackers();
                if (!onlineAttackers.isEmpty()) {
                    World world = onlineAttackers.get(0).getWorld();
                    chunksToRefresh.forEach(chunk -> {
                        world.refreshChunk(chunk.getX(), chunk.getZ());
                    });
                }

                hiddenBlocksByLayer.clear();
                return;
            }

            Map.Entry<Integer, List<GhostBlock>> entry = layerIterator.next();
            List<GhostBlock> layerBlocks = entry.getValue();
            List<Player> onlineAttackers = teamManager.getOnlineAttackers();
            if (onlineAttackers.isEmpty()) return;

            layerBlocks.forEach(block -> {
                revealBlockToAttackers(block, onlineAttackers);
                chunksToRefresh.add(block.getLocation().getChunk());
            });
        }, 0L, 10L);
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

    public boolean chunkContainsHiddenBlocks(int chunkX, int chunkZ) {
        return hiddenBlocksByLayer.values().stream()
                .flatMap(List::stream)
                .anyMatch(ghostBlock ->
                        (ghostBlock.getLocation().getBlockX() >> 4) == chunkX &&
                                (ghostBlock.getLocation().getBlockZ() >> 4) == chunkZ
                );
    }

    public List<GhostBlock> getHiddenBlocksInChunk(int chunkX, int chunkZ) {
        return hiddenBlocksByLayer.values().stream()
                .flatMap(List::stream)
                .filter(ghostBlock ->
                        (ghostBlock.getLocation().getBlockX() >> 4) == chunkX &&
                                (ghostBlock.getLocation().getBlockZ() >> 4) == chunkZ
                )
                .collect(Collectors.toList());
    }
}
