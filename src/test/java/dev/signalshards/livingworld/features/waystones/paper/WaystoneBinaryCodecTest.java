package dev.signalshards.livingworld.features.waystones.paper;

import dev.signalshards.livingworld.features.waystones.domain.Waystone;
import dev.signalshards.livingworld.features.waystones.domain.WaystoneId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WaystoneBinaryCodecTest {
    private final WaystoneBinaryCodec codec = new WaystoneBinaryCodec();

    @Test
    void roundTripPreservaDados() {
        Waystone waystone = new Waystone(
                WaystoneId.random(),
                "Vila do Vale",
                UUID.randomUUID(),
                -120,
                70,
                342
        );

        assertEquals(
                waystone,
                codec.decode(waystone.id(), waystone.worldId(), codec.encode(waystone))
        );
    }

    @Test
    void rejeitaVersaoDesconhecida() {
        assertThrows(
                IllegalStateException.class,
                () -> codec.decode(
                        WaystoneId.random(),
                        UUID.randomUUID(),
                        new byte[]{99}
                )
        );
    }
}
