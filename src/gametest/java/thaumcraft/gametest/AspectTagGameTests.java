package thaumcraft.gametest;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectHelper;
import thaumcraft.api.aspects.AspectList;

@GameTestHolder("thaumcraft")
@PrefixGameTestTemplate(false)
public final class AspectTagGameTests {
    private static final String TEMPLATE = "fnd01_empty";
    private static final String PACK_PREFIX = "fnd04-tag-reload-";
    private static final Logger LOGGER = LogUtils.getLogger();

    private AspectTagGameTests() {}

    @GameTest(template = TEMPLATE)
    public static void boundIronTagSuppliesSeededAspects(GameTestHelper helper) {
        assertBoundMember(helper, "forge:ingots/iron", Items.IRON_INGOT);
        assertExact(helper, new ItemStack(Items.IRON_INGOT), Aspect.METAL, 15);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void boundDiamondTagSuppliesSeededAspects(GameTestHelper helper) {
        assertBoundMember(helper, "forge:gems/diamond", Items.DIAMOND);
        ItemStack diamond = new ItemStack(Items.DIAMOND);
        AspectList raw = AspectHelper.getRegisteredObjectAspects(diamond);
        helper.assertTrue(raw != null && raw.size() == 2 && raw.getAmount(Aspect.CRYSTAL) == 15
                        && raw.getAmount(Aspect.DESIRE) == 15,
                "Bound diamond tag must supply exactly CRYSTAL 15 and DESIRE 15 after startup");
        AspectList computed = AspectHelper.getObjectAspects(diamond);
        helper.assertTrue(computed.size() == 2 && computed.getAmount(Aspect.CRYSTAL) == 15
                        && computed.getAmount(Aspect.DESIRE) == 15
                        && computed.getAmount(Aspect.ENTROPY) == 0,
                "Computed diamond aspects must match the seeded tag without fallback");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void explicitRedstoneOreSeedRemainsControl(GameTestHelper helper) {
        ItemStack stack = new ItemStack(Items.REDSTONE_ORE);
        AspectList raw = AspectHelper.getRegisteredObjectAspects(stack);
        helper.assertTrue(raw != null && raw.size() == 2 && raw.getAmount(Aspect.EARTH) == 5
                        && raw.getAmount(Aspect.ENERGY) == 15,
                "Redstone ore must retain exactly EARTH 5 and ENERGY 15 as a direct control");
        AspectList computed = AspectHelper.getObjectAspects(stack);
        helper.assertTrue(computed.size() == 2 && computed.getAmount(Aspect.EARTH) == 5
                        && computed.getAmount(Aspect.ENERGY) == 15,
                "Computed redstone ore aspects must match its direct registration");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 1200)
    public static void actualReloadRebindsRetainedTagRules(GameTestHelper helper) {
        assertFixtureTagMembership(helper, true, false, "initial");
        assertFixtureState(helper, true, false);
        MinecraftServer server = helper.getLevel().getServer();
        List<String> originalSelection = List.copyOf(server.getPackRepository().getSelectedIds());
        Path root = server.getWorldPath(LevelResource.DATAPACK_DIR).toAbsolutePath().normalize();
        Path pack = root.resolve(PACK_PREFIX + UUID.randomUUID()).normalize();
        try {
            writeReplacementPack(pack);
        } catch (IOException failure) {
            helper.fail("Unable to create reload pack: " + failure.getMessage()
                    + suffix(deleteOwnedPack(root, pack)));
            return;
        }

        server.getPackRepository().reload();
        String packId = "file/" + pack.getFileName();
        if (!server.getPackRepository().getAvailableIds().contains(packId)) {
            helper.fail("Pack repository did not discover " + packId
                    + suffix(deleteOwnedPack(root, pack)));
            return;
        }
        List<String> replacement = new ArrayList<>(originalSelection);
        replacement.add(packId);
        server.reloadResources(replacement).thenRunAsync(() -> {
            try {
                assertFixtureTagMembership(helper, false, true, "replacement");
                assertFixtureState(helper, false, true);
                LOGGER.info("FND-04 replacement reload verified: A direct, B tag, C direct");
                restoreAndVerify(helper, server, originalSelection, root, pack);
            } catch (Throwable failure) {
                restoreAfterFailure(helper, server, originalSelection, root, pack, failure);
            }
        }, server).exceptionally(failure -> {
            server.execute(() -> restoreAfterFailure(
                    helper, server, originalSelection, root, pack, failure));
            return null;
        });
    }

    private static void restoreAndVerify(GameTestHelper helper, MinecraftServer server,
            List<String> originalSelection, Path root, Path pack) {
        server.reloadResources(originalSelection).thenRunAsync(() -> {
            try {
                assertFixtureTagMembership(helper, true, false, "restored");
                assertFixtureState(helper, true, false);
                LOGGER.info("FND-04 restoration reload verified: A tag, B absent, C direct");
                String cleanupFailure = deleteOwnedPack(root, pack);
                server.getPackRepository().reload();
                if (cleanupFailure != null) helper.fail(cleanupFailure); else helper.succeed();
            } catch (Throwable failure) {
                helper.fail("Restored stage failed: " + rootMessage(failure)
                        + suffix(deleteOwnedPack(root, pack)));
            }
        }, server).exceptionally(failure -> {
            server.execute(() -> helper.fail("Unable to restore original pack selection: "
                    + rootMessage(failure) + suffix(deleteOwnedPack(root, pack))));
            return null;
        });
    }

    private static void restoreAfterFailure(GameTestHelper helper, MinecraftServer server,
            List<String> originalSelection, Path root, Path pack, Throwable failure) {
        server.reloadResources(originalSelection).whenCompleteAsync((unused, restoreFailure) -> {
            String cleanupFailure = deleteOwnedPack(root, pack);
            server.getPackRepository().reload();
            String message = "Replacement stage failed: " + rootMessage(failure);
            if (restoreFailure != null) message += "; restoration failed: " + rootMessage(restoreFailure);
            helper.fail(message + suffix(cleanupFailure));
        }, server);
    }

    private static void assertFixtureState(GameTestHelper helper, boolean expectA, boolean expectB) {
        assertExactFixture(helper, AspectLookupGameTests.TestItems.RELOAD_A,
                expectA ? Aspect.EARTH : Aspect.AIR,
                expectA ? 2 : 1, "A");
        if (expectB) assertExactFixture(helper, AspectLookupGameTests.TestItems.RELOAD_B,
                Aspect.EARTH, 2, "B");
        else helper.assertTrue(AspectHelper.getRegisteredObjectAspects(
                        new ItemStack(AspectLookupGameTests.TestItems.RELOAD_B)) == null,
                "Fixture B must be absent outside the replacement pack");
        assertExactFixture(helper, AspectLookupGameTests.TestItems.RELOAD_C, Aspect.WATER, 4, "C");
        assertExact(helper, new ItemStack(Items.IRON_INGOT), Aspect.METAL, 15);
    }

    private static void assertFixtureTagMembership(GameTestHelper helper, boolean expectA,
            boolean expectB, String stage) {
        var tag = ForgeRegistries.ITEMS.tags().getTag(
                ItemTags.create(new ResourceLocation("thaumcraft", "fnd04_reload")));
        helper.assertTrue(tag.isBound(), "Fixture tag must be bound at the " + stage + " stage");
        helper.assertTrue(tag.size() == 2,
                "Fixture tag must have exactly two members at the " + stage + " stage");
        helper.assertTrue(tag.contains(AspectLookupGameTests.TestItems.RELOAD_A) == expectA,
                "Fixture A membership must match the " + stage + " tag set");
        helper.assertTrue(tag.contains(AspectLookupGameTests.TestItems.RELOAD_B) == expectB,
                "Fixture B membership must match the " + stage + " tag set");
        helper.assertTrue(tag.contains(AspectLookupGameTests.TestItems.RELOAD_C),
                "Fixture C must belong to the " + stage + " tag set");
    }

    private static void assertExactFixture(GameTestHelper helper, Item item, Aspect aspect,
            int amount, String name) {
        ItemStack stack = new ItemStack(item);
        AspectList raw = AspectHelper.getRegisteredObjectAspects(stack);
        helper.assertTrue(raw != null && raw.size() == 1 && raw.getAmount(aspect) == amount,
                "Fixture " + name + " must have exactly " + aspect.getTag() + " " + amount);
        AspectList computed = AspectHelper.getObjectAspects(stack);
        helper.assertTrue(computed.size() == 1 && computed.getAmount(aspect) == amount,
                "Computed fixture " + name + " must match raw registration");
    }

    private static void writeReplacementPack(Path pack) throws IOException {
        Path tag = pack.resolve("data/thaumcraft/tags/items/fnd04_reload.json");
        Files.createDirectories(tag.getParent());
        Files.writeString(pack.resolve("pack.mcmeta"),
                "{\"pack\":{\"pack_format\":15,\"description\":\"FND-04 reload test\"}}");
        Files.writeString(tag,
                "{\"replace\":true,\"values\":[\"thaumcraft:fnd04_reload_b\",\"thaumcraft:fnd04_reload_c\"]}");
    }

    private static String deleteOwnedPack(Path datapacksRoot, Path pack) {
        Path root = datapacksRoot.toAbsolutePath().normalize();
        Path target = pack.toAbsolutePath().normalize();
        if (!root.equals(target.getParent()) || !target.getFileName().toString().startsWith(PACK_PREFIX))
            return "Refused cleanup outside owned GameTest datapack path: " + target;
        if (!Files.exists(target)) return null;
        try (var paths = Files.walk(target)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
            return null;
        } catch (IOException failure) {
            return "Unable to remove test-owned datapack " + target + ": " + failure.getMessage();
        }
    }

    private static String rootMessage(Throwable failure) {
        Throwable current = failure;
        while (current.getCause() != null) current = current.getCause();
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }

    private static String suffix(String cleanupFailure) {
        return cleanupFailure == null ? "" : "; " + cleanupFailure;
    }

    private static void assertBoundMember(GameTestHelper helper, String tagId, Item expected) {
        var tag = ForgeRegistries.ITEMS.tags().getTag(ItemTags.create(new ResourceLocation(tagId)));
        helper.assertTrue(tag.isBound(), tagId + " must be bound");
        helper.assertTrue(tag.contains(expected), tagId + " must contain "
                + ForgeRegistries.ITEMS.getKey(expected));
    }

    private static void assertExact(GameTestHelper helper, ItemStack stack, Aspect aspect, int amount) {
        AspectList raw = AspectHelper.getRegisteredObjectAspects(stack);
        helper.assertTrue(raw != null && raw.size() == 1 && raw.getAmount(aspect) == amount,
                "Bound iron tag must supply raw METAL 15 after startup");
        AspectList computed = AspectHelper.getObjectAspects(stack);
        helper.assertTrue(computed.size() == 1 && computed.getAmount(aspect) == amount
                        && computed.getAmount(Aspect.ENTROPY) == 0,
                "Computed iron aspects must match the seeded tag without fallback");
    }

}
