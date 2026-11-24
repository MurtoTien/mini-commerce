package com.murto.goods.dao;

import com.murto.goods.dto.GoodsInfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GoodsDAO {

    private final JdbcTemplate jdbcTemplate;

    public GoodsDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<GoodsInfo> findByShop(
            Long shopId,
            int status,
            long afterId,
            int limit
    ) {
        return jdbcTemplate.query(
                """
                SELECT id, shop_id, name, description, price_cent, stock, status, created_at, updated_at
                FROM goods
                WHERE shop_id = ? AND status = ? AND id > ?
                ORDER BY id ASC
                LIMIT ?
                """,
                (rs, row) -> new GoodsInfo(
                        rs.getLong("id"),
                        rs.getLong("shop_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getLong("price_cent"),
                        rs.getInt("stock"),
                        rs.getInt("status"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                ),
                shopId,
                status,
                afterId,
                limit
        );
    }
}
