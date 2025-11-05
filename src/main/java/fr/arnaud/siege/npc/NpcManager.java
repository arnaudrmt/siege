package fr.arnaud.siege.npc;

import fr.arnaud.siege.Siege;
import fr.arnaud.siege.npc.api.INpc;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NpcManager {

    private final Siege plugin;

    private final Map<Integer, INpc> npcs = new ConcurrentHashMap<>();
    private BukkitTask trackingTask;

    public NpcManager(Siege plugin) {
        this.plugin = plugin;
    }

    public void spawnNpc(Collection<Player> players, String name, String skinValue, String skinSignature,
                         Location location) {

        INpc npc = plugin.getNmsHandler().createNpc(name, skinValue, skinSignature, location);
        npcs.put(npc.getEntityId(), npc);

        npc.spawn(players);

        if (trackingTask == null) {
            startPlayerTracking();
        }
    }

    public void removeAllNpc() {

        if (trackingTask != null) {
            trackingTask.cancel();
            trackingTask = null;
        }

        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        for (INpc npc : npcs.values()) {
            npc.destroy(onlinePlayers);
        }
        npcs.clear();
    }

    private void startPlayerTracking() {
        trackingTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (npcs.isEmpty()) {
                trackingTask.cancel();
                trackingTask = null;
                return;
            }

            for (INpc npc : npcs.values()) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getWorld().equals(npc.getLocation().getWorld()) &&
                        player.getLocation().distanceSquared(npc.getLocation()) < 100) {
                        npc.lookAt(player);
                    }
                }
            }
        }, 0L, 2L);
    }

    public void handleNpcClick(Player player, int entityId) {
        INpc npc = getNpcById(entityId);

        if(npc != null) {
            player.sendMessage("You've clicked on an NPC.");
            // Implement Upgrade logic
        }
    }

    public INpc getNpcById(int entityId) {
        return npcs.get(entityId);
    }

    public Collection<INpc> getNpcs() {
        return Collections.unmodifiableCollection(npcs.values());
    }

    public Collection<Integer> getNpcIds() {
        return Collections.unmodifiableCollection(npcs.keySet());
    }
}
