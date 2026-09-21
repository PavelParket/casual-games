package com.casualgames.websockethub.domain.enums.model;

public enum DurakCardRank {

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
