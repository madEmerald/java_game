package ru.lab5.game.model;

public enum MapPreset {
    VALLEY("Зелёная долина", new String[]{
        "..FFF...H.", ".RFFSS....", ".R..SS.H..", ".RRRSSRR..",
        "..F..XXX..", "H.F.......", "..FFFF...."}),
    FROZEN("Ледяные земли", new String[]{
        "III..IIIII", "I..FF..III", ".RRIIIRR..", ".IIIIXIIH.",
        "..IXXXI...", "H.II..F...", "II...F..II"}),
    VOLCANO("Вулканический край", new String[]{
        "..LLL...XX", ".R.LLL..X.", ".R..L..H..", ".RLLXLLR..",
        "..LLXLL...", "H...X..F..", "..RRRR...."});

    private final String name;
    private final String[] rows;
    MapPreset(String name, String[] rows) { this.name = name; this.rows = rows; }
    public WorldMap createMap() { return WorldMap.fromRows(rows); }
    public Position start() { return new Position(0, 3); }
    @Override public String toString() { return name; }
}
