package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WaterExposureTest {
    @Test
    void aceitaContatoValido() {
        WaterExposure exposure = new WaterExposure(0.50D, 3.0D);
        assertEquals(0.50D, exposure.submergedFraction());
        assertEquals(3.0D, exposure.depthBlocks());
    }

    @Test
    void rejeitaFracaoOuProfundidadeInvalidas() {
        assertThrows(IllegalArgumentException.class, () -> new WaterExposure(-0.01D, 0.0D));
        assertThrows(IllegalArgumentException.class, () -> new WaterExposure(1.01D, 0.0D));
        assertThrows(IllegalArgumentException.class, () -> new WaterExposure(0.50D, -1.0D));
        assertThrows(IllegalArgumentException.class, () -> new WaterExposure(0.0D, 1.0D));
        assertThrows(IllegalArgumentException.class, () -> new WaterExposure(Double.NaN, 0.0D));
    }
}
