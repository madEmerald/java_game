package ru.lab5.game.movement;

/** Strategy: взаимозаменяемый алгоритм расчёта одного шага. */
public interface MovementStrategy {
    MovementMode mode();
    MovementPlan plan(MovementRequest request);
}
