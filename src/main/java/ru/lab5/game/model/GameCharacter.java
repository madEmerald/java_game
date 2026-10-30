package ru.lab5.game.model;

import ru.lab5.game.movement.*;
import java.util.*;

/** Context: хранит стратегию и делегирует ей расчёт перемещения. */
public final class GameCharacter {
    public static final int MAX_HEALTH = 100;
    public static final double MAX_STAMINA = 100;
    private final String name;
    private final Species species;
    private final EnumMap<MovementMode, MovementStrategy> naturalStrategies;
    private Position position;
    private int health;
    private double stamina;
    private double elapsedSeconds;
    private MovementStrategy strategy;
    private Party magicOwner;

    public GameCharacter(String name, Species species, Position position) {
        this(name, species, position, MAX_HEALTH, MAX_STAMINA);
    }
    public GameCharacter(String name, Species species, Position position, int health, double stamina) {
        this.name = Objects.requireNonNull(name, "name").strip();
        if (this.name.isEmpty()) throw new IllegalArgumentException("Имя пусто");
        this.species = Objects.requireNonNull(species, "species");
        this.position = Objects.requireNonNull(position, "position");
        if (health < 0 || health > MAX_HEALTH || !Double.isFinite(stamina)
                || stamina < 0 || stamina > MAX_STAMINA)
            throw new IllegalArgumentException("Некорректные начальные ресурсы");
        this.health = health;
        this.stamina = stamina;
        naturalStrategies = new EnumMap<>(MovementMode.class);
        registerNaturalStrategy(new WalkingStrategy());
        registerNaturalStrategy(new FlyingStrategy());
        strategy = naturalStrategies.getOrDefault(MovementMode.WALK,
                naturalStrategies.get(MovementMode.FLY));
    }
    private void registerNaturalStrategy(MovementStrategy candidate) {
        if (species.supports(candidate.mode())) naturalStrategies.put(candidate.mode(), candidate);
    }
    public String name() { return name; }
    public Species species() { return species; }
    public Position position() { return position; }
    public int health() { return health; }
    public double stamina() { return stamina; }
    public double elapsedSeconds() { return elapsedSeconds; }
    public boolean isAlive() { return health > 0; }
    public boolean isUnderMagic() { return magicOwner != null; }
    public MovementMode movementMode() { return strategy.mode(); }
    public Set<MovementMode> availableModes() {
        return Collections.unmodifiableSet(naturalStrategies.keySet());
    }
    public void setMovementMode(MovementMode mode) {
        if (magicOwner != null) throw new IllegalStateException("Сперва завершите полёт группы");
        MovementStrategy selected = naturalStrategies.get(Objects.requireNonNull(mode, "mode"));
        if (selected == null) throw new IllegalArgumentException("Недоступный способ передвижения: " + mode);
        strategy = selected;
    }
    public MoveResult moveTo(WorldMap map, Position target) {
        if (magicOwner != null) throw new IllegalStateException("Участник магического полёта движется только с группой");
        MovementPlan plan = prepareMove(map, target);
        return commitMove(map, target, plan);
    }
    MovementPlan prepareMove(WorldMap map, Position target) {
        if (!isAlive()) throw new IllegalStateException(name + ": персонаж погиб");
        Terrain terrain = Objects.requireNonNull(map, "map").destination(position, target);
        // После обычного полёта нужно сначала приземлиться на проходимой клетке.
        if (strategy.mode() == MovementMode.WALK && !map.terrainAt(position).surfaceEffect().walkable())
            throw new IllegalStateException("Нельзя начать ходьбу над пропастью; продолжайте полёт");
        MovementPlan plan = strategy.plan(new MovementRequest(species, terrain, WorldMap.CELL_METERS));
        if (stamina < plan.staminaCost()) throw new IllegalStateException(name + ": недостаточно сил");
        return plan;
    }
    MoveResult commitMove(WorldMap map, Position target, MovementPlan plan) {
        Position oldPosition = position;
        int oldHealth = health;
        position = target;
        stamina = Math.max(0, stamina - plan.staminaCost());
        health = Math.max(0, Math.min(MAX_HEALTH, health + plan.healthDelta()));
        elapsedSeconds += plan.seconds();
        return new MoveResult(name, oldPosition, target, map.terrainAt(target).displayName(),
                plan.mode(), plan.seconds(), plan.staminaCost(), health - oldHealth,
                plan.effect(), plan.surfaceIgnored());
    }
    MovementStrategy beginMagic(Party owner, MovementStrategy magicStrategy) {
        if (magicOwner != null) throw new IllegalStateException("Персонаж уже зачарован");
        MovementStrategy previous = strategy;
        magicOwner = owner;
        strategy = magicStrategy;
        return previous;
    }
    void finishMagic(Party owner, MovementStrategy previous, int landingHealthDelta) {
        if (magicOwner != owner) throw new IllegalStateException("Не владелец заклинания");
        strategy = previous;
        magicOwner = null;
        health = Math.max(0, Math.min(MAX_HEALTH, health + landingHealthDelta));
    }
    @Override public String toString() { return name + " — " + species.displayName(); }
}
