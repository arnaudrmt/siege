package fr.arnaud.siege;

import fr.arnaud.siege.build.BuildVisibilityManager;
import fr.arnaud.siege.command.GameCommand;
import fr.arnaud.siege.game.GameManager;
import fr.arnaud.siege.game.TeamManager;
import fr.arnaud.siege.game.setup.MapValidationService;
import fr.arnaud.siege.listener.StateProtectionListener;
import fr.arnaud.siege.listener.WorldControlListener;
import fr.arnaud.siege.marker.MarkerManager;
import fr.arnaud.siege.nms.NMSHandler;
import fr.arnaud.siege.npc.NpcManager;
import fr.arnaud.siege.wall.WallBuildManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class Siege extends JavaPlugin {

    public static final int MIN_PLAYERS = 3;

    private static Siege instance;
    private NMSHandler nmsHandler;

    private GameManager gameManager;
    private MarkerManager markerManager;
    private WallBuildManager wallBuildManager;
    private BuildVisibilityManager buildVisibilityManager;
    private TeamManager teamManager;
    private NpcManager npcManager;

    @Override
    public void onEnable() {
        instance = this;

        if (!setupImplementations()) {
            getLogger().severe("----------------------------------------------------");
            getLogger().severe("Siege could not find a compatible implementation for this server version.");
            getLogger().severe("This version of Siege is not compatible with your server.");
            getLogger().severe("Disabling plugin.");
            getLogger().severe("----------------------------------------------------");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        markerManager = new MarkerManager();
        teamManager = new TeamManager(this);
        npcManager = new NpcManager(this);
        buildVisibilityManager = new BuildVisibilityManager(teamManager);
        wallBuildManager = new WallBuildManager(this, teamManager, markerManager);
        gameManager = new GameManager(this, buildVisibilityManager, wallBuildManager, npcManager);
        StateProtectionListener stateProtectionListener = new StateProtectionListener(gameManager.getStateManager());

        World world = Bukkit.getWorlds().get(0);
        markerManager.scanMarkers(world);

        try {
            MapValidationService.validateMarkers(markerManager);
            getLogger().info("Map markers successfully validated.");
        } catch (IllegalStateException e) {
            getLogger().severe(e.getMessage());
            getLogger().severe("Disabling plugin due to invalid map configuration.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Marker scanning complete. Markers loaded: ");

        getServer().getPluginManager().registerEvents(stateProtectionListener, this);
        getServer().getPluginManager().registerEvents(gameManager, this);
        getServer().getPluginManager().registerEvents(new WorldControlListener(), this);

        Bukkit.getPluginCommand("siege").setExecutor(new GameCommand());

        gameManager.startGame();
    }

    private boolean setupImplementations() {
        String version;
        try {
            version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        } catch (ArrayIndexOutOfBoundsException e) {
            getLogger().severe("Could not determinate server version.");
            return false;
        }

        getLogger().info("Detected server version: " + version);

        try {
            String nmsHandlerClassName = "fr.arnaud.siege.nms." + version + ".NMSHandler_" + version;

            Class<?> nmsHandlerClass = Class.forName(nmsHandlerClassName);

            this.nmsHandler = (NMSHandler) nmsHandlerClass.getConstructor(Siege.class).newInstance(this);

            getLogger().info("Succesffully loaded implementations for: " + version);
            return true;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not find implementations for " + version, e);
            return false;
        }
    }

    @Override
    public void onDisable() {
        gameManager.shutdown();
        npcManager.removeAllNpc();
    }

    public MarkerManager getMarkerManager() { return markerManager; }
    public WallBuildManager getWallBuildManager() { return wallBuildManager; }
    public BuildVisibilityManager getBuildVisibilityManager() { return buildVisibilityManager; }
    public TeamManager getTeamManager() { return teamManager; }
    public GameManager getGameManager() { return gameManager; }
    public NpcManager getNpcManager() { return npcManager; }
    public NMSHandler getNmsHandler() { return nmsHandler; }
    public static Siege getInstance() { return instance; }
}
