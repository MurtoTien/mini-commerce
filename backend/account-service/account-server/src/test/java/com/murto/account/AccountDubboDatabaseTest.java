package com.murto.account;

import com.murto.account.api.AccountQueryService;
import com.murto.account.api.dto.AccountInfo;
import com.murto.account.api.dto.UpdateAccountRequest;
import com.murto.account.api.exception.AccountNotFoundException;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

// 独立测试端口，强制走网络 RPC；测试结束删除本测试新建的账号。
@EnabledIfEnvironmentVariable(named = "RUN_DB_TESTS", matches = "true")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "dubbo.registry.address=N/A",
                "dubbo.protocol.port=30882",
                "logging.level.root=WARN"
        }
)
@Import(AccountDubboDatabaseTest.RpcClientConfiguration.class)
@DirtiesContext
class AccountDubboDatabaseTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RpcClient rpcClient;

    @Test
    void queriesUpdatesAndDeletesOverDubbo() {
        String loginName = "rpc_" + UUID.randomUUID().toString().replace("-", "");

        jdbcTemplate.update(
                "INSERT INTO user_account (login_name, password_hash, nickname, status) VALUES (?, ?, ?, 0)",
                loginName,
                "!test-disabled",
                "RPC测试账号"
        );

        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM user_account WHERE login_name = ?",
                Long.class,
                loginName
        );

        try {
            AccountInfo original = rpcClient.service.queryAccount(userId);

            assertEquals(loginName, original.loginName());
            assertNotNull(original.createdAt());

            AccountInfo updated = rpcClient.service.updateAccount(
                    userId,
                    new UpdateAccountRequest("Dubbo新昵称", null)
            );

            assertEquals("Dubbo新昵称", updated.nickname());
            assertEquals(original.createdAt(), updated.createdAt());
            assertEquals(loginName, updated.loginName());

            assertThrows(
                    IllegalArgumentException.class,
                    () -> rpcClient.service.updateAccount(
                            userId,
                            new UpdateAccountRequest("  ", null)
                    )
            );

            assertTrue(rpcClient.service.deleteAccount(userId));

            assertThrows(
                    AccountNotFoundException.class,
                    () -> rpcClient.service.queryAccount(userId)
            );

            assertThrows(
                    AccountNotFoundException.class,
                    () -> rpcClient.service.deleteAccount(userId)
            );
        } finally {
            jdbcTemplate.update("DELETE FROM user_account WHERE login_name = ?", loginName);
        }
    }

    @TestConfiguration
    static class RpcClientConfiguration {

        @Bean
        RpcClient accountRpcClient() {
            return new RpcClient();
        }
    }

    static class RpcClient {

        @DubboReference(
                url = "dubbo://127.0.0.1:30882/com.murto.account.api.AccountQueryService",
                injvm = false,
                check = false,
                lazy = true,
                timeout = 3000,
                retries = 0
        )
        private AccountQueryService service;
    }
}
