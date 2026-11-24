package com.murto.shop.controller;

import com.murto.shop.api.dto.ShopInfo;
import com.murto.shop.dto.CreateShopRequest;
import com.murto.shop.service.ShopService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Objects;

@RestController
public class ShopController {
    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @PostMapping("/shop/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ShopInfo createShop(
            @RequestBody CreateShopRequest request) {
        ShopInfo shopInfo = new ShopInfo(
                null,
                request.name(),
                request.description(),
                request.shopType(),
                null
        );

        try {
            Long shopId = shopService.createShop(shopInfo);
            if (shopId != null) {
                return shopService.queryShop(shopId);
            }
            return null;
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage(),
                    e
            );
        }
    }

    @GetMapping("/shop/info")
    public ShopInfo getShopInfo(
            @RequestParam("shopId") Long shopId) {

        if (shopId == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "店铺不存在");
        }

        try {
            return shopService.queryShop(shopId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage(),
                    e
            );
        }
    }
}
