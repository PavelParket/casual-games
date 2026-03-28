package com.game_service.durak.domain.entity;

import lombok.Builder;

@Builder
public record DurakTablePair(

        DurakCard attackCard,

        DurakCard defendCard
) {

    public static DurakTablePair attack(DurakCard durakCard) {
        return new DurakTablePair(durakCard, null);
    }

    public boolean isDefended() {
        return defendCard != null;
    }

    public DurakTablePair withDefend(DurakCard defense) {
        return new DurakTablePair(this.attackCard, defense);
    }
}
