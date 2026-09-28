package dev.signalshards.livingworld.features.climate.domain;

/**
 * Categorias Vanilla+ de atividade física relevantes ao calor metabólico.
 *
 * <p>Movimento aéreo/vento não pertence a este enum: por exemplo, Elytra
 * possui pouco esforço metabólico aqui, enquanto sua convecção será tratada
 * por WindExposure em adapters posteriores.</p>
 */
public enum PlayerActivity {
    RESTING,
    WALKING,
    SPRINTING,
    SWIMMING,
    CLIMBING,
    GLIDING
}
