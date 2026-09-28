package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.calendar.application.CalendarView;
import dev.signalshards.livingworld.features.climate.application.ClimateRuleReadout;
import dev.signalshards.livingworld.features.climate.application.LocalClimateReadout;
import dev.signalshards.livingworld.features.climate.application.LocalClimateReadoutProvider;
import dev.signalshards.livingworld.features.climate.domain.ApparentTemperaturePolicy;
import dev.signalshards.livingworld.features.ecology.domain.FarmlandMoistureRetentionPolicy;
import dev.signalshards.livingworld.features.ecology.domain.FireSpreadSuitabilityPolicy;
import dev.signalshards.livingworld.features.ecology.domain.FrozenSurfacePolicy;
import dev.signalshards.livingworld.features.ecology.domain.NaturalGrowthSuitabilityPolicy;
import dev.signalshards.livingworld.features.ecology.paper.PaperEcologySettings;
import org.bukkit.entity.Player;

import java.util.Objects;

public final class PaperLocalClimateReadoutProvider
        implements LocalClimateReadoutProvider {
    private final CalendarView calendar;
    private final PaperLocalClimateResolver climate;
    private final PaperEcologySettings ecology;
    private final ApparentTemperaturePolicy temperaturePolicy;
    private final NaturalGrowthSuitabilityPolicy growthPolicy;
    private final FarmlandMoistureRetentionPolicy farmlandPolicy;
    private final FireSpreadSuitabilityPolicy firePolicy;
    private final FrozenSurfacePolicy frozenPolicy;

    public PaperLocalClimateReadoutProvider(
            CalendarView calendar,
            PaperLocalClimateResolver climate,
            PaperEcologySettings ecology,
            ApparentTemperaturePolicy temperaturePolicy,
            NaturalGrowthSuitabilityPolicy growthPolicy,
            FarmlandMoistureRetentionPolicy farmlandPolicy,
            FireSpreadSuitabilityPolicy firePolicy,
            FrozenSurfacePolicy frozenPolicy
    ) {
        this.calendar = Objects.requireNonNull(calendar, "calendário");
        this.climate = Objects.requireNonNull(climate, "clima local");
        this.ecology = Objects.requireNonNull(ecology, "configuração ecológica");
        this.temperaturePolicy = Objects.requireNonNull(
                temperaturePolicy,
                "política de temperatura"
        );
        this.growthPolicy = Objects.requireNonNull(
                growthPolicy,
                "política de crescimento"
        );
        this.farmlandPolicy = Objects.requireNonNull(
                farmlandPolicy,
                "política de farmland"
        );
        this.firePolicy = Objects.requireNonNull(
                firePolicy,
                "política de fogo"
        );
        this.frozenPolicy = Objects.requireNonNull(
                frozenPolicy,
                "política de superfícies congeladas"
        );
    }

    @Override
    public LocalClimateReadout snapshot(Player player) {
        Objects.requireNonNull(player, "jogador");
        var block = player.getLocation().getBlock();
        var snapshot = climate.snapshotAt(block);
        var season = calendar.currentSeason();
        var ambientTemperature = climate.baseAmbientTemperatureAt(block);

        return new LocalClimateReadout(
                season,
                temperaturePolicy.degreesCelsius(
                        ambientTemperature,
                        PaperTemperatureExposureResolver.forPlayer(player)
                ),
                snapshot.temperature(),
                snapshot.moisture(),
                snapshot.weatherTendency(),
                chance(
                        ecology.naturalGrowthEnabled(),
                        growthPolicy.acceptanceChance(
                                snapshot,
                                ecology.cropGrowthStrength()
                        )
                ),
                chance(
                        ecology.naturalGrowthEnabled(),
                        growthPolicy.acceptanceChance(
                                snapshot,
                                ecology.treeGrowthStrength()
                        )
                ),
                chance(
                        ecology.groundCoverSpreadEnabled(),
                        growthPolicy.acceptanceChance(
                                snapshot,
                                ecology.groundCoverSpreadStrength()
                        )
                ),
                chance(
                        ecology.farmlandMoistureRetentionEnabled(),
                        farmlandPolicy.retentionChance(
                                snapshot,
                                ecology.farmlandMoistureRetentionStrength()
                        )
                ),
                chance(
                        ecology.fireSpreadEnabled(),
                        firePolicy.acceptanceChance(
                                snapshot,
                                ecology.fireSpreadStrength()
                        )
                ),
                ecology.frozenSurfacesEnabled(),
                frozenPolicy.preserveFromFade(snapshot)
        );
    }

    private ClimateRuleReadout chance(boolean enabled, double value) {
        return new ClimateRuleReadout(
                enabled,
                (int) Math.round(Math.clamp(value, 0.0D, 1.0D) * 100.0D)
        );
    }
}
