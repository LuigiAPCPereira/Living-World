package dev.signalshards.livingworld.core.status;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;

/**
 * Contadores cumulativos thread-safe; snapshots permitem comparar deltas.
 */
public final class EnvironmentalPerformanceMetrics {
    private final EnumMap<EnvironmentalMetric, LongAdder> counters =
            new EnumMap<>(EnvironmentalMetric.class);

    public EnvironmentalPerformanceMetrics() {
        for (EnvironmentalMetric metric : EnvironmentalMetric.values()) {
            counters.put(metric, new LongAdder());
        }
    }

    public void increment(EnvironmentalMetric metric) {
        add(metric, 1L);
    }

    public void add(EnvironmentalMetric metric, long amount) {
        if (amount < 0L) {
            throw new IllegalArgumentException(
                    "Métrica cumulativa não aceita valor negativo"
            );
        }
        counters.get(
                Objects.requireNonNull(metric, "métrica")
        ).add(amount);
    }

    public EnvironmentalPerformanceSnapshot snapshot() {
        Map<EnvironmentalMetric, Long> values =
                new EnumMap<>(EnvironmentalMetric.class);
        counters.forEach((metric, counter) -> values.put(metric, counter.sum()));
        return new EnvironmentalPerformanceSnapshot(values);
    }
}
