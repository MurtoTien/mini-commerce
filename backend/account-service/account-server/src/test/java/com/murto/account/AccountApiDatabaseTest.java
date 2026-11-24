package com.murto.account;

import com.murto.account.controller.AccountController;
import com.murto.account.controller.AccountExceptionHandler;
import com.murto.account.dao.AccountDAO;
import com.murto.account.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 仅显式启用时连接本机数据库。测试数据在事务结束时回滚，不修改演示账号。
@EnabledIfEnvironmentVariable(named = "RUN_DB_TESTS", matches = "true")
class AccountApiDatabaseTest {

    @Test
    void profileLifecycleAndErrors() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                System.getenv().getOrDefault("TEST_ACCOUNT_DB_URL", "jdbc:mysql://127.0.0.1:3306/account_db"),
                System.getenv().getOrDefault("DB_USERNAME", "root"),
                System.getenv().getOrDefault("DB_PASSWORD", "")
        );

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(
                new AccountController(new AccountService(new AccountDAO(jdbc))))
                .setControllerAdvice(new AccountExceptionHandler())
                .build();

        new TransactionTemplate(new DataSourceTransactionManager(dataSource)).executeWithoutResult(tx -> {
            tx.setRollbackOnly();

            try {
                String login = "test_" + UUID.randomUUID().toString().replace("-", "");
                jdbc.update("INSERT INTO user_account (login_name, password_hash, nickname) VALUES (?, ?, ?)",
                        login, "!test-disabled", "测试昵称");

                Long id = jdbc.queryForObject("SELECT id FROM user_account WHERE login_name = ?", Long.class, login);

                mvc.perform(get("/accounts/" + id))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.loginName").value(login))
                        .andExpect(jsonPath("$.passwordHash").doesNotExist())
                        .andExpect(jsonPath("$.password_hash").doesNotExist());

                String createdAt = jdbc.queryForObject(
                        "SELECT CAST(created_at AS CHAR) FROM user_account WHERE id = ?",
                        String.class,
                        id
                );

                String updateBody = "{\"nickname\":\"新昵称\",\"avatarUrl\":null}";

                for (int attempt = 0; attempt < 2; attempt++) {
                    mvc.perform(put("/accounts/" + id).contentType(MediaType.APPLICATION_JSON).content(updateBody))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.nickname").value("新昵称"))
                            .andExpect(jsonPath("$.loginName").value(login));
                }

                assertEquals(
                        createdAt,
                        jdbc.queryForObject(
                                "SELECT CAST(created_at AS CHAR) FROM user_account WHERE id = ?",
                                String.class,
                                id
                        )
                );

                assertEquals(
                        "!test-disabled",
                        jdbc.queryForObject(
                                "SELECT password_hash FROM user_account WHERE id = ?",
                                String.class,
                                id
                        )
                );

                mvc.perform(put("/accounts/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"  \"}"))
                        .andExpect(status().isBadRequest());

                mvc.perform(get("/accounts/0"))
                        .andExpect(status().isBadRequest());

                mvc.perform(delete("/accounts/" + id))
                        .andExpect(status().isNoContent());

                mvc.perform(get("/accounts/" + id))
                        .andExpect(status().isNotFound());

                mvc.perform(delete("/accounts/" + id))
                        .andExpect(status().isNotFound());

                mvc.perform(put("/accounts/" + id).contentType(MediaType.APPLICATION_JSON).content(updateBody))
                        .andExpect(status().isNotFound());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
