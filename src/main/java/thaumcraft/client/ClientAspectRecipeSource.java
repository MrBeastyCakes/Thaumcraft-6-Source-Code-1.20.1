package thaumcraft.client;

import net.minecraft.client.Minecraft;
import thaumcraft.common.lib.crafting.RecipeAspectSource;

/** Remote-client recipe data is read anew after every connect, reload or disconnect. */
public final class ClientAspectRecipeSource {
    private ClientAspectRecipeSource() {}

    public static void install() {
        RecipeAspectSource.installClientSource(() -> {
            var connection = Minecraft.getInstance().getConnection();
            return connection == null ? null
                    : new RecipeAspectSource(connection.getRecipeManager(), connection.registryAccess());
        });
    }
}
