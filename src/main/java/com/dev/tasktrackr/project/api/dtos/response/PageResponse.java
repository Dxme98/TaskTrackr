package com.dev.tasktrackr.project.api.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@Schema(description = "Generic pagination response wrapper")
public class PageResponse<T> {

    @Schema(description = "List of items on the current page")
    private List<T> content;

    @Schema(description = "Current page number (0-based)")
    private int page;

    @Schema(description = "Number of elements per page")
    private int size;

    @Schema(description = "Total number of elements across all pages")
    private long totalElements;

    @Schema(description = "Total number of pages")
    private int totalPages;

    @Schema(description = "Is this the last page?")
    private boolean last;

    public PageResponse(List<T> content, int page, int size,
                        long totalElements, int totalPages, boolean last) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.last = last;
    }

    // Konstruktor zum praktischen Umwandeln
    public static <T> PageResponse<T> from(Page page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
