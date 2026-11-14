package com.mjr.extraplanets.gametest;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.block.Block;

import com.mjr.extraplanets.blocks.ExtraPlanets_MicroBlocks;

/**
 * Covers the ForgeMultipart microblock registration.
 *
 * <p>
 * The whole path is reflective - ForgeMultipart is an optional dependency, so the classes are looked up by name - and
 * it used to sit inside a bare {@code catch (Exception e) {}}, which meant a typo in a tile name produced a silently
 * missing material. Two checks, both of which need the real registry:
 * </p>
 *
 * <ul>
 * <li>each registration is looked up again by name in {@code MicroMaterialRegistry} and must resolve to the block and
 * metadata it was registered for - that is what proves the reflection actually landed;</li>
 * <li>the full set of (block, metadata, name) registrations is hashed against a golden, so the table-driven rewrite
 * cannot quietly drop, duplicate or renumber one.</li>
 * </ul>
 */
final class MicroBlockSnapshots {

    private MicroBlockSnapshots() {}

    static void run(TestReporter reporter) {
        reporter.check("microblocks: every material resolves to the block and metadata it was registered for", () -> {
            TestReporter.assertFalse(
                "ForgeMultipart is on the dev runtime, so materials should have been registered",
                ExtraPlanets_MicroBlocks.getRegistered()
                    .isEmpty());

            for (ExtraPlanets_MicroBlocks.Material material : ExtraPlanets_MicroBlocks.getRegistered()) {
                Object registered = lookup(material.name);
                TestReporter.assertNotNull(
                    "'" + material.name + "' was registered but MicroMaterialRegistry does not know it",
                    registered);
                TestReporter.assertEquals(
                    "'" + material.name + "' points at the wrong block",
                    material.block,
                    call(registered, "block"));
                TestReporter.assertEquals(
                    "'" + material.name + "' points at the wrong metadata",
                    Integer.valueOf(material.meta),
                    Integer.valueOf(((Number) call(registered, "meta")).intValue()));
            }
        });

        reporter.check("microblocks: the registered set is unchanged (golden)", () -> {
            Map<String, Long> actual = new TreeMap<String, Long>();
            for (ExtraPlanets_MicroBlocks.Material material : ExtraPlanets_MicroBlocks.getRegistered()) {
                // Keyed by material name, not by block+metadata: the registration set contains a deliberate duplicate
                // (uranusBlocks meta 0 is registered twice, once under a bogus name - see CODE_REVIEW.md), and a
                // block-keyed map would silently collapse the two.
                String key = "microblock/" + material.name;
                long target = Block.getIdFromBlock(material.block) * 16L + material.meta;
                TestReporter.assertNull("'" + material.name + "' is registered twice", actual.put(key, target));
            }
            GoldenFiles.verify(
                actual,
                "microblock-golden.txt",
                key -> key.startsWith("microblock/"),
                "the set of registered microblock materials changed");
        });
    }

    // ---------------------------------------------------------------------------------------------------------
    // The ForgeMultipart API, reached reflectively for the same reason the production code does
    // ---------------------------------------------------------------------------------------------------------

    private static Object lookup(String name) {
        try {
            Class<?> registry = Class.forName("codechicken.microblock.MicroMaterialRegistry");
            Method getMaterial = registry.getMethod("getMaterial", String.class);
            return getMaterial.invoke(null, name);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("cannot query MicroMaterialRegistry", e);
        }
    }

    private static Object call(Object material, String method) {
        try {
            return material.getClass()
                .getMethod(method)
                .invoke(material);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("cannot call " + method + " on " + material.getClass(), e);
        }
    }
}
