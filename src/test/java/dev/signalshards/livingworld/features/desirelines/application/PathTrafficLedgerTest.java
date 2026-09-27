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

    @Test
    void recuperaSomentePosicoesNaoUsadasNoDia() {
        PathTrafficLedger ledger = new PathTrafficLedger(
                Map.of(1, 12, 2, 12),
                10,
                24
        );

        ledger.record(1);
        var changes = ledger.decayUntouched(1);

        assertEquals(13, ledger.snapshot().get(1));
        assertEquals(11, ledger.snapshot().get(2));
        assertEquals(1, changes.size());
        assertEquals(2, changes.getFirst().positionKey());
    }

    @Test
    void saltoDeVariosDiasProtegeApenasODiaComTrafego() {
        PathTrafficLedger ledger = new PathTrafficLedger(Map.of(1, 12), 10, 24);
        ledger.record(1);

        ledger.decayUntouched(3);

        assertEquals(11, ledger.snapshot().get(1));
    }

    @Test
    void removeEntradaQuandoRecuperaAteZero() {
        PathTrafficLedger ledger = new PathTrafficLedger(Map.of(1, 1), 10, 24);

        ledger.decayUntouched(1);

        assertFalse(ledger.contains(1));
    }
}
