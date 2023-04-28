package com.alisurgim.consul.listener.autoconfigure;

import com.alisurgim.consul.listener.register.ConsulSafeShutdownInterceptor;
import com.alisurgim.consul.listener.register.SafeOfflineProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.cloud.consul.ConditionalOnConsulEnabled;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.serviceregistry.ConsulAutoServiceRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulAutoServiceRegistrationAutoConfiguration;
import org.springframework.cloud.consul.serviceregistry.ConsulAutoServiceRegistrationListener;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistryAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;

/**
 * @author alisurgim
 * @date 2023/3/29 19:08
 */
@Slf4j
@Configuration
@ConditionalOnConsulEnabled
@Import(ConsulServiceRegistryBeanPostProcessor.class)
@EnableConfigurationProperties(SafeOfflineProperties.class)
@ConditionalOnBean({ConsulAutoServiceRegistration.class, ConsulAutoServiceRegistrationListener.class})
@AutoConfigureAfter({ConsulServiceRegistryAutoConfiguration.class, ConsulAutoServiceRegistrationAutoConfiguration.class})
public class ConsulRegisterListenerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DelegatingConsulRegistryListener delegatingConsulRegistryListener(
            ConsulDiscoveryProperties consulDiscoveryProperties,
            List<ConsulRegistryInterceptor> consulRegistryInterceptors) {
        return new DelegatingConsulRegistryListener(consulDiscoveryProperties, consulRegistryInterceptors);
    }

    @Bean
    @ConditionalOnProperty(value = "spring.cloud.consul.safe-offline.enabled", matchIfMissing = true)
    public ConsulRegistryInterceptor consulSafeOfflineHandler(ServletWebServerApplicationContext context, SafeOfflineProperties properties) {
        return new ConsulSafeShutdownInterceptor(context, properties);
    }

}
