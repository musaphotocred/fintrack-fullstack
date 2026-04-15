package com.fintrack.common.dto;

import org.springframework.data.domain.Page;
import java.util.List;

/**
 * Pagination wrapper for list endpoints.
 * Maps Spring Data Page to a frontend-friendly structure.
 *
 * @param <T> the type of content items
 */
public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    /**
     * Creates a PagedResponse from a Spring Data Page.
     */
    public static <T> PagedResponse<T> from(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    /**
     * Creates a PagedResponse from a Spring Data Page with mapped content.
     * Useful when entity needs to be transformed to DTO.
     */
    public static <T, U> PagedResponse<U> from(Page<T> page, List<U> mappedContent) {
        return new PagedResponse<>(
                mappedContent,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
