package ru.lab5.game.model;

public enum TerrainType implements Terrain {
    PLAIN("Равнина", '.', 1.00, 1.00, 0, true, "Без эффекта"),
    ROAD("Дорога", 'R', 1.30, 0.80, 0, true, "Ускорение и экономия сил"),
    FOREST("Лес", 'F', 0.75, 1.25, 0, true, "Густая растительность"),
    SWAMP("Болото", 'S', 0.45, 1.80, -5, true, "Топь: замедление и 5 урона"),
    ICE("Лёд", 'I', 0.65, 1.40, -2, true, "Скольжение и 2 урона"),
    LAVA("Лава", 'L', 0.55, 1.50, -20, true, "Ожог: 20 урона"),
    GROVE("Целебная роща", 'H', 0.90, 1.00, 10, true, "Восстановление 10 HP"),
    CHASM("Пропасть", 'X', 1.00, 1.00, 0, false, "Пеший проход запрещён");

    private final String displayName;
    private final char symbol;
    private final SurfaceEffect effect;

    TerrainType(String name, char symbol, double speed, double stamina,
                int health, boolean walkable, String description) {
        this.displayName = name;
        this.symbol = symbol;
        this.effect = new SurfaceEffect(speed, stamina, health, walkable, description);
    }
    @Override public String displayName() { return displayName; }
    @Override public char symbol() { return symbol; }
    @Override public SurfaceEffect surfaceEffect() { return effect; }

    public static TerrainType fromSymbol(char symbol) {
        for (TerrainType terrain : values()) {
            if (terrain.symbol == symbol) return terrain;
        }
        throw new IllegalArgumentException("Неизвестный символ ландшафта: " + symbol);
    }
}
