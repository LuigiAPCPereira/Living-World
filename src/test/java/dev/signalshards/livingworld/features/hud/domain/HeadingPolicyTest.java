package dev.signalshards.livingworld.features.hud.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HeadingPolicyTest {
    private final HeadingPolicy policy = new HeadingPolicy();

    @Test
    void segueSemanticaDeYawDoMinecraft() {
        assertEquals(CardinalDirection.SUL, policy.directionFor(0));
        assertEquals(CardinalDirection.SUDOESTE, policy.directionFor(45));
        assertEquals(CardinalDirection.OESTE, policy.directionFor(90));
        assertEquals(CardinalDirection.NOROESTE, policy.directionFor(135));
        assertEquals(CardinalDirection.NORTE, policy.directionFor(180));
        assertEquals(CardinalDirection.NORDESTE, policy.directionFor(-135));
        assertEquals(CardinalDirection.LESTE, policy.directionFor(-90));
        assertEquals(CardinalDirection.SUDESTE, policy.directionFor(-45));
    }
}
