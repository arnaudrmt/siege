package fr.arnaud.siege.loot.tables;

import fr.arnaud.siege.loot.LootItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum DefenderLootTable {

    // Blocks
    WOOD_PLANKS(new LootItem(new ItemStack(Material.WOOD), 8, 16, 0.5)),
    COBBLESTONE(new LootItem(new ItemStack(Material.COBBLESTONE), 4, 8, 0.3)),
    IRON_BLOCK(new LootItem(new ItemStack(Material.IRON_BLOCK), 1, 3, 0.1)),
    OBSIDIAN(new LootItem(new ItemStack(Material.OBSIDIAN), 1, 3, 0.05)),

    // Utilities
    WATER_BUCKET(new LootItem(new ItemStack(Material.WATER_BUCKET), 1, 1, 0.15)),
    LAVA_BUCKET(new LootItem(new ItemStack(Material.LAVA_BUCKET), 1, 1, 0.1)),

    // Cobwebs
    COBWEB(new LootItem(new ItemStack(Material.WEB), 1, 3, 0.1)),

    // Weapons (same as attackers)
    WOOD_SWORD(new LootItem(new ItemStack(Material.WOOD_SWORD), 1, 1, 0.3)),
    STONE_SWORD(new LootItem(new ItemStack(Material.STONE_SWORD), 1, 1, 0.25)),
    GOLD_SWORD(new LootItem(new ItemStack(Material.GOLD_SWORD), 1, 1, 0.15)),
    IRON_SWORD(new LootItem(new ItemStack(Material.IRON_SWORD), 1, 1, 0.1)),
    DIAMOND_SWORD(new LootItem(new ItemStack(Material.DIAMOND_SWORD), 1, 1, 0.05)),

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

    DefenderLootTable(LootItem lootItem) {
        this.lootItem = lootItem;
    }

    public LootItem getLootItem() {
        return lootItem;
    }
}