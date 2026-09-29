package dev.signalshards.livingworld.features.ecology.paper;

import dev.signalshards.livingworld.features.ecology.application.WinterSurfaceOwnershipLedger;
import dev.signalshards.livingworld.features.ecology.domain.WinterSnowTarget;
import dev.signalshards.livingworld.features.ecology.domain.WinterSurfaceKind;
import dev.signalshards.livingworld.features.ecology.domain.WinterSurfacePosition;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Snow;

import java.util.Objects;

public final class PaperWinterSnowTargetResolver {
    private final Snow placementData;

    PaperWinterSnowTargetResolver(Snow placementData) {
        this.placementData = Objects.requireNonNull(
                placementData,
                "block data de snow"
        );
    }

    public Observation resolve(
            Block surface,
            WinterSurfaceOwnershipLedger ownership
    ) {
        Objects.requireNonNull(surface, "superfície");
        Objects.requireNonNull(ownership, "ownership");

        if (surface.getType() == Material.SNOW) {
            if (surface.getBlockData() instanceof Snow snow
                    && ownership.kindAt(position(surface)).orElse(null)
                    == WinterSurfaceKind.SNOW) {
                return new Observation(
                        WinterSnowTarget.OWNED_SNOW,
                        surface,
                        snow.getLayers()
                );
            }
            return new Observation(
                    WinterSnowTarget.OTHER,
                    surface,
                    0
            );
        }

        if (surface.getType() == Material.WATER
                || surface.getType() == Material.LAVA) {
            return new Observation(
                    WinterSnowTarget.OTHER,
                    surface,
                    0
            );
        }

        Block above = surface.getRelative(BlockFace.UP);
        if (!above.isEmpty() || !above.canPlace(placementData)) {
            return new Observation(
                    WinterSnowTarget.OTHER,
                    above,
                    0
            );
        }
        return new Observation(
                WinterSnowTarget.EXPOSED_SUPPORT,
                above,
                0
        );
    }

    private WinterSurfacePosition position(Block block) {
        return WinterSurfacePosition.fromWorld(
                block.getX(),
                block.getY(),
                block.getZ()
        );
    }

    public record Observation(
            WinterSnowTarget target,
            Block mutationBlock,
            int currentLayers
    ) {
        public Observation {
            Objects.requireNonNull(target, "alvo de neve");
            Objects.requireNonNull(mutationBlock, "bloco de mutação");
            if (currentLayers < 0 || currentLayers > 8) {
                throw new IllegalArgumentException(
                        "Camadas atuais devem ficar entre 0 e 8"
                );
            }
        }
    }
}
