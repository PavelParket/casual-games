package com.casualgames.websockethub.domain.entity;

import lombok.Builder;

@Builder
public record DurakTablePair(

        DurakCard attackCard,

        DurakCard defendCard
) {
}
