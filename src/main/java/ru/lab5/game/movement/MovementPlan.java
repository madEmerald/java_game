package ru.lab5.game.movement;

import java.util.Objects;

/** План сначала проверяется; изменение состояния выполняет контекст. */
public record MovementPlan(MovementMode mode, double seconds, double staminaCost,
                           int healthDelta, String effect, boolean surfaceIgnored) {
    public MovementPlan {
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(effect, "effect");
        if (!Double.isFinite(seconds) || seconds <= 0
                || !Double.isFinite(staminaCost) || staminaCost < 0)
            throw new IllegalArgumentException("Некорректный план движения");
    }
}
