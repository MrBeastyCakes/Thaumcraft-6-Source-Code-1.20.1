package thaumcraft.common.lib.events;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.core.RegistryAccess;
import net.minecraftforge.event.TagsUpdatedEvent;
import org.junit.jupiter.api.Test;

class AspectTagEventsTest {
    @Test
    void serverDataLoadRefreshesStaticRegistrations() {
        assertTrue(AspectTagEvents.shouldRefresh(new TagsUpdatedEvent(RegistryAccess.EMPTY, false, false)));
    }

    @Test
    void remoteClientPacketRefreshesStaticRegistrations() {
        assertTrue(AspectTagEvents.shouldRefresh(new TagsUpdatedEvent(RegistryAccess.EMPTY, true, false)));
    }

    @Test
    void integratedClientPacketDoesNotRefreshSharedStaticRegistrations() {
        assertFalse(AspectTagEvents.shouldRefresh(new TagsUpdatedEvent(RegistryAccess.EMPTY, true, true)));
    }
}
