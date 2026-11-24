package com.murto.account;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "dubbo.registry.address=N/A",
        "dubbo.protocol.port=-1"
})
class AccountServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
