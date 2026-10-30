package ru.lab5.game;

import ru.lab5.game.model.*;
import ru.lab5.game.movement.MovementMode;
import java.util.*;

/** Детерминированная демонстрация; не требует графического окружения. */
public final class Demo {
    private Demo() { }
    public static void main(String[] args) {
        System.out.println("Лабораторная №5, вариант d — Strategy");
        System.out.println("Один шаг = 10 метров; эффекты применяются к клетке назначения.\n");
        System.out.println("1. Одинаковый орк на разных поверхностях:");
        for (TerrainType terrain : TerrainType.values()) {
            WorldMap map = new WorldMap(new Terrain[][] {{TerrainType.PLAIN, terrain}});
            GameCharacter orc = new GameCharacter("Гром", Species.ORC, new Position(0, 0), 80, 100);
            try { System.out.println(orc.moveTo(map, new Position(1, 0))); }
            catch (IllegalStateException ex) { System.out.println(terrain.displayName() + ": " + ex.getMessage()); }
        }
        System.out.println("\n2. Пегас летит: даже лечение и пропасть не меняют результат:");
        for (TerrainType terrain : TerrainType.values()) {
            WorldMap map = new WorldMap(new Terrain[][] {{TerrainType.PLAIN, terrain}});
            GameCharacter pegasus = new GameCharacter("Астра", Species.PEGASUS, new Position(0, 0), 80, 100);
            pegasus.setMovementMode(MovementMode.FLY);
            System.out.println(pegasus.moveTo(map, new Position(1, 0)));
        }
        System.out.println("\n3. Смена стратегии у одного объекта:");
        WorldMap road = WorldMap.fromRows(".LLL");
        GameCharacter vampire = new GameCharacter("Нокс", Species.VAMPIRE, new Position(0, 0));
        System.out.println(vampire.moveTo(road, new Position(1, 0)));
        vampire.setMovementMode(MovementMode.FLY);
        System.out.println(vampire.moveTo(road, new Position(2, 0)));
        vampire.setMovementMode(MovementMode.WALK);
        System.out.println(vampire.moveTo(road, new Position(3, 0)));
        System.out.println("\n4. Орк, тролль и гарпия вместе летят с помощью магии:");
        WorldMap route = WorldMap.fromRows(".LXH.");
        List<GameCharacter> characters = List.of(
                new GameCharacter("Гром", Species.ORC, new Position(0, 0), 70, 100),
                new GameCharacter("Бруг", Species.TROLL, new Position(0, 0), 70, 100),
                new GameCharacter("Эхо", Species.HARPY, new Position(0, 0), 70, 100));
        Party party = new Party(characters, 100);
        party.castFlight(route);
        for (int x = 1; x <= 3; x++) {
            PartyMoveResult result = party.moveTo(route, new Position(x, 0));
            result.moves().forEach(System.out::println);
            System.out.println("Мана группы: " + party.mana());
        }
        System.out.println("Приземление в роще (не часть полёта):");
        party.endFlight(route).forEach(System.out::println);
        System.out.println("Гарпия остаётся в естественном полёте и не получает даже лечение.");
    }
}
