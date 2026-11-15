package com.mjr.extraplanets.blocks;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.util.ExtraPlanetsLog;

/**
 * Registers this mod's blocks with ForgeMultipart so they can be cut into microblocks.
 *
 * <p>
 * The registration is reflective because ForgeMultipart is an optional dependency: it is a dev-runtime and
 * server-side extra, never a compile or a shipped dependency (see {@code dependencies.gradle}), so the classes are
 * looked up by name and the whole feature quietly disappears when it is absent.
 * </p>
 *
 * <p>
 * There is one {@code register(...)} call per run of contiguous metadata values, listing that run's material names;
 * the helper walks them. That replaces 63 copy-pasted reflective invocations, and it makes adding a body one line
 * instead of eight. Failures are logged rather than swallowed: this used to sit inside a bare
 * {@code catch (Exception e) {}}, so a typo in a tile name silently produced a missing material.
 * </p>
 */
public class ExtraPlanets_MicroBlocks {

    /** One microblock material this mod asked ForgeMultipart to register. */
    public static final class Material {

        public final Block block;
        public final int meta;
        public final String name;

        Material(Block block, int meta, String name) {
            this.block = block;
            this.meta = meta;
            this.name = name;
        }
    }

    /**
     * Everything this mod registered, in registration order.
     *
     * <p>
     * Exposed so the in-game suite can check that each one really resolves to the block and metadata it was
     * registered for: the whole path is reflective, so a typo in a tile name is otherwise invisible.
     * </p>
     */
    private static final List<Material> REGISTERED = new ArrayList<Material>();

    public static List<Material> getRegistered() {
        return REGISTERED;
    }

    public static void init() {
        registerMicroBlocks();
    }

    private static void registerMicroBlocks() {
        final Method registerMethod;
        final Constructor<?> materialCtor;
        try {
            Class<?> registry = Class.forName("codechicken.microblock.MicroMaterialRegistry");
            registerMethod = findRegisterMaterial(registry);
            materialCtor = Class.forName("codechicken.microblock.BlockMicroMaterial")
                .getConstructor(Block.class, int.class);
        } catch (ClassNotFoundException e) {
            ExtraPlanetsLog.debug("ForgeMultipart is not installed; skipping microblock registration");
            return;
        } catch (ReflectiveOperationException e) {
            ExtraPlanetsLog.severe(
                "[ExtraPlanets] ForgeMultipart is installed but its microblock API is not the expected shape",
                e);
            return;
        }

        register(
            registerMethod,
            materialCtor,
            Config.mercury,
            ExtraPlanets_Blocks.mercuryBlocks,
            0,
            "tile.mercury.surface",
            "tile.mercury.subSurface",
            "tile.mercury.stone");
        register(
            registerMethod,
            materialCtor,
            Config.ceres,
            ExtraPlanets_Blocks.ceresBlocks,
            0,
            "tile.ceres.surface",
            "tile.ceres.subSurface",
            "tile.ceres.stone");
        register(
            registerMethod,
            materialCtor,
            Config.venus,
            ExtraPlanets_Blocks.venusBlocks,
            0,
            "tile.venus.surface",
            "tile.venus.subSurface",
            "tile.venus.stone");
        register(
            registerMethod,
            materialCtor,
            Config.venus,
            ExtraPlanets_Blocks.venusBlocks,
            7,
            "tile.venus.carbonBlock");
        register(
            registerMethod,
            materialCtor,
            Config.venus,
            ExtraPlanets_Blocks.veunsDungeonBrick,
            0,
            "tile.veunsDungeonBrick");
        register(
            registerMethod,
            materialCtor,
            Config.jupiter,
            ExtraPlanets_Blocks.jupiterBlocks,
            0,
            "tile.jupiter.surface",
            "tile.jupiter.subSurface",
            "tile.jupiter.stone");
        register(
            registerMethod,
            materialCtor,
            Config.jupiter,
            ExtraPlanets_Blocks.jupiterBlocks,
            8,
            "tile.jupiter.palladiumBlock");
        register(
            registerMethod,
            materialCtor,
            Config.jupiter,
            ExtraPlanets_Blocks.jupiterDungeonBrick,
            0,
            "tile.jupiterDungeonBrick");
        register(
            registerMethod,
            materialCtor,
            Config.jupiter,
            ExtraPlanets_Blocks.jupiterBlocks,
            11,
            "tile.jupiter.redGemBlock");
        register(
            registerMethod,
            materialCtor,
            Config.saturn,
            ExtraPlanets_Blocks.saturnBlocks,
            0,
            "tile.saturn.surface",
            "tile.saturn.subSurface",
            "tile.saturn.stone");
        register(
            registerMethod,
            materialCtor,
            Config.saturn,
            ExtraPlanets_Blocks.saturnBlocks,
            7,
            "tile.saturn.magnesiumBlock");
        register(
            registerMethod,
            materialCtor,
            Config.saturn,
            ExtraPlanets_Blocks.saturnDungeonBrick,
            0,
            "tile.saturnDungeonBrick");
        register(
            registerMethod,
            materialCtor,
            Config.uranus,
            ExtraPlanets_Blocks.uranusBlocks,
            0,
            "tile.uranus.surface",
            "tile.uranus.subSurface",
            "tile.uranus.stone");
        register(
            registerMethod,
            materialCtor,
            Config.uranus,
            ExtraPlanets_Blocks.uranusBlocks,
            4,
            "tile.uranus.crystalBlock");
        register(registerMethod, materialCtor, Config.uranus, ExtraPlanets_Blocks.uranusBlocks, 0, "tile.uranusBlocks");
        register(
            registerMethod,
            materialCtor,
            Config.uranus,
            ExtraPlanets_Blocks.uranusBlocks,
            7,
            "tile.uranus.whiteGemBlock");
        register(
            registerMethod,
            materialCtor,
            Config.neptune,
            ExtraPlanets_Blocks.neptuneBlocks,
            0,
            "tile.neptune.surface",
            "tile.neptune.subSurface",
            "tile.neptune.stone");
        register(
            registerMethod,
            materialCtor,
            Config.neptune,
            ExtraPlanets_Blocks.neptuneBlocks,
            7,
            "tile.neptune.zincBlock");
        register(
            registerMethod,
            materialCtor,
            Config.neptune,
            ExtraPlanets_Blocks.neptuneDungeonBrick,
            0,
            "tile.neptuneDungeonBrick");
        register(
            registerMethod,
            materialCtor,
            Config.neptune,
            ExtraPlanets_Blocks.neptuneBlocks,
            10,
            "tile.neptune.neptuneGemBlock");
        register(
            registerMethod,
            materialCtor,
            Config.pluto,
            ExtraPlanets_Blocks.plutoBlocks,
            0,
            "tile.pluto.surface",
            "tile.pluto.subSurface",
            "tile.pluto.stone");
        register(
            registerMethod,
            materialCtor,
            Config.pluto,
            ExtraPlanets_Blocks.plutoBlocks,
            7,
            "tile.pluto.tungstenBlock");
        register(
            registerMethod,
            materialCtor,
            Config.pluto,
            ExtraPlanets_Blocks.plutoDungeonBrick,
            0,
            "tile.plutoDungeonBrick");
        register(
            registerMethod,
            materialCtor,
            Config.eris,
            ExtraPlanets_Blocks.erisBlocks,
            0,
            "tile.eris.surface",
            "tile.eris.subSurface",
            "tile.eris.stone");
        register(
            registerMethod,
            materialCtor,
            Config.eris,
            ExtraPlanets_Blocks.erisDungeonBrick,
            0,
            "tile.erisDungeonBrick");
        register(
            registerMethod,
            materialCtor,
            Config.kepler22b,
            ExtraPlanets_Blocks.kepler22bBlocks,
            0,
            "tile.kepler22b.Dirt",
            "tile.kepler22b.Stone");
        register(
            registerMethod,
            materialCtor,
            Config.kepler22b,
            ExtraPlanets_Blocks.kepler22bBlocks,
            12,
            "tile.kepler22b.Cobblestone");
        register(
            registerMethod,
            materialCtor,
            Config.kepler22b,
            ExtraPlanets_Blocks.kepler22bGrass,
            0,
            "tile.kepler22bGrass");
        register(
            registerMethod,
            materialCtor,
            Config.kepler22b,
            ExtraPlanets_Blocks.kepler22bBlueGrass,
            0,
            "tile.kepler22b_blueGrass");
        register(
            registerMethod,
            materialCtor,
            Config.kepler22b,
            ExtraPlanets_Blocks.kepler22bPurpleGrass,
            0,
            "tile.kepler22b_purpleGrass");
        register(
            registerMethod,
            materialCtor,
            Config.kepler22b,
            ExtraPlanets_Blocks.kepler22bRedGrass,
            0,
            "tile.kepler22b_redGrass");
        register(
            registerMethod,
            materialCtor,
            Config.kepler22b,
            ExtraPlanets_Blocks.kepler22bYellowGrass,
            0,
            "tile.kepler22b_yellowGrass");
        register(
            registerMethod,
            materialCtor,
            Config.kepler22b,
            ExtraPlanets_Blocks.kepler22bPlanks,
            0,
            "tile.kepler22bPlanks.PlankMapleBlue",
            "tile.kepler22bPlanks.PlankMapleRed",
            "tile.kepler22bPlanks.PlankMaplePurple",
            "tile.kepler22bPlanks.PlankMapleYellow",
            "tile.kepler22bPlanks.PlankMapleGreen",
            "tile.kepler22bPlanks.PlankMapleBrown");
        register(
            registerMethod,
            materialCtor,
            Config.kepler22b,
            ExtraPlanets_Blocks.kepler22bMapleLog,
            0,
            "tile.kepler22bMapleLogs.blueMapleLog",
            "tile.kepler22bMapleLogs.redleLog",
            "tile.kepler22bMapleLogs.purpleMapleLog",
            "tile.kepler22bMapleLogs.yellowMapleLog");
        register(
            registerMethod,
            materialCtor,
            Config.kepler22b,
            ExtraPlanets_Blocks.kepler22bMapleLog2,
            0,
            "tile.kepler22bMapleLogs2.greenMapleLog",
            "tile.kepler22bMapleLogs2.brownMapleLog");

        ExtraPlanetsLog.info("[ExtraPlanets] Registered " + REGISTERED.size() + " microblock materials");
    }

    private static Method findRegisterMaterial(Class<?> registry) {
        for (Method method : registry.getMethods()) {
            if (method.getName()
                .equals("registerMaterial")) {
                return method;
            }
        }
        throw new IllegalStateException("MicroMaterialRegistry has no registerMaterial method");
    }

    /** Registers one material per name, counting metadata up from {@code firstMeta}. */
    private static void register(Method registerMethod, Constructor<?> materialCtor, boolean enabled, Block block,
        int firstMeta, String... names) {
        if (!enabled) {
            return;
        }
        if (block == null) {
            ExtraPlanetsLog.severe(
                "[ExtraPlanets] A body is enabled but its block set is missing, so its microblock materials were not"
                    + " registered");
            return;
        }

        for (int i = 0; i < names.length; i++) {
            int meta = firstMeta + i;
            try {
                registerMethod.invoke(null, materialCtor.newInstance(block, meta), names[i]);
                REGISTERED.add(new Material(block, meta, names[i]));
            } catch (ReflectiveOperationException e) {
                ExtraPlanetsLog
                    .severe("[ExtraPlanets] Could not register the microblock material '" + names[i] + "'", e);
            }
        }
    }
}
