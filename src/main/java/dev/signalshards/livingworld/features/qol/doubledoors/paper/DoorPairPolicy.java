package dev.signalshards.livingworld.features.qol.doubledoors.paper;

import org.bukkit.Material;
import org.bukkit.block.data.type.Door;

final class DoorPairPolicy {
    boolean isCompatible(
            Material sourceMaterial,
            Door source,
            Material candidateMaterial,
            Door candidate
    ) {
        return sourceMaterial == candidateMaterial
                && source.getFacing() == candidate.getFacing()
                && source.getHinge() != candidate.getHinge()
                && !source.isPowered()
                && !candidate.isPowered();
    }
}
