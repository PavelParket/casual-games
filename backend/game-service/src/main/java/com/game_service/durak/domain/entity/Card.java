package com.game_service.durak.domain.entity;

import com.game_service.durak.domain.enums.CardRank;
import com.game_service.durak.domain.enums.CardSuit;
import lombok.Builder;

@Builder
public record Card(

        CardRank rank,

        CardSuit suit
) {

    public boolean beats(Card other, CardSuit trumpSuit) {
        if (this.suit == other.suit) {
            return this.rank.strength() > other.rank.strength();
        }

        return this.suit == trumpSuit;
    }

    @Override
    public String toString() {
        return String.format("%s_%s", rank, suit);
    }
}
