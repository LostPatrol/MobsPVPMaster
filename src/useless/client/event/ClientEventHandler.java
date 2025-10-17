package net.lostpatrol.mobspvpmaster.client.event;

import net.lostpatrol.mobspvpmaster.MobsPVPMaster;
import net.lostpatrol.mobspvpmaster.client.render.entity.EnhancedPhantomRenderer;
import net.lostpatrol.mobspvpmaster.client.render.entity.layers.PhantomWeaponLayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.PhantomRenderer;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = MobsPVPMaster.MODID, value = Dist.CLIENT)
public class ClientEventHandler {
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityType.PHANTOM, EnhancedPhantomRenderer::new);
    }

//    @SubscribeEvent // on the mod event bus only on the physical client
//    public static void addLayers(EntityRenderersEvent.AddLayers event) {
//        // Add a layer to every single entity type.
//        for (EntityType<?> entityType : event.getEntityTypes()) {
//            // Get our renderer.
//            EntityRenderer<?, ?> renderer = event.getRenderer(entityType);
//            // We check if our render layer is supported by the renderer.
//            // If you want a more general-purpose render layer, you will need to work with wildcard generics.
//            if (renderer instanceof PhantomRenderer phantomRenderer) {
//                // Add the layer to the renderer. Like above, construct a new MyRenderLayer.
//                // The EntityModelSet can be retrieved from the event through #getEntityModels.
//                phantomRenderer.addLayer(new PhantomWeaponLayer(renderer, event.getEntityModels()));
//            }
//        }
//    }
}
