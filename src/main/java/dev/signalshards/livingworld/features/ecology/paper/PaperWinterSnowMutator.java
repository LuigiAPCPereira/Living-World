package dev.signalshards.livingworld.features.ecology.paper;

import dev.signalshards.livingworld.features.climate.domain.EnvironmentalDimensionProfile;
import dev.signalshards.livingworld.features.ecology.application.WinterSurfaceOwnershipLedger;
import dev.signalshards.livingworld.features.ecology.domain.PhysicalSnowSettings;
import dev.signalshards.livingworld.features.ecology.domain.WinterSnowMutation;
import dev.signalshards.livingworld.features.ecology.domain.WinterSnowMutationPolicy;
import dev.signalshards.livingworld.features.ecology.domain.WinterSurfaceKind;
import dev.signalshards.livingworld.features.ecology.domain.WinterSurfacePosition;
import dev.signalshards.livingworld.features.ecology.domain.WinterThermalPhase;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Snow;

import java.util.Objects;

public final class PaperWinterSnowMutator {
    private final WinterSnowMutationPolicy policy;
    private final PaperWinterSnowTargetResolver targetResolver;

    public PaperWinterSnowMutator(PhysicalSnowSettings settings) {
        this(
                new WinterSnowMutationPolicy(settings),
                new PaperWinterSnowTargetResolver(
                        (Snow) Material.SNOW.createBlockData()
                )
        );
    }

    PaperWinterSnowMutator(
            WinterSnowMutationPolicy policy,
            PaperWinterSnowTargetResolver targetResolver
    ) {
        this.policy = Objects.requireNonNull(policy, "policy de neve");
        this.targetResolver = Objects.requireNonNull(
                targetResolver,
                "resolver de alvo de neve"
        );
    }

    public boolean mutate(
            Block surface,
            WinterSurfaceOwnershipLedger ownership,
            WinterThermalPhase phase,
            EnvironmentalDimensionProfile dimension,
            boolean precipitating
    ) {
        var observation = targetResolver.resolve(surface, ownership);
        WinterSnowMutation mutation = policy.decide(
                phase,
                dimension,
                precipitating,
                observation.target(),
                observation.currentLayers()
        );

        return switch (mutation) {
            case NONE -> false;
            case PLACE_SNOW -> place(observation.mutationBlock(), ownership);
            case ADD_SNOW_LAYER -> addLayer(observation.mutationBlock());
            case MELT_SNOW_LAYER -> meltLayer(
                    observation.mutationBlock(),
                    ownership
            );
        };
    }

    private boolean place(
            Block block,
            WinterSurfaceOwnershipLedger ownership
    ) {
        if (!ownership.claim(position(block), WinterSurfaceKind.SNOW)) {
            return false;
        }
        block.setType(Material.SNOW, false);
        return true;
    }

    private boolean addLayer(Block block) {
        if (!(block.getBlockData() instanceof Snow snow)) {
            return false;
        }
        snow.setLayers(snow.getLayers() + 1);
        block.setBlockData(snow, false);
        return true;
    }

    private boolean meltLayer(
            Block block,
            WinterSurfaceOwnershipLedger ownership
    ) {
        if (!(block.getBlockData() instanceof Snow snow)) {
            return false;
        }
        if (snow.getLayers() > 1) {
            snow.setLayers(snow.getLayers() - 1);
            block.setBlockData(snow, false);
            return true;
        }

        block.setType(Material.AIR, false);
        ownership.release(position(block));
        return true;
    }

    private WinterSurfacePosition position(Block block) {
        return WinterSurfacePosition.fromWorld(
                block.getX(),
                block.getY(),
                block.getZ()
        );
    }
}
