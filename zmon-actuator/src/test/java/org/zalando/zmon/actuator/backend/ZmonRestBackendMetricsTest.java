package org.zalando.zmon.actuator.backend;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import org.zalando.zmon.actuator.ExampleApplication;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author jbellmann
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ExampleApplication.class, webEnvironment = RANDOM_PORT,
        properties = {"debug=false", "management.security.enabled=false"})
public class ZmonRestBackendMetricsTest {

    private static final int REPEATS = 100;
    private final Logger logger = LoggerFactory.getLogger(ZmonRestBackendMetricsTest.class);

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private MetricRegistry metricRegistry;

    private RestTemplate externalClient = new RestTemplate();

    private final Random random = new Random(System.currentTimeMillis());

    @Rule
    public final WireMockRule wireMockRule = new WireMockRule(9999);

    @Before
    public void setUp() {
        ConsoleReporter reporter = ConsoleReporter.forRegistry(metricRegistry).convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS).build();
        reporter.start(2, TimeUnit.SECONDS);
        expectDeleteCall();
    }

    @Test
    public void test() throws InterruptedException {
        for (int i = 0; i < REPEATS; i++) {

            externalClient.getForObject("http://localhost:" + port + "/timeConsumingCall", String.class);
            TimeUnit.MILLISECONDS.sleep(random.nextInt(30));
        }

        assertThat(metricRegistry.getTimers().get("zmon.request.204.DELETE.localhost:9999")).isNotNull();

        String metricsEndpointResponse = externalClient.getForObject("http://localhost:" + port + "/metrics",
                String.class);

        logger.info(metricsEndpointResponse);
    }

    private void expectDeleteCall() {
        WireMock.addRequestProcessingDelay(200);
        stubFor(delete(urlEqualTo("/something")).willReturn(
                aResponse().withStatus(HttpStatus.NO_CONTENT.value()).withFixedDelay(100)));
    }

}
