package com.game_service.durak.domain.entity;

import lombok.Builder;

@Builder
public record TablePair(

        Card attackCard,

        Card defendCard
) {

    public static TablePair attack(Card card) {
        return new TablePair(card, null);
    }

    public boolean isDefended() {
        return defendCard != null;
    }

    public TablePair withDefend(Card defense) {
        return new TablePair(this.attackCard, defense);
    }
}
