package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WindExposureTest {
    @Test
    void calmEZerado() {
        assertEquals(0.0D, WindExposure.calm().level());
    }

    @Test
    void rejeitaNivelInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new WindExposure(-0.01D));
        assertThrows(IllegalArgumentException.class, () -> new WindExposure(1.01D));
    }
}
