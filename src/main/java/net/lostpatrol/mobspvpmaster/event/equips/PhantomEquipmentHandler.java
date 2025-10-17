package net.lostpatrol.mobspvpmaster.event.equips;

import net.lostpatrol.mobspvpmaster.MobsPVPMaster;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import static net.lostpatrol.mobspvpmaster.util.Constants.ENHANCED_PHANTOM_BOOLEAN;

@EventBusSubscriber(modid = MobsPVPMaster.MODID)
public class PhantomEquipmentHandler {
    @SubscribeEvent
    public static void onPhantomSpawn(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        if (event.getEntity() instanceof Phantom phantom) {
            if (phantom.getRandom().nextFloat() < 0.9f) {

                phantom.getPersistentData().putBoolean(ENHANCED_PHANTOM_BOOLEAN, true);

                ItemStack diamondSword = new ItemStack(Items.DIAMOND_SWORD);
                phantom.setItemSlot(EquipmentSlot.MAINHAND, diamondSword);
                ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET, 3 + event.getLevel().getDifficulty().ordinal());
                phantom.setItemSlot(EquipmentSlot.OFFHAND, rocket);


//                phantom.goalSelector.addGoal(1, new PhantomRocketChargeAttackGoal());
            }
        }
    }
}