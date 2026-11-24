package com.murto.goods.client;

import com.murto.shop.api.ShopQueryService;
import com.murto.shop.api.dto.ShopInfo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ShopDubboClient {

    @DubboReference(
            timeout = 3000,
            retries = 0)
    private ShopQueryService shopQueryService;

    public ShopInfo getShop(Long shopId) {
        return shopQueryService.queryShop(shopId);
    }
}
