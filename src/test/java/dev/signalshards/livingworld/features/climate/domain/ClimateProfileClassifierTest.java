package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClimateProfileClassifierTest {
    private final ClimateProfileClassifier classifier = new ClimateProfileClassifier(
            ClimateProfileThresholds.livingWorldDefaults()
    );

    @Test
    void classificaExtremosSemDependerDeNomeDeBioma() {
        assertEquals(
                new ClimateProfile(ThermalBand.CONGELANTE, MoistureBand.ARIDO),
                classifier.classify(-0.5, 0.0)
        );
        assertEquals(
                new ClimateProfile(ThermalBand.ESCALDANTE, MoistureBand.ENCHARCADO),
                classifier.classify(2.0, 1.0)
        );
    }

    @Test
    void classificaFaixasIntermediarias() {
        assertEquals(
                new ClimateProfile(ThermalBand.FRIO, MoistureBand.SECO),
                classifier.classify(0.30, 0.20)
        );
        assertEquals(
                new ClimateProfile(ThermalBand.TEMPERADO, MoistureBand.EQUILIBRADO),
                classifier.classify(0.80, 0.50)
        );
        assertEquals(
                new ClimateProfile(ThermalBand.QUENTE, MoistureBand.UMIDO),
                classifier.classify(1.10, 0.75)
        );
    }

    @Test
    void rejeitaValoresNaoFinitos() {
        assertThrows(
                IllegalArgumentException.class,
                () -> classifier.classify(Double.NaN, 0.5)
        );
    }

    @Test
    void rejeitaThresholdsForaDeOrdem() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ClimateProfileThresholds(
                        0.5, 0.4, 0.9, 1.5,
                        0.1, 0.35, 0.65, 0.9
                )
        );
    }
}
