package thaumcraft.common.lib.events;

import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.api.aspects.AspectHelper;

@Mod.EventBusSubscriber(modid = "thaumcraft")
public final class AspectTagEvents {
    private AspectTagEvents() {
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        if (shouldRefresh(event)) {
            AspectHelper.refreshTagRegistrations();
        }
    }

    static boolean shouldRefresh(TagsUpdatedEvent event) {
        return event.shouldUpdateStaticData();
    }
}
