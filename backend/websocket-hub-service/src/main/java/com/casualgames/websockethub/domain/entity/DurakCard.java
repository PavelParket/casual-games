package com.casualgames.websockethub.domain.entity;

import com.casualgames.websockethub.domain.enums.model.DurakCardRank;
import com.casualgames.websockethub.domain.enums.model.DurakCardSuit;
import lombok.Builder;

@Builder
public record DurakCard(

        DurakCardRank rank,

        DurakCardSuit suit
) {
}
