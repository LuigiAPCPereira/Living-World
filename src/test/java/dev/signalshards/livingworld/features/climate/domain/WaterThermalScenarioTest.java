package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaterThermalScenarioTest {
    @Test
    void quedaEmAguaGeladaMolhaEEsfriaProgressivamente() {
        WaterExposurePolicy exposurePolicy = new WaterExposurePolicy();
        WaterTemperaturePolicy temperaturePolicy = new WaterTemperaturePolicy();
        WaterThermalExchangePolicy exchangePolicy = new WaterThermalExchangePolicy();
        WetnessPolicy wetnessPolicy = new WetnessPolicy();
        PlayerThermalPolicy thermalPolicy = new PlayerThermalPolicy();

        WaterExposure exposure = new WaterExposure(1.0D, 0.0D);
        WaterExposureEffect effect = exposurePolicy.evaluate(exposure);
        WaterTemperature waterTemperature = temperaturePolicy.temperature(
                new AmbientTemperature(-10.0D),
                exposure.depthBlocks()
        );

        WetnessState wetness = WetnessState.dry();
        PlayerThermalState thermal = PlayerThermalState.neutral();
        for (int second = 0; second < 10; second++) {
            wetness = wetnessPolicy.advance(
                    wetness,
                    effect.wetnessRate(),
                    Duration.ofSeconds(1)
            );
            thermal = thermalPolicy.advance(
                    thermal,
                    exchangePolicy.exchangeRate(
                            thermal,
                            waterTemperature,
                            effect.thermalTransferFactor()
                    ),
                    Duration.ofSeconds(1)
            );
        }

        assertEquals(0.0D, waterTemperature.degreesCelsius());
        assertEquals(1.0D, wetness.level());
        assertTrue(thermal.thermalLoad() < -0.50D);
        assertEquals(PlayerThermalBand.FREEZING, thermalPolicy.bandFor(thermal));
    }

    @Test
    void exposicaoParcialEsfriaMaisDevagarQueSubmersaoTotal() {
        WaterExposurePolicy exposurePolicy = new WaterExposurePolicy();
        WaterThermalExchangePolicy exchangePolicy = new WaterThermalExchangePolicy();
        WaterTemperature water = new WaterTemperature(0.0D);

        ThermalExchangeRate feet = exchangePolicy.exchangeRate(
                PlayerThermalState.neutral(),
                water,
                exposurePolicy.evaluate(new WaterExposure(0.15D, 0.0D))
                        .thermalTransferFactor()
        );
        ThermalExchangeRate full = exchangePolicy.exchangeRate(
                PlayerThermalState.neutral(),
                water,
                exposurePolicy.evaluate(new WaterExposure(1.0D, 0.0D))
                        .thermalTransferFactor()
        );

        assertTrue(Math.abs(feet.loadPerSecond()) < Math.abs(full.loadPerSecond()));
    }
}
