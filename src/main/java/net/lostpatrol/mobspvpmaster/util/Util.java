package net.lostpatrol.mobspvpmaster.util;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;

public class Util {
    public static boolean isItemInDrops(Collection<ItemEntity> drops, Item item) {
        for (ItemEntity itemEntity : drops) {
            if (itemEntity.getItem().getItem() == item) {
                return true;
            }
        }
        return false;
    }

    public static ItemStack applyRandomDamage(ItemStack itemStack, RandomSource random, float minDurability, float randomRange) {
        if (itemStack.isDamageableItem()) {
            int maxDamage = itemStack.getMaxDamage();

            float damagePercentage = minDurability + (random.nextFloat() * randomRange);
            int damageAmount = (int) (maxDamage * damagePercentage);

            damageAmount = Math.min(damageAmount, maxDamage - 1);
            itemStack.setDamageValue(damageAmount);
        }
        return itemStack;
    }
}
