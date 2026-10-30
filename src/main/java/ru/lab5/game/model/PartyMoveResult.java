package ru.lab5.game.model;

import java.util.List;

public record PartyMoveResult(List<MoveResult> moves, int manaSpent, double seconds) {
    public PartyMoveResult { moves = List.copyOf(moves); }
}
