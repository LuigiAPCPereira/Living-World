package dev.signalshards.livingworld.features.climate.domain;

import java.util.Objects;

/**
 * Combina molhamento por água/precipitação e secagem ambiental.
 *
 * <p>Água e chuva adicionam wetness. Fora de exposição líquida, o jogador
 * seca gradualmente; vento, calor ambiente e fontes locais aceleram a secagem.</p>
 */
public final class WetnessEnvironmentPolicy {
    private final WetnessEnvironmentSettings settings;

    public WetnessEnvironmentPolicy() {
        this(WetnessEnvironmentSettings.livingWorldDefaults());
    }

    public WetnessEnvironmentPolicy(WetnessEnvironmentSettings settings) {
        this.settings = Objects.requireNonNull(settings, "configuração de wetness");
    }

    public WetnessRate exchangeRate(
            WetnessRate waterRate,
            PrecipitationExposure precipitation,
            AmbientTemperature ambientTemperature,
            WindExposure windExposure,
            ThermalExchangeRate localHeatRate,
            WetnessState currentWetness
    ) {
        Objects.requireNonNull(waterRate, "wetness da água");
        Objects.requireNonNull(precipitation, "precipitação");
        Objects.requireNonNull(ambientTemperature, "temperatura ambiente");
        Objects.requireNonNull(windExposure, "vento");
        Objects.requireNonNull(localHeatRate, "calor local");
        Objects.requireNonNull(currentWetness, "wetness atual");

        double precipitationMultiplier = ambientTemperature.degreesCelsius() <= 0.0D
                ? settings.coldPrecipitationMultiplier()
                : 1.0D;
        double precipitationRate = settings.precipitationWetnessPerSecond()
                * precipitation.level()
                * precipitationMultiplier;
        double wettingRate = Math.max(0.0D, waterRate.levelPerSecond())
                + precipitationRate;

        double dryingRate = dryingRate(
                ambientTemperature,
                windExposure,
                localHeatRate
        );

        double net = wettingRate - dryingRate;
        if (currentWetness.level() == 0.0D && net < 0.0D) {
            return WetnessRate.neutral();
        }
        return new WetnessRate(net);
    }

    private double dryingRate(
            AmbientTemperature ambientTemperature,
            WindExposure windExposure,
            ThermalExchangeRate localHeatRate
    ) {
        double warmth = Math.clamp(
                (ambientTemperature.degreesCelsius() - settings.warmDryingStartCelsius())
                        / (settings.warmDryingFullCelsius()
                        - settings.warmDryingStartCelsius()),
                0.0D,
                1.0D
        );
        double localHeat = Math.clamp(
                Math.max(0.0D, localHeatRate.loadPerSecond())
                        / settings.localHeatForMaxDryingBonus(),
                0.0D,
                1.0D
        );
        double multiplier = 1.0D
                + (windExposure.level() * settings.windDryingBonus())
                + (warmth * settings.warmDryingBonus())
                + (localHeat * settings.localHeatDryingBonus());
        return settings.baseDryingPerSecond() * multiplier;
    }
}
