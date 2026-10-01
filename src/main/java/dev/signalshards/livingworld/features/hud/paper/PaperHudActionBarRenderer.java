package dev.signalshards.livingworld.features.hud.paper;

import dev.signalshards.livingworld.core.i18n.MessageCatalog;
import dev.signalshards.livingworld.features.climate.application.ThermalRuntimeReadoutProvider;
import dev.signalshards.livingworld.features.hud.domain.CardinalDirection;
import dev.signalshards.livingworld.features.hud.domain.HeadingPolicy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Renderiza a action bar a partir das fontes autoritativas já resolvidas.
 *
 * <p>Temperatura não é recalculada pelo HUD: ela vem do mesmo readout térmico
 * consumido pelos diagnósticos do runtime.</p>
 */
final class PaperHudActionBarRenderer {
    private final PaperHudSettings settings;
    private final MessageCatalog messages;
    private final HeadingPolicy headingPolicy;
    private final ThermalRuntimeReadoutProvider thermalReadoutProvider;
    private final TemperatureColorPolicy temperatureColorPolicy;

    PaperHudActionBarRenderer(
            PaperHudSettings settings,
            MessageCatalog messages,
            HeadingPolicy headingPolicy,
            ThermalRuntimeReadoutProvider thermalReadoutProvider,
            TemperatureColorPolicy temperatureColorPolicy
    ) {
        this.settings = Objects.requireNonNull(settings, "configuração de HUD");
        this.messages = Objects.requireNonNull(messages, "catálogo de mensagens");
        this.headingPolicy = Objects.requireNonNull(headingPolicy, "política de direção");
        this.thermalReadoutProvider = Objects.requireNonNull(
                thermalReadoutProvider,
                "readout térmico"
        );
        this.temperatureColorPolicy = Objects.requireNonNull(
                temperatureColorPolicy,
                "política visual da temperatura"
        );
    }

    Component render(Player player) {
        Objects.requireNonNull(player, "jogador");
        Location location = player.getLocation();
        Component result = Component.empty();
        boolean hasSegment = false;

        if (settings.navigationEnabled()) {
            result = Component.text(
                    directionText(headingPolicy.directionFor(location.getYaw())),
                    NamedTextColor.AQUA
            );
            hasSegment = true;
        }

        if (settings.coordinatesEnabled()) {
            result = appendSegment(
                    result,
                    Component.text(
                            messages.text(
                                    "hud.coordinates",
                                    location.getBlockX(),
                                    location.getBlockY(),
                                    location.getBlockZ()
                            ),
                            NamedTextColor.GRAY
                    ),
                    hasSegment
            );
            hasSegment = true;
        }

        if (settings.temperatureEnabled()) {
            int celsius = (int) Math.round(
                    thermalReadoutProvider.snapshot(player).ambientCelsius()
            );
            result = appendSegment(
                    result,
                    Component.text(
                            messages.text("hud.temperature", celsius),
                            temperatureColorPolicy.colorFor(celsius)
                    ),
                    hasSegment
            );
        }

        return result;
    }

    private Component appendSegment(
            Component base,
            Component segment,
            boolean hasPrevious
    ) {
        if (hasPrevious) {
            return base
                    .append(Component.text("  •  ", NamedTextColor.DARK_GRAY))
                    .append(segment);
        }
        return base.append(segment);
    }

    private String directionText(CardinalDirection direction) {
        return messages.text(switch (direction) {
            case NORTE -> "direction.north";
            case NORDESTE -> "direction.northeast";
            case LESTE -> "direction.east";
            case SUDESTE -> "direction.southeast";
            case SUL -> "direction.south";
            case SUDOESTE -> "direction.southwest";
            case OESTE -> "direction.west";
            case NOROESTE -> "direction.northwest";
        });
    }
}
