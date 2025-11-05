package fr.arnaud.siege.game;

import fr.arnaud.siege.Siege;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.stream.Collectors;

public class TeamManager {

    private final Siege plugin;

    private final Map<UUID, PlayerRole> playerRoles = new HashMap<>();
    private final Set<UUID> attackers = new HashSet<>();
    private final Set<UUID> defenders = new HashSet<>();
    private UUID kingUUID = null;

    private final Scoreboard scoreboard;
    private final Team bukkitAttackers;
    private final Team bukkitDefenders;
    private final Team bukkitKing;

    public TeamManager(Siege plugin) {
        this.plugin = plugin;
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        bukkitAttackers = scoreboard.registerNewTeam("Attackers");
        bukkitAttackers.setPrefix(ChatColor.RED + "");
        bukkitAttackers.setAllowFriendlyFire(false);

        bukkitDefenders = scoreboard.registerNewTeam("Defenders");
        bukkitDefenders.setPrefix(ChatColor.BLUE + "");
        bukkitDefenders.setAllowFriendlyFire(false);

        bukkitKing = scoreboard.registerNewTeam("King");
        bukkitKing.setPrefix(ChatColor.GOLD + "");
        bukkitKing.setAllowFriendlyFire(false);
    }

    public void assignRoles(List<Player> players) {
        resetTeams();

        Collections.shuffle(players);

        int playerCount = players.size();
        if (playerCount < Siege.MIN_PLAYERS) {
            plugin.getLogger().warning("Not enough players to start the game!");
            return;
        }

        Player king = players.get(0);
        kingUUID = king.getUniqueId();
        playerRoles.put(kingUUID, PlayerRole.KING);
        bukkitKing.addEntry(king.getName());

        int defendersCount = Math.max(1, playerCount / 3);

        players.stream().skip(1).limit(defendersCount)
                .forEach(defender -> {
                    UUID uuid = defender.getUniqueId();
                    playerRoles.put(uuid, PlayerRole.DEFENDER);
                    defenders.add(uuid);
                    bukkitDefenders.addEntry(defender.getName());
                });

        players.subList(defendersCount + 1, playerCount)
                .forEach(attacker -> {
                    UUID uuid = attacker.getUniqueId();
                    playerRoles.put(uuid, PlayerRole.ATTACKER);
                    attackers.add(uuid);
                    bukkitAttackers.addEntry(attacker.getName());
                });

        players.forEach(p -> p.setScoreboard(scoreboard));
    }

    public PlayerRole getRole(Player player) {
        return playerRoles.getOrDefault(player.getUniqueId(), PlayerRole.NONE);
    }

    public boolean isAttacker(Player player) {
        return getRole(player) == PlayerRole.ATTACKER;
    }

    public boolean isDefender(Player player) {
        PlayerRole role = getRole(player);
        return role == PlayerRole.DEFENDER || role == PlayerRole.KING;
    }

    public boolean isKing(Player player) {
        return getRole(player) == PlayerRole.KING;
    }

    public Set<UUID> getAttackers() {
        return Collections.unmodifiableSet(attackers);
    }

    public Set<UUID> getDefenders() {
        Set<UUID> allDefenders = new HashSet<>(defenders);
        if (kingUUID != null) allDefenders.add(kingUUID);
        return Collections.unmodifiableSet(allDefenders);
    }

    public ArrayList<Player> getOnlineAttackers() {
        return attackers.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .filter(Player::isOnline).collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Player> getOnlineDefenders() {
        List<Player> allDefenders = defenders.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .filter(Player::isOnline)
                .collect(Collectors.toList());

        if (kingUUID != null && Bukkit.getPlayer(kingUUID) != null && Bukkit.getPlayer(kingUUID).isOnline()) {
            allDefenders.add(Bukkit.getPlayer(kingUUID));
        }
        return new ArrayList<>(allDefenders);
    }

    public void removePlayer(Player player) {
        if(attackers.contains(player.getUniqueId())) {
            attackers.remove(player.getUniqueId());
        } else if(defenders.contains(player.getUniqueId())) {
            defenders.remove(player.getUniqueId());
        } else if(player.getUniqueId().equals(kingUUID)) {
            kingUUID = null;
        }
    }

    public UUID getKingUUID() {
        return kingUUID;
    }

    public void resetTeams() {
        playerRoles.clear();
        attackers.clear();
        defenders.clear();
        kingUUID = null;

        bukkitAttackers.getEntries().forEach(bukkitAttackers::removeEntry);
        bukkitDefenders.getEntries().forEach(bukkitDefenders::removeEntry);
        bukkitKing.getEntries().forEach(bukkitKing::removeEntry);
    }
}
