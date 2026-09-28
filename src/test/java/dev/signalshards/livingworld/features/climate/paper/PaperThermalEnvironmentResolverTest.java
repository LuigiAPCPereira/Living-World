package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.climate.application.ThermalEnvironmentContext;
import dev.signalshards.livingworld.features.climate.domain.AmbientTemperature;
import dev.signalshards.livingworld.features.climate.domain.ArmorThermalLoadout;
import dev.signalshards.livingworld.features.climate.domain.PlayerActivity;
import dev.signalshards.livingworld.features.climate.domain.PrecipitationExposure;
import dev.signalshards.livingworld.features.climate.domain.LocalHeatExposure;
import dev.signalshards.livingworld.features.climate.domain.LocalHeatSourceType;
import dev.signalshards.livingworld.features.climate.domain.ShelterFactor;
import dev.signalshards.livingworld.features.climate.domain.WaterExposure;
import dev.signalshards.livingworld.features.climate.domain.WindExposure;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperThermalEnvironmentResolverTest {
    @Test
    void primeiroSliceMantemVentoAbrigoECalorLocalEmBaselineExplicito() {
        ThermalEnvironmentContext context = PaperThermalEnvironmentResolver.compose(
                new AmbientTemperature(12.0D),
                new WaterExposure(0.50D, 0.0D),
                PrecipitationExposure.none(),
                PlayerActivity.SWIMMING,
                ArmorThermalLoadout.empty(),
                new WindExposure(0.40D),
                new ShelterFactor(0.25D),
                java.util.List.of(new LocalHeatExposure(
                        LocalHeatSourceType.TORCH,
                        1.0D,
                        1.0D,
                        1.0D
                ))
        );

        assertEquals(12.0D, context.ambientTemperature().degreesCelsius());
        assertEquals(0.50D, context.waterExposure().submergedFraction());
        assertEquals(PlayerActivity.SWIMMING, context.activity());
        assertEquals(0.40D, context.windExposure().level());
        assertEquals(0.25D, context.shelterFactor().level());
        assertEquals(1, context.localHeatExposures().size());
    }
}
