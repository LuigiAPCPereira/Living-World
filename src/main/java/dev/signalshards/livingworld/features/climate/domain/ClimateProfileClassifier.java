package dev.signalshards.livingworld.features.climate.domain;

import java.util.Objects;

public final class ClimateProfileClassifier {
    private final ClimateProfileThresholds thresholds;

    public ClimateProfileClassifier(ClimateProfileThresholds thresholds) {
        this.thresholds = Objects.requireNonNull(thresholds, "limites climáticos");
    }

    public ClimateProfile classify(double temperature, double humidity) {
        if (!Double.isFinite(temperature) || !Double.isFinite(humidity)) {
            throw new IllegalArgumentException("Temperatura e umidade devem ser valores finitos");
        }

        return new ClimateProfile(
                thermalBand(temperature),
                moistureBand(humidity)
        );
    }

    private ThermalBand thermalBand(double value) {
        if (value < thresholds.freezingMax()) {
            return ThermalBand.CONGELANTE;
        }
        if (value < thresholds.coldMax()) {
            return ThermalBand.FRIO;
        }
        if (value < thresholds.temperateMax()) {
            return ThermalBand.TEMPERADO;
        }
        if (value < thresholds.hotMax()) {
            return ThermalBand.QUENTE;
        }
        return ThermalBand.ESCALDANTE;
    }

    private MoistureBand moistureBand(double value) {
        if (value < thresholds.aridMax()) {
            return MoistureBand.ARIDO;
        }
        if (value < thresholds.dryMax()) {
            return MoistureBand.SECO;
        }
        if (value < thresholds.balancedMax()) {
            return MoistureBand.EQUILIBRADO;
        }
        if (value < thresholds.humidMax()) {
            return MoistureBand.UMIDO;
        }
        return MoistureBand.ENCHARCADO;
    }
}
