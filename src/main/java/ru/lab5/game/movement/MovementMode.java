package ru.lab5.game.movement;

public enum MovementMode {
    WALK("Ходьба"), FLY("Полёт"), MAGIC_FLY("Магический полёт");
    private final String label;
    MovementMode(String label) { this.label = label; }
    @Override public String toString() { return label; }
}
