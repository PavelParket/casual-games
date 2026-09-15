package com.common_utils.mapper;

import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;

import java.util.function.Function;

public interface PagedModelMapper {

    default <E> PagedModel<E> toPagedModel(Page<E> entityPage) {
        return new PagedModel<>(entityPage);
    }

    default <E, R> PagedModel<R> toPagedModel(Page<E> entityPage, Function<E, R> mapper) {
        return new PagedModel<>(entityPage.map(mapper));
    }
}
