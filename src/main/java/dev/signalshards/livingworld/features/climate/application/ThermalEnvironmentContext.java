package dev.signalshards.livingworld.features.climate.application;

import dev.signalshards.livingworld.features.climate.domain.AmbientTemperature;
import dev.signalshards.livingworld.features.climate.domain.ArmorThermalLoadout;
import dev.signalshards.livingworld.features.climate.domain.LocalHeatExposure;
import dev.signalshards.livingworld.features.climate.domain.PlayerActivity;
import dev.signalshards.livingworld.features.climate.domain.ShelterFactor;
import dev.signalshards.livingworld.features.climate.domain.WaterExposure;
import dev.signalshards.livingworld.features.climate.domain.WindExposure;

import java.util.List;
import java.util.Objects;

/**
 * Parte ambiental/observável do contexto térmico, sem estado corporal.
 *
 * <p>É a fronteira esperada para futuros resolvers Paper.</p>
 */
public record ThermalEnvironmentContext(
        AmbientTemperature ambientTemperature,
        WaterExposure waterExposure,
        PlayerActivity activity,
        WindExposure windExposure,
        ShelterFactor shelterFactor,
        ArmorThermalLoadout armorLoadout,
        List<LocalHeatExposure> localHeatExposures
) {
    public ThermalEnvironmentContext {
        Objects.requireNonNull(ambientTemperature, "temperatura ambiente");
        Objects.requireNonNull(waterExposure, "exposição à água");
        Objects.requireNonNull(activity, "atividade");
        Objects.requireNonNull(windExposure, "vento");
        Objects.requireNonNull(shelterFactor, "abrigo");
        Objects.requireNonNull(armorLoadout, "armadura");
        localHeatExposures = List.copyOf(
                Objects.requireNonNull(localHeatExposures, "fontes locais de calor")
        );
    }
}
