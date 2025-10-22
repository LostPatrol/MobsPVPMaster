package net.lostpatrol.mobspvpmaster.event.equips;

import net.lostpatrol.mobspvpmaster.MobsPVPMaster;
import net.lostpatrol.mobspvpmaster.entity.ai.PhantomRocketChargeAttackGoal;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import net.lostpatrol.mobspvpmaster.util.Constants;
import org.slf4j.Logger;

@EventBusSubscriber(modid = MobsPVPMaster.MODID)
public class PhantomEquipmentHandler {
    public static final Logger logger = MobsPVPMaster.LOGGER;

    @SubscribeEvent
    public static void onPhantomSpawn(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        if (event.getEntity() instanceof Phantom phantom) {
            if (phantom.getRandom().nextFloat() < 0.9f) {
                phantom.getPersistentData().putBoolean(Constants.ENHANCED_PHANTOM_BOOLEAN, true);

                ItemStack diamondSword = new ItemStack(Items.DIAMOND_SWORD);
                phantom.setItemSlot(EquipmentSlot.MAINHAND, diamondSword);
                ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET, 3 + event.getLevel().getDifficulty().ordinal());
                phantom.setItemSlot(EquipmentSlot.OFFHAND, rocket);

                rerankPhantomGoals(phantom);
            }
        }
    }

    public static void rerankPhantomGoals(Phantom phantom) {
        Goal vanillaAttackGoal =  null;
        Goal vanillaCircleGoal = null;
        for (WrappedGoal wrappedGoal : phantom.goalSelector.getAvailableGoals()) {
            Goal goal = wrappedGoal.getGoal();
            if (goal instanceof Phantom.PhantomSweepAttackGoal) {
                vanillaAttackGoal = goal;
                phantom.goalSelector.removeGoal(goal);
            } else if (goal instanceof Phantom.PhantomCircleAroundAnchorGoal) {
                vanillaCircleGoal = goal;
                phantom.goalSelector.removeGoal(goal);
            }
        }

        // priority 1: strategyGoal
        phantom.goalSelector.addGoal(2, new PhantomRocketChargeAttackGoal(phantom));
        phantom.goalSelector.addGoal(3, vanillaAttackGoal);
        phantom.goalSelector.addGoal(4, vanillaCircleGoal);
    }
}