package com.common_utils.mapper;

import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;

import java.util.function.Function;

public interface PagedModelMapper<E, R> {

    default PagedModel<R> toPagedModel(Page<E> entities, Function<E, R> mapper) {
        return new PagedModel<>(entities.map(mapper));
    }
}
