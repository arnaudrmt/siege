package fr.arnaud.siege.loot;

import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class LootItem {

    private final ItemStack item;
    private final int minAmount;
    private final int maxAmount;
    private final double chance;

    public LootItem(ItemStack item, int minAmount, int maxAmount, double chance) {
        this.item = item;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.chance = chance;
    }

    public String getItemName() {
        return item.getType().name();
    }

    public ItemStack getItemStack(int amount) {
        ItemStack clone = item.clone();
        clone.setAmount(amount);
        return clone;
    }

    public double getChance() {
        return chance;
    }

    public int getRandomAmount() {
        return minAmount == maxAmount ? minAmount : ThreadLocalRandom.current().nextInt(minAmount, maxAmount + 1);
    }
}
