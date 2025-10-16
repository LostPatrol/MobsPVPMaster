package net.lostpatrol.mobspvpmaster.event.equips;

import net.lostpatrol.mobspvpmaster.MobsPVPMaster;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Phantom;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.UUID;

@EventBusSubscriber(modid = MobsPVPMaster.MODID)
public class PhantomEquipmentHandler {

    @SubscribeEvent
    public static void onPhantomSpawn(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        if (event.getEntity() instanceof Phantom phantom) {
            if (phantom.getRandom().nextFloat() < 0.3f) {
                phantom.getPersistentData().putBoolean("has_mace", true);
            }
        }
    }
}