package dev.signalshards.livingworld.features.waystones.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class WaystoneIdTest {
    @Test
    void mesmaAncoraProduzIdentidadeEstavel() {
        UUID world = UUID.randomUUID();

        assertEquals(
                WaystoneId.fromAnchor(world, 10, 64, -20),
                WaystoneId.fromAnchor(world, 10, 64, -20)
        );
    }

    @Test
    void coordenadaOuMundoDiferenteProduzOutraIdentidade() {
        UUID world = UUID.randomUUID();

        assertNotEquals(
                WaystoneId.fromAnchor(world, 10, 64, -20),
                WaystoneId.fromAnchor(world, 11, 64, -20)
        );
        assertNotEquals(
                WaystoneId.fromAnchor(world, 10, 64, -20),
                WaystoneId.fromAnchor(UUID.randomUUID(), 10, 64, -20)
        );
    }
}
