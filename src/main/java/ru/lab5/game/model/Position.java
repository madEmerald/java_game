package ru.lab5.game.model;

/** Координаты клетки; начало координат — левый верхний угол. */
public record Position(int x, int y) {
    public boolean isAdjacentTo(Position other) {
        return Math.abs((long) x - other.x) + Math.abs((long) y - other.y) == 1;
    }
    @Override public String toString() { return "(" + x + ", " + y + ")"; }
}
