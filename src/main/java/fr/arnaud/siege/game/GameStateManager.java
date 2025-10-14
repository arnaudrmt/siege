package fr.arnaud.siege.game;

import fr.arnaud.siege.Siege;
import org.bukkit.scheduler.BukkitRunnable;

public class GameStateManager {

    private final Siege plugin;

    private GameState currentState;
    private long stateStartTime;

    private BukkitRunnable updateTask;

    private long stateDurationSeconds = 0;

    public GameStateManager(Siege plugin) {
        this.plugin = plugin;
    }

    public void start(GameState initialState) {

        if (updateTask != null) {
            updateTask.cancel();
        }

        changeState(initialState);

        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                if(currentState != null) {
                    stateDurationSeconds = (System.currentTimeMillis() - stateStartTime) / 1000;
                    currentState.onUpdate(stateDurationSeconds);
                }
            }
        };

        updateTask.runTaskTimer(plugin, 0L, 20L);
    }

    public void stop() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        if (currentState != null) {
            currentState.onExit();
            currentState = null;
        }
    }

    public void changeState(GameState newState) {
        if(currentState != null) {
            currentState.onExit();
        }
        currentState = newState;
        stateStartTime = System.currentTimeMillis();
        currentState.onEnter();

        plugin.getLogger().info("Changed game state to: " + newState.getName());
    }

    public long getStateDurationSeconds() {
        return stateDurationSeconds;
    }
}
