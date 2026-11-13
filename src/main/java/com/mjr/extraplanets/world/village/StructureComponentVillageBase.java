package com.mjr.extraplanets.world.village;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;

import micdoodle8.mods.galacticraft.core.blocks.GCBlocks;
import micdoodle8.mods.galacticraft.core.entities.EntityAlienVillager;

/**
 * The parts of a Galacticraft village component that are the same in every body.
 *
 * <p>
 * ExtraPlanets ships 22 copies of the village classes (see {@code CODE_REVIEW.md} §6.3). Everything in this class was
 * identical in all 22 - the villager bookkeeping, the ground-level probe, the villager spawner and the four
 * block-placement overrides that route every block through {@link #getBiomeSpecificBlock(Block, int)} and
 * {@link #getBiomeSpecificBlockMetadata(Block, int)} - so it lives here once instead of 22 times.
 * </p>
 *
 * <p>
 * What stays per body is genuinely body-specific: the {@code MapGenStructureIO} registration, the start-piece type and
 * the two piece-lookup helpers that call the body's {@code StructureVillagePieces}.
 * </p>
 */
public abstract class StructureComponentVillageBase extends StructureComponent {

    private int villagersSpawned;

    /** Ground level this component settled on, or -1 until {@link #buildHouse} works it out. */
    protected int averageGroundLevel = -1;

    public StructureComponentVillageBase() {}

    protected StructureComponentVillageBase(int par2) {
        super(par2);
    }

    @Override
    protected void func_143012_a(NBTTagCompound nbttagcompound) {
        nbttagcompound.setInteger("VCount", this.villagersSpawned);
    }

    @Override
    protected void func_143011_b(NBTTagCompound nbttagcompound) {
        this.villagersSpawned = nbttagcompound.getInteger("VCount");
    }

    /**
     * Some bodies were given a shortcut that snaps the ground level to the first air block at or above y=64, and to
     * 100 above that. It is not vanilla behaviour and it is not in every body, so it stays a switch rather than being
     * silently unified: the bodies that have it override this.
     */
    protected boolean snapsGroundLevelToAir() {
        return false;
    }

    protected int getAverageGroundLevel(World par1World, StructureBoundingBox par2StructureBoundingBox) {
        int var3 = 0;
        int var4 = 0;

        for (int var5 = this.boundingBox.minZ; var5 <= this.boundingBox.maxZ; ++var5) {
            for (int var6 = this.boundingBox.minX; var6 <= this.boundingBox.maxX; ++var6) {
                if (this.snapsGroundLevelToAir()) {
                    if (par1World.getTopSolidOrLiquidBlock(var5, var6) <= 100) {
                        for (int n = 0; n < 20; n++) {
                            if (par1World.getBlock(var5, 64 + n, var6) == Blocks.air) return 64 + n;
                        }
                    } else {
                        return 100;
                    }
                }

                if (par2StructureBoundingBox.isVecInside(var6, 64, var5)) {
                    var3 += Math.max(
                        par1World.getTopSolidOrLiquidBlock(var6, var5),
                        par1World.provider.getAverageGroundLevel());
                    ++var4;
                }
            }
        }

        if (var4 == 0) {
            return -1;
        } else {
            return var3 / var4;
        }
    }

    protected static boolean canVillageGoDeeper(StructureBoundingBox par0StructureBoundingBox) {
        return par0StructureBoundingBox != null && par0StructureBoundingBox.minY > 10;
    }

    protected void spawnVillagers(World par1World, StructureBoundingBox par2StructureBoundingBox, int par3, int par4,
        int par5, int par6) {
        if (this.villagersSpawned < par6) {
            for (int var7 = this.villagersSpawned; var7 < par6; ++var7) {
                int var8 = this.getXWithOffset(par3 + var7, par5);
                final int var9 = this.getYWithOffset(par4);
                int var10 = this.getZWithOffset(par3 + var7, par5);

                var8 += par1World.rand.nextInt(3) - 1;
                var10 += par1World.rand.nextInt(3) - 1;

                if (!par2StructureBoundingBox.isVecInside(var8, var9, var10)) {
                    break;
                }

                ++this.villagersSpawned;
                final EntityAlienVillager var11 = new EntityAlienVillager(par1World);
                var11.setLocationAndAngles(var8 + 0.5D, var9, var10 + 0.5D, 0.0F, 0.0F);
                par1World.spawnEntityInWorld(var11);
            }
        }
    }

    protected int getVillagerType(int par1) {
        return 0;
    }

    protected Block getBiomeSpecificBlock(Block par1, int par2) {
        return par1;
    }

    protected int getBiomeSpecificBlockMetadata(Block par1, int par2) {
        return par2;
    }

    @Override
    protected void placeBlockAtCurrentPosition(World par1World, Block par2, int par3, int par4, int par5, int par6,
        StructureBoundingBox par7StructureBoundingBox) {
        final Block var8 = this.getBiomeSpecificBlock(par2, par3);
        final int var9 = this.getBiomeSpecificBlockMetadata(par2, par3);
        super.placeBlockAtCurrentPosition(par1World, var8, var9, par4, par5, par6, par7StructureBoundingBox);
    }

    @Override
    protected void fillWithBlocks(World par1World, StructureBoundingBox par2StructureBoundingBox, int par3, int par4,
        int par5, int par6, int par7, int par8, Block par9, Block par10, boolean par11) {
        final Block var12 = this.getBiomeSpecificBlock(par9, 0);
        final int var13 = this.getBiomeSpecificBlockMetadata(par9, 0);
        final Block var14 = this.getBiomeSpecificBlock(par10, 0);
        final int var15 = this.getBiomeSpecificBlockMetadata(par10, 0);
        super.fillWithMetadataBlocks(
            par1World,
            par2StructureBoundingBox,
            par3,
            par4,
            par5,
            par6,
            par7,
            par8,
            var12,
            var13,
            var14,
            var15,
            par11);
    }

    @Override
    protected void func_151554_b(World par1World, Block par2, int par3, int par4, int par5, int par6,
        StructureBoundingBox par7StructureBoundingBox) {
        final Block var8 = this.getBiomeSpecificBlock(par2, par3);
        final int var9 = this.getBiomeSpecificBlockMetadata(par2, par3);
        super.func_151554_b(par1World, var8, var9, par4, par5, par6, par7StructureBoundingBox);
    }

    /**
     * Builds the 17x9x17 village house. The code is identical in every body except for which block it is made of, so
     * it takes the body's block as a parameter instead of being copied 21 times.
     *
     * @param bodyBlock the body's own block set, e.g. {@code ExtraPlanets_Blocks.jupiterBlocks}.
     * @return false if the component could not be placed, which the caller uses to skip it.
     */
    protected boolean buildHouse(World par1World, Random par2Random, StructureBoundingBox par3StructureBoundingBox,
        Block bodyBlock) {
        if (this.averageGroundLevel < 0) {
            this.averageGroundLevel = this.getAverageGroundLevel(par1World, par3StructureBoundingBox);

            if (this.averageGroundLevel < 0) {
                return true;
            }

            this.boundingBox.offset(0, this.averageGroundLevel - this.boundingBox.maxY + 9 - 1, 0);
        }

        this.fillWithAir(par1World, par3StructureBoundingBox, 3, 0, 3, 13, 9, 13);
        this.fillWithAir(par1World, par3StructureBoundingBox, 5, 0, 2, 11, 9, 14);
        this.fillWithAir(par1World, par3StructureBoundingBox, 2, 0, 5, 14, 9, 11);

        for (int i = 3; i <= 13; i++) {
            for (int j = 3; j <= 13; j++) {
                this.placeBlockAtCurrentPosition(par1World, bodyBlock, 0, i, 0, j, par3StructureBoundingBox);
            }
        }

        for (int i = 5; i <= 11; i++) {
            for (int j = 2; j <= 14; j++) {
                this.placeBlockAtCurrentPosition(par1World, bodyBlock, 0, i, 0, j, par3StructureBoundingBox);
            }
        }

        for (int i = 2; i <= 14; i++) {
            for (int j = 5; j <= 11; j++) {
                this.placeBlockAtCurrentPosition(par1World, bodyBlock, 0, i, 0, j, par3StructureBoundingBox);
            }
        }

        int yLevel = 0;

        for (yLevel = -8; yLevel < 4; yLevel++) {
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 4, yLevel, 2, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 3, yLevel, 2, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 2, yLevel, 3, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 2, yLevel, 4, par3StructureBoundingBox);

            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 1, yLevel, 5, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 1, yLevel, 6, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 1, yLevel, 7, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(
                par1World,
                yLevel <= 1 ? bodyBlock : Blocks.air,
                1,
                1,
                yLevel,
                8,
                par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 1, yLevel, 9, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 1, yLevel, 10, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 1, yLevel, 11, par3StructureBoundingBox);

            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 2, yLevel, 12, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 2, yLevel, 13, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 3, yLevel, 14, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 4, yLevel, 14, par3StructureBoundingBox);

            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 5, yLevel, 15, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 6, yLevel, 15, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 7, yLevel, 15, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(
                par1World,
                yLevel <= 1 ? bodyBlock : Blocks.air,
                1,
                8,
                yLevel,
                15,
                par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 9, yLevel, 15, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 10, yLevel, 15, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 11, yLevel, 15, par3StructureBoundingBox);

            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 12, yLevel, 14, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 13, yLevel, 14, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 14, yLevel, 13, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 14, yLevel, 12, par3StructureBoundingBox);

            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 15, yLevel, 11, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 15, yLevel, 10, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 15, yLevel, 9, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(
                par1World,
                yLevel <= 1 ? bodyBlock : Blocks.air,
                1,
                15,
                yLevel,
                8,
                par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 15, yLevel, 7, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 15, yLevel, 6, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 15, yLevel, 5, par3StructureBoundingBox);

            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 14, yLevel, 4, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 14, yLevel, 3, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 13, yLevel, 2, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 12, yLevel, 2, par3StructureBoundingBox);

            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 11, yLevel, 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 10, yLevel, 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 9, yLevel, 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(
                par1World,
                yLevel <= 1 ? bodyBlock : Blocks.air,
                1,
                8,
                yLevel,
                1,
                par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 7, yLevel, 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 6, yLevel, 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 5, yLevel, 1, par3StructureBoundingBox);
        }

        yLevel = 4;

        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 4, yLevel, 2, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 3, yLevel, 3, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 2, yLevel, 4, par3StructureBoundingBox);

        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 1, yLevel, 5, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 1, yLevel, 6, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 1, yLevel, 7, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 1, yLevel, 8, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 1, yLevel, 9, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 1, yLevel, 10, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 1, yLevel, 11, par3StructureBoundingBox);

        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 2, yLevel, 12, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 3, yLevel, 13, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 4, yLevel, 14, par3StructureBoundingBox);

        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 5, yLevel, 15, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 6, yLevel, 15, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 7, yLevel, 15, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 8, yLevel, 15, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 9, yLevel, 15, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 10, yLevel, 15, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 11, yLevel, 15, par3StructureBoundingBox);

        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 12, yLevel, 14, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 13, yLevel, 13, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 14, yLevel, 12, par3StructureBoundingBox);

        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 15, yLevel, 11, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 15, yLevel, 10, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 15, yLevel, 9, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 15, yLevel, 8, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 15, yLevel, 7, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 15, yLevel, 6, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 15, yLevel, 5, par3StructureBoundingBox);

        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 14, yLevel, 4, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 13, yLevel, 3, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 12, yLevel, 2, par3StructureBoundingBox);

        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 11, yLevel, 1, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 10, yLevel, 1, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 9, yLevel, 1, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 8, yLevel, 1, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 7, yLevel, 1, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 6, yLevel, 1, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 5, yLevel, 1, par3StructureBoundingBox);

        this.placeBlockAtCurrentPosition(par1World, GCBlocks.glowstoneTorch, 0, 8, yLevel, 2, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(
            par1World,
            GCBlocks.glowstoneTorch,
            0,
            14,
            yLevel,
            8,
            par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(
            par1World,
            GCBlocks.glowstoneTorch,
            0,
            8,
            yLevel,
            14,
            par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, GCBlocks.glowstoneTorch, 0, 2, yLevel, 8, par3StructureBoundingBox);

        yLevel = 5;

        // corner 1
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 5, yLevel, 2, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 4, yLevel, 2, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 3, yLevel, 3, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 2, yLevel, 4, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 2, yLevel, 5, par3StructureBoundingBox);

        // side 1
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 1, yLevel, 6, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 1, yLevel, 7, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 1, yLevel, 8, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 1, yLevel, 9, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 1, yLevel, 10, par3StructureBoundingBox);

        // corner 2
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 2, yLevel, 11, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 2, yLevel, 12, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 3, yLevel, 13, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 4, yLevel, 14, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 5, yLevel, 14, par3StructureBoundingBox);

        // side 2
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 6, yLevel, 15, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 7, yLevel, 15, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 8, yLevel, 15, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 9, yLevel, 15, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 10, yLevel, 15, par3StructureBoundingBox);

        // corner 3
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 11, yLevel, 14, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 12, yLevel, 14, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 13, yLevel, 13, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 14, yLevel, 12, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 14, yLevel, 11, par3StructureBoundingBox);

        // side 3
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 15, yLevel, 10, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 15, yLevel, 9, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 15, yLevel, 8, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 15, yLevel, 7, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 15, yLevel, 6, par3StructureBoundingBox);

        // corner 4
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 14, yLevel, 5, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 14, yLevel, 4, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 13, yLevel, 3, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 12, yLevel, 2, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 11, yLevel, 2, par3StructureBoundingBox);

        // side 4
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 10, yLevel, 1, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 9, yLevel, 1, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 8, yLevel, 1, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 7, yLevel, 1, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 6, yLevel, 1, par3StructureBoundingBox);

        yLevel = 6;

        // corner 1
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 4, yLevel, 3, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 3, yLevel, 4, par3StructureBoundingBox);

        // side 1
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 2, yLevel, 5, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 2, yLevel, 6, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 2, yLevel, 7, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 2, yLevel, 8, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 2, yLevel, 9, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 2, yLevel, 10, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 2, yLevel, 11, par3StructureBoundingBox);

        // corner 2
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 3, yLevel, 12, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 4, yLevel, 13, par3StructureBoundingBox);

        // side 2
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 5, yLevel, 14, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 6, yLevel, 14, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 7, yLevel, 14, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 8, yLevel, 14, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 9, yLevel, 14, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 10, yLevel, 14, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 11, yLevel, 14, par3StructureBoundingBox);

        // corner 3
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 12, yLevel, 13, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 13, yLevel, 12, par3StructureBoundingBox);

        // side 3
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 14, yLevel, 11, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 14, yLevel, 10, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 14, yLevel, 9, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 14, yLevel, 8, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 14, yLevel, 7, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 14, yLevel, 6, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 14, yLevel, 5, par3StructureBoundingBox);

        // corner 4
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 13, yLevel, 4, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 12, yLevel, 3, par3StructureBoundingBox);

        // side 4
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 11, yLevel, 2, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 10, yLevel, 2, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 9, yLevel, 2, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 8, yLevel, 2, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 7, yLevel, 2, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 6, yLevel, 2, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 5, yLevel, 2, par3StructureBoundingBox);

        yLevel = 7;

        // corner 1
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 6, yLevel, 3, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 5, yLevel, 3, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 4, yLevel, 4, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 3, yLevel, 5, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 3, yLevel, 6, par3StructureBoundingBox);

        // side 1
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 2, yLevel, 7, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 2, yLevel, 8, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 2, yLevel, 9, par3StructureBoundingBox);

        // corner 2
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 3, yLevel, 10, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 3, yLevel, 11, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 4, yLevel, 12, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 5, yLevel, 13, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 6, yLevel, 13, par3StructureBoundingBox);

        // side 2
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 7, yLevel, 14, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 8, yLevel, 14, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 9, yLevel, 14, par3StructureBoundingBox);

        // corner 3
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 10, yLevel, 13, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 11, yLevel, 13, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 12, yLevel, 12, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 13, yLevel, 11, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 13, yLevel, 10, par3StructureBoundingBox);

        // side 3
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 14, yLevel, 9, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 14, yLevel, 8, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 14, yLevel, 7, par3StructureBoundingBox);

        // corner 4
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 13, yLevel, 6, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 13, yLevel, 5, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 12, yLevel, 4, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 11, yLevel, 3, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 10, yLevel, 3, par3StructureBoundingBox);

        // side 4
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 9, yLevel, 2, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 8, yLevel, 2, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 7, yLevel, 2, par3StructureBoundingBox);

        yLevel = 8;

        // corner 1
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 6, yLevel, 4, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 5, yLevel, 4, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 4, yLevel, 5, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 4, yLevel, 6, par3StructureBoundingBox);

        // side 1
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 3, yLevel, 7, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 3, yLevel, 8, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 3, yLevel, 9, par3StructureBoundingBox);

        // corner 2
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 4, yLevel, 10, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 4, yLevel, 11, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 5, yLevel, 12, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 6, yLevel, 12, par3StructureBoundingBox);

        // side 2
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 7, yLevel, 13, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 8, yLevel, 13, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 9, yLevel, 13, par3StructureBoundingBox);

        // corner 3
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 10, yLevel, 12, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 11, yLevel, 12, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 12, yLevel, 11, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 12, yLevel, 10, par3StructureBoundingBox);

        // side 3
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 13, yLevel, 9, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 13, yLevel, 8, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 13, yLevel, 7, par3StructureBoundingBox);

        // corner 4
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 12, yLevel, 6, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 12, yLevel, 5, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 11, yLevel, 4, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 10, yLevel, 4, par3StructureBoundingBox);

        // side 4
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 9, yLevel, 3, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 8, yLevel, 3, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 7, yLevel, 3, par3StructureBoundingBox);

        // extras
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 5, yLevel, 5, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 5, yLevel, 11, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 11, yLevel, 11, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 11, yLevel, 5, par3StructureBoundingBox);

        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 4, yLevel, 7, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 4, yLevel, 8, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 4, yLevel, 9, par3StructureBoundingBox);

        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 7, yLevel, 12, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 8, yLevel, 12, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 9, yLevel, 12, par3StructureBoundingBox);

        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 9, yLevel, 4, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 8, yLevel, 4, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 7, yLevel, 4, par3StructureBoundingBox);

        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 12, yLevel, 7, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 12, yLevel, 8, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, bodyBlock, 2, 12, yLevel, 9, par3StructureBoundingBox);

        yLevel = 9;

        for (int i = 5; i <= 11; i++) {
            for (int j = 5; j <= 11; j++) {
                if (!(j == 5 && i == 5 || j == 5 && i == 11 || j == 11 && i == 5 || j == 11 && i == 11)) {
                    if (i >= 7 && i <= 9 && j >= 7 && j <= 9) {
                        this.placeBlockAtCurrentPosition(
                            par1World,
                            Blocks.glass,
                            0,
                            i,
                            yLevel,
                            j,
                            par3StructureBoundingBox);
                    } else {
                        this.placeBlockAtCurrentPosition(
                            par1World,
                            bodyBlock,
                            2,
                            i,
                            yLevel,
                            j,
                            par3StructureBoundingBox);
                    }
                }
            }
        }

        this.spawnVillagers(par1World, par3StructureBoundingBox, 6, 5, 6, 4);
        return true;
    }
}
