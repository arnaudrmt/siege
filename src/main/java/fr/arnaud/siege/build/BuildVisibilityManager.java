package fr.arnaud.siege.build;

import fr.arnaud.siege.Siege;
import fr.arnaud.siege.game.TeamManager;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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

        if (hiddenBlocksByLayer.isEmpty() || revealingTask != null) return;

        Iterator<Map.Entry<Integer, List<GhostBlock>>> layerIterator = hiddenBlocksByLayer.entrySet().iterator();

        revealingTask = Bukkit.getScheduler().runTaskTimer(Siege.getInstance(), () -> {
            if (!layerIterator.hasNext()) {
                hiddenBlocksByLayer.clear();
                revealingTask.cancel();
                revealingTask = null;
                return;
            }

            Map.Entry<Integer, List<GhostBlock>> entry = layerIterator.next();
            List<GhostBlock> layerBlocks = entry.getValue();

            List<Player> onlineAttackers = teamManager.getAttackers().stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .filter(Player::isOnline)
                    .collect(Collectors.toList());

            layerBlocks.forEach(block -> revealBlockToAttackers(block, onlineAttackers));
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
}
