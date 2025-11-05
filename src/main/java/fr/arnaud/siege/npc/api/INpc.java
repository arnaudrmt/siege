package fr.arnaud.siege.npc.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

public interface INpc {

    void spawn(Collection<Player> players);

    void destroy(Collection<Player> players);

    void lookAt(Player player);

    int getEntityId();

    UUID getUniqueId();

    Location getLocation();
}
