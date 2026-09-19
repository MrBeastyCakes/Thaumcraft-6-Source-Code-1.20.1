package thaumcraft.common.lib.crafting;

import java.util.function.Supplier;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.server.ServerLifecycleHooks;

/** Captures recipe data for one query; the client supplier never retains a connection. */
public record RecipeAspectSource(RecipeManager recipes, RegistryAccess registries) {
    private static volatile Supplier<RecipeAspectSource> clientSource = () -> null;

    public static void installClientSource(Supplier<RecipeAspectSource> source) {
        clientSource = java.util.Objects.requireNonNull(source);
    }

    static RecipeAspectSource current() {
        var server = ServerLifecycleHooks.getCurrentServer();
        return server != null ? new RecipeAspectSource(server.getRecipeManager(), server.registryAccess())
                : clientSource.get();
    }
}
