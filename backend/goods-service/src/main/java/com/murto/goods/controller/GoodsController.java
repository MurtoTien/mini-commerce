package com.murto.goods.controller;

import com.murto.goods.client.ShopClient;
import com.murto.goods.client.ShopDubboClient;
import com.murto.goods.dto.ShopDTO;
import com.murto.shop.api.dto.ShopInfo;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@RestController
public class GoodsController {

    private final ShopClient shopClient;
    private final ShopDubboClient shopDubbleClient;

    public GoodsController(ShopClient shopClient, ShopDubboClient shopDubbleClient) {
        this.shopClient = shopClient;
        this.shopDubbleClient = shopDubbleClient;
    }

    @GetMapping("/goods/info_v2")
    public ShopInfo getGoodsInfoV2(
            @RequestParam(name = "goods_id") Long goodsId) {
        // 演示数据：暂时假设商品属于 ID 为 1 的店铺
        Long shopId = 1L;

        ShopInfo shopInfo;

        try {
            shopInfo = shopDubbleClient.getShop(shopId);
        } catch (RestClientException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "获取店铺信息失败",
                    e
            );
        }

        if (shopInfo == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "店铺服务返回空数据"
            );
        }

        return shopInfo;
    }

    @GetMapping("/goods/info")
    public Map<String, Object> getGoodsInfo(
            @RequestParam(name = "goods_id") Long goodsId) {

        // 演示数据：暂时假设商品属于 ID 为 1 的店铺
        Long shopId = 1L;

        ShopDTO shop;

        try {
            shop = shopClient.getShop(shopId);
        } catch (RestClientException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "获取店铺信息失败",
                    e
            );
        }

        if (shop == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "店铺服务返回空数据"
            );
        }

        return Map.of(
                "id", goodsId,
                "name", "面包",
                "des", "香香糯糯的面包",
                "shop", shop
        );
    }
}
