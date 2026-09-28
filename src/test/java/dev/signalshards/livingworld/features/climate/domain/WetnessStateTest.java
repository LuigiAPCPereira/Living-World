package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WetnessStateTest {
    @Test
    void ajusteSaturaEntreSecoEMolhado() {
        assertEquals(1.0D, new WetnessState(0.9D).adjustedBy(0.5D).level());
        assertEquals(0.0D, new WetnessState(0.1D).adjustedBy(-0.5D).level());
    }

    @Test
    void rejeitaNivelInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new WetnessState(-0.01D));
        assertThrows(IllegalArgumentException.class, () -> new WetnessState(1.01D));
        assertThrows(IllegalArgumentException.class, () -> new WetnessState(Double.NaN));
    }
}
