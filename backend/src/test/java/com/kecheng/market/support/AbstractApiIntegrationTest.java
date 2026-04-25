package com.kecheng.market.support;

import com.kecheng.market.common.store.MarketStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "market.memory.snapshot-path=target/test-data/${random.uuid}/market-store.ser",
                "market.upload.base-dir=target/test-upload/${random.uuid}"
        }
)
@AutoConfigureMockMvc
@ActiveProfiles("memory")
public abstract class AbstractApiIntegrationTest extends ApiIntegrationTestSupport {

    @Autowired
    private MarketStore marketStore;

    @org.junit.jupiter.api.BeforeEach
    void resetMarketStore() {
        ReflectionTestUtils.invokeMethod(marketStore, "initializeDefaults");
    }
}
