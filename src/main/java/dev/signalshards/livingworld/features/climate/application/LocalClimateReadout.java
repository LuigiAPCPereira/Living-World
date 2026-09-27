package dev.signalshards.livingworld.features.climate.application;

import dev.signalshards.livingworld.features.climate.domain.MoistureBand;
import dev.signalshards.livingworld.features.climate.domain.ThermalBand;
import dev.signalshards.livingworld.features.seasons.domain.Season;

import java.util.Objects;

public record LocalClimateReadout(
        Season season,
        int apparentCelsius,
        ThermalBand thermalBand,
        MoistureBand moistureBand,
        ClimateRuleReadout cropGrowth,
        ClimateRuleReadout treeGrowth,
        ClimateRuleReadout grassSpread,
        ClimateRuleReadout farmlandRetention,
        ClimateRuleReadout fireSpread,
        boolean frozenSurfacesEnabled,
        boolean frozenSurfacesPersist
) {
    public LocalClimateReadout {
        Objects.requireNonNull(season, "estação");
        Objects.requireNonNull(thermalBand, "faixa térmica");
        Objects.requireNonNull(moistureBand, "faixa de umidade");
        Objects.requireNonNull(cropGrowth, "crescimento de plantações");
        Objects.requireNonNull(treeGrowth, "crescimento de árvores");
        Objects.requireNonNull(grassSpread, "propagação de grama");
        Objects.requireNonNull(farmlandRetention, "retenção de farmland");
        Objects.requireNonNull(fireSpread, "propagação de fogo");
    }
}
