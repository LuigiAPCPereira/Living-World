package dev.signalshards.livingworld.features.climate.application;

import dev.signalshards.livingworld.features.climate.domain.AmbientTemperature;
import dev.signalshards.livingworld.features.climate.domain.ArmorThermalLoadout;
import dev.signalshards.livingworld.features.climate.domain.LocalHeatExposure;
import dev.signalshards.livingworld.features.climate.domain.LocalHeatSourceType;
import dev.signalshards.livingworld.features.climate.domain.PlayerActivity;
import dev.signalshards.livingworld.features.climate.domain.ShelterFactor;
import dev.signalshards.livingworld.features.climate.domain.WaterExposure;
import dev.signalshards.livingworld.features.climate.domain.WindExposure;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThermalEnvironmentContextTest {
    @Test
    void copiaFontesLocaisParaSerImutavel() {
        var heat = new ArrayList<LocalHeatExposure>();
        heat.add(new LocalHeatExposure(LocalHeatSourceType.TORCH, 1.0D, 1.0D, 1.0D));
        ThermalEnvironmentContext context = new ThermalEnvironmentContext(
                new AmbientTemperature(20.0D),
                WaterExposure.none(),
                PlayerActivity.RESTING,
                WindExposure.calm(),
                ShelterFactor.exposed(),
                ArmorThermalLoadout.empty(),
                heat
        );

        heat.clear();
        assertEquals(1, context.localHeatExposures().size());
    }
}
