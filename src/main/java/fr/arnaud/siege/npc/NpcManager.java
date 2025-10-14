package fr.arnaud.siege.npc;

import fr.arnaud.siege.Siege;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;

public class NpcManager {

    private final List<NpcPlayerEntity> npcs = Collections.synchronizedList(new ArrayList<>());
    private final Siege plugin;

    private BukkitTask tracking;

    public NpcManager(Siege plugin) {
        this.plugin = plugin;
    }

    public void spawnNpc(Collection<Player> players, String name, String skinValue, String skinSignature, Location location) {

        NpcPlayerEntity npc = new NpcPlayerEntity(name, skinValue, skinSignature, location);
        npcs.add(npc);

        npc.bulkSpawn(players);

        lookAtPlayers();
    }

    public void removeAllNpc() {

        Collection<Player> players = Bukkit.getOnlinePlayers().stream()
                .filter(Objects::nonNull)
                .filter(OfflinePlayer::isOnline)
                .collect(Collectors.toList());

        npcs.forEach(npc -> {
            npc.bulkDelete(players);
            npcs.remove(npc);
        });
    }

    public void lookAtPlayers() {

        if(tracking != null) return;

        tracking = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            npcs.forEach(npc -> {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (player.getLocation().distance(npc.getLocation()) < 10) {
                        npc.lookAtPlayer(player);
                    }
                });
            });
        }, 0L, 1L);
    }

    public void handleNpcClick(Player player, int entityId) {

        NpcPlayerEntity npc = getNpcById(entityId);

        if(npc != null) {
            player.sendMessage("You've clicked on an NPC.");
        }
    }

    public NpcPlayerEntity getNpcById(int id) {
        return npcs.stream()
                .filter(npc -> npc.getEntityId() == id)
                .findFirst()
                .orElse(null);
    }

    public List<NpcPlayerEntity> getNpcs() {
        return npcs;
    }

    public void cleanUp() {
        Bukkit.getOnlinePlayers().forEach(player -> npcs.forEach(npc -> npc.deleteFor(player)));
        if(tracking != null) {
            tracking.cancel();
            tracking = null;
        }
        npcs.clear();
    }
}
