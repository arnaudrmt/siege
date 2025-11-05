package fr.arnaud.siege.game.setup;

import fr.arnaud.siege.Siege;
import fr.arnaud.siege.loot.LootGenerator;
import fr.arnaud.siege.marker.MarkerManager;
import fr.arnaud.siege.marker.MarkerType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;

import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

public final class WorldSetupService {

    private static final String NPC_SKIN_VALUE = "ewogICJ0aW1lc3RhbXAiIDogMTc1MTgwMjk0MjI2MywKICAicHJvZmlsZUlkIiA6ICIzOTVkNDMxNmZiNDk0NmVjODQwYTQ3OWRhMTcxNGJhNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJWb2lkS2luZ1NsYXllciIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iYzRjMWRjOTFmNTRkZTY4MWU2M2JmYjJkZGI1MDUwMzAyMjdiYTQ1YWI5MWRkZjU3NzEwZWY2MWRlYTA2NmRjIgogICAgfQogIH0KfQ==";
    private static final String NPC_SKIN_SIGNATURE = "lWMFVGB4475VJoNbC1eI4PBAHUtpYXZtpItfAkL9rXAoAlkw0msCoQRMFZ1TbP6yMD0jCOvZlHITenlSy3ZzN3YpYVhv+WWqsA8m3pcvwbkGVQgRFUf1yn9s4lA2zesvx6oax/JG5R0RefPmlBqkV3lje0ruY/jby8aCt18egiuyCk7JfDfSGwNgN+S0FRGRpwccNdU7irzslOoCEvPUetx1ct4OjG7dZ0BoyTT2m0vFpwqHT6MfGVWlzRCRP7hKoLesw2xcMkNwKIfnf4r1fFQRNY1iv0TL+D+HyUqlCfYYQPy5wxlkYlGuDfTRGfpy6Egtt62DA9qY2dYbR7Xn0d6lvB+m8K2FsqqKRRC5a3kg68xLmSqN2+UN/PmHcIFPhG6Mpxxnn0KGDaNLBLUahssaMfW/5Rk7QJah9L1kIEIxXPxWPavOSkyKmRrisZVdNJa4eOMIu9k0GGwZQ3WNKl8rnJnJw8X2mMeY8UjFtKnKbcAiBnGgyIMzLy4wmss1ZdSnlp9MEYe+PFvdveUSJcagWAVwvBIviIfYXSue/RddJgmVFwAwPZOyxBxV7uMWYsvKvkNGghtKjYGr7dechqu/GB3SU4vEjcBCsYMMswFpt52uP6v9sGL13qbNtUY7lndFnJpiB/2K8y9nxSvxiP6+GoRTXFhG/ZU4/nbyl1U=";

    private WorldSetupService() {}

    public static void prepareWorld(Siege plugin) {
        spawnLootChests(plugin.getMarkerManager(), plugin.getLogger());
        spawnUpgradeNpc(plugin);
    }

    private static void spawnLootChests(MarkerManager markerManager, Logger logger) {

        List<Location> attackerLocations = markerManager.getMarkers(MarkerType.ATTACKERS_LOOT);
        List<Location> defenderLocations = markerManager.getMarkers(MarkerType.DEFENDERS_LOOT);

        Consumer<Chest> attackerFiller = chest -> LootGenerator.fillChestForAttackers(chest, 3, 5);
        Consumer<Chest> defenderFiller = chest -> LootGenerator.fillChestForDefenders(chest, 3, 5);

        fillChests(attackerLocations, attackerFiller, logger);
        fillChests(defenderLocations, defenderFiller, logger);

        logger.info("Loot chests have been placed and filled.");
    }

    private static void fillChests(List<Location> locations, Consumer<Chest> filler, Logger logger) {
        for (Location loc : locations) {
            Block block = loc.getBlock();

            if (block.getType() != Material.CHEST) {
                block.setType(Material.CHEST, false);
            }

            if (block.getState() instanceof Chest) {
                Chest chest = (Chest) block.getState();
                chest.getBlockInventory().clear();
                filler.accept(chest);
            } else {
                logger.warning("Failed to create or find a chest at location: " + loc);
            }
        }
    }

    private static void spawnUpgradeNpc(Siege plugin) {
        List<Location> npcSpawns = plugin.getMarkerManager().getMarkers(MarkerType.ATTACKERS_NPC_UPGRADE);

        if (npcSpawns.isEmpty()) {
            plugin.getLogger().warning("No upgrade NPC marker found. Attackers won't have upgrade access.");
            return;
        }

        if (plugin.getTeamManager().getOnlineAttackers().isEmpty()) return;

        Location npcLocation = npcSpawns.get(0);
        String npcName = ChatColor.AQUA + "" + ChatColor.BOLD + "Upgrade Loot";

        plugin.getNpcManager().spawnNpc(plugin.getTeamManager().getOnlineAttackers(),
                npcName, NPC_SKIN_VALUE, NPC_SKIN_SIGNATURE, npcLocation);
        plugin.getLogger().info("Upgrade NPC spawned for attackers.");
    }

    public static void removeLootChests(Siege plugin) {
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
}