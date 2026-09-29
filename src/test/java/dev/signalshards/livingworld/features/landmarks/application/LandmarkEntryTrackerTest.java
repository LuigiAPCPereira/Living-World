package dev.signalshards.livingworld.features.landmarks.application;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LandmarkEntryTrackerTest {
    @Test
    void primeiraEntradaEhEmitidaEUmaPermanenciaEhSuprimida() {
        LandmarkEntryTracker tracker = new LandmarkEntryTracker();
        UUID player = UUID.randomUUID();
        ResolvedLandmark landmark = landmark("minecraft:village_plains");

        assertEquals(List.of(landmark), tracker.observe(
                player,
                List.of(landmark)
        ));
        assertTrue(tracker.observe(player, List.of(landmark)).isEmpty());
    }

    @Test
    void sobreposicaoEmiteSomenteONovoLandmark() {
        LandmarkEntryTracker tracker = new LandmarkEntryTracker();
        UUID player = UUID.randomUUID();
        ResolvedLandmark first = landmark("minecraft:mineshaft");
        ResolvedLandmark second = landmark("custom:ancient_ruins");

        tracker.observe(player, List.of(first));

        assertEquals(
                List.of(second),
                tracker.observe(player, List.of(first, second))
        );
    }

    @Test
    void sairEReentrarVoltaAEmitirEntrada() {
        LandmarkEntryTracker tracker = new LandmarkEntryTracker();
        UUID player = UUID.randomUUID();
        ResolvedLandmark landmark = landmark("minecraft:monument");

        tracker.observe(player, List.of(landmark));
        assertTrue(tracker.observe(player, List.of()).isEmpty());

        assertEquals(
                List.of(landmark),
                tracker.observe(player, List.of(landmark))
        );
    }

    @Test
    void jogadoresSaoIndependentesEQuitLimpaCache() {
        LandmarkEntryTracker tracker = new LandmarkEntryTracker();
        UUID firstPlayer = UUID.randomUUID();
        UUID secondPlayer = UUID.randomUUID();
        ResolvedLandmark landmark = landmark("minecraft:stronghold");

        tracker.observe(firstPlayer, List.of(landmark));
        tracker.observe(secondPlayer, List.of(landmark));
        assertEquals(2, tracker.trackedPlayerCount());

        tracker.forget(firstPlayer);
        assertEquals(1, tracker.trackedPlayerCount());

        assertEquals(
                List.of(landmark),
                tracker.observe(firstPlayer, List.of(landmark))
        );
    }

    @Test
    void duplicatasDaMesmaInstanciaNaoGeramEntradasDuplicadas() {
        LandmarkEntryTracker tracker = new LandmarkEntryTracker();
        UUID player = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        ResolvedLandmark first = new ResolvedLandmark(
                id,
                "minecraft:mineshaft"
        );
        ResolvedLandmark duplicate = new ResolvedLandmark(
                id,
                "minecraft:mineshaft"
        );

        assertEquals(
                List.of(first),
                tracker.observe(player, List.of(first, duplicate))
        );
    }

    @Test
    void disableLimpaTodoCache() {
        LandmarkEntryTracker tracker = new LandmarkEntryTracker();
        tracker.observe(
                UUID.randomUUID(),
                List.of(landmark("minecraft:fortress"))
        );
        tracker.observe(
                UUID.randomUUID(),
                List.of(landmark("minecraft:end_city"))
        );

        tracker.clear();

        assertEquals(0, tracker.trackedPlayerCount());
    }

    private ResolvedLandmark landmark(String structureKey) {
        return new ResolvedLandmark(UUID.randomUUID(), structureKey);
    }
}
