package dev.signalshards.livingworld.features.hud.paper;

import dev.signalshards.livingworld.core.i18n.MessageCatalog;
import dev.signalshards.livingworld.features.climate.application.ThermalRuntimeReadout;
import dev.signalshards.livingworld.features.climate.application.ThermalRuntimeReadoutProvider;
import dev.signalshards.livingworld.features.climate.application.ThermalTrend;
import dev.signalshards.livingworld.features.climate.application.ThermalTrendPolicy;
import dev.signalshards.livingworld.features.climate.domain.PlayerThermalBand;
import dev.signalshards.livingworld.features.hud.domain.CardinalDirection;
import dev.signalshards.livingworld.features.hud.domain.HeadingPolicy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
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
    private final ThermalTrendPolicy thermalTrendPolicy = new ThermalTrendPolicy();

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

        if (settings.coordinatesEnabled() && coordinatesVisible(
                player.getInventory().getItemInMainHand().getType(),
                player.getInventory().getItemInOffHand().getType()
        )) {
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
            ThermalRuntimeReadout thermal = thermalReadoutProvider.snapshot(player);
            int celsius = (int) Math.round(thermal.ambientCelsius());
            result = appendSegment(
                    result,
                    Component.text(
                            messages.text("hud.temperature", celsius),
                            temperatureColorPolicy.colorFor(celsius)
                    ),
                    hasSegment
            );

            if (settings.thermalContextEnabled()) {
                result = appendSegment(
                        result,
                        Component.text(
                                thermalBandText(thermal.thermalBand())
                                        + " "
                                        + trendSymbol(
                                                thermalTrendPolicy.classify(
                                                        thermal.netRatePerSecond()
                                                )
                                        ),
                                NamedTextColor.WHITE
                        ),
                        true
                );

                if (thermal.wetness() >= settings.wetnessMinimumDisplay()) {
                    int wetnessPercent = (int) Math.round(
                            thermal.wetness() * 100.0D
                    );
                    result = appendSegment(
                            result,
                            Component.text(
                                    messages.text(
                                            "hud.thermal.wetness",
                                            wetnessPercent
                                    ),
                                    NamedTextColor.AQUA
                            ),
                            true
                    );
                }
            }
        }

        return result;
    }

    static boolean coordinatesVisible(
            Material mainHand,
            Material offHand
    ) {
        Objects.requireNonNull(mainHand, "material da mão principal");
        Objects.requireNonNull(offHand, "material da mão secundária");
        return mainHand == Material.COMPASS || offHand == Material.COMPASS;
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

    private String thermalBandText(PlayerThermalBand band) {
        return messages.text(switch (band) {
            case EXTREME_COLD -> "thermal.band.extreme-cold";
            case FREEZING -> "thermal.band.freezing";
            case VERY_COLD -> "thermal.band.very-cold";
            case COLD -> "thermal.band.cold";
            case COOL -> "thermal.band.cool";
            case COMFORTABLE -> "thermal.band.comfortable";
            case WARM -> "thermal.band.warm";
            case HOT -> "thermal.band.hot";
            case OVERHEATING -> "thermal.band.overheating";
            case EXTREME_HEAT -> "thermal.band.extreme-heat";
        });
    }

    private String trendSymbol(ThermalTrend trend) {
        return switch (trend) {
            case COOLING -> "↓";
            case STABLE -> "→";
            case WARMING -> "↑";
        };
    }
}
