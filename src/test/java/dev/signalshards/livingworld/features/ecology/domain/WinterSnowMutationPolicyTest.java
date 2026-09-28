package dev.signalshards.livingworld.features.ecology.domain;

import dev.signalshards.livingworld.features.climate.domain.EnvironmentalDimension;
import dev.signalshards.livingworld.features.climate.domain.EnvironmentalDimensionPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WinterSnowMutationPolicyTest {
    private final WinterSnowMutationPolicy policy =
            new WinterSnowMutationPolicy(PhysicalSnowSettings.defaults());
    private final EnvironmentalDimensionPolicy dimensions =
            new EnvironmentalDimensionPolicy();

    @Test
    void freezingComPrecipitacaoColocaEAcumulaAteOCap() {
        var overworld = dimensions.profileFor(
                EnvironmentalDimension.OVERWORLD
        );

        assertEquals(
                WinterSnowMutation.PLACE_SNOW,
                policy.decide(
                        WinterThermalPhase.FREEZING,
                        overworld,
                        true,
                        WinterSnowTarget.EXPOSED_SUPPORT,
                        0
                )
        );
        assertEquals(
                WinterSnowMutation.ADD_SNOW_LAYER,
                policy.decide(
                        WinterThermalPhase.FREEZING,
                        overworld,
                        true,
                        WinterSnowTarget.OWNED_SNOW,
                        3
                )
        );
        assertEquals(
                WinterSnowMutation.NONE,
                policy.decide(
                        WinterThermalPhase.FREEZING,
                        overworld,
                        true,
                        WinterSnowTarget.OWNED_SNOW,
                        4
                )
        );
    }

    @Test
    void frioSemPrecipitacaoPreservaMasNaoAcumula() {
        var overworld = dimensions.profileFor(
                EnvironmentalDimension.OVERWORLD
        );

        assertEquals(
                WinterSnowMutation.NONE,
                policy.decide(
                        WinterThermalPhase.FREEZING,
                        overworld,
                        false,
                        WinterSnowTarget.EXPOSED_SUPPORT,
                        0
                )
        );
        assertEquals(
                WinterSnowMutation.NONE,
                policy.decide(
                        WinterThermalPhase.FREEZING,
                        overworld,
                        false,
                        WinterSnowTarget.OWNED_SNOW,
                        2
                )
        );
    }

    @Test
    void thawDerreteSomenteSnowOwned() {
        var overworld = dimensions.profileFor(
                EnvironmentalDimension.OVERWORLD
        );

        assertEquals(
                WinterSnowMutation.MELT_SNOW_LAYER,
                policy.decide(
                        WinterThermalPhase.THAWING,
                        overworld,
                        false,
                        WinterSnowTarget.OWNED_SNOW,
                        2
                )
        );
        assertEquals(
                WinterSnowMutation.NONE,
                policy.decide(
                        WinterThermalPhase.THAWING,
                        overworld,
                        false,
                        WinterSnowTarget.OTHER,
                        1
                )
        );
    }

    @Test
    void dimensaoNaoTerrestreNaoCriaNeveELimpaOwned() {
        var end = dimensions.profileFor(EnvironmentalDimension.END);

        assertEquals(
                WinterSnowMutation.NONE,
                policy.decide(
                        WinterThermalPhase.FREEZING,
                        end,
                        true,
                        WinterSnowTarget.EXPOSED_SUPPORT,
                        0
                )
        );
        assertEquals(
                WinterSnowMutation.MELT_SNOW_LAYER,
                policy.decide(
                        WinterThermalPhase.HOLDING,
                        end,
                        false,
                        WinterSnowTarget.OWNED_SNOW,
                        1
                )
        );
    }
}
