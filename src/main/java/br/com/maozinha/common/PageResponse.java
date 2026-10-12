package br.com.maozinha.common;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Objects;

public record PageResponse<T>(

        List<T> content,

        int page,

        int size,

        long totalElements,

        int totalPages,

        boolean first,

        boolean last

) {

    public PageResponse {

        content = content == null
                ? List.of()
                : List.copyOf(content);
    }

    public static <T> PageResponse<T> from(
            Page<T> page
    ) {

        Objects.requireNonNull(
                page,
                "page não pode ser nulo"
        );

        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}