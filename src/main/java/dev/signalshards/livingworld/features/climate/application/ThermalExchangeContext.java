package dev.signalshards.livingworld.features.climate.application;

import dev.signalshards.livingworld.features.climate.domain.AmbientTemperature;
import dev.signalshards.livingworld.features.climate.domain.ArmorThermalLoadout;
import dev.signalshards.livingworld.features.climate.domain.LocalHeatExposure;
import dev.signalshards.livingworld.features.climate.domain.PlayerActivity;
import dev.signalshards.livingworld.features.climate.domain.PlayerThermalSnapshot;
import dev.signalshards.livingworld.features.climate.domain.PlayerThermalState;
import dev.signalshards.livingworld.features.climate.domain.ShelterFactor;
import dev.signalshards.livingworld.features.climate.domain.WaterExposure;
import dev.signalshards.livingworld.features.climate.domain.WetnessState;
import dev.signalshards.livingworld.features.climate.domain.WindExposure;

import java.util.List;
import java.util.Objects;

/**
 * Contexto imutável já resolvido para uma avaliação térmica instantânea.
 *
 * <p>Adapters Paper futuros convertem mundo/jogador em valores deste contexto.
 * A aplicação não faz scans, não mantém cache e não conhece Bukkit.</p>
 */
public record ThermalExchangeContext(
        PlayerThermalState thermalState,
        WetnessState wetness,
        AmbientTemperature ambientTemperature,
        WaterExposure waterExposure,
        PlayerActivity activity,
        WindExposure windExposure,
        ShelterFactor shelterFactor,
        ArmorThermalLoadout armorLoadout,
        List<LocalHeatExposure> localHeatExposures
) {
    public ThermalExchangeContext {
        Objects.requireNonNull(thermalState, "estado térmico");
        Objects.requireNonNull(wetness, "wetness");
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

    public ThermalExchangeContext withPlayerState(
            PlayerThermalState newThermalState,
            WetnessState newWetness
    ) {
        return new ThermalExchangeContext(
                newThermalState,
                newWetness,
                ambientTemperature,
                waterExposure,
                activity,
                windExposure,
                shelterFactor,
                armorLoadout,
                localHeatExposures
        );
    }

    public static ThermalExchangeContext from(
            PlayerThermalSnapshot snapshot,
            ThermalEnvironmentContext environment
    ) {
        Objects.requireNonNull(snapshot, "snapshot térmico");
        Objects.requireNonNull(environment, "contexto ambiental");
        return new ThermalExchangeContext(
                snapshot.thermalState(),
                snapshot.wetnessState(),
                environment.ambientTemperature(),
                environment.waterExposure(),
                environment.activity(),
                environment.windExposure(),
                environment.shelterFactor(),
                environment.armorLoadout(),
                environment.localHeatExposures()
        );
    }
}
