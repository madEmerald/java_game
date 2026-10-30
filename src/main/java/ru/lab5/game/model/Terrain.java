package ru.lab5.game.model;

/** Расширение ландшафтов не требует изменения алгоритмов движения. */
public interface Terrain {
    String displayName();
    char symbol();
    SurfaceEffect surfaceEffect();
}
