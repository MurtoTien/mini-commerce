package com.murto.shop.dto;

public record CreateShopRequest(
        String name,
        String description,
        String shopType
) {
}
