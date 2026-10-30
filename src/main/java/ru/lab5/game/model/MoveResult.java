package ru.lab5.game.model;

import ru.lab5.game.movement.MovementMode;
import java.util.Locale;

public record MoveResult(String characterName, Position from, Position to,
                         String terrain, MovementMode mode, double seconds,
                         double staminaSpent, int healthChange, String effect,
                         boolean surfaceIgnored) {
    @Override public String toString() {
        return String.format(Locale.ROOT,
                "%s: %s -> %s; %s; %s; %.2f с; силы -%.2f; HP %+d; %s",
                characterName, from, to, mode, terrain, seconds, staminaSpent, healthChange, effect);
    }
}
