package com.murto.shop.dao;

import com.murto.shop.api.dto.ShopInfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;

@Repository
public class ShopDAO {
    private final JdbcTemplate jdbcTemplate;

    public ShopDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long createShop(ShopInfo shopInfo) {
        String sql = """
                INSERT INTO shop (name, description, shop_type)
                VALUES (?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            statement.setString(1, shopInfo.name());
            statement.setString(2, shopInfo.description());
            statement.setString(3, shopInfo.shopType());

            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException("创建店铺后未获取到 ID");
        }

        return key.longValue();
    }

    public ShopInfo updateShop(ShopInfo shopInfo) {
        String sql = """
                UPDATE shop
                SET name = ?, description = ?, shop_type = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(sql, shopInfo.name(), shopInfo.description(),
                shopInfo.shopType(), shopInfo.id());

        // 重新读取数据库，保留真实的创建时间；相同内容的更新也应成功。
        return queryShop(shopInfo.id());
    }

    public boolean deleteShop(Long shopId) {
        return jdbcTemplate.update("DELETE FROM shop WHERE id = ?", shopId) == 1;
    }

    public ShopInfo queryShop(Long shopId) {
        String sql = """
                SELECT id, name, description, shop_type, created_at
                FROM shop
                WHERE id = ?
                """;

        try {
            return jdbcTemplate.queryForObject(
                    sql,
                    (rs, rowNum) -> new ShopInfo(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getString("shop_type"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    ),
                    shopId
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
