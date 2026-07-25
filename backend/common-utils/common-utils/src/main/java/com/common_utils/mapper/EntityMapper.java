package com.common_utils.mapper;

import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;

public interface EntityMapper<E, R> {

    R toResponse(E entity);

    default PagedModel<R> toResponsePagedModel(Page<E> entities) {
        return new PagedModel<>(entities.map(this::toResponse));
    }
}
