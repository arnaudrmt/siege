package fr.arnaud.siege.listener;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WorldControlListener implements Listener {

    public WorldControlListener() {
        World world = Bukkit.getWorlds().get(0);
        if (world.hasStorm()) {
            world.setStorm(false);
        }
        world.setTime(6000);
        world.setGameRuleValue("DO_DAYLIGHT_CYCLE", "false");
        world.setGameRuleValue("DO_FIRE_TICK", "false");
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        if(!(event.getEntity() instanceof Player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onExplosion(org.bukkit.event.entity.EntityExplodeEvent event) {
        event.blockList().clear();
    }

    @EventHandler
    public void onBlockIgnite(org.bukkit.event.block.BlockIgniteEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBurn(org.bukkit.event.block.BlockBurnEvent event) {
        event.setCancelled(true);
    }
}
