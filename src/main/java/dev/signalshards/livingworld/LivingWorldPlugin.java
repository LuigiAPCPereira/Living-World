package dev.signalshards.livingworld;

import dev.signalshards.livingworld.core.i18n.MessageCatalog;
import dev.signalshards.livingworld.core.module.ModuleManager;
import dev.signalshards.livingworld.core.status.LivingWorldStatusSnapshot;
import dev.signalshards.livingworld.core.status.LivingWorldStatusProvider;
import dev.signalshards.livingworld.features.calendar.paper.PaperCalendarModule;
import dev.signalshards.livingworld.features.calendar.paper.PaperCalendarWorldResolver;
import dev.signalshards.livingworld.features.calendar.domain.CalendarRules;
import dev.signalshards.livingworld.features.climate.domain.ApparentTemperaturePolicy;
import dev.signalshards.livingworld.features.climate.application.WeatherEventPlanner;
import dev.signalshards.livingworld.features.climate.domain.ClimatePolicy;
import dev.signalshards.livingworld.features.climate.domain.ClimateProfileClassifier;
import dev.signalshards.livingworld.features.climate.domain.ClimateProfileThresholds;
import dev.signalshards.livingworld.features.climate.paper.PaperClimateCoordinator;
import dev.signalshards.livingworld.features.climate.paper.PaperClimateSampler;
import dev.signalshards.livingworld.features.climate.paper.PaperLocalClimateResolver;
import dev.signalshards.livingworld.features.climate.paper.PaperWeatherController;
import dev.signalshards.livingworld.features.climate.paper.PaperWeatherEventSettingsLoader;
import dev.signalshards.livingworld.features.desirelines.paper.PaperDesireLinesModule;
import dev.signalshards.livingworld.features.desirelines.paper.PaperPathWearSettingsLoader;
import dev.signalshards.livingworld.features.ecology.domain.FarmlandMoistureRetentionPolicy;
import dev.signalshards.livingworld.features.ecology.domain.FireSpreadSuitabilityPolicy;
import dev.signalshards.livingworld.features.ecology.domain.FrozenSurfacePolicy;
import dev.signalshards.livingworld.features.ecology.domain.NaturalGrowthSuitabilityPolicy;
import dev.signalshards.livingworld.features.ecology.paper.PaperEcologySettings;
import dev.signalshards.livingworld.features.ecology.paper.PaperEcologySettingsLoader;
import dev.signalshards.livingworld.features.ecology.paper.PaperFrozenSurfaceModule;
import dev.signalshards.livingworld.features.ecology.paper.PaperFarmlandMoistureModule;
import dev.signalshards.livingworld.features.ecology.paper.PaperFireSpreadModule;
import dev.signalshards.livingworld.features.ecology.paper.PaperGroundCoverSpreadModule;
import dev.signalshards.livingworld.features.ecology.paper.PaperNaturalGrowthModule;
import dev.signalshards.livingworld.features.qol.doubledoors.paper.PaperDoubleDoorsModule;
import dev.signalshards.livingworld.features.hud.domain.HeadingPolicy;
import dev.signalshards.livingworld.features.hud.paper.PaperHudModule;
import dev.signalshards.livingworld.features.hud.paper.PaperHudSettingsLoader;
import dev.signalshards.livingworld.features.hud.paper.TemperatureColorPolicy;
import dev.signalshards.livingworld.features.seasons.domain.SeasonCycle;
import dev.signalshards.livingworld.features.seasons.paper.PaperSeasonTransitionAnnouncement;
import dev.signalshards.livingworld.features.waystones.application.WaystoneService;
import dev.signalshards.livingworld.features.waystones.paper.PaperPlayerWaystoneAccessStore;
import dev.signalshards.livingworld.features.waystones.paper.PaperSafeWaystoneDestination;
import dev.signalshards.livingworld.features.waystones.paper.PaperWaystoneModule;
import dev.signalshards.livingworld.features.waystones.paper.PaperWaystoneRegistry;
import dev.signalshards.livingworld.features.waystones.paper.PaperWaystoneSettings;
import dev.signalshards.livingworld.features.waystones.paper.PaperWaystoneSettingsLoader;
import dev.signalshards.livingworld.features.waystones.paper.PaperWaystoneTravelService;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class LivingWorldPlugin extends JavaPlugin {
    private ModuleManager moduleManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        MessageCatalog messages = MessageCatalog.fromLanguageTag(
                getConfig().getString("language", "pt-BR")
        );

        World calendarWorld = PaperCalendarWorldResolver.resolve(
                getServer(),
                getConfig().getString("calendar.world", "")
        );

        SeasonCycle seasons = SeasonCycle.livingWorldDefaults();
        CalendarRules calendarRules = CalendarRules.livingWorldDefaults();
        ClimateProfileClassifier climateClassifier = new ClimateProfileClassifier(
                ClimateProfileThresholds.livingWorldDefaults()
        );
        ClimatePolicy climatePolicy = new ClimatePolicy();
        PaperClimateCoordinator climateCoordinator = new PaperClimateCoordinator(
                calendarWorld,
                new PaperClimateSampler(climateClassifier),
                climatePolicy,
                new WeatherEventPlanner(
                        PaperWeatherEventSettingsLoader.load(getConfig())
                ),
                new PaperWeatherController(),
                seasons,
                messages,
                getLogger()
        );

        PaperDesireLinesModule desireLines = new PaperDesireLinesModule(
                this,
                calendarWorld,
                PaperPathWearSettingsLoader.load(getConfig())
        );

        PaperWaystoneSettings waystoneSettings = PaperWaystoneSettingsLoader.load(getConfig());
        WaystoneService waystones = new WaystoneService(
                new PaperWaystoneRegistry(this),
                new PaperPlayerWaystoneAccessStore(this)
        );
        PaperWaystoneTravelService waystoneTravel = new PaperWaystoneTravelService(
                getServer(),
                waystones,
                new PaperSafeWaystoneDestination(waystoneSettings.anchorMaterial())
        );
        PaperSeasonTransitionAnnouncement seasonAnnouncement =
                new PaperSeasonTransitionAnnouncement(
                        getServer(),
                        messages,
                        getConfig().getBoolean(
                                "seasons.transition-announcement.enabled",
                                true
                        )
                );

        PaperCalendarModule calendarModule = new PaperCalendarModule(
                this,
                calendarWorld,
                messages,
                calendarRules,
                seasons,
                climateCoordinator,
                desireLines,
                seasonAnnouncement
        );
        PaperHudModule hud = new PaperHudModule(
                this,
                PaperHudSettingsLoader.load(getConfig()),
                calendarModule,
                messages,
                new HeadingPolicy(),
                new ApparentTemperaturePolicy(),
                new TemperatureColorPolicy()
        );
        PaperEcologySettings ecologySettings = PaperEcologySettingsLoader.load(getConfig());
        PaperLocalClimateResolver localClimate = new PaperLocalClimateResolver(
                calendarWorld,
                calendarModule,
                climateClassifier,
                climatePolicy
        );
        PaperNaturalGrowthModule naturalGrowth = new PaperNaturalGrowthModule(
                this,
                calendarWorld,
                ecologySettings,
                localClimate,
                new NaturalGrowthSuitabilityPolicy()
        );
        PaperFrozenSurfaceModule frozenSurfaces = new PaperFrozenSurfaceModule(
                this,
                calendarWorld,
                ecologySettings,
                localClimate,
                new FrozenSurfacePolicy()
        );
        PaperGroundCoverSpreadModule groundCoverSpread =
                new PaperGroundCoverSpreadModule(
                        this,
                        calendarWorld,
                        ecologySettings,
                        localClimate,
                        new NaturalGrowthSuitabilityPolicy()
                );
        PaperFarmlandMoistureModule farmlandMoisture =
                new PaperFarmlandMoistureModule(
                        this,
                        calendarWorld,
                        ecologySettings,
                        localClimate,
                        new FarmlandMoistureRetentionPolicy()
                );
        PaperFireSpreadModule fireSpread = new PaperFireSpreadModule(
                this,
                calendarWorld,
                ecologySettings,
                localClimate,
                new FireSpreadSuitabilityPolicy()
        );

        LivingWorldStatusProvider statusProvider = () -> {
            var date = calendarModule.currentDate();
            var season = calendarModule.currentSeason();
            return new LivingWorldStatusSnapshot(
                    getPluginMeta().getVersion(),
                    calendarWorld.getName(),
                    messages.text(switch (season) {
                        case PRIMAVERA -> "season.spring";
                        case VERAO -> "season.summer";
                        case OUTONO -> "season.autumn";
                        case INVERNO -> "season.winter";
                    }),
                    date.year(),
                    date.month(),
                    date.day(),
                    getServer().getOnlinePlayers().size(),
                    hud.activeSessionCount(),
                    desireLines.cachedChunkCount(),
                    waystones.allWaystones().size(),
                    getServer().getTPS()[0],
                    getServer().getAverageTickTime()
            );
        };

        moduleManager = new ModuleManager(
                desireLines,
                calendarModule,
                naturalGrowth,
                frozenSurfaces,
                groundCoverSpread,
                farmlandMoisture,
                fireSpread,
                new PaperDoubleDoorsModule(
                        this,
                        getConfig().getBoolean("qol.double-doors.enabled", true)
                ),
                new PaperWaystoneModule(
                        this,
                        waystoneSettings,
                        waystones,
                        waystoneTravel,
                        messages,
                        statusProvider
                ),
                hud
        );

        try {
            moduleManager.enableAll();
            getLogger().info(messages.text("plugin.enabled"));
        } catch (RuntimeException exception) {
            getLogger().severe(messages.text("plugin.enable-failed", exception.getMessage()));
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (moduleManager != null) {
            try {
                moduleManager.disableAll();
            } catch (RuntimeException exception) {
                getLogger().log(
                        Level.SEVERE,
                        "Falha ao desabilitar um ou mais módulos do Living World",
                        exception
                );
            }
        }
    }
}
