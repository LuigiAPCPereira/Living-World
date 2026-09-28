package dev.signalshards.livingworld.features.ecology.domain;

import dev.signalshards.livingworld.features.seasons.domain.Season;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeasonalEcologyModifierTest {
    private final SeasonalEcologyModifier modifier =
            new DefaultSeasonalEcologyModifier();

    @Test
    void primaveraFavoreceFloraMaisQueInverno() {
        assertTrue(
                modifier.cropMultiplier(Season.PRIMAVERA)
                        > modifier.cropMultiplier(Season.INVERNO)
        );
        assertTrue(
                modifier.grassMultiplier(Season.PRIMAVERA)
                        > modifier.grassMultiplier(Season.INVERNO)
        );
    }

    @Test
    void invernoReduzCrescimentoVegetal() {
        assertEquals(0.50D, modifier.cropMultiplier(Season.INVERNO));
        assertEquals(0.60D, modifier.treeMultiplier(Season.INVERNO));
        assertEquals(0.40D, modifier.grassMultiplier(Season.INVERNO));
    }

    @Test
    void todosOsValoresFicamNoLimiteEcologico() {
        for (Season season : Season.values()) {
            assertTrue(modifier.cropMultiplier(season) >= 0.0D);
            assertTrue(modifier.cropMultiplier(season) <= 1.5D);
            assertTrue(modifier.treeMultiplier(season) >= 0.0D);
            assertTrue(modifier.treeMultiplier(season) <= 1.5D);
            assertTrue(modifier.grassMultiplier(season) >= 0.0D);
            assertTrue(modifier.grassMultiplier(season) <= 1.5D);
        }
    }
}
