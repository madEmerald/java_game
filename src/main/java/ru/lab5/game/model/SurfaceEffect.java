package ru.lab5.game.model;

/** Неизменяемое описание эффекта входа на поверхность. */
public record SurfaceEffect(double speedMultiplier, double staminaMultiplier,
                            int healthDelta, boolean walkable, String description) {
    public SurfaceEffect {
        if (!Double.isFinite(speedMultiplier) || speedMultiplier <= 0
                || !Double.isFinite(staminaMultiplier) || staminaMultiplier < 0) {
            throw new IllegalArgumentException("Некорректные коэффициенты поверхности");
        }
        java.util.Objects.requireNonNull(description, "description");
    }
}
