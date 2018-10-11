package org.zalando.zmon.actuator;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author jbellmann
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {ExampleApplication.class}, value = {"debug=true"}, webEnvironment = RANDOM_PORT)
public class ZmonMetricsFilterTest {

    private final Logger logger = LoggerFactory.getLogger(ZmonMetricsFilterTest.class);

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private MeterRegistry meterRegistry;

    private TestRestTemplate restTemplate = new TestRestTemplate();

    private final Random random = new Random(System.currentTimeMillis());

    @Test
    public void test() throws InterruptedException {
        for (int i = 0; i < 100; i++) {

            restTemplate.getForObject("http://localhost:" + port + "/hello", String.class);
            TimeUnit.MILLISECONDS.sleep(random.nextInt(500));
        }

        assertThat(meterRegistry.get("zmon.response.200.GET.hello").timer()).isNotNull();
        assertThat(meterRegistry.get("zmon.response.503.GET.hello").timer()).isNotNull();

        String metricsEndpointResponse = restTemplate.getForObject("http://localhost:" + port + "/actuator/prometheus",
                String.class);

        logger.info(metricsEndpointResponse);
    }
}
