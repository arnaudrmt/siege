package fr.arnaud.siege.game;

import fr.arnaud.siege.Siege;
import fr.arnaud.siege.game.state.LobbyState;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class GameManager implements Listener {

    private final Siege plugin;
    private final GameStateManager stateManager;

    public GameManager(Siege plugin) {
        this.plugin = plugin;
        this.stateManager = new GameStateManager(plugin);
    }

    public void startGame() {
        changeState(new LobbyState(plugin));
    }

    public void changeState(GameState newState) {
        stateManager.start(newState);
    }

    public void shutdown(){
        plugin.getNpcManager().removeAllNpc();
        plugin.getWallBuildManager().stopShowingBoundaries();
        plugin.getBuildVisibilityManager().resetReveal();
        stateManager.stop();
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
