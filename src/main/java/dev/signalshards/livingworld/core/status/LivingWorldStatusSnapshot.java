package dev.signalshards.livingworld.core.status;

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
        double averageTickTimeMillis
) {
}
