package com.murto.shop.api.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record ShopInfo(
        Long id,
        String name,
        String description,
        String shopType,
        LocalDateTime createdAt
) implements Serializable {

}
