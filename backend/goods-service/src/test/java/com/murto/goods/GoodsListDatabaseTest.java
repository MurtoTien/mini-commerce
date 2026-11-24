package com.murto.goods;

import com.murto.goods.controller.GoodsListController;
import com.murto.goods.dao.GoodsDAO;
import com.murto.goods.service.GoodsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@EnabledIfEnvironmentVariable(named = "RUN_DB_TESTS", matches = "true")
class GoodsListDatabaseTest {

    @Test
    void filtersShopStatusAndPaginatesWithoutDuplicates() {
        DriverManagerDataSource ds = new DriverManagerDataSource(
                System.getenv().getOrDefault("TEST_GOODS_DB_URL", "jdbc:mysql://127.0.0.1:3306/goods_db"),
                System.getenv().getOrDefault("DB_USERNAME", "root"),
                System.getenv().getOrDefault("DB_PASSWORD", "")
        );

        JdbcTemplate jdbc = new JdbcTemplate(ds);
        GoodsService service = new GoodsService(new GoodsDAO(jdbc));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new GoodsListController(service)).build();

        new TransactionTemplate(new DataSourceTransactionManager(ds)).executeWithoutResult(tx -> {
            tx.setRollbackOnly();

            try {
                long shopId = System.nanoTime();
                String sql = "INSERT INTO goods (shop_id, name, price_cent, stock, status) VALUES (?, ?, 1234, 10, ?)";
                jdbc.update(sql, shopId, "第一件", 1);
                jdbc.update(sql, shopId, "第二件", 1);
                jdbc.update(sql, shopId, "下架商品", 0);
                jdbc.update(sql, shopId + 1, "其他店铺", 1);

                long firstId = jdbc.queryForObject(
                        "SELECT MIN(id) FROM goods WHERE shop_id = ? AND status = 1",
                        Long.class,
                        shopId
                );

                mvc.perform(get("/goods/list").param("shopId", String.valueOf(shopId)).param("size", "1"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.items.length()").value(1))
                        .andExpect(jsonPath("$.items[0].name").value("第一件"))
                        .andExpect(jsonPath("$.items[0].priceCent").value(1234))
                        .andExpect(jsonPath("$.hasMore").value(true))
                        .andExpect(jsonPath("$.nextCursor").value((int) firstId));

                mvc.perform(get("/goods/list").param("shopId", String.valueOf(shopId))
                        .param("size", "1").param("afterId", String.valueOf(firstId)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.items.length()").value(1))
                        .andExpect(jsonPath("$.items[0].name").value("第二件"))
                        .andExpect(jsonPath("$.hasMore").value(false))
                        .andExpect(jsonPath("$.nextCursor").isEmpty());

                assertEquals(1, service.listByShop(shopId, 0, 0, 20).items().size());

                assertTrue(service.listByShop(shopId + 2, 1, 0, 20).items().isEmpty());

                mvc.perform(get("/goods/list").param("shopId", "0"))
                        .andExpect(status().isBadRequest());

                mvc.perform(get("/goods/list").param("shopId", "1").param("size", "101"))
                        .andExpect(status().isBadRequest());

                mvc.perform(get("/goods/list").param("shopId", "1").param("afterId", "-1"))
                        .andExpect(status().isBadRequest());

                mvc.perform(get("/goods/list").param("shopId", "1").param("status", "2"))
                        .andExpect(status().isBadRequest());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
