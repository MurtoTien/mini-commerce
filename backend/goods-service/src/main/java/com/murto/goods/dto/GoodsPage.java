package com.murto.goods.dto;

import java.util.List;

public record GoodsPage(
        List<GoodsInfo> items,
        Long nextCursor,
        boolean hasMore
) {
}
