package fr.arnaud.siege.game.state;

import fr.arnaud.siege.Siege;
import fr.arnaud.siege.game.GameState;
import fr.arnaud.siege.loot.LootGenerator;
import fr.arnaud.siege.marker.MarkerManager;
import fr.arnaud.siege.marker.MarkerType;
import fr.arnaud.siege.packet.PacketInjector;
import fr.arnaud.siege.util.Utils;
import fr.arnaud.siege.wall.WallBuildManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PreparationState implements GameState, Listener {

    private final Siege plugin;

    private static final int DURATION_SECONDS = (10 * 60) + 10;
    private static final double PUSHBACK_FORCE = 2.0;

    public PreparationState(Siege plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnter() {
        plugin.getLogger().info("Entering Preparation State...");

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);

        plugin.getStateProtectionListener().setProperties(false, false, true);

        plugin.getTeamManager().assignRoles(new ArrayList<>(Bukkit.getOnlinePlayers()));
        setupMarkersAndMap();
        spawnLootChest();
        spawnAttackersUpgradeNpc();
        plugin.getWallBuildManager().showBoundaries();

        List<Location> defendersSpawn = plugin.getMarkerManager().getMarkers(MarkerType.DEFENDERS_SPAWN_POINT);
        List<Location> attackersSpawn = plugin.getMarkerManager().getMarkers(MarkerType.ATTACKERS_SPAWN_POINT);

        plugin.getTeamManager().getAttackers().stream()
                .map(Bukkit::getPlayer)
                .filter(player -> player != null && player.isOnline())
                .forEach(player -> {
                    if(!attackersSpawn.isEmpty()) {
                        Location randomAttackerSpawn = Utils.pickRandom(attackersSpawn);
                        player.teleport(randomAttackerSpawn.add(0, 1, 0));
                    }
                    PacketInjector.inject(player);
                });

        plugin.getTeamManager().getDefenders().stream()
                .map(Bukkit::getPlayer)
                .filter(player -> player != null && player.isOnline())
                .forEach(player -> {
            if(!defendersSpawn.isEmpty()) {
                Location randomDefenderSpawn = Utils.pickRandom(defendersSpawn);
                player.teleport(randomDefenderSpawn);
            }
        });
    }

    private void setupMarkersAndMap() {
        MarkerManager markerManager = plugin.getMarkerManager();

        if (markerManager.getMarkers(MarkerType.DEFENDERS_SPAWN_POINT).isEmpty() ||
                markerManager.getMarkers(MarkerType.ATTACKERS_SPAWN_POINT).isEmpty() ||
                markerManager.getMarkers(MarkerType.WALL_DELIMITATION_NORTH).isEmpty()) {

            plugin.getLogger().severe("Missing required map markers! Cancelling game start.");
        }
    }

    private void spawnLootChest() {

        List<Location> attackersLoot = plugin.getMarkerManager().getMarkers(MarkerType.ATTACKERS_LOOT);
        List<Location> defendersLoot = plugin.getMarkerManager().getMarkers(MarkerType.DEFENDERS_LOOT);

        attackersLoot.stream().filter(loc -> loc.getBlock().getType().equals(Material.CHEST)).forEach(loot -> {

            Chest chest = (Chest) loot.getBlock().getState();
            chest.getBlockInventory().clear();

            LootGenerator.fillChestForAttackers(chest, 3, 5);
        });

        defendersLoot.stream().filter(loc -> loc.getBlock().getType().equals(Material.CHEST)).forEach(loot -> {

            Chest chest = (Chest) loot.getBlock().getState();
            chest.getBlockInventory().clear();
            LootGenerator.fillChestForDefenders(chest, 3,5);
        });
    }

    private void spawnAttackersUpgradeNpc() {

        if(plugin.getMarkerManager().getMarkers(MarkerType.ATTACKERS_NPC_UPGRADE).isEmpty()) {
            plugin.getLogger().warning("No upgrade NPC marker found. Attackers won't have upgrade access.");
            return;
        }

        List<Location> attackersUpgradeNpc = plugin.getMarkerManager().getMarkers(MarkerType.ATTACKERS_NPC_UPGRADE);
        Collection<Player> attackers = plugin.getTeamManager().getAttackers().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .filter(OfflinePlayer::isOnline)
                .collect(Collectors.toList());

        if (attackersUpgradeNpc.isEmpty()) {
            plugin.getLogger().warning("No upgrade NPC marker found.");
            return;
        }

        Location npcLocation = attackersUpgradeNpc.get(0);

        String npcName = ChatColor.AQUA + "" + ChatColor.BOLD +  "Upgrade Loot";

        String skinValue = "ewogICJ0aW1lc3RhbXAiIDogMTc1MTgwMjk0MjI2MywKICAicHJvZmlsZUlkIiA6ICIzOTVkNDMxNmZiNDk0NmVjODQwYTQ3OWRhMTcxNGJhNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJWb2lkS2luZ1NsYXllciIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iYzRjMWRjOTFmNTRkZTY4MWU2M2JmYjJkZGI1MDUwMzAyMjdiYTQ1YWI5MWRkZjU3NzEwZWY2MWRlYTA2NmRjIgogICAgfQogIH0KfQ==";
        String skinSignature = "lWMFVGB4475VJoNbC1eI4PBAHUtpYXZtpItfAkL9rXAoAlkw0msCoQRMFZ1TbP6yMD0jCOvZlHITenlSy3ZzN3YpYVhv+WWqsA8m3pcvwbkGVQgRFUf1yn9s4lA2zesvx6oax/JG5R0RefPmlBqkV3lje0ruY/jby8aCt18egiuyCk7JfDfSGwNgN+S0FRGRpwccNdU7irzslOoCEvPUetx1ct4OjG7dZ0BoyTT2m0vFpwqHT6MfGVWlzRCRP7hKoLesw2xcMkNwKIfnf4r1fFQRNY1iv0TL+D+HyUqlCfYYQPy5wxlkYlGuDfTRGfpy6Egtt62DA9qY2dYbR7Xn0d6lvB+m8K2FsqqKRRC5a3kg68xLmSqN2+UN/PmHcIFPhG6Mpxxnn0KGDaNLBLUahssaMfW/5Rk7QJah9L1kIEIxXPxWPavOSkyKmRrisZVdNJa4eOMIu9k0GGwZQ3WNKl8rnJnJw8X2mMeY8UjFtKnKbcAiBnGgyIMzLy4wmss1ZdSnlp9MEYe+PFvdveUSJcagWAVwvBIviIfYXSue/RddJgmVFwAwPZOyxBxV7uMWYsvKvkNGghtKjYGr7dechqu/GB3SU4vEjcBCsYMMswFpt52uP6v9sGL13qbNtUY7lndFnJpiB/2K8y9nxSvxiP6+GoRTXFhG/ZU4/nbyl1U=";

        plugin.getNpcManager().spawnNpc(attackers, npcName, skinValue, skinSignature, npcLocation);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null) return;

        Location location = event.getClickedBlock().getLocation();
        Action action = event.getAction();

        List<Location> attackersLoot = plugin.getMarkerManager().getMarkers(MarkerType.ATTACKERS_LOOT);
        List<Location> defendersLoot = plugin.getMarkerManager().getMarkers(MarkerType.DEFENDERS_LOOT);

        if(action.equals(Action.RIGHT_CLICK_BLOCK) && block.getType().equals(Material.CHEST)) {

            if(plugin.getTeamManager().isAttacker(player) && !defendersLoot.isEmpty() &&
                    defendersLoot.stream().anyMatch(loot -> sameBlock(loot, location))) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You cannot loot a defender's chest.");
            }

            if((plugin.getTeamManager().isDefender(player) || plugin.getTeamManager().isKing(player)) && !attackersLoot.isEmpty() &&
                    attackersLoot.stream().anyMatch(loot -> sameBlock(loot, location))) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot loot a attackers's chest.");
            }
        }
    }

    public boolean sameBlock(Location loc1, Location loc2) {
        if (loc1.getWorld() == null || loc2.getWorld() == null) return false;
        return loc1.getBlockX() == loc2.getBlockX()
                && loc1.getBlockY() == loc2.getBlockY()
                && loc1.getBlockZ() == loc2.getBlockZ()
                && loc1.getWorld().getName().equals(loc2.getWorld().getName());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {

        Player player = event.getPlayer();
        Location blockLoc = event.getBlock().getLocation();
        Block block = event.getBlock();

        if (!plugin.getWallBuildManager().isAllowed(player, blockLoc, WallBuildManager.ActionType.BLOCK)) {
            player.sendMessage(ChatColor.RED + "You cannot build here!");
            event.setCancelled(true);
        } else {
            plugin.getBuildVisibilityManager().addGhostBlock(blockLoc, block.getType());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        Location blockLoc = event.getBlock().getLocation();

        if (!plugin.getWallBuildManager().isAllowed(player, blockLoc, WallBuildManager.ActionType.BLOCK)) {
            player.sendMessage(ChatColor.RED + "You cannot build here!");
            event.setCancelled(true);
        } else {
            plugin.getBuildVisibilityManager().removeGhostBlock(blockLoc);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();

        if (to == null) return;
        if(plugin.getWallBuildManager().getCurrentMarkerLocation() == null) return;

        if (plugin.getWallBuildManager().isAllowed(player, to, WallBuildManager.ActionType.MOVE)) {

            Location markerLoc = plugin.getWallBuildManager().getCurrentMarkerLocation().clone();
            WallBuildManager.WallDirection direction = plugin.getWallBuildManager().getCurrentMarkerDirection();

            Vector pushDirection = new Vector(0, 0, 0);

            if (direction.equals(WallBuildManager.WallDirection.NORTH)) {
                double xDiff = to.getX() - markerLoc.getX();
                pushDirection = new Vector(xDiff, 0, 0).normalize();
            } else if (direction.equals(WallBuildManager.WallDirection.EAST)) {
                double zDiff = to.getZ() - markerLoc.getZ();
                pushDirection = new Vector(0, 0, zDiff).normalize();
            }

            Vector pushBack = pushDirection.multiply(PUSHBACK_FORCE);
            if (pushDirection.lengthSquared() == 0) {
                pushBack = new Vector(0, 0.5, 0);
            }
            player.setVelocity(pushBack);

            player.sendMessage(ChatColor.RED + "You cannot pass this wall boundary!");
        }
    }

    private long lastBroadcastTime = -1;

    @Override
    public void onUpdate(long elapsedTime) {

        long timeLeft = (DURATION_SECONDS - elapsedTime) - 10;
        long minutesLeft = timeLeft / 60;

        if (minutesLeft != lastBroadcastTime && timeLeft > 0) {
            Bukkit.broadcastMessage("Preparation time remaining: " + minutesLeft + " minutes");
            lastBroadcastTime = minutesLeft;
        }

        Bukkit.getOnlinePlayers().forEach(player -> {

            int secondsLeft = (int) timeLeft % 60;

            player.setLevel((int) minutesLeft);
            player.setExp(secondsLeft / 60f);
        });

        if(timeLeft == 0) {

            List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
            players.forEach(PacketInjector::uninject);

            plugin.getBuildVisibilityManager().startLayeredReveal();
            plugin.getWallBuildManager().stopShowingBoundaries();
        }
        if(timeLeft == -10) {
            plugin.getGameManager().changeState(new FightingState(plugin));
        }
    }

    @Override
    public void onExit() {

        plugin.getLogger().info("Exiting Preparation State");

        plugin.getNpcManager().removeAllNpc();

        removeLootChests();
        HandlerList.unregisterAll(this);

        Bukkit.broadcastMessage("Preparation phase ended! Fight!");
    }

    private void removeLootChests() {
        List<Location> attackersLoot = plugin.getMarkerManager().getMarkers(MarkerType.ATTACKERS_LOOT);
        List<Location> defendersLoot = plugin.getMarkerManager().getMarkers(MarkerType.DEFENDERS_LOOT);

        attackersLoot.stream().filter(loc -> loc.getBlock().getType().equals(Material.CHEST))
                .forEach(loc -> {
                    Chest chest = (Chest) loc.getBlock().getState();
                    chest.getBlockInventory().clear();
                    loc.getBlock().setType(Material.AIR);
        });

        defendersLoot.stream().filter(loc -> loc.getBlock().getType().equals(Material.CHEST))
                .forEach(loc -> {
                    Chest chest = (Chest) loc.getBlock().getState();
                    chest.getBlockInventory().clear();
                    loc.getBlock().setType(Material.AIR);
        });
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("State", "").toUpperCase();
    }
}
