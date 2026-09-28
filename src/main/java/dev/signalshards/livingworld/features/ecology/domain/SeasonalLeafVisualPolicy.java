package dev.signalshards.livingworld.features.ecology.domain;

import dev.signalshards.livingworld.features.climate.domain.ClimateSnapshot;
import dev.signalshards.livingworld.features.climate.domain.MoistureBand;
import dev.signalshards.livingworld.features.climate.domain.ThermalBand;
import dev.signalshards.livingworld.features.seasons.domain.Season;

import java.util.Objects;

/**
 * Seleciona um efeito visual leve para a vegetação próxima de jogadores.
 *
 * <p>A chance reutiliza a adequação de crescimento de árvores para manter a
 * mesma leitura de clima e estação sem reproduzir essas regras.</p>
 */
public final class SeasonalLeafVisualPolicy {
    private static final double BASE_CHANCE = 0.35D;

    private final NaturalGrowthSuitabilityPolicy growthPolicy;
    private final SeasonalEcologyModifier seasonalModifier;

    public SeasonalLeafVisualPolicy(
            NaturalGrowthSuitabilityPolicy growthPolicy,
            SeasonalEcologyModifier seasonalModifier
    ) {
        this.growthPolicy = Objects.requireNonNull(
                growthPolicy,
                "política de crescimento"
        );
        this.seasonalModifier = Objects.requireNonNull(
                seasonalModifier,
                "modificador sazonal"
        );
    }

    public Effect select(ClimateSnapshot climate, Season season, double sample) {
        Objects.requireNonNull(climate, "clima");
        Objects.requireNonNull(season, "estação");
        validateSample(sample);

        Effect seasonalEffect = effectFor(climate, season);
        if (seasonalEffect == Effect.NONE) {
            return Effect.NONE;
        }

        double ecologicalChance = growthPolicy.acceptanceChance(
                climate,
                season,
                GrowthCategory.TREE,
                1.0D,
                seasonalModifier
        );
        double visualChance = BASE_CHANCE * Math.min(1.0D, ecologicalChance);
        return sample < visualChance ? seasonalEffect : Effect.NONE;
    }

    private Effect effectFor(ClimateSnapshot climate, Season season) {
        return switch (season) {
            case PRIMAVERA -> Effect.SPRING;
            case VERAO -> Effect.NONE;
            case OUTONO -> Effect.AUTUMN;
            case INVERNO -> supportsSnow(climate) ? Effect.SNOW : Effect.NONE;
        };
    }

    private boolean supportsSnow(ClimateSnapshot climate) {
        boolean coldEnough = climate.temperature() == ThermalBand.FRIO
                || climate.temperature() == ThermalBand.CONGELANTE;
        boolean humidEnough = climate.moisture() != MoistureBand.ARIDO
                && climate.moisture() != MoistureBand.SECO;
        return coldEnough && humidEnough;
    }

    private void validateSample(double sample) {
        if (!Double.isFinite(sample) || sample < 0.0D || sample >= 1.0D) {
            throw new IllegalArgumentException(
                    "A amostra visual deve ficar entre 0 (inclusivo) e 1 (exclusivo)"
            );
        }
    }

    public enum Effect {
        NONE,
        SPRING,
        AUTUMN,
        SNOW
    }
}
