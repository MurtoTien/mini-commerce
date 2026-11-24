package com.murto.goods.dto;

import java.time.LocalDateTime;

public record GoodsInfo(
        Long id,
        Long shopId,
        String name,
        String description,
        long priceCent,
        int stock,
        int status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
