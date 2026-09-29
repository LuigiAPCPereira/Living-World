package dev.signalshards.livingworld.core.status;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnvironmentalPerformanceMetricsTest {
    @Test
    void snapshotEhCumulativoEIncluiZeros() {
        EnvironmentalPerformanceMetrics metrics =
                new EnvironmentalPerformanceMetrics();

        metrics.increment(EnvironmentalMetric.WINTER_PROBES);
        metrics.add(EnvironmentalMetric.WINTER_PROBES, 3L);
        metrics.add(EnvironmentalMetric.WINTER_UPDATE_NANOS, 250L);

        EnvironmentalPerformanceSnapshot snapshot = metrics.snapshot();

        assertEquals(4L, snapshot.value(EnvironmentalMetric.WINTER_PROBES));
        assertEquals(
                250L,
                snapshot.value(EnvironmentalMetric.WINTER_UPDATE_NANOS)
        );
        assertEquals(
                0L,
                snapshot.value(EnvironmentalMetric.THERMAL_PLAYER_UPDATES)
        );
    }

    @Test
    void rejeitaIncrementoNegativo() {
        EnvironmentalPerformanceMetrics metrics =
                new EnvironmentalPerformanceMetrics();

        assertThrows(
                IllegalArgumentException.class,
                () -> metrics.add(EnvironmentalMetric.WINTER_PROBES, -1L)
        );
    }
}
