package dev.signalshards.livingworld.features.hud.paper;

import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemperatureColorPolicyTest {
    private final TemperatureColorPolicy policy = new TemperatureColorPolicy();

    @Test
    void coloreTemperaturaPorFaixa() {
        assertEquals(NamedTextColor.BLUE, policy.colorFor(-5));
        assertEquals(NamedTextColor.AQUA, policy.colorFor(5));
        assertEquals(NamedTextColor.GREEN, policy.colorFor(15));
        assertEquals(NamedTextColor.YELLOW, policy.colorFor(25));
        assertEquals(NamedTextColor.GOLD, policy.colorFor(35));
        assertEquals(NamedTextColor.RED, policy.colorFor(45));
    }
}
