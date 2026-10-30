package ru.lab5.game.movement;

import ru.lab5.game.model.WorldMap;

/** Временная стратегия группы. Врождённые крылья не требуются. */
public final class MagicFlightStrategy implements MovementStrategy {
    public static final double SPEED = 6.0;
    public static final double STAMINA_PER_CELL = 1.0;
    @Override public MovementMode mode() { return MovementMode.MAGIC_FLY; }
    @Override public MovementPlan plan(MovementRequest request) {
        // Не читаем surfaceEffect(), в том числе walkable и лечебные свойства.
        return new MovementPlan(mode(), request.distanceMeters() / SPEED,
                STAMINA_PER_CELL * request.distanceMeters() / WorldMap.CELL_METERS,
                0, "Левитация: поверхность полностью игнорируется", true);
    }
}
