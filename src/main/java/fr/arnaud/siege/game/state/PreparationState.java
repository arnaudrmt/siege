package fr.arnaud.siege.game.state;

import fr.arnaud.siege.Siege;
import fr.arnaud.siege.game.GameState;
import fr.arnaud.siege.game.setup.PlayerSetupService;
import fr.arnaud.siege.game.setup.WorldSetupService;
import fr.arnaud.siege.marker.MarkerType;
import fr.arnaud.siege.wall.WallBuildManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
import java.util.List;

public class PreparationState implements GameState, Listener {

    private final Siege plugin;

    private static final int PREPARATION_DURATION_SECONDS = 1 * 40;
    private static final int REVEAL_DURATION_SECONDS = 10;
    private static final int TOTAL_DURATION_SECONDS = PREPARATION_DURATION_SECONDS + REVEAL_DURATION_SECONDS;
    private static final double PUSHBACK_FORCE = 2.0;

    private long lastBroadcastedTime = -1;

    public PreparationState(Siege plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnter() {
        plugin.getLogger().info("Entering Preparation State...");
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);

        PlayerSetupService.assignRolesAndTeleport(plugin);
        WorldSetupService.prepareWorld(plugin);

        plugin.getWallBuildManager().showBoundaries();
    }

    @Override
    public void onUpdate(long elapsedTime) {

        long timeLeft = TOTAL_DURATION_SECONDS - elapsedTime;
        long minutesLeft = (timeLeft / 60) + 1;

        if (minutesLeft != lastBroadcastedTime && timeLeft > 0) {
            Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "Preparation time ends in: " + ChatColor.YELLOW + minutesLeft + ChatColor.DARK_PURPLE + " minute(s).");
            lastBroadcastedTime = minutesLeft;
        }

        Bukkit.getOnlinePlayers().forEach(player -> {

            int secondsLeft = (int) timeLeft % 60;

            player.setLevel((int) minutesLeft);
            player.setExp(secondsLeft / 60f);
        });

        if(timeLeft == REVEAL_DURATION_SECONDS) {

            List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
            players.forEach(player -> plugin.getNmsHandler().uninject(player));

            plugin.getBuildVisibilityManager().startLayeredReveal();
            plugin.getWallBuildManager().stopShowingBoundaries();
        }

        if(timeLeft <= 0) {
            plugin.getGameManager().changeState(new FightingState(plugin));
        }
    }

    @Override
    public void onExit() {

        plugin.getLogger().info("Exiting Preparation State");
        plugin.getNpcManager().removeAllNpc();
        WorldSetupService.removeLootChests(plugin);
        HandlerList.unregisterAll(this);
        Bukkit.broadcastMessage("Preparation phase ended! Fight!");
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

        if (!plugin.getWallBuildManager().isAllowed(player, to, WallBuildManager.ActionType.MOVE)) {

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

    public boolean sameBlock(Location loc1, Location loc2) {
        if (loc1.getWorld() == null || loc2.getWorld() == null) return false;
        return loc1.getBlockX() == loc2.getBlockX()
                && loc1.getBlockY() == loc2.getBlockY()
                && loc1.getBlockZ() == loc2.getBlockZ()
                && loc1.getWorld().getName().equals(loc2.getWorld().getName());
    }

    @Override public boolean canBreakBlocks() { return false; }
    @Override public boolean canPlaceBlocks() { return false; }
    @Override public boolean canReceiveDamage() { return false; }

    @Override
    public String getName() {
        return "PREPARATION";
    }
}
