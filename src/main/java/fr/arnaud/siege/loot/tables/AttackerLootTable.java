package fr.arnaud.siege.loot.tables;

import fr.arnaud.siege.loot.LootItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum AttackerLootTable {

    // Cobwebs
    COBWEB(new LootItem(new ItemStack(Material.WEB), 1, 3, 0.1)),

    // Swords
    WOOD_SWORD(new LootItem(new ItemStack(Material.WOOD_SWORD), 1, 1, 0.3)),
    STONE_SWORD(new LootItem(new ItemStack(Material.STONE_SWORD), 1, 1, 0.25)),
    GOLD_SWORD(new LootItem(new ItemStack(Material.GOLD_SWORD), 1, 1, 0.15)),
    IRON_SWORD(new LootItem(new ItemStack(Material.IRON_SWORD), 1, 1, 0.1)),
    DIAMOND_SWORD(new LootItem(new ItemStack(Material.DIAMOND_SWORD), 1, 1, 0.05)),

    // Pickaxes (same rarity as swords)
    WOOD_PICKAXE(new LootItem(new ItemStack(Material.WOOD_PICKAXE), 1, 1, 0.3)),
    STONE_PICKAXE(new LootItem(new ItemStack(Material.STONE_PICKAXE), 1, 1, 0.25)),
    GOLD_PICKAXE(new LootItem(new ItemStack(Material.GOLD_PICKAXE), 1, 1, 0.15)),
    IRON_PICKAXE(new LootItem(new ItemStack(Material.IRON_PICKAXE), 1, 1, 0.1)),
    DIAMOND_PICKAXE(new LootItem(new ItemStack(Material.DIAMOND_PICKAXE), 1, 1, 0.05)),

    WOOD_AXE(new LootItem(new ItemStack(Material.WOOD_AXE), 1, 1, 0.3)),
    STONE_AXE(new LootItem(new ItemStack(Material.STONE_AXE), 1, 1, 0.25)),
    GOLD_AXE(new LootItem(new ItemStack(Material.GOLD_AXE), 1, 1, 0.15)),
    IRON_AXE(new LootItem(new ItemStack(Material.IRON_AXE), 1, 1, 0.1)),
    DIAMOND_AXE(new LootItem(new ItemStack(Material.DIAMOND_AXE), 1, 1, 0.05));

    private final LootItem lootItem;

    AttackerLootTable(LootItem lootItem) {
        this.lootItem = lootItem;
    }

    public LootItem getLootItem() {
        return lootItem;
    }
}