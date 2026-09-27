package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.climate.application.WeatherEventSettings;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaperWeatherEventSettingsLoaderTest {
    @Test
    void carregaDuracoesConfiguradas() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("climate.weather-events.clear-duration-seconds", 120);
        config.set("climate.weather-events.precipitation-duration-seconds", 90);
        config.set("climate.weather-events.storm-duration-seconds", 60);

        WeatherEventSettings settings = PaperWeatherEventSettingsLoader.load(config);

        assertEquals(Duration.ofSeconds(120), settings.clearDuration());
        assertEquals(Duration.ofSeconds(90), settings.precipitationDuration());
        assertEquals(Duration.ofSeconds(60), settings.stormDuration());
    }

    @Test
    void usaPadroesQuandoConfigNaoDefineDuracoes() {
        WeatherEventSettings settings = PaperWeatherEventSettingsLoader.load(new YamlConfiguration());

        assertEquals(WeatherEventSettings.defaults(), settings);
    }
}
