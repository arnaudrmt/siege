package fr.arnaud.siege.game.state;

import fr.arnaud.siege.Siege;
import fr.arnaud.siege.game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public class EndState implements GameState, Listener {

    private final Siege plugin;
    private static final int SHUTDOWN_DELAY_SECONDS = 15;

    public EndState(Siege plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnter() {

        plugin.getLogger().info("Entering End State...");

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);

        plugin.getStateProtectionListener().enableAllProtections();

        Bukkit.broadcastMessage("Game over! Server will shutdown in " + SHUTDOWN_DELAY_SECONDS + " seconds.");
    }

    @Override
    public void onUpdate(long elapsedTime) {

        if (elapsedTime >= SHUTDOWN_DELAY_SECONDS) {
            plugin.getLogger().info("Shutting down server now.");
            Bukkit.broadcastMessage("Shutting down server now.");
            Bukkit.getServer().shutdown();
        } else {
            // Optionally broadcast countdown every 5 seconds
            if (elapsedTime % 5 == 0) {
                Bukkit.broadcastMessage("Server shutdown in " + (SHUTDOWN_DELAY_SECONDS - elapsedTime) + " seconds.");
            }
        }
    }

    @Override
    public void onExit() {

        HandlerList.unregisterAll(this);
        plugin.getLogger().info("Exiting Fighting State");
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("State", "").toUpperCase();
    }
}
