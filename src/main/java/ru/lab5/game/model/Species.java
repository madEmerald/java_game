package ru.lab5.game.model;

import ru.lab5.game.movement.MovementMode;

/** Значения скоростей и затрат — выбранные параметры учебной модели. */
public enum Species {
    ORC("Орк", "O", 2.5, 0, 6, 0),
    TROLL("Тролль", "T", 1.8, 0, 8, 0),
    ELF("Эльф", "E", 3.2, 0, 4, 0),
    PEGASUS("Пегас", "P", 3.8, 8.0, 5, 3),
    VAMPIRE("Вампир", "V", 3.0, 7.0, 4, 4),
    HARPY("Гарпия", "G", 0, 6.5, 0, 4);

    private final String name;
    private final String marker;
    private final double walkingSpeed, flyingSpeed, walkingCost, flyingCost;
    Species(String name, String marker, double walkingSpeed, double flyingSpeed,
            double walkingCost, double flyingCost) {
        this.name = name;
        this.marker = marker;
        this.walkingSpeed = walkingSpeed;
        this.flyingSpeed = flyingSpeed;
        this.walkingCost = walkingCost;
        this.flyingCost = flyingCost;
    }
    public boolean supports(MovementMode mode) {
        return (mode == MovementMode.WALK && walkingSpeed > 0)
                || (mode == MovementMode.FLY && flyingSpeed > 0);
    }
    public String displayName() { return name; }
    public String marker() { return marker; }
    public double walkingSpeed() { return walkingSpeed; }
    public double flyingSpeed() { return flyingSpeed; }
    public double walkingCost() { return walkingCost; }
    public double flyingCost() { return flyingCost; }
    @Override public String toString() { return name; }
}
