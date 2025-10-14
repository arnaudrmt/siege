package fr.arnaud.siege.loot;

import fr.arnaud.siege.loot.tables.AttackerLootTable;
import fr.arnaud.siege.loot.tables.DefenderLootTable;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LootGenerator {

    private static void fillChest(Chest chest, LootItem[] lootPool, int minItems, int maxItems) {
        Inventory inv = chest.getBlockInventory();

        Map<String, List<LootItem>> groupedByCategory = Arrays.stream(lootPool)
                .filter(loot -> loot.getChance() > 0)
                .collect(Collectors.groupingBy(loot -> getCategoryFromEnumName(loot.getItemName())));

        List<LootItem> selectedLoot = new ArrayList<>();

        for (Map.Entry<String, List<LootItem>> entry : groupedByCategory.entrySet()) {
            LootItem chosen = weightedRandomPick(entry.getValue());
            if (chosen != null) selectedLoot.add(chosen);
        }

        if (selectedLoot.isEmpty()) return;

        int itemsToGenerate = ThreadLocalRandom.current().nextInt(minItems, maxItems + 1);
        itemsToGenerate = Math.min(itemsToGenerate, selectedLoot.size());

        Collections.shuffle(selectedLoot);

        List<Integer> availableSlots = IntStream.range(0, inv.getSize())
                .boxed()
                .collect(Collectors.toList());
        Collections.shuffle(availableSlots);

        for (int i = 0; i < itemsToGenerate && i < availableSlots.size(); i++) {
            LootItem loot = selectedLoot.get(i);
            int amount = loot.getRandomAmount();
            ItemStack item = loot.getItemStack(amount);
            if(item == null) return;
            inv.setItem(availableSlots.get(i), item);
        }
    }

    public static void fillChestForAttackers(Chest chest, int min, int max) {
        fillChest(chest, Arrays.stream(AttackerLootTable.values()).map(AttackerLootTable::getLootItem).toArray(LootItem[]::new), min, max);
    }

    public static void fillChestForDefenders(Chest chest, int min, int max) {
        fillChest(chest, Arrays.stream(DefenderLootTable.values()).map(DefenderLootTable::getLootItem).toArray(LootItem[]::new), min, max);
    }

    private static String getCategoryFromEnumName(String enumName) {
        String[] parts = enumName.split("_");
        return parts[parts.length - 1].toUpperCase();
    }

    private static LootItem weightedRandomPick(List<LootItem> lootItems) {
        double totalWeight = lootItems.stream().mapToDouble(LootItem::getChance).sum();
        double random = Math.random() * totalWeight;
        double cumulative = 0.0;

        for (LootItem loot : lootItems) {
            cumulative += loot.getChance();
            if (random <= cumulative) {
                return loot;
            }
        }
        return null;
    }
}
