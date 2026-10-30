package ru.lab5.game.movement;

import ru.lab5.game.model.Species;
import ru.lab5.game.model.Terrain;
import java.util.Objects;

/** Исходная и целевая поверхности доступны стратегии без возможности менять персонажа. */
public record MovementRequest(Species species, Terrain sourceTerrain, Terrain terrain, double distanceMeters) {
    public MovementRequest {
        Objects.requireNonNull(species, "species");
        Objects.requireNonNull(sourceTerrain, "sourceTerrain");
        Objects.requireNonNull(terrain, "terrain");
        if (!Double.isFinite(distanceMeters) || distanceMeters <= 0)
            throw new IllegalArgumentException("Расстояние должно быть конечным и положительным");
    }
}
