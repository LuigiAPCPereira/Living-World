package dev.signalshards.livingworld.features.ecology.domain;

import dev.signalshards.livingworld.features.climate.domain.EnvironmentalDimension;
import dev.signalshards.livingworld.features.climate.domain.EnvironmentalDimensionPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WinterSurfaceMutationPolicyTest {
    private final WinterSurfaceMutationPolicy policy =
            new WinterSurfaceMutationPolicy();
    private final EnvironmentalDimensionPolicy dimensions =
            new EnvironmentalDimensionPolicy();

    @Test
    void freezingSoCongelaAguaSourceExpostaNoOverworld() {
        var overworld = dimensions.profileFor(
                EnvironmentalDimension.OVERWORLD
        );

        assertEquals(
                WinterSurfaceMutation.FREEZE_WATER,
                policy.decide(
                        WinterThermalPhase.FREEZING,
                        overworld,
                        WinterSurfaceTarget.EXPOSED_SOURCE_WATER
                )
        );
        assertEquals(
                WinterSurfaceMutation.NONE,
                policy.decide(
                        WinterThermalPhase.FREEZING,
                        overworld,
                        WinterSurfaceTarget.OTHER
                )
        );
    }

    @Test
    void holdingNuncaMudaSuperficie() {
        var overworld = dimensions.profileFor(
                EnvironmentalDimension.OVERWORLD
        );

        assertEquals(
                WinterSurfaceMutation.NONE,
                policy.decide(
                        WinterThermalPhase.HOLDING,
                        overworld,
                        WinterSurfaceTarget.EXPOSED_SOURCE_WATER
                )
        );
        assertEquals(
                WinterSurfaceMutation.NONE,
                policy.decide(
                        WinterThermalPhase.HOLDING,
                        overworld,
                        WinterSurfaceTarget.OWNED_ICE
                )
        );
    }

    @Test
    void thawSoReverteIceComOwnership() {
        var overworld = dimensions.profileFor(
                EnvironmentalDimension.OVERWORLD
        );

        assertEquals(
                WinterSurfaceMutation.THAW_OWNED_ICE,
                policy.decide(
                        WinterThermalPhase.THAWING,
                        overworld,
                        WinterSurfaceTarget.OWNED_ICE
                )
        );
        assertEquals(
                WinterSurfaceMutation.NONE,
                policy.decide(
                        WinterThermalPhase.THAWING,
                        overworld,
                        WinterSurfaceTarget.OTHER
                )
        );
    }

    @Test
    void dimensaoNaoTerrestreNuncaCongelaELimpaOwnedIce() {
        var nether = dimensions.profileFor(EnvironmentalDimension.NETHER);

        assertEquals(
                WinterSurfaceMutation.NONE,
                policy.decide(
                        WinterThermalPhase.FREEZING,
                        nether,
                        WinterSurfaceTarget.EXPOSED_SOURCE_WATER
                )
        );
        assertEquals(
                WinterSurfaceMutation.THAW_OWNED_ICE,
                policy.decide(
                        WinterThermalPhase.HOLDING,
                        nether,
                        WinterSurfaceTarget.OWNED_ICE
                )
        );
    }
}
