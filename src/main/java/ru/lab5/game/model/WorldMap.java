package ru.lab5.game.model;

import java.util.Objects;

/** Карта неизменяема; внешние массивы копируются. */
public final class WorldMap {
    public static final double CELL_METERS = 10.0;
    private final Terrain[][] cells;
    private final int width;

    public WorldMap(Terrain[][] source) {
        Objects.requireNonNull(source, "source");
        if (source.length == 0 || source[0] == null || source[0].length == 0)
            throw new IllegalArgumentException("Карта не должна быть пустой");
        width = source[0].length;
        cells = new Terrain[source.length][width];
        for (int y = 0; y < source.length; y++) {
            if (source[y] == null || source[y].length != width)
                throw new IllegalArgumentException("Карта должна быть прямоугольной");
            for (int x = 0; x < width; x++) {
                cells[y][x] = Objects.requireNonNull(source[y][x], "Пустая клетка");
            }
        }
    }
    public static WorldMap fromRows(String... rows) {
        Objects.requireNonNull(rows, "rows");
        Terrain[][] cells = new Terrain[rows.length][];
        for (int y = 0; y < rows.length; y++) {
            Objects.requireNonNull(rows[y], "Пустая строка");
            cells[y] = new Terrain[rows[y].length()];
            for (int x = 0; x < rows[y].length(); x++) {
                cells[y][x] = TerrainType.fromSymbol(rows[y].charAt(x));
            }
        }
        return new WorldMap(cells);
    }
    public int width() { return width; }
    public int height() { return cells.length; }
    public boolean contains(Position position) {
        return position != null && position.x() >= 0 && position.x() < width
                && position.y() >= 0 && position.y() < height();
    }
    public Terrain terrainAt(Position position) {
        if (!contains(position)) throw new IllegalArgumentException("Клетка вне карты: " + position);
        return cells[position.y()][position.x()];
    }
    public Terrain destination(Position from, Position to) {
        if (!contains(from) || !contains(to))
            throw new IllegalArgumentException("Координаты вне карты");
        if (!from.isAdjacentTo(to))
            throw new IllegalArgumentException("За ход разрешён переход в соседнюю клетку без диагонали");
        return terrainAt(to);
    }
}
