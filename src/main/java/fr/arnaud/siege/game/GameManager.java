package fr.arnaud.siege.game;

import fr.arnaud.siege.Siege;
import fr.arnaud.siege.build.BuildVisibilityManager;
import fr.arnaud.siege.game.state.LobbyState;
import fr.arnaud.siege.npc.NpcManager;
import fr.arnaud.siege.wall.WallBuildManager;
import org.bukkit.event.Listener;

public class GameManager implements Listener {

    private final Siege plugin;
    private final BuildVisibilityManager visibilityManager;
    private final WallBuildManager wallBuildManager;
    private final NpcManager npcManager;

    private final GameStateManager stateManager;

    public GameManager(Siege plugin, BuildVisibilityManager visibilityManager,
                       WallBuildManager wallBuildManager, NpcManager npcManager) {
        this.plugin = plugin;
        this.visibilityManager = visibilityManager;
        this.wallBuildManager = wallBuildManager;
        this.npcManager = npcManager;

        this.stateManager = new GameStateManager(plugin);
    }

    public void startGame() {
        changeState(new LobbyState(plugin));
    }

    public void changeState(GameState newState) {
        stateManager.start(newState);
    }

    public void shutdown(){
        npcManager.removeAllNpc();
        wallBuildManager.stopShowingBoundaries();
        stateManager.stop();
    }

    public GameStateManager getStateManager() {
        return stateManager;
    }
}
