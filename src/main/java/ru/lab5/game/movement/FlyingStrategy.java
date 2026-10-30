package ru.lab5.game.movement;

import ru.lab5.game.model.WorldMap;

public final class FlyingStrategy implements MovementStrategy {
    @Override public MovementMode mode() { return MovementMode.FLY; }
    @Override public MovementPlan plan(MovementRequest request) {
        if (!request.species().supports(mode()))
            throw new IllegalStateException("Этот вид не умеет летать без магии");
        // surfaceEffect() намеренно НЕ вызывается: игнорируются и штрафы, и бонусы.
        return new MovementPlan(mode(), request.distanceMeters() / request.species().flyingSpeed(),
                request.species().flyingCost() * request.distanceMeters() / WorldMap.CELL_METERS,
                0, "Поверхность полностью игнорируется", true);
    }
}
