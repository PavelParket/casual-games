package com.game_service.durak.domain.entity;

import com.game_service.durak.domain.enums.DurakCardRank;
import com.game_service.durak.domain.enums.DurakCardSuit;
import lombok.Builder;

@Builder
public record DurakCard(

        DurakCardRank rank,

        DurakCardSuit suit
) {

    public boolean beats(DurakCard other, DurakCardSuit trumpSuit) {
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
