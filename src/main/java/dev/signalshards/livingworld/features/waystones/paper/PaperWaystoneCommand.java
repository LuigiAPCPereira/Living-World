package dev.signalshards.livingworld.features.waystones.paper;

import dev.signalshards.livingworld.core.i18n.MessageCatalog;
import dev.signalshards.livingworld.core.status.LivingWorldStatusProvider;
import dev.signalshards.livingworld.features.climate.application.ClimateRuleReadout;
import dev.signalshards.livingworld.features.climate.application.LocalClimateReadout;
import dev.signalshards.livingworld.features.climate.application.LocalClimateReadoutProvider;
import dev.signalshards.livingworld.features.climate.application.ThermalRuntimeReadout;
import dev.signalshards.livingworld.features.climate.application.ThermalRuntimeReadoutProvider;
import dev.signalshards.livingworld.features.climate.domain.DirectThermalExposure;
import dev.signalshards.livingworld.features.climate.domain.PlayerActivity;
import dev.signalshards.livingworld.features.climate.domain.PlayerThermalBand;
import dev.signalshards.livingworld.features.climate.domain.MoistureBand;
import dev.signalshards.livingworld.features.climate.domain.ThermalBand;
import dev.signalshards.livingworld.features.climate.domain.WeatherTendency;
import dev.signalshards.livingworld.features.seasons.domain.Season;
import dev.signalshards.livingworld.features.waystones.application.WaystoneService;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@NullMarked
public final class PaperWaystoneCommand implements BasicCommand {
    private final WaystoneService waystones;
    private final PaperWaystoneTravelService travel;
    private final MessageCatalog messages;
    private final LivingWorldStatusProvider statusProvider;
    private final double renameMaxDistance;
    private final PaperWaystoneMenu menu;
    private final LocalClimateReadoutProvider climateReadoutProvider;
    private final ThermalRuntimeReadoutProvider thermalReadoutProvider;

    public PaperWaystoneCommand(
            WaystoneService waystones,
            PaperWaystoneTravelService travel,
            MessageCatalog messages,
            LivingWorldStatusProvider statusProvider,
            double renameMaxDistance,
            PaperWaystoneMenu menu,
            LocalClimateReadoutProvider climateReadoutProvider,
            ThermalRuntimeReadoutProvider thermalReadoutProvider
    ) {
        this.waystones = Objects.requireNonNull(waystones, "serviço de waystones");
        this.travel = Objects.requireNonNull(travel, "viagem de waystones");
        this.messages = Objects.requireNonNull(messages, "catálogo de mensagens");
        this.statusProvider = Objects.requireNonNull(
                statusProvider,
                "diagnóstico do Living World"
        );
        if (!Double.isFinite(renameMaxDistance) || renameMaxDistance <= 0.0D) {
            throw new IllegalArgumentException(
                    "A distância para renomear waystones deve ser positiva"
            );
        }
        this.renameMaxDistance = renameMaxDistance;
        this.menu = Objects.requireNonNull(menu, "menu de waystones");
        this.climateReadoutProvider = Objects.requireNonNull(
                climateReadoutProvider,
                "leitura de clima local"
        );
        this.thermalReadoutProvider = Objects.requireNonNull(
                thermalReadoutProvider,
                "diagnóstico térmico"
        );
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("status")) {
            status(source.getSender());
            return;
        }

        if (!(source.getSender() instanceof Player player)) {
            source.getSender().sendMessage(messages.component(
                    NamedTextColor.RED,
                    "waystone.player-only"
            ));
            return;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("list")
                || args[0].equalsIgnoreCase("waystones")) {
            list(player);
            return;
        }

        if (args[0].equalsIgnoreCase("travel")) {
            travel(player, args);
            return;
        }
        if (args[0].equalsIgnoreCase("rename")) {
            rename(player, args);
            return;
        }
        if (args[0].equalsIgnoreCase("menu")) {
            menu.open(player);
            return;
        }
        if (args[0].equalsIgnoreCase("climate")) {
            climate(player);
            return;
        }
        if (args[0].equalsIgnoreCase("thermal")) {
            thermal(player);
            return;
        }

        player.sendMessage(messages.component(
                NamedTextColor.YELLOW,
                "waystone.command-usage"
        ));
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (args.length <= 1) {
            if (source.getSender() instanceof Player) {
                return List.of(
                        "status",
                        "climate",
                        "thermal",
                        "list",
                        "travel",
                        "rename",
                        "menu"
                );
            }
            return List.of("status");
        }

        if (!(source.getSender() instanceof Player player)) {
            return List.of();
        }

        if (args.length == 2 && (
                args[0].equalsIgnoreCase("travel")
                        || args[0].equalsIgnoreCase("rename")
        )) {
            int size = waystones.activatedWaystones(player.getUniqueId()).size();
            List<String> suggestions = new ArrayList<>(size);
            for (int index = 1; index <= size; index++) {
                suggestions.add(Integer.toString(index));
            }
            return suggestions;
        }

        return List.of();
    }

    private void climate(Player player) {
        LocalClimateReadout snapshot = climateReadoutProvider.snapshot(player);
        player.sendMessage(messages.component(
                NamedTextColor.GOLD,
                "climate.readout-header"
        ));
        player.sendMessage(messages.component(
                NamedTextColor.AQUA,
                "climate.readout-summary",
                seasonText(snapshot.season()),
                snapshot.apparentCelsius(),
                thermalText(snapshot.thermalBand()),
                moistureText(snapshot.moistureBand())
        ));
        player.sendMessage(messages.component(
                NamedTextColor.BLUE,
                "climate.readout-weather",
                weatherTendencyText(snapshot.weatherTendency())
        ));
        sendRule(
                player,
                "climate.readout-crops",
                "climate.label-crops",
                snapshot.cropGrowth()
        );
        sendRule(
                player,
                "climate.readout-trees",
                "climate.label-trees",
                snapshot.treeGrowth()
        );
        sendRule(
                player,
                "climate.readout-grass",
                "climate.label-grass",
                snapshot.grassSpread()
        );
        sendRule(
                player,
                "climate.readout-farmland",
                "climate.label-farmland",
                snapshot.farmlandRetention()
        );
        sendRule(
                player,
                "climate.readout-fire",
                "climate.label-fire",
                snapshot.fireSpread()
        );
        if (!snapshot.frozenSurfacesEnabled()) {
            player.sendMessage(messages.component(
                    NamedTextColor.DARK_GRAY,
                    "climate.readout-frozen-disabled"
            ));
        } else {
            player.sendMessage(messages.component(
                    snapshot.frozenSurfacesPersist()
                            ? NamedTextColor.AQUA
                            : NamedTextColor.GRAY,
                    snapshot.frozenSurfacesPersist()
                            ? "climate.readout-frozen-yes"
                            : "climate.readout-frozen-no"
            ));
        }
    }

    private void thermal(Player player) {
        ThermalRuntimeReadout snapshot = thermalReadoutProvider.snapshot(player);
        player.sendMessage(messages.component(
                NamedTextColor.GOLD,
                "thermal.readout-header"
        ));
        player.sendMessage(messages.component(
                NamedTextColor.AQUA,
                "thermal.readout-body",
                thermalBandText(snapshot.thermalBand()),
                formatSigned(snapshot.thermalLoad()),
                (int) Math.round(snapshot.wetness() * 100.0D)
        ));
        player.sendMessage(messages.component(
                NamedTextColor.GRAY,
                "thermal.readout-environment",
                snapshot.ambientCelsius(),
                activityText(snapshot.activity()),
                (int) Math.round(snapshot.submergedFraction() * 100.0D),
                snapshot.waterDepthBlocks()
        ));
        player.sendMessage(messages.component(
                NamedTextColor.GRAY,
                "thermal.readout-exposure",
                (int) Math.round(snapshot.precipitationExposure() * 100.0D),
                (int) Math.round(snapshot.windExposure() * 100.0D),
                (int) Math.round(snapshot.shelterFactor() * 100.0D),
                snapshot.armorPieces(),
                snapshot.localHeatSources(),
                directExposureText(snapshot.directExposure())
        ));
        player.sendMessage(messages.component(
                NamedTextColor.DARK_AQUA,
                "thermal.readout-rates",
                formatSigned(snapshot.airRatePerSecond()),
                formatSigned(snapshot.waterRatePerSecond()),
                formatSigned(snapshot.activityRatePerSecond()),
                formatSigned(snapshot.localHeatRatePerSecond()),
                formatSigned(snapshot.directExposureRatePerSecond()),
                formatSigned(snapshot.netRatePerSecond()),
                formatSigned(snapshot.wetnessRatePerSecond())
        ));
    }

    private String directExposureText(DirectThermalExposure exposure) {
        return messages.text(switch (exposure) {
            case NONE -> "thermal.direct.none";
            case FIRE -> "thermal.direct.fire";
            case LAVA -> "thermal.direct.lava";
            case POWDER_SNOW -> "thermal.direct.powder-snow";
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

    private String activityText(PlayerActivity activity) {
        return messages.text(switch (activity) {
            case RESTING -> "thermal.activity.resting";
            case WALKING -> "thermal.activity.walking";
            case SPRINTING -> "thermal.activity.sprinting";
            case SWIMMING -> "thermal.activity.swimming";
            case CLIMBING -> "thermal.activity.climbing";
            case GLIDING -> "thermal.activity.gliding";
        });
    }

    private String formatSigned(double value) {
        return String.format(Locale.ROOT, "%+.4f", value);
    }

    private void sendRule(
            Player player,
            String messageKey,
            String labelKey,
            ClimateRuleReadout readout
    ) {
        if (!readout.enabled()) {
            player.sendMessage(messages.component(
                    NamedTextColor.DARK_GRAY,
                    "climate.readout-rule-disabled",
                    messages.text(labelKey)
            ));
            return;
        }
        player.sendMessage(messages.component(
                NamedTextColor.GRAY,
                messageKey,
                readout.percent()
        ));
    }

    private String seasonText(Season season) {
        return messages.text(switch (season) {
            case PRIMAVERA -> "season.spring";
            case VERAO -> "season.summer";
            case OUTONO -> "season.autumn";
            case INVERNO -> "season.winter";
        });
    }

    private String thermalText(ThermalBand band) {
        return messages.text(switch (band) {
            case CONGELANTE -> "climate.thermal.freezing";
            case FRIO -> "climate.thermal.cold";
            case TEMPERADO -> "climate.thermal.temperate";
            case QUENTE -> "climate.thermal.hot";
            case ESCALDANTE -> "climate.thermal.scorching";
        });
    }

    private String moistureText(MoistureBand band) {
        return messages.text(switch (band) {
            case ARIDO -> "climate.moisture.arid";
            case SECO -> "climate.moisture.dry";
            case EQUILIBRADO -> "climate.moisture.balanced";
            case UMIDO -> "climate.moisture.humid";
            case ENCHARCADO -> "climate.moisture.saturated";
        });
    }

    private String weatherTendencyText(WeatherTendency tendency) {
        return messages.text(switch (tendency) {
            case TEMPO_LIMPO -> "climate.weather.clear";
            case ESTAVEL -> "climate.weather.stable";
            case PRECIPITACAO -> "climate.weather.precipitation";
            case TEMPESTADE -> "climate.weather.storm";
        });
    }

    private void rename(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(messages.component(
                    NamedTextColor.YELLOW,
                    "waystone.rename-usage"
            ));
            return;
        }

        int selection;
        try {
            selection = Integer.parseInt(args[1]);
        } catch (NumberFormatException exception) {
            player.sendMessage(messages.component(
                    NamedTextColor.RED,
                    "waystone.rename-invalid-selection"
            ));
            return;
        }

        var activated = waystones.activatedWaystones(player.getUniqueId());
        if (selection < 1 || selection > activated.size()) {
            player.sendMessage(messages.component(
                    NamedTextColor.RED,
                    "waystone.rename-invalid-selection"
            ));
            return;
        }

        var target = activated.get(selection - 1);
        if (!player.getWorld().getUID().equals(target.worldId())
                || distanceSquaredToAnchor(player, target) >
                renameMaxDistance * renameMaxDistance) {
            player.sendMessage(messages.component(
                    NamedTextColor.YELLOW,
                    "waystone.rename-too-far",
                    renameMaxDistance
            ));
            return;
        }

        String newName = String.join(
                " ",
                Arrays.copyOfRange(args, 2, args.length)
        );
        try {
            var renamed = waystones.renameActivated(
                    player.getUniqueId(),
                    target.id(),
                    newName
            );
            if (renamed.isEmpty()) {
                player.sendMessage(messages.component(
                        NamedTextColor.RED,
                        "waystone.rename-not-activated"
                ));
                return;
            }

            player.sendMessage(messages.component(
                    NamedTextColor.GREEN,
                    "waystone.renamed",
                    target.name(),
                    renamed.orElseThrow().name()
            ));
        } catch (IllegalArgumentException exception) {
            player.sendMessage(messages.component(
                    NamedTextColor.RED,
                    "waystone.rename-invalid-name"
            ));
        }
    }

    private double distanceSquaredToAnchor(
            Player player,
            dev.signalshards.livingworld.features.waystones.domain.Waystone waystone
    ) {
        var location = player.getLocation();
        double dx = location.getX() - (waystone.x() + 0.5D);
        double dy = location.getY() - waystone.y();
        double dz = location.getZ() - (waystone.z() + 0.5D);
        return (dx * dx) + (dy * dy) + (dz * dz);
    }

    private void status(CommandSender sender) {
        var snapshot = statusProvider.snapshot();
        sender.sendMessage(messages.component(
                NamedTextColor.GOLD,
                "status.header",
                snapshot.version()
        ));
        sender.sendMessage(messages.component(
                NamedTextColor.GRAY,
                "status.world",
                snapshot.worldName()
        ));
        sender.sendMessage(messages.component(
                NamedTextColor.AQUA,
                "status.calendar",
                snapshot.seasonName(),
                snapshot.year(),
                snapshot.month(),
                snapshot.day()
        ));
        sender.sendMessage(messages.component(
                NamedTextColor.GRAY,
                "status.players",
                snapshot.onlinePlayers()
        ));
        sender.sendMessage(messages.component(
                NamedTextColor.GRAY,
                "status.hud",
                snapshot.hudSessions()
        ));
        sender.sendMessage(messages.component(
                NamedTextColor.GRAY,
                "status.desire-lines",
                snapshot.desireLineCachedChunks()
        ));
        sender.sendMessage(messages.component(
                NamedTextColor.GRAY,
                "status.waystones",
                snapshot.registeredWaystones()
        ));
        sender.sendMessage(messages.component(
                NamedTextColor.GREEN,
                "status.performance",
                snapshot.tpsOneMinute(),
                snapshot.averageTickTimeMillis()
        ));
        sender.sendMessage(messages.component(
                NamedTextColor.YELLOW,
                "status.restart-required"
        ));
    }

    private void list(Player player) {
        var activated = waystones.activatedWaystones(player.getUniqueId());
        if (activated.isEmpty()) {
            player.sendMessage(messages.component(
                    NamedTextColor.YELLOW,
                    "waystone.list-empty"
            ));
            return;
        }

        player.sendMessage(messages.component(
                NamedTextColor.GOLD,
                "waystone.list-header",
                activated.size()
        ));
        for (int index = 0; index < activated.size(); index++) {
            player.sendMessage(messages.component(
                    NamedTextColor.AQUA,
                    "waystone.list-entry",
                    index + 1,
                    activated.get(index).name()
            ));
        }
    }

    private void travel(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(messages.component(
                    NamedTextColor.YELLOW,
                    "waystone.travel-usage"
            ));
            return;
        }

        int selection;
        try {
            selection = Integer.parseInt(args[1]);
        } catch (NumberFormatException exception) {
            player.sendMessage(messages.component(
                    NamedTextColor.RED,
                    "waystone.travel-invalid"
            ));
            return;
        }

        var activated = waystones.activatedWaystones(player.getUniqueId());
        if (selection < 1 || selection > activated.size()) {
            player.sendMessage(messages.component(
                    NamedTextColor.RED,
                    "waystone.travel-invalid"
            ));
            return;
        }

        var target = activated.get(selection - 1);
        PaperWaystoneTravelFeedback.sendStart(
                player,
                messages,
                target.name()
        );
        travel.travel(player, target.id()).thenAccept(result ->
                PaperWaystoneTravelFeedback.sendResult(
                        player,
                        messages,
                        target.name(),
                        result
                )
        );
    }
}
