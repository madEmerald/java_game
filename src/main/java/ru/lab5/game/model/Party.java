package ru.lab5.game.model;

import ru.lab5.game.movement.*;
import java.util.*;

/** Группа с общим запасом маны. Планирование всех шагов предшествует их применению. */
public final class Party {
    public static final int CAST_MANA = 10;
    public static final int MANA_PER_MEMBER_STEP = 2;
    private final List<GameCharacter> members;
    private final Map<GameCharacter, MovementStrategy> previousStrategies = new LinkedHashMap<>();
    private int mana;
    private boolean flying;

    public Party(List<GameCharacter> members, int mana) {
        this.members = List.copyOf(Objects.requireNonNull(members, "members"));
        if (this.members.isEmpty()) throw new IllegalArgumentException("Группа пуста");
        if (new HashSet<>(this.members).size() != this.members.size())
            throw new IllegalArgumentException("Персонаж включён в группу дважды");
        if (mana < 0) throw new IllegalArgumentException("Мана отрицательна");
        this.mana = mana;
    }
    public List<GameCharacter> members() { return members; }
    public int mana() { return mana; }
    public boolean isFlying() { return flying; }
    public int nextMoveManaCost() { return Math.multiplyExact(members.size(), MANA_PER_MEMBER_STEP); }
    private void requireTogether() {
        Position start = members.get(0).position();
        for (GameCharacter member : members) {
            if (!member.position().equals(start)) throw new IllegalStateException("Соберите группу в одной клетке");
            if (!member.isAlive()) throw new IllegalStateException("В группе есть погибший персонаж");
        }
    }
    public void castFlight(WorldMap map) {
        Objects.requireNonNull(map, "map");
        if (flying) throw new IllegalStateException("Заклинание уже активно");
        requireTogether();
        if (mana < CAST_MANA) throw new IllegalStateException("Недостаточно маны для заклинания");
        for (GameCharacter member : members) {
            if (!map.contains(member.position())) throw new IllegalStateException("Участник вне карты");
            if (member.isUnderMagic()) throw new IllegalStateException("Участник уже летит с другой группой");
        }
        MovementStrategy magic = new MagicFlightStrategy();
        for (GameCharacter member : members) previousStrategies.put(member, member.beginMagic(this, magic));
        mana -= CAST_MANA;
        flying = true;
    }
    public PartyMoveResult moveTo(WorldMap map, Position target) {
        if (!flying) throw new IllegalStateException("Сначала наложите магический полёт");
        requireTogether();
        int manaCost = nextMoveManaCost();
        if (mana < manaCost) throw new IllegalStateException("Недостаточно маны на шаг всей группы");
        List<MovementPlan> plans = new ArrayList<>();
        for (GameCharacter member : members) plans.add(member.prepareMove(map, target));
        List<MoveResult> results = new ArrayList<>();
        for (int i = 0; i < members.size(); i++) results.add(members.get(i).commitMove(map, target, plans.get(i)));
        mana -= manaCost;
        double seconds = plans.stream().mapToDouble(MovementPlan::seconds).max().orElseThrow();
        return new PartyMoveResult(results, manaCost, seconds);
    }
    /** Наземные участники приземляются, летающие продолжают естественный полёт. */
    public List<String> endFlight(WorldMap map) {
        Objects.requireNonNull(map, "map");
        if (!flying) throw new IllegalStateException("Заклинание не активно");
        Map<GameCharacter, Integer> landingEffects = new LinkedHashMap<>();
        for (GameCharacter member : members) {
            Terrain terrain = map.terrainAt(member.position());
            MovementStrategy previous = previousStrategies.get(member);
            int healthDelta = 0;
            if (previous.mode() == MovementMode.WALK) {
                SurfaceEffect effect = terrain.surfaceEffect();
                if (!effect.walkable()) throw new IllegalStateException("Нельзя посадить группу в пропасть");
                healthDelta = effect.healthDelta();
            }
            landingEffects.put(member, healthDelta);
        }
        List<String> messages = new ArrayList<>();
        for (GameCharacter member : members) {
            int oldHealth = member.health();
            member.finishMagic(this, previousStrategies.get(member), landingEffects.get(member));
            messages.add(member.name() + ": восстановлен режим «" + member.movementMode()
                    + "», HP " + String.format(Locale.ROOT, "%+d", member.health() - oldHealth));
        }
        previousStrategies.clear();
        flying = false;
        return List.copyOf(messages);
    }
}
