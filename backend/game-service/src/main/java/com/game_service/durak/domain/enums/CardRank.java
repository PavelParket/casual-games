package com.game_service.durak.domain.enums;

public enum CardRank {

    SIX,
    SEVEN,
    EIGHT,
    NINE,
    TEN,
    JACK,
    QUEEN,
    KING,
    ACE;

    public int strength() {
        return this.ordinal();
    }
}
