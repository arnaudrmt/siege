package fr.arnaud.siege.game.state;

import fr.arnaud.siege.Siege;
import fr.arnaud.siege.game.GameState;
import fr.arnaud.siege.util.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public class LobbyState implements GameState, Listener {

    private final Siege plugin;

    private static final int LOBBY_COUNTDOWN_SECONDS = 5;
    private int countdown = LOBBY_COUNTDOWN_SECONDS;

    public LobbyState(Siege plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnter() {
        countdown = LOBBY_COUNTDOWN_SECONDS;
        plugin.getLogger().info("Entering LOBBY state. Waiting for players...");
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getStateProtectionListener().enableAllProtections();
    }

    @Override
    public void onUpdate(long elapsedTime) {

        int onlinePlayers = Bukkit.getOnlinePlayers().size();

        if(onlinePlayers < Siege.MIN_PLAYERS) {
            countdown = LOBBY_COUNTDOWN_SECONDS;
            return;
        }

        countdown--;

        Bukkit.getOnlinePlayers()
                .forEach(player ->
                        Title.sendActionBar(player, ChatColor.RED + "Game starting in " + countdown + " seconds!"));

        if(countdown <= 0) {
            plugin.getGameManager().changeState(new PreparationState(plugin));
        }
    }

    @Override
    public void onExit() {
        HandlerList.unregisterAll(this);
        plugin.getLogger().info("Exiting LOBBY state");
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("State", "").toUpperCase();
    }
}
