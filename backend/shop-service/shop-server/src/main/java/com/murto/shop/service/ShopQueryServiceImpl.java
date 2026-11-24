package com.murto.shop.service;

import com.murto.shop.api.ShopQueryService;
import com.murto.shop.api.dto.ShopInfo;
import org.apache.dubbo.config.annotation.DubboService;
@DubboService
public class ShopQueryServiceImpl implements ShopQueryService {
    private final ShopService shopService;

    public ShopQueryServiceImpl(ShopService shopService) {
        this.shopService = shopService;
    }

    @Override
    public Long createShop(ShopInfo shopInfo) {
        return shopService.createShop(shopInfo);
    }

    @Override
    public ShopInfo updateShop(ShopInfo shopInfo) {
        return shopService.updateShop(shopInfo);
    }
    @Override
    public boolean deleteShop(Long shopId) {
        return shopService.deleteShop(shopId);
    }

    @Override
    public ShopInfo queryShop(Long shopId) {
        return shopService.queryShop(shopId);
    }
}
