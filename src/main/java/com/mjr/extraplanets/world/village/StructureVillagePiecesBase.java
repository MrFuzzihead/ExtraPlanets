package com.mjr.extraplanets.world.village;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureComponent;

/**
 * The village piece table: how big a village may get, which piece comes next, and how far it may spread.
 *
 * <p>
 * Every body ships its own {@code StructureVillagePieces} (see {@code CODE_REVIEW.md} §6.3) and four of its methods
 * were byte-identical in all 22. What genuinely differs is data: the weight table, the class-comparison dispatch, and
 * the two factories the generator falls back to (a torch when the weighted pick finds nothing, a path when the
 * village grows).
 * </p>
 *
 * <p>
 * The per-body subclasses keep a static {@code INSTANCE} and their original static methods as forwarders, so the
 * call sites in the components did not have to change.
 * </p>
 */
public abstract class StructureVillagePiecesBase {

    /** This body's piece table: which components may spawn, with what weight and how many. */
    public abstract List<? extends StructureVillagePieceWeightBase> weightedPieceList(Random rand, int terrainType);

    /** Builds one component from a weight. The body's dispatch switch. */
    protected abstract StructureComponent create(StructureVillagePieceWeightBase weight, StructureComponent startPiece,
        List<StructureComponent> components, Random rand, int x, int y, int z, int mode, int type);

    /** The fallback when the weighted pick produced nothing. */
    protected abstract StructureComponent createTorch(StructureComponent startPiece,
        List<StructureComponent> components, Random rand, int x, int y, int z, int mode, int type);

    /** One path segment, or null if it does not fit. */
    protected abstract StructureComponent createPath(StructureComponent startPiece, List<StructureComponent> components,
        Random rand, int x, int y, int z, int mode, int type);

    /** @return the total weight still available, or -1 when every piece type is exhausted. */
    public int sumLimits(List<? extends StructureVillagePieceWeightBase> weights) {
        boolean anyLeft = false;
        int total = 0;

        for (final Iterator<? extends StructureVillagePieceWeightBase> iterator = weights.iterator(); iterator
            .hasNext(); total += iterator.next().villagePieceWeight) {
            // side effect only: the increment in the loop header sums the weights
        }

        for (final StructureVillagePieceWeightBase weight : weights) {
            if (weight.villagePiecesLimit > 0 && weight.villagePiecesSpawned < weight.villagePiecesLimit) {
                anyLeft = true;
            }
        }

        return anyLeft ? total : -1;
    }

    /** One weighted pick, with the torch as a fallback. */
    private StructureComponent nextComponent(StructureComponent startPiece, List<StructureComponent> components,
        Random rand, int x, int y, int z, int mode, int type) {
        final List<StructureVillagePieceWeightBase> weights = ((StructureComponentVillageBase) startPiece).structureVillageWeightedPieceList;
        final int total = this.sumLimits(weights);

        if (total <= 0) {
            return null;
        }

        for (int attempt = 0; attempt < 5; ++attempt) {
            int remaining = rand.nextInt(total);

            for (final StructureVillagePieceWeightBase weight : weights) {
                remaining -= weight.villagePieceWeight;

                if (remaining < 0) {
                    if (!weight.canSpawnMoreVillagePiecesOfType(type)
                        || weight == ((StructureComponentVillageBase) startPiece).structVillagePieceWeight
                            && weights.size() > 1) {
                        break;
                    }

                    final StructureComponent component = this
                        .create(weight, startPiece, components, rand, x, y, z, mode, type);

                    if (component != null) {
                        ++weight.villagePiecesSpawned;
                        ((StructureComponentVillageBase) startPiece).structVillagePieceWeight = weight;

                        if (!weight.canSpawnMoreVillagePieces()) {
                            weights.remove(weight);
                        }

                        return component;
                    }
                }
            }
        }

        return this.createTorch(startPiece, components, rand, x, y, z, mode, type);
    }

    /** Grows the village by one component, or returns null when it cannot. */
    public StructureComponent next(StructureComponent startPiece, List<StructureComponent> components, Random rand,
        int x, int y, int z, int mode, int type) {
        if (type > 50) {
            return null;
        } else if (Math.abs(x - startPiece.getBoundingBox().minX) <= 112
            && Math.abs(z - startPiece.getBoundingBox().minZ) <= 112) {
                final StructureComponent component = this
                    .nextComponent(startPiece, components, rand, x, y, z, mode, type + 1);

                if (component != null) {
                    components.add(component);
                    ((StructureComponentVillageBase) startPiece).field_74932_i.add(component);
                    return component;
                }

                return null;
            } else {
                return null;
            }
    }

    /** Grows the village by one path segment, or returns null when it cannot. */
    public StructureComponent nextPath(StructureComponent startPiece, List<StructureComponent> components, Random rand,
        int x, int y, int z, int mode, int type) {
        if (type > 3 + ((StructureComponentVillageBase) startPiece).terrainType) {
            return null;
        } else if (Math.abs(x - startPiece.getBoundingBox().minX) <= 112
            && Math.abs(z - startPiece.getBoundingBox().minZ) <= 112) {
                final StructureComponent component = this.createPath(startPiece, components, rand, x, y, z, mode, type);

                if (component != null) {
                    components.add(component);
                    ((StructureComponentVillageBase) startPiece).field_74930_j.add(component);
                    return component;
                }

                return null;
            } else {
                return null;
            }
    }
}
