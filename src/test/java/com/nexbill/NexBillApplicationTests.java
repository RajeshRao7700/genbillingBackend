package com.nexbill;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class NexBillApplicationTests {

    @Test
    void contextLoads() {
        // Verifies Spring context loads and Flyway migrations execute successfully against the test schema
    }
}
