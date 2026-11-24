package com.murto.shop.service;

import com.murto.shop.api.dto.ShopInfo;
import com.murto.shop.dao.ShopDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShopService {
    private final ShopDAO shopDAO;

    public ShopService(ShopDAO shopDAO) {
        this.shopDAO = shopDAO;
    }

    public Long createShop(ShopInfo shopInfo) {
        validateShopInfo(shopInfo);
        return shopDAO.createShop(shopInfo);
    }

    private void validateShopInfo(ShopInfo shopInfo) {
        if (shopInfo == null) {
            throw new IllegalArgumentException("店铺信息不能为空");
        }

        if (shopInfo.name() == null || shopInfo.name().isBlank()) {
            throw new IllegalArgumentException("店铺名称不能为空");
        }

        if (shopInfo.shopType() == null || shopInfo.shopType().isBlank()) {
            throw new IllegalArgumentException("店铺类型不能为空");
        }

        if (shopInfo.name().length() > 100) {
            throw new IllegalArgumentException("店铺名称不能超过100个字符");
        }

        if (shopInfo.description() != null
                && shopInfo.description().length() > 500) {
            throw new IllegalArgumentException("店铺描述不能超过500个字符");
        }

        if (shopInfo.shopType().length() > 30) {
            throw new IllegalArgumentException("店铺类型不能超过30个字符");
        }
    }

    @Transactional
    public ShopInfo updateShop(ShopInfo shopInfo) {
        validateShopInfo(shopInfo);
        validateShopId(shopInfo.id());

        ShopInfo updatedShop = shopDAO.updateShop(shopInfo);
        if (updatedShop == null) {
            throw new IllegalArgumentException("店铺不存在");
        }

        return updatedShop;
    }

    public boolean deleteShop(Long shopId) {
        validateShopId(shopId);
        return shopDAO.deleteShop(shopId);
    }

    public ShopInfo queryShop(Long shopId) {
        validateShopId(shopId);

        // 读数据库
        ShopInfo shopInfo = shopDAO.queryShop(shopId);

        if (shopInfo == null) {
            throw new IllegalArgumentException("店铺不存在");
        }

        return shopInfo;
    }

    private void validateShopId(Long shopId) {
        if (shopId == null || shopId <= 0) {
            throw new IllegalArgumentException("店铺 ID 必须为正数");
        }
    }
}
