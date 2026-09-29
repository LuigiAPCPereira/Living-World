package dev.signalshards.livingworld.core.status;

import java.util.Map;
import java.util.Objects;

public record EnvironmentalPerformanceSnapshot(
        Map<EnvironmentalMetric, Long> values
) {
    public EnvironmentalPerformanceSnapshot {
        values = Map.copyOf(Objects.requireNonNull(values, "métricas"));
    }

    public long value(EnvironmentalMetric metric) {
        return values.getOrDefault(
                Objects.requireNonNull(metric, "métrica"),
                0L
        );
    }
}
