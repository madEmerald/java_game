package ru.lab5.game.movement;

import ru.lab5.game.model.Species;
import ru.lab5.game.model.Terrain;
import java.util.Objects;

/** Вход стратегии не даёт ей возможности менять персонажа. */
public record MovementRequest(Species species, Terrain terrain, double distanceMeters) {
    public MovementRequest {
        Objects.requireNonNull(species, "species");
        Objects.requireNonNull(terrain, "terrain");
        if (!Double.isFinite(distanceMeters) || distanceMeters <= 0)
            throw new IllegalArgumentException("Расстояние должно быть конечным и положительным");
    }
}
