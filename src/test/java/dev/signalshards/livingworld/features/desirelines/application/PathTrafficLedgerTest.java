package dev.signalshards.livingworld.features.desirelines.application;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathTrafficLedgerTest {
    @Test
    void saturaScoreNoMaximoConfigurado() {
        PathTrafficLedger ledger = PathTrafficLedger.empty(10, 3);

        assertEquals(1, ledger.record(100));
        assertEquals(2, ledger.record(100));
        assertEquals(3, ledger.record(100));
        assertEquals(3, ledger.record(100));
        assertEquals(3, ledger.snapshot().get(100));
    }

    @Test
    void limitaQuantidadeDePosicoesRastreadas() {
        PathTrafficLedger ledger = PathTrafficLedger.empty(2, 10);

        assertEquals(1, ledger.record(1));
        assertEquals(1, ledger.record(2));
        assertEquals(0, ledger.record(3));
        assertEquals(2, ledger.snapshot().size());
    }

    @Test
    void dirtySoMudaQuandoEstadoMuda() {
        PathTrafficLedger ledger = new PathTrafficLedger(Map.of(1, 3), 10, 3);

        assertFalse(ledger.isDirty());
        ledger.record(1);
        assertFalse(ledger.isDirty());

        ledger.record(2);
        assertTrue(ledger.isDirty());
        ledger.markClean();
        assertFalse(ledger.isDirty());
    }
}
