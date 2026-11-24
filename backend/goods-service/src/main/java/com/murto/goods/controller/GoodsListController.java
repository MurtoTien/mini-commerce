package com.murto.goods.controller;

import com.murto.goods.dto.GoodsPage;
import com.murto.goods.service.GoodsService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class GoodsListController {

    private final GoodsService goodsService;

    public GoodsListController(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    @GetMapping("/goods/list")
    public GoodsPage list(
            @RequestParam("shopId") Long shopId,
            @RequestParam(name = "status", defaultValue = "1") int status,
            @RequestParam(name = "afterId", defaultValue = "0") long afterId,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        try {
            return goodsService.listByShop(shopId, status, afterId, size);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }
}
