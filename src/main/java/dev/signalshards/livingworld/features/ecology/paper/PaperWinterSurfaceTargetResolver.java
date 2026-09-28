package dev.signalshards.livingworld.features.ecology.paper;

import dev.signalshards.livingworld.features.ecology.application.WinterSurfaceOwnershipLedger;
import dev.signalshards.livingworld.features.ecology.domain.WinterSurfaceKind;
import dev.signalshards.livingworld.features.ecology.domain.WinterSurfacePosition;
import dev.signalshards.livingworld.features.ecology.domain.WinterSurfaceTarget;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;

import java.util.Objects;

public final class PaperWinterSurfaceTargetResolver {
    public WinterSurfaceTarget resolve(
            Block block,
            WinterSurfaceOwnershipLedger ownership
    ) {
        Objects.requireNonNull(block, "bloco");
        Objects.requireNonNull(ownership, "ownership");
        WinterSurfacePosition position = WinterSurfacePosition.fromWorld(
                block.getX(),
                block.getY(),
                block.getZ()
        );

        if (block.getType() == Material.ICE
                && ownership.kindAt(position).orElse(null)
                == WinterSurfaceKind.ICE) {
            return WinterSurfaceTarget.OWNED_ICE;
        }

        if (block.getType() != Material.WATER
                || !(block.getBlockData() instanceof Levelled levelled)
                || levelled.getLevel() != 0
                || block.getLightFromSky() < 15
                || !block.getRelative(BlockFace.UP).isEmpty()) {
            return WinterSurfaceTarget.OTHER;
        }

        return WinterSurfaceTarget.EXPOSED_SOURCE_WATER;
    }
}
