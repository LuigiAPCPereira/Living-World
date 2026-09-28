package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShelterFactorTest {
    @Test
    void exposedEZerado() {
        assertEquals(0.0D, ShelterFactor.exposed().level());
    }

    @Test
    void rejeitaNivelInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new ShelterFactor(-0.01D));
        assertThrows(IllegalArgumentException.class, () -> new ShelterFactor(1.01D));
    }
}
