package ru.lab5.game;

import ru.lab5.game.model.*;
import ru.lab5.game.movement.*;
import ru.lab5.game.ui.MapPanel;
import java.util.*;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import javax.swing.SwingUtilities;

/** Автономные проверки без JUnit и внешних зависимостей. */
public final class TestRunner {
    private static int passed;
    private static int failed;
    private TestRunner() { }
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private static void near(double actual, double expected) {
        check(Math.abs(actual - expected) < 1e-9, "Ожидалось " + expected + ", получено " + actual);
    }
    private static void fails(Class<? extends Throwable> type, Runnable action) {
        try { action.run(); }
        catch (Throwable throwable) {
            if (type.isInstance(throwable)) return;
            throw new AssertionError("Неверный тип исключения: " + throwable, throwable);
        }
        throw new AssertionError("Ожидалось исключение: " + type.getSimpleName());
    }
    private static void test(String name, Runnable action) {
        try { action.run(); passed++; System.out.println("PASS " + name); }
        catch (Throwable throwable) {
            failed++; System.out.println("FAIL " + name + ": " + throwable); throwable.printStackTrace(System.out);
        }
    }
    private static WorldMap pair(Terrain terrain) {
        return new WorldMap(new Terrain[][]{{TerrainType.PLAIN, terrain}});
    }
    private static GameCharacter actor(Species species) {
        return new GameCharacter("Тест", species, new Position(0, 0), 70, 100);
    }
    private record Snapshot(Position position, int health, double stamina, double elapsed, MovementMode mode) {
        static Snapshot of(GameCharacter actor) {
            return new Snapshot(actor.position(), actor.health(), actor.stamina(), actor.elapsedSeconds(), actor.movementMode());
        }
    }
    public static void main(String[] args) {
        Position target = new Position(1, 0);
        test("Равнина: базовые время и выносливость", () -> {
            GameCharacter orc = actor(Species.ORC);
            MoveResult result = orc.moveTo(pair(TerrainType.PLAIN), target);
            near(result.seconds(), 4); near(result.staminaSpent(), 6); check(orc.health() == 70, "HP");
        });
        test("Дорога ускоряет и экономит силы", () -> {
            MoveResult result = actor(Species.ORC).moveTo(pair(TerrainType.ROAD), target);
            near(result.seconds(), 10 / (2.5 * 1.3)); near(result.staminaSpent(), 4.8);
        });
        test("Болото замедляет, расходует силы, наносит урон", () -> {
            GameCharacter orc = actor(Species.ORC);
            MoveResult result = orc.moveTo(pair(TerrainType.SWAMP), target);
            near(result.seconds(), 10 / (2.5 * .45)); near(result.staminaSpent(), 10.8);
            check(orc.health() == 65 && result.healthChange() == -5, "HP");
        });
        test("Лёд наносит 2 урона", () -> {
            GameCharacter orc = actor(Species.ORC);
            orc.moveTo(pair(TerrainType.ICE), target); check(orc.health() == 68, "HP");
        });
        test("Лава наносит 20 урона", () -> {
            GameCharacter orc = actor(Species.ORC);
            orc.moveTo(pair(TerrainType.LAVA), target); check(orc.health() == 50, "HP");
        });
        test("Роща лечит, HP ограничено максимумом", () -> {
            GameCharacter orc = new GameCharacter("Тест", Species.ORC, new Position(0, 0), 95, 100);
            MoveResult result = orc.moveTo(pair(TerrainType.GROVE), target);
            check(orc.health() == 100 && result.healthChange() == 5, "Ограничение лечения");
        });
        test("Пропасть: пеший шаг не меняет состояние", () -> {
            GameCharacter orc = actor(Species.ORC); Snapshot before = Snapshot.of(orc);
            fails(IllegalStateException.class, () -> orc.moveTo(pair(TerrainType.CHASM), target));
            check(before.equals(Snapshot.of(orc)), "Частичное изменение");
        });
        for (Species species : List.of(Species.PEGASUS, Species.VAMPIRE, Species.HARPY)) {
            for (TerrainType terrain : TerrainType.values()) {
                test("Естественный полёт " + species + " / " + terrain, () -> {
                    GameCharacter actor = actor(species); actor.setMovementMode(MovementMode.FLY);
                    MoveResult result = actor.moveTo(pair(terrain), target);
                    near(result.seconds(), 10 / species.flyingSpeed());
                    near(result.staminaSpent(), species.flyingCost());
                    check(actor.health() == 70 && result.surfaceIgnored(), "Эффект поверхности при полёте");
                });
            }
        }
        for (TerrainType terrain : TerrainType.values()) {
            test("Магический полёт / " + terrain, () -> {
                GameCharacter orc = actor(Species.ORC), troll = actor(Species.TROLL);
                Party party = new Party(List.of(orc, troll), 100); party.castFlight(pair(terrain));
                PartyMoveResult result = party.moveTo(pair(terrain), target);
                near(result.seconds(), 10 / MagicFlightStrategy.SPEED);
                check(party.mana() == 86, "Стоимость заклинания и шага");
                for (GameCharacter actor : party.members()) {
                    check(actor.health() == 70, "HP при магическом полёте"); near(actor.stamina(), 99);
                }
            });
        }
        test("Полёт не читает surfaceEffect вообще", () -> {
            Terrain explosive = new Terrain() {
                public String displayName() { return "Поверхность-ловушка"; }
                public char symbol() { return '?'; }
                public SurfaceEffect surfaceEffect() { throw new AssertionError("Летающий прочитал поверхность"); }
            };
            WorldMap map = new WorldMap(new Terrain[][]{{explosive, explosive}});
            GameCharacter pegasus = actor(Species.PEGASUS); pegasus.setMovementMode(MovementMode.FLY);
            pegasus.moveTo(map, target);
            GameCharacter orc = actor(Species.ORC);
            Party party = new Party(List.of(orc), 50); party.castFlight(map); party.moveTo(map, target);
        });
        test("Орк не может летать без магии", () ->
                fails(IllegalArgumentException.class, () -> actor(Species.ORC).setMovementMode(MovementMode.FLY)));
        test("Гарпия не ходит", () ->
                fails(IllegalArgumentException.class, () -> actor(Species.HARPY).setMovementMode(MovementMode.WALK)));
        test("Стратегия меняется у того же объекта", () -> {
            GameCharacter pegasus = actor(Species.PEGASUS); WorldMap map = WorldMap.fromRows(".LLL");
            pegasus.moveTo(map, new Position(1, 0)); pegasus.setMovementMode(MovementMode.FLY);
            pegasus.moveTo(map, new Position(2, 0)); pegasus.setMovementMode(MovementMode.WALK);
            pegasus.moveTo(map, new Position(3, 0)); check(pegasus.health() == 30, "Смена стратегии");
        });
        test("Недостаточно сил: состояние сохраняется", () -> {
            GameCharacter orc = new GameCharacter("Уставший", Species.ORC, new Position(0, 0), 100, 1);
            Snapshot before = Snapshot.of(orc);
            fails(IllegalStateException.class, () -> orc.moveTo(pair(TerrainType.LAVA), target));
            check(before.equals(Snapshot.of(orc)), "Частичное изменение");
        });
        test("Ровно достаточно сил: шаг разрешён", () -> {
            GameCharacter orc = new GameCharacter("Тест", Species.ORC, new Position(0, 0), 100, 6);
            orc.moveTo(pair(TerrainType.PLAIN), target); near(orc.stamina(), 0);
        });
        test("Погибший не движется и не оживает в роще", () -> {
            GameCharacter orc = new GameCharacter("Тест", Species.ORC, new Position(0, 0), 0, 100);
            fails(IllegalStateException.class, () -> orc.moveTo(pair(TerrainType.GROVE), target));
        });
        test("Смерть от лавы: HP не отрицательно", () -> {
            GameCharacter orc = new GameCharacter("Тест", Species.ORC, new Position(0, 0), 5, 100);
            orc.moveTo(pair(TerrainType.LAVA), target); check(orc.health() == 0, "HP ниже нуля");
        });
        test("Диагональ запрещена", () -> fails(IllegalArgumentException.class,
                () -> actor(Species.ORC).moveTo(WorldMap.fromRows("..", ".."), new Position(1, 1))));
        test("Телепортация запрещена", () -> fails(IllegalArgumentException.class,
                () -> actor(Species.ORC).moveTo(WorldMap.fromRows("..."), new Position(2, 0))));
        test("Выход за карту запрещён", () -> fails(IllegalArgumentException.class,
                () -> actor(Species.ORC).moveTo(pair(TerrainType.PLAIN), new Position(-1, 0))));
        test("Стоять на месте — не ход", () -> fails(IllegalArgumentException.class,
                () -> actor(Species.ORC).moveTo(pair(TerrainType.PLAIN), new Position(0, 0))));
        test("Нельзя начать ходить над пропастью", () -> {
            GameCharacter actor = actor(Species.PEGASUS); WorldMap map = WorldMap.fromRows(".X.");
            actor.setMovementMode(MovementMode.FLY); actor.moveTo(map, target); actor.setMovementMode(MovementMode.WALK);
            Snapshot before = Snapshot.of(actor);
            fails(IllegalStateException.class, () -> actor.moveTo(map, new Position(2, 0)));
            check(before.equals(Snapshot.of(actor)), "Отказ изменил состояние персонажа");
        });
        test("WalkingStrategy сама запрещает ходьбу с непроходимой поверхности", () -> {
            MovementRequest request = new MovementRequest(
                    Species.PEGASUS, TerrainType.CHASM, TerrainType.PLAIN, WorldMap.CELL_METERS);
            fails(IllegalStateException.class, () -> new WalkingStrategy().plan(request));
        });
        test("Ходьба учитывает эффекты только клетки назначения", () -> {
            GameCharacter orc = actor(Species.ORC);
            WorldMap map = new WorldMap(new Terrain[][]{{TerrainType.LAVA, TerrainType.PLAIN}});
            MoveResult result = orc.moveTo(map, target);
            near(result.seconds(), 4); near(result.staminaSpent(), 6);
            check(orc.health() == 70 && result.healthChange() == 0, "Применён эффект исходной клетки");
        });
        test("Естественный полёт может начинаться над пропастью", () -> {
            GameCharacter pegasus = actor(Species.PEGASUS);
            pegasus.setMovementMode(MovementMode.FLY);
            WorldMap map = new WorldMap(new Terrain[][]{{TerrainType.CHASM, TerrainType.LAVA}});
            MoveResult result = pegasus.moveTo(map, target);
            near(result.seconds(), 10 / Species.PEGASUS.flyingSpeed());
            near(result.staminaSpent(), Species.PEGASUS.flyingCost());
            check(pegasus.health() == 70 && result.surfaceIgnored(), "Наземные ограничения при полёте");
        });
        test("Магический полёт может начинаться над пропастью", () -> {
            GameCharacter orc = actor(Species.ORC);
            Party party = new Party(List.of(orc), 50);
            WorldMap map = new WorldMap(new Terrain[][]{{TerrainType.CHASM, TerrainType.LAVA}});
            party.castFlight(map);
            party.moveTo(map, target);
            check(orc.position().equals(target) && orc.health() == 70, "Наземные ограничения при магии");
            near(orc.stamina(), 99);
            check(party.mana() == 38 && party.isFlying(), "Изменены правила магии");
        });
        test("MovementRequest требует исходную поверхность", () ->
                fails(NullPointerException.class, () -> new MovementRequest(
                        Species.ORC, null, TerrainType.PLAIN, WorldMap.CELL_METERS)));
        test("Заклинание восстанавливает прежние, а не стандартные режимы", () -> {
            GameCharacter pegasus = actor(Species.PEGASUS); pegasus.setMovementMode(MovementMode.FLY);
            GameCharacter orc = actor(Species.ORC), harpy = actor(Species.HARPY);
            Party party = new Party(List.of(pegasus, orc, harpy), 100); WorldMap map = pair(TerrainType.PLAIN);
            party.castFlight(map); party.endFlight(map);
            check(pegasus.movementMode() == MovementMode.FLY && orc.movementMode() == MovementMode.WALK
                    && harpy.movementMode() == MovementMode.FLY, "Режимы не восстановлены");
        });
        test("Приземление в роще: лечатся только приземлившиеся", () -> {
            GameCharacter orc = actor(Species.ORC), harpy = actor(Species.HARPY);
            Party party = new Party(List.of(orc, harpy), 100); WorldMap map = pair(TerrainType.GROVE);
            party.castFlight(map); party.moveTo(map, target);
            check(orc.health() == 70 && harpy.health() == 70, "Лечение во время полёта");
            party.endFlight(map); check(orc.health() == 80 && harpy.health() == 70, "Лечение при посадке");
        });
        test("Приземление на лаву: урон только наземным участникам", () -> {
            GameCharacter orc = actor(Species.ORC), harpy = actor(Species.HARPY);
            Party party = new Party(List.of(orc, harpy), 100); WorldMap map = pair(TerrainType.LAVA);
            party.castFlight(map); party.moveTo(map, target); party.endFlight(map);
            check(orc.health() == 50 && harpy.health() == 70, "Посадка");
        });
        test("Нельзя посадить наземную группу в пропасть; атомарный отказ", () -> {
            GameCharacter orc = actor(Species.ORC), harpy = actor(Species.HARPY);
            Party party = new Party(List.of(orc, harpy), 100); WorldMap map = pair(TerrainType.CHASM);
            party.castFlight(map); party.moveTo(map, target); int mana = party.mana();
            fails(IllegalStateException.class, () -> party.endFlight(map));
            check(party.isFlying() && orc.isUnderMagic() && harpy.isUnderMagic() && party.mana() == mana, "Частичная посадка");
        });
        test("Воздушная группа может завершить магию над пропастью", () -> {
            GameCharacter harpy = actor(Species.HARPY); WorldMap map = pair(TerrainType.CHASM);
            Party party = new Party(List.of(harpy), 30); party.castFlight(map); party.moveTo(map, target); party.endFlight(map);
            check(harpy.movementMode() == MovementMode.FLY, "Естественный полёт");
        });
        test("Недостаток сил одного: никто не движется", () -> {
            GameCharacter orc = actor(Species.ORC);
            GameCharacter troll = new GameCharacter("Уставший", Species.TROLL, new Position(0, 0), 70, 0);
            WorldMap map = pair(TerrainType.PLAIN); Party party = new Party(List.of(orc, troll), 100); party.castFlight(map);
            Snapshot a = Snapshot.of(orc), b = Snapshot.of(troll); int mana = party.mana();
            fails(IllegalStateException.class, () -> party.moveTo(map, target));
            check(a.equals(Snapshot.of(orc)) && b.equals(Snapshot.of(troll)) && party.mana() == mana, "Неатомарный шаг");
        });
        test("Недостаток маны на шаг: никто не движется", () -> {
            GameCharacter orc = actor(Species.ORC); WorldMap map = pair(TerrainType.PLAIN);
            Party party = new Party(List.of(orc), 11); party.castFlight(map); Snapshot before = Snapshot.of(orc);
            fails(IllegalStateException.class, () -> party.moveTo(map, target));
            check(before.equals(Snapshot.of(orc)) && party.mana() == 1 && party.isFlying(), "Состояние изменилось");
        });
        test("Недостаток маны на заклинание: режим не меняется", () -> {
            GameCharacter orc = actor(Species.ORC); Party party = new Party(List.of(orc), 9);
            fails(IllegalStateException.class, () -> party.castFlight(pair(TerrainType.PLAIN)));
            check(!orc.isUnderMagic() && orc.movementMode() == MovementMode.WALK && party.mana() == 9, "Частичное заклинание");
        });
        test("Группа должна быть собрана", () -> {
            Party party = new Party(List.of(actor(Species.ORC),
                    new GameCharacter("Тест2", Species.TROLL, target)), 100);
            fails(IllegalStateException.class, () -> party.castFlight(pair(TerrainType.PLAIN)));
        });
        test("Погибший не входит в магический полёт", () -> {
            Party party = new Party(List.of(new GameCharacter("Тест", Species.ORC, new Position(0, 0), 0, 100)), 100);
            fails(IllegalStateException.class, () -> party.castFlight(pair(TerrainType.PLAIN)));
        });
        test("Нельзя двигаться отдельно от зачарованной группы", () -> {
            GameCharacter orc = actor(Species.ORC); Party party = new Party(List.of(orc), 100);
            WorldMap map = pair(TerrainType.PLAIN); party.castFlight(map);
            fails(IllegalStateException.class, () -> orc.moveTo(map, target));
            fails(IllegalStateException.class, () -> orc.setMovementMode(MovementMode.WALK));
        });
        test("Повторное заклинание не списывает ману", () -> {
            Party party = new Party(List.of(actor(Species.ORC)), 100); WorldMap map = pair(TerrainType.PLAIN);
            party.castFlight(map); fails(IllegalStateException.class, () -> party.castFlight(map));
            check(party.mana() == 90, "Повторное списание маны");
        });
        test("Две группы не могут зачаровать одного участника", () -> {
            GameCharacter orc = actor(Species.ORC); GameCharacter troll = actor(Species.TROLL);
            Party first = new Party(List.of(orc), 100), second = new Party(List.of(troll, orc), 100);
            WorldMap map = pair(TerrainType.PLAIN); first.castFlight(map);
            fails(IllegalStateException.class, () -> second.castFlight(map));
            check(!troll.isUnderMagic() && second.mana() == 100, "Частичное заклинание второй группы");
        });
        test("Группа без магии не движется совместно", () -> {
            Party party = new Party(List.of(actor(Species.ORC)), 100);
            fails(IllegalStateException.class, () -> party.moveTo(pair(TerrainType.PLAIN), target));
            fails(IllegalStateException.class, () -> party.endFlight(pair(TerrainType.PLAIN)));
        });
        test("Пустая группа и дубликаты запрещены", () -> {
            fails(IllegalArgumentException.class, () -> new Party(List.of(), 10));
            GameCharacter orc = actor(Species.ORC);
            fails(IllegalArgumentException.class, () -> new Party(List.of(orc, orc), 10));
        });
        test("Некорректные ресурсы запрещены", () -> {
            fails(IllegalArgumentException.class, () -> new GameCharacter("Тест", Species.ORC, target, 101, 10));
            fails(IllegalArgumentException.class, () -> new GameCharacter("Тест", Species.ORC, target, 100, Double.NaN));
            fails(IllegalArgumentException.class, () -> new Party(List.of(actor(Species.ORC)), -1));
        });
        test("Непрямоугольная или пустая карта запрещена", () -> {
            fails(IllegalArgumentException.class, () -> WorldMap.fromRows(".", ".."));
            fails(IllegalArgumentException.class, () -> WorldMap.fromRows());
            fails(IllegalArgumentException.class, () -> WorldMap.fromRows(""));
        });
        test("Карта защищена от изменения исходного массива", () -> {
            Terrain[][] cells = {{TerrainType.PLAIN}}; WorldMap map = new WorldMap(cells); cells[0][0] = TerrainType.LAVA;
            check(map.terrainAt(new Position(0, 0)) == TerrainType.PLAIN, "Не сделана копия");
        });
        test("Все карты имеют ожидаемый размер и допустимый старт", () -> {
            for (MapPreset preset : MapPreset.values()) {
                WorldMap map = preset.createMap();
                check(map.width() == 10 && map.height() == 7, "Размер карты");
                check(map.terrainAt(preset.start()).surfaceEffect().walkable(), "Недопустимый старт");
            }
        });
        test("Список группы нельзя менять снаружи", () -> {
            List<GameCharacter> source = new ArrayList<>(List.of(actor(Species.ORC))); Party party = new Party(source, 10);
            source.clear(); check(party.members().size() == 1, "Нет копии списка");
            fails(UnsupportedOperationException.class, () -> party.members().clear());
        });
        test("Крайние координаты не переполняют проверку соседства", () ->
                check(!new Position(Integer.MIN_VALUE, 0).isAdjacentTo(new Position(Integer.MAX_VALUE, 0)), "Переполнение"));
        test("Отрисовка Swing-карты в headless-режиме", () -> {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    MapPanel panel = new MapPanel(); panel.setSize(760, 550);
                    GameCharacter harpy = new GameCharacter("Эхо", Species.HARPY, MapPreset.VALLEY.start());
                    panel.setScene(MapPreset.VALLEY.createMap(), List.of(harpy), harpy);
                    BufferedImage image = new BufferedImage(760, 550, BufferedImage.TYPE_INT_RGB);
                    Graphics2D graphics = image.createGraphics(); panel.paint(graphics); graphics.dispose();
                    check(image.getRGB(400, 300) != 0, "Пустое изображение");
                });
            } catch (Exception exception) { throw new AssertionError(exception); }
        });
        System.out.println("\nИТОГО: " + passed + " пройдено, " + failed + " ошибок.");
        if (failed > 0) System.exit(1);
    }
}
