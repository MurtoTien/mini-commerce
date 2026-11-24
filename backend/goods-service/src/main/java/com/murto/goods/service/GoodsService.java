package com.murto.goods.service;

import com.murto.goods.dao.GoodsDAO;
import com.murto.goods.dto.GoodsInfo;
import com.murto.goods.dto.GoodsPage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {

    private final GoodsDAO goodsDAO;

    public GoodsService(GoodsDAO goodsDAO) {
        this.goodsDAO = goodsDAO;
    }

    public GoodsPage listByShop(
            Long shopId,
            int status,
            long afterId,
            int size
    ) {
        if (shopId == null || shopId <= 0) {
            throw new IllegalArgumentException("店铺ID必须为正数");
        }

        if (status != 0 && status != 1) {
            throw new IllegalArgumentException("商品状态只能为0（下架）或1（上架）");
        }

        if (afterId < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("afterId不能小于0，size必须在1到100之间");
        }

        List<GoodsInfo> rows = goodsDAO.findByShop(shopId, status, afterId, size + 1);
        boolean hasMore = rows.size() > size;

        List<GoodsInfo> items = List.copyOf(rows.subList(0, Math.min(size, rows.size())));
        Long nextCursor = hasMore ? items.get(items.size() - 1).id() : null;
        return new GoodsPage(items, nextCursor, hasMore);
    }
}
