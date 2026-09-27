package dev.signalshards.livingworld;

import dev.signalshards.livingworld.core.i18n.MessageCatalog;
import dev.signalshards.livingworld.core.module.ModuleManager;
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
import dev.signalshards.livingworld.features.climate.paper.PaperWeatherController;
import dev.signalshards.livingworld.features.climate.paper.PaperWeatherEventSettingsLoader;
import dev.signalshards.livingworld.features.desirelines.paper.PaperDesireLinesModule;
import dev.signalshards.livingworld.features.desirelines.paper.PaperPathWearSettingsLoader;
import dev.signalshards.livingworld.features.ecology.domain.NaturalGrowthSuitabilityPolicy;
import dev.signalshards.livingworld.features.ecology.paper.PaperEcologySettingsLoader;
import dev.signalshards.livingworld.features.ecology.paper.PaperNaturalGrowthModule;
import dev.signalshards.livingworld.features.qol.doubledoors.paper.PaperDoubleDoorsModule;
import dev.signalshards.livingworld.features.hud.domain.HeadingPolicy;
import dev.signalshards.livingworld.features.hud.paper.PaperHudModule;
import dev.signalshards.livingworld.features.hud.paper.PaperHudSettingsLoader;
import dev.signalshards.livingworld.features.hud.paper.TemperatureColorPolicy;
import dev.signalshards.livingworld.features.seasons.domain.SeasonCycle;
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
        PaperClimateCoordinator climateCoordinator = new PaperClimateCoordinator(
                calendarWorld,
                new PaperClimateSampler(
                        new ClimateProfileClassifier(
                                ClimateProfileThresholds.livingWorldDefaults()
                        )
                ),
                new ClimatePolicy(),
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

        PaperCalendarModule calendarModule = new PaperCalendarModule(
                this,
                calendarWorld,
                messages,
                calendarRules,
                seasons,
                climateCoordinator,
                desireLines
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
        PaperNaturalGrowthModule naturalGrowth = new PaperNaturalGrowthModule(
                this,
                calendarWorld,
                PaperEcologySettingsLoader.load(getConfig()),
                calendarModule,
                new ClimateProfileClassifier(
                        ClimateProfileThresholds.livingWorldDefaults()
                ),
                new ClimatePolicy(),
                new NaturalGrowthSuitabilityPolicy()
        );

        moduleManager = new ModuleManager(
                desireLines,
                calendarModule,
                naturalGrowth,
                new PaperDoubleDoorsModule(
                        this,
                        getConfig().getBoolean("qol.double-doors.enabled", true)
                ),
                new PaperWaystoneModule(
                        this,
                        waystoneSettings,
                        waystones,
                        waystoneTravel,
                        messages
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
            moduleManager.disableAll();
        }
    }
}
