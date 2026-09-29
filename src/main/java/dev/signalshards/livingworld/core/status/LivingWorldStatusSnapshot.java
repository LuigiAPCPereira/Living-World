package dev.signalshards.livingworld.core.status;

import java.util.Map;

public record LivingWorldStatusSnapshot(
        String version,
        String worldName,
        String seasonName,
        long year,
        int month,
        int day,
        int onlinePlayers,
        int hudSessions,
        int desireLineCachedChunks,
        int registeredWaystones,
        double tpsOneMinute,
        double averageTickTimeMillis,
        EnvironmentalPerformanceSnapshot environmentalPerformance
) {
    public LivingWorldStatusSnapshot(
            String version,
            String worldName,
            String seasonName,
            long year,
            int month,
            int day,
            int onlinePlayers,
            int hudSessions,
            int desireLineCachedChunks,
            int registeredWaystones,
            double tpsOneMinute,
            double averageTickTimeMillis
    ) {
        this(
                version,
                worldName,
                seasonName,
                year,
                month,
                day,
                onlinePlayers,
                hudSessions,
                desireLineCachedChunks,
                registeredWaystones,
                tpsOneMinute,
                averageTickTimeMillis,
                new EnvironmentalPerformanceSnapshot(Map.of())
        );
    }
}
