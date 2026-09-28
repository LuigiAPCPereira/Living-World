package dev.signalshards.livingworld.features.climate.application;

import dev.signalshards.livingworld.features.climate.domain.ActivityThermalPolicy;
import dev.signalshards.livingworld.features.climate.domain.AirThermalExchangePolicy;
import dev.signalshards.livingworld.features.climate.domain.AirThermalTransferFactor;
import dev.signalshards.livingworld.features.climate.domain.AirTransferPolicy;
import dev.signalshards.livingworld.features.climate.domain.ArmorInsulationPolicy;
import dev.signalshards.livingworld.features.climate.domain.ArmorWetnessPolicy;
import dev.signalshards.livingworld.features.climate.domain.LocalHeatSourcePolicy;
import dev.signalshards.livingworld.features.climate.domain.ThermalExchangeRate;
import dev.signalshards.livingworld.features.climate.domain.WaterExposureEffect;
import dev.signalshards.livingworld.features.climate.domain.WaterExposurePolicy;
import dev.signalshards.livingworld.features.climate.domain.WaterTemperature;
import dev.signalshards.livingworld.features.climate.domain.WaterTemperaturePolicy;
import dev.signalshards.livingworld.features.climate.domain.WaterThermalExchangePolicy;
import dev.signalshards.livingworld.features.climate.domain.WetnessRate;
import dev.signalshards.livingworld.features.climate.domain.WetnessEnvironmentPolicy;

import java.util.Optional;

/**
 * Orquestra as policies térmicas puras em um breakdown instantâneo.
 *
 * <p>Não avança o relógio nem mantém estado. Isso evita que o compositor
 * vire um EnvironmentManager global: integração temporal continua em
 * PlayerThermalPolicy/WetnessPolicy, enquanto adapters resolvem o mundo.</p>
 */
public final class ThermalExchangeComposer {
    private final WaterExposurePolicy waterExposurePolicy = new WaterExposurePolicy();
    private final WaterTemperaturePolicy waterTemperaturePolicy = new WaterTemperaturePolicy();
    private final WaterThermalExchangePolicy waterExchangePolicy = new WaterThermalExchangePolicy();
    private final AirTransferPolicy airTransferPolicy = new AirTransferPolicy();
    private final ArmorInsulationPolicy armorInsulationPolicy = new ArmorInsulationPolicy();
    private final ArmorWetnessPolicy armorWetnessPolicy = new ArmorWetnessPolicy();
    private final WetnessEnvironmentPolicy wetnessEnvironmentPolicy =
            new WetnessEnvironmentPolicy();
    private final AirThermalExchangePolicy airExchangePolicy = new AirThermalExchangePolicy();
    private final ActivityThermalPolicy activityPolicy = new ActivityThermalPolicy();
    private final LocalHeatSourcePolicy localHeatPolicy = new LocalHeatSourcePolicy();

    public ThermalExchangeResolution resolve(ThermalExchangeContext context) {
        WaterExposureEffect waterEffect = waterExposurePolicy.evaluate(context.waterExposure());
        AirThermalTransferFactor environmentalAir = airTransferPolicy.factorFor(
                context.windExposure(),
                context.shelterFactor()
        );
        AirThermalTransferFactor armoredAir = armorInsulationPolicy.apply(
                environmentalAir,
                context.armorLoadout(),
                context.wetness()
        );
        AirThermalTransferFactor exposedAir = new AirThermalTransferFactor(
                armoredAir.value() * (1.0D - context.waterExposure().submergedFraction())
        );
        ThermalExchangeRate airRate = airExchangePolicy.exchangeRate(
                context.thermalState(),
                context.ambientTemperature(),
                context.wetness(),
                exposedAir
        );

        Optional<WaterTemperature> waterTemperature = waterTemperature(context);
        ThermalExchangeRate waterRate = waterTemperature
                .map(temperature -> waterExchangePolicy.exchangeRate(
                        context.thermalState(),
                        temperature,
                        waterEffect.thermalTransferFactor()
                ))
                .orElseGet(ThermalExchangeRate::neutral);

        ThermalExchangeRate activityRate = activityPolicy.exchangeRate(context.activity());
        ThermalExchangeRate localHeatRate = localHeatPolicy.exchangeRate(
                context.localHeatExposures()
        );
        WetnessRate environmentalWetness = wetnessEnvironmentPolicy.exchangeRate(
                waterEffect.wetnessRate(),
                context.precipitationExposure(),
                context.ambientTemperature(),
                context.windExposure(),
                localHeatRate,
                context.wetness()
        );
        WetnessRate wetnessRate = armorWetnessPolicy.apply(
                environmentalWetness,
                context.armorLoadout()
        );
        ThermalExchangeRate netRate = airRate
                .plus(waterRate)
                .plus(activityRate)
                .plus(localHeatRate);

        return new ThermalExchangeResolution(
                airRate,
                waterRate,
                activityRate,
                localHeatRate,
                netRate,
                wetnessRate,
                exposedAir,
                waterTemperature
        );
    }

    private Optional<WaterTemperature> waterTemperature(ThermalExchangeContext context) {
        if (context.waterExposure().submergedFraction() == 0.0D) {
            return Optional.empty();
        }
        return Optional.of(waterTemperaturePolicy.temperature(
                context.ambientTemperature(),
                context.waterExposure().depthBlocks()
        ));
    }
}
