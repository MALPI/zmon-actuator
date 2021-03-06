package org.zalando.zmon.actuator.config;

import com.codahale.metrics.MetricRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.zalando.zmon.actuator.ZmonRestFilterBeanPostProcessor;

/**
 * @author jbellmann
 */
@Configuration
@ConditionalOnClass(MetricRegistry.class)
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
public class ZmonPostProcessorAutoConfiguration {

    @Bean
    public static ZmonRestFilterBeanPostProcessor zmonRestFilterBeanPostProcessor() {
        return new ZmonRestFilterBeanPostProcessor();
    }

}
