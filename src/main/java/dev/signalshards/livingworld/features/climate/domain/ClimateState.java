package dev.signalshards.livingworld.features.climate.domain;

/**
 * Anomalias climáticas transitórias e limitadas.
 *
 * <p>Os deslocamentos de temperatura e umidade ficam entre -2 e +2. A pressão
 * de tempestade fica entre 0 e 2. Dessa forma, nenhum evento pode acumular uma
 * alteração sem limite ao longo de uma sessão longa.</p>
 */
public record ClimateState(int temperatureShift, int moistureShift, int stormPressure) {
    private static final int MIN_SHIFT = -2;
    private static final int MAX_SHIFT = 2;
    private static final int MIN_STORM_PRESSURE = 0;
    private static final int MAX_STORM_PRESSURE = 2;

    public ClimateState {
        if (temperatureShift < MIN_SHIFT || temperatureShift > MAX_SHIFT) {
            throw new IllegalArgumentException("O deslocamento de temperatura deve ficar entre -2 e 2");
        }
        if (moistureShift < MIN_SHIFT || moistureShift > MAX_SHIFT) {
            throw new IllegalArgumentException("O deslocamento de umidade deve ficar entre -2 e 2");
        }
        if (stormPressure < MIN_STORM_PRESSURE || stormPressure > MAX_STORM_PRESSURE) {
            throw new IllegalArgumentException("A pressão de tempestade deve ficar entre 0 e 2");
        }
    }

    public static ClimateState stable() {
        return new ClimateState(0, 0, 0);
    }

    public ClimateState adjustTemperature(int amount) {
        return new ClimateState(clampShift(temperatureShift + amount), moistureShift, stormPressure);
    }

    public ClimateState adjustMoisture(int amount) {
        return new ClimateState(temperatureShift, clampShift(moistureShift + amount), stormPressure);
    }

    public ClimateState adjustStormPressure(int amount) {
        int adjusted = Math.max(
                MIN_STORM_PRESSURE,
                Math.min(MAX_STORM_PRESSURE, stormPressure + amount)
        );
        return new ClimateState(temperatureShift, moistureShift, adjusted);
    }

    private static int clampShift(int value) {
        return Math.max(MIN_SHIFT, Math.min(MAX_SHIFT, value));
    }
}
