package dev.signalshards.livingworld.features.hud.paper;

import net.kyori.adventure.text.format.NamedTextColor;

public final class TemperatureColorPolicy {
    public NamedTextColor colorFor(int celsius) {
        if (celsius <= 0) {
            return NamedTextColor.BLUE;
        }
        if (celsius <= 10) {
            return NamedTextColor.AQUA;
        }
        if (celsius <= 20) {
            return NamedTextColor.GREEN;
        }
        if (celsius <= 30) {
            return NamedTextColor.YELLOW;
        }
        if (celsius <= 39) {
            return NamedTextColor.GOLD;
        }
        return NamedTextColor.RED;
    }
}
