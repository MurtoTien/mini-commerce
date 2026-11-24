package com.murto.shop.api;

import com.murto.shop.api.dto.ShopInfo;

import java.util.Map;

public interface ShopQueryService {

    Long createShop(ShopInfo shopInfo);
    ShopInfo updateShop(ShopInfo shopInfo);
    boolean deleteShop(Long shopId);
    ShopInfo queryShop(Long shopId);
}
