package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalHeatSourceCatalogTest {
    @Test
    void defaultsPreservamPapeisDeGameplay() {
        LocalHeatSourceCatalog catalog = LocalHeatSourceCatalog.livingWorldDefaults();
        assertTrue(
                catalog.profileFor(LocalHeatSourceType.TORCH).peakLoadPerSecond()
                        < catalog.profileFor(LocalHeatSourceType.CAMPFIRE).peakLoadPerSecond()
        );
        assertTrue(
                catalog.profileFor(LocalHeatSourceType.CAMPFIRE).peakLoadPerSecond()
                        < catalog.profileFor(LocalHeatSourceType.LAVA).peakLoadPerSecond()
        );
    }
}
