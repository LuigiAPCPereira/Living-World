package dev.signalshards.livingworld.features.ecology.application;

import dev.signalshards.livingworld.features.ecology.domain.WinterSurfaceKind;
import dev.signalshards.livingworld.features.ecology.domain.WinterSurfacePosition;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WinterSurfaceOwnershipLedgerTest {
    @Test
    void positionCompactaPreservaLocaisEYNegativo() {
        WinterSurfacePosition position =
                WinterSurfacePosition.fromWorld(-1, -32, 16);

        assertEquals(15, position.localX());
        assertEquals(0, position.localZ());
        assertEquals(position, WinterSurfacePosition.unpack(position.packed()));
    }

    @Test
    void ledgerRespeitaLimiteEPermiteAtualizarTipoExistente() {
        WinterSurfacePosition first = new WinterSurfacePosition(1, 64, 2);
        WinterSurfacePosition second = new WinterSurfacePosition(3, 65, 4);
        WinterSurfaceOwnershipLedger ledger =
                WinterSurfaceOwnershipLedger.empty(1);

        assertTrue(ledger.claim(first, WinterSurfaceKind.ICE));
        assertFalse(ledger.claim(second, WinterSurfaceKind.SNOW));
        assertTrue(ledger.claim(first, WinterSurfaceKind.SNOW));
        assertEquals(
                WinterSurfaceKind.SNOW,
                ledger.kindAt(first).orElseThrow()
        );
        assertEquals(1, ledger.size());
    }

    @Test
    void dirtyMudaSomenteQuandoOwnershipMuda() {
        WinterSurfacePosition position =
                new WinterSurfacePosition(5, 70, 6);
        WinterSurfaceOwnershipLedger ledger =
                new WinterSurfaceOwnershipLedger(
                        Map.of(position, WinterSurfaceKind.ICE),
                        4
                );

        assertFalse(ledger.isDirty());
        assertFalse(ledger.claim(position, WinterSurfaceKind.ICE));
        assertFalse(ledger.isDirty());

        assertTrue(ledger.release(position));
        assertTrue(ledger.isDirty());
        ledger.markClean();
        assertFalse(ledger.isDirty());
    }
}
