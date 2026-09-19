package thaumcraft.common.lib.crafting;

import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.RecipeManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RecipeAspectSourceTest {
    @Test void connectionAvailabilityAndReplacementAreReadEveryQuery() {
        AtomicReference<RecipeAspectSource> connection = new AtomicReference<>();
        RecipeAspectSource.installClientSource(connection::get);
        try {
            assertNull(RecipeAspectSource.current());
            RecipeAspectSource first = new RecipeAspectSource(new RecipeManager(), RegistryAccess.EMPTY);
            connection.set(first);
            assertSame(first, RecipeAspectSource.current());
            RecipeAspectSource replacement = new RecipeAspectSource(new RecipeManager(), RegistryAccess.EMPTY);
            connection.set(replacement);
            assertSame(replacement, RecipeAspectSource.current());
            connection.set(null);
            assertNull(RecipeAspectSource.current());
        } finally { RecipeAspectSource.installClientSource(() -> null); }
    }
}
