package fr.arnaud.siege;

import fr.arnaud.siege.build.BuildVisibilityManager;
import fr.arnaud.siege.command.GameCommand;
import fr.arnaud.siege.game.GameManager;
import fr.arnaud.siege.game.TeamManager;
import fr.arnaud.siege.marker.MarkerManager;
import fr.arnaud.siege.marker.MarkerType;
import fr.arnaud.siege.npc.NpcManager;
import fr.arnaud.siege.protection.StateProtectionListener;
import fr.arnaud.siege.wall.WallBuildManager;
import fr.arnaud.siege.world.WorldControlListener;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class Siege extends JavaPlugin {

    public static final int MIN_PLAYERS = 3;

    private GameManager gameManager;
    private MarkerManager markerManager;
    private WallBuildManager wallBuildManager;
    private BuildVisibilityManager buildVisibilityManager;
    private StateProtectionListener stateProtectionListener;
    private TeamManager teamManager;
    private NpcManager npcManager;

    private static Siege instance;

    @Override
    public void onEnable() {

        if (instance != null) {
            throw new IllegalStateException("Siege already initialized");
        }

        instance = this;

        Bukkit.getServer().getPluginManager().registerEvents(new WorldControlListener(), this);
        Bukkit.getPluginCommand("siege").setExecutor(new GameCommand());

        this.markerManager = new MarkerManager();
        this.gameManager = new GameManager(this);
        this.wallBuildManager = new WallBuildManager(this);
        this.buildVisibilityManager = new BuildVisibilityManager(this);
        this.teamManager = new TeamManager(this);
        this.npcManager = new NpcManager(this);

        World world = Bukkit.getWorlds().get(0);
        markerManager.scanMarkers(world);

        getLogger().info("Marker scanning complete. Markers loaded: ");

        Arrays.stream(MarkerType.values()).forEach(type -> {
            getLogger().info(type + ": " + markerManager.getMarkers(type).size() + " found.");
        });

        stateProtectionListener = new StateProtectionListener();
        Bukkit.getServer().getPluginManager().registerEvents(stateProtectionListener, this);

        getServer().getPluginManager().registerEvents(gameManager, this);
        gameManager.startGame();
    }

    @Override
    public void onDisable() {
        gameManager.shutdown();
        npcManager.cleanUp();
    }

    public MarkerManager getMarkerManager() {
        return markerManager;
    }

    public WallBuildManager getWallBuildManager() {
        return wallBuildManager;
    }

    public BuildVisibilityManager getBuildVisibilityManager() {
        return buildVisibilityManager;
    }

    public StateProtectionListener getStateProtectionListener() {
        return stateProtectionListener;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public NpcManager getNpcManager() {
        return npcManager;
    }

    public static Siege getInstance() {
        return instance;
    }
}
