package dev.signalshards.livingworld.features.hud.paper;

import dev.signalshards.livingworld.core.i18n.MessageCatalog;
import dev.signalshards.livingworld.features.climate.application.ThermalRuntimeReadout;
import dev.signalshards.livingworld.features.climate.application.ThermalRuntimeReadoutProvider;
import dev.signalshards.livingworld.features.climate.domain.BreathFeedback;
import dev.signalshards.livingworld.features.climate.domain.DirectThermalExposure;
import dev.signalshards.livingworld.features.climate.domain.PlayerActivity;
import dev.signalshards.livingworld.features.climate.domain.PlayerThermalBand;
import dev.signalshards.livingworld.features.climate.domain.ThermalFeedbackProfile;
import dev.signalshards.livingworld.features.hud.domain.HeadingPolicy;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("unused")
class PaperHudActionBarRendererTest {

    @Test
    void temperaturaDoHudVemDoReadoutTermicoUnificado() {
        AtomicInteger snapshots = new AtomicInteger();
        ThermalRuntimeReadoutProvider thermal = player -> {
            snapshots.incrementAndGet();
            return readout(12.6D);
        };
        var renderer = new PaperHudActionBarRenderer(
                new PaperHudSettings(true, 10L, false, false, false, true),
                MessageCatalog.fromLanguageTag("pt-BR"),
                new HeadingPolicy(),
                thermal,
                new TemperatureColorPolicy()
        );

        var rendered = renderer.render(playerAt(new Location(
                null,
                10.0D,
                64.0D,
                -4.0D,
                0.0F,
                0.0F
        )));

        assertEquals(1, snapshots.get());
        assertEquals(
                "🌡 13 °C",
                PlainTextComponentSerializer.plainText().serialize(rendered)
        );
    }

    @Test
    void hudSemTemperaturaNaoConsultaRuntimeTermico() {
        AtomicInteger snapshots = new AtomicInteger();
        ThermalRuntimeReadoutProvider thermal = player -> {
            snapshots.incrementAndGet();
            return readout(-30.0D);
        };
        var renderer = new PaperHudActionBarRenderer(
                new PaperHudSettings(true, 10L, false, true, false, false),
                MessageCatalog.fromLanguageTag("pt-BR"),
                new HeadingPolicy(),
                thermal,
                new TemperatureColorPolicy()
        );

        renderer.render(playerAt(new Location(
                null,
                0.0D,
                64.0D,
                0.0D,
                0.0F,
                0.0F
        )));

        assertEquals(0, snapshots.get());
    }

    private ThermalRuntimeReadout readout(double ambientCelsius) {
        return new ThermalRuntimeReadout(
                PlayerThermalBand.COMFORTABLE,
                0.0D,
                0.0D,
                ambientCelsius,
                PlayerActivity.RESTING,
                0.0D,
                0.0D,
                0.0D,
                DirectThermalExposure.NONE,
                0.0D,
                1.0D,
                0,
                0,
                0.0D,
                0.0D,
                0.0D,
                0.0D,
                0.0D,
                0.0D,
                0.0D,
                new ThermalFeedbackProfile(BreathFeedback.none(), 0.0D)
        );
    }

    private Player playerAt(Location location) {
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getLocation" -> location.clone();
                    case "toString" -> "FakeHudPlayer";
                    default -> defaultValue(method.getReturnType());
                }
        );
    }

    private Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0.0F;
        }
        if (type == double.class) {
            return 0.0D;
        }
        if (type == char.class) {
            return '\0';
        }
        return null;
    }
}
