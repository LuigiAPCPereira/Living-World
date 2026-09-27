package dev.signalshards.livingworld.features.waystones.application;

import dev.signalshards.livingworld.features.waystones.domain.Waystone;
import dev.signalshards.livingworld.features.waystones.domain.WaystoneId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaystoneAnchorIndexTest {
    @Test
    void agrupaPorChunkSemQuebrarCoordenadasNegativas() {
        UUID world = UUID.randomUUID();
        Waystone negative = waystone(world, -1, -17);
        Waystone positive = waystone(world, 16, 31);
        WaystoneAnchorIndex index = new WaystoneAnchorIndex();

        index.rebuild(List.of(negative, positive));

        assertEquals(List.of(negative), index.inChunk(world, -1, -2));
        assertEquals(List.of(positive), index.inChunk(world, 1, 1));
        assertEquals(-1, WaystoneAnchorIndex.chunkCoordinate(-1));
        assertEquals(-2, WaystoneAnchorIndex.chunkCoordinate(-17));
    }

    @Test
    void removeDoIndiceSemAfetarOutrasWaystones() {
        UUID world = UUID.randomUUID();
        Waystone first = waystone(world, 1, 1);
        Waystone second = waystone(world, 2, 2);
        WaystoneAnchorIndex index = new WaystoneAnchorIndex();
        index.rebuild(List.of(first, second));

        assertTrue(index.remove(first.id()));
        assertEquals(List.of(second), index.inChunk(world, 0, 0));
    }

    private Waystone waystone(UUID world, int x, int z) {
        return new Waystone(
                WaystoneId.random(),
                "Teste " + x + "," + z,
                world,
                x,
                64,
                z
        );
    }
}
