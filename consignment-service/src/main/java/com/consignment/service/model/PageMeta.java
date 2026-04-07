package com.consignment.service.model;

public record PageMeta(
        int page,
        int perPage,
        long totalData,
        int totalPage
) {
    public static PageMeta of(int page, int perPage, long totalData) {
        int totalPage = perPage > 0 ? (int) Math.ceil((double) totalData / perPage) : 0;
        return new PageMeta(page, perPage, totalData, totalPage);
    }
}
