package ru.lab5.game.movement;

import ru.lab5.game.model.SurfaceEffect;
import ru.lab5.game.model.WorldMap;

public final class WalkingStrategy implements MovementStrategy {
    @Override public MovementMode mode() { return MovementMode.WALK; }
    @Override public MovementPlan plan(MovementRequest request) {
        if (!request.species().supports(mode()))
            throw new IllegalStateException("Этот вид не умеет ходить");
        SurfaceEffect effect = request.terrain().surfaceEffect();
        if (!effect.walkable())
            throw new IllegalStateException("Пеший проход невозможен: " + request.terrain().displayName());
        return new MovementPlan(mode(),
                request.distanceMeters() / (request.species().walkingSpeed() * effect.speedMultiplier()),
                request.species().walkingCost() * request.distanceMeters() / WorldMap.CELL_METERS
                        * effect.staminaMultiplier(),
                effect.healthDelta(), effect.description(), false);
    }
}
