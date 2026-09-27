package dev.signalshards.livingworld.features.climate.domain;

import java.util.Objects;

/**
 * Perfil climático base de uma região/bioma.
 *
 * <p>A integração com Paper é responsável por converter o bioma observado em
 * um perfil. O domínio não depende de identificadores ou classes do servidor.</p>
 */
public record ClimateProfile(ThermalBand temperature, MoistureBand moisture) {
    public ClimateProfile {
        Objects.requireNonNull(temperature, "faixa térmica");
        Objects.requireNonNull(moisture, "faixa de umidade");
    }
}
