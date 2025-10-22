package net.lostpatrol.mobspvpmaster.event.drops;

import net.lostpatrol.mobspvpmaster.MobsPVPMaster;
import net.lostpatrol.mobspvpmaster.util.Constants;
import net.lostpatrol.mobspvpmaster.util.Util;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

@EventBusSubscriber(modid = MobsPVPMaster.MODID)
public class PhantomDropsHandler {

    @SubscribeEvent
    public static void onPhantomDrops(LivingDropsEvent event) {
        if (event.getEntity().level().isClientSide())
            return;

        if (!(event.getEntity() instanceof Phantom phantom))
            return;

        if(! phantom.getPersistentData().getBoolean(Constants.ENHANCED_PHANTOM_BOOLEAN).orElse(false))
            return;

        RandomSource random = phantom.getRandom();

        // Firework rocket drop
        ItemStack fireworkRocket = phantom.getItemBySlot(EquipmentSlot.OFFHAND).copy();
        if (!fireworkRocket.isEmpty() && fireworkRocket.getItem() == Items.FIREWORK_ROCKET) {
            fireworkRocket.setCount(Math.max(1, fireworkRocket.getCount() - 1));
            event.getDrops().add(new ItemEntity(phantom.level(), phantom.getX(), phantom.getY(), phantom.getZ(), fireworkRocket));
        }

        // Weapon drop
        if (!Util.isItemInDrops(event.getDrops(), Items.MACE) && random.nextFloat() < 0.4f) {
            ItemStack weaponDrop = phantom.getItemBySlot(EquipmentSlot.MAINHAND).copy();
            event.getDrops().add(new ItemEntity(phantom.level(), phantom.getX(), phantom.getY(), phantom.getZ(), Util.applyRandomDamage(weaponDrop, random, 0.2f, 0.75f)));
        }
    }
}
