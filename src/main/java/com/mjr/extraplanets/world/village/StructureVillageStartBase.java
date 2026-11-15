package com.mjr.extraplanets.world.village;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;

/**
 * The village start algorithm, which was identical in all 22 bodies.
 *
 * <p>
 * A village start seeds a weighted piece list, builds the start piece (a well) and then keeps extending the two
 * pending queues until both are empty, which is the vanilla growth loop. What stays per body is the constructor that
 * names the body's own piece list and start piece, and the {@link #isSizeableStructure()} answer.
 * </p>
 *
 * <p>
 * The per-body copies also counted the non-road components into a local that was never read; that is gone.
 * </p>
 */
public abstract class StructureVillageStartBase extends StructureStart {

    protected StructureVillageStartBase() {}

    protected StructureVillageStartBase(int chunkX, int chunkZ) {
        super(chunkX, chunkZ);
    }

    /** Grows the village from {@code startPiece} and fixes up the overall bounding box. */
    @SuppressWarnings("unchecked")
    protected void buildVillage(StructureComponentVillageBase startPiece, Random rand) {
        this.components.add(startPiece);
        startPiece.buildComponent(startPiece, this.components, rand);

        ArrayList<Object> roads = startPiece.field_74930_j;
        ArrayList<Object> houses = startPiece.field_74932_i;

        while (!roads.isEmpty() || !houses.isEmpty()) {
            StructureComponent next;

            if (roads.isEmpty()) {
                int index = rand.nextInt(houses.size());
                next = (StructureComponent) houses.remove(index);
                next.buildComponent(startPiece, this.components, rand);
            } else {
                int index = rand.nextInt(roads.size());
                next = (StructureComponent) roads.remove(index);
                next.buildComponent(startPiece, this.components, rand);
            }
        }

        this.updateBoundingBox();
    }
}
