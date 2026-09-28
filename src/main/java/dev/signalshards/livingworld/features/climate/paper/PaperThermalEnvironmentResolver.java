package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.climate.application.ThermalEnvironmentContext;
import dev.signalshards.livingworld.features.climate.domain.AmbientTemperature;
import dev.signalshards.livingworld.features.climate.domain.ArmorThermalLoadout;
import dev.signalshards.livingworld.features.climate.domain.DirectThermalExposure;
import dev.signalshards.livingworld.features.climate.domain.PlayerActivity;
import dev.signalshards.livingworld.features.climate.domain.PrecipitationExposure;
import dev.signalshards.livingworld.features.climate.domain.ShelterFactor;
import dev.signalshards.livingworld.features.climate.domain.WaterExposure;
import dev.signalshards.livingworld.features.climate.domain.WindExposure;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

/**
 * Primeira fronteira Paper -> ThermalEnvironmentContext.
 *
 * <p>Neste slice, atividade/armadura/água já são observadas. Vento, abrigo e
 * fontes locais permanecem em baseline explícito até seus resolvers próprios.</p>
 */
public final class PaperThermalEnvironmentResolver implements PaperThermalEnvironmentProvider {
    private final PaperAmbientTemperatureProvider ambientTemperature;

    public PaperThermalEnvironmentResolver(
            PaperAmbientTemperatureProvider ambientTemperature
    ) {
        this.ambientTemperature = Objects.requireNonNull(
                ambientTemperature,
                "temperatura ambiente"
        );
    }

    @Override
    public ThermalEnvironmentContext forPlayer(Player player) {
        Objects.requireNonNull(player, "jogador");
        AmbientTemperature ambient = ambientTemperature.ambientTemperatureAt(
                player.getLocation().getBlock()
        );
        PaperWindShelterResolver.Observation windShelter =
                PaperWindShelterResolver.forPlayer(player);
        return compose(
                ambient,
                PaperWaterExposureResolver.forPlayer(player),
                windShelter.precipitationExposure(),
                PaperDirectThermalExposureResolver.forPlayer(player),
                PaperPlayerActivityResolver.forPlayer(player),
                PaperArmorThermalResolver.forPlayer(player),
                windShelter.windExposure(),
                windShelter.shelterFactor(),
                PaperLocalHeatSourceResolver.forPlayer(player)
        );
    }

    static ThermalEnvironmentContext compose(
            AmbientTemperature ambientTemperature,
            WaterExposure waterExposure,
            PrecipitationExposure precipitationExposure,
            DirectThermalExposure directExposure,
            PlayerActivity activity,
            ArmorThermalLoadout armorLoadout,
            WindExposure windExposure,
            ShelterFactor shelterFactor,
            List<dev.signalshards.livingworld.features.climate.domain.LocalHeatExposure>
                    localHeatExposures
    ) {
        return new ThermalEnvironmentContext(
                ambientTemperature,
                waterExposure,
                precipitationExposure,
                directExposure,
                activity,
                windExposure,
                shelterFactor,
                armorLoadout,
                localHeatExposures
        );
    }

}
