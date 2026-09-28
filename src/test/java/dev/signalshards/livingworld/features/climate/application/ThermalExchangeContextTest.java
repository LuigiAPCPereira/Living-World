package dev.signalshards.livingworld.features.climate.application;

import dev.signalshards.livingworld.features.climate.domain.AmbientTemperature;
import dev.signalshards.livingworld.features.climate.domain.ArmorThermalLoadout;
import dev.signalshards.livingworld.features.climate.domain.LocalHeatExposure;
import dev.signalshards.livingworld.features.climate.domain.LocalHeatSourceType;
import dev.signalshards.livingworld.features.climate.domain.PlayerActivity;
import dev.signalshards.livingworld.features.climate.domain.PlayerThermalState;
import dev.signalshards.livingworld.features.climate.domain.ShelterFactor;
import dev.signalshards.livingworld.features.climate.domain.WaterExposure;
import dev.signalshards.livingworld.features.climate.domain.WetnessState;
import dev.signalshards.livingworld.features.climate.domain.WindExposure;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThermalExchangeContextTest {
    @Test
    void copiaListaDeFontesParaManterContextoImutavel() {
        var sources = new ArrayList<LocalHeatExposure>();
        sources.add(new LocalHeatExposure(LocalHeatSourceType.TORCH, 1.0D, 1.0D, 1.0D));
        ThermalExchangeContext context = new ThermalExchangeContext(
                PlayerThermalState.neutral(),
                WetnessState.dry(),
                new AmbientTemperature(20.0D),
                WaterExposure.none(),
                PlayerActivity.RESTING,
                WindExposure.calm(),
                ShelterFactor.exposed(),
                ArmorThermalLoadout.empty(),
                sources
        );

        sources.clear();
        assertEquals(1, context.localHeatExposures().size());
    }
}
