package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnvironmentalDimensionPolicyTest {
    private final EnvironmentalDimensionPolicy policy =
            new EnvironmentalDimensionPolicy();

    @Test
    void overworldPermiteSazonalidadeEcologiaENeve() {
        EnvironmentalDimensionProfile profile = policy.profileFor(
                EnvironmentalDimension.OVERWORLD
        );

        assertTrue(profile.seasonalityFactor() > 0.0D);
        assertTrue(profile.dayNightFactor() > 0.0D);
        assertTrue(profile.weatherFactor() > 0.0D);
        assertTrue(profile.terrestrialEcology());
        assertTrue(profile.frozenSurfaces());
    }

    @Test
    void netherEndECustomSaoConservadoresENaoTerrestres() {
        for (EnvironmentalDimension dimension : new EnvironmentalDimension[]{
                EnvironmentalDimension.NETHER,
                EnvironmentalDimension.END,
                EnvironmentalDimension.CUSTOM
        }) {
            EnvironmentalDimensionProfile profile = policy.profileFor(dimension);
            assertFalse(profile.terrestrialEcology());
            assertFalse(profile.frozenSurfaces());
            assertTrue(profile.seasonalityFactor() == 0.0D);
            assertTrue(profile.dayNightFactor() == 0.0D);
            assertTrue(profile.weatherFactor() == 0.0D);
        }
    }
}
