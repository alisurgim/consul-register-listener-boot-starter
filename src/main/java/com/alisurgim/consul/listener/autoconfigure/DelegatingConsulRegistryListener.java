package com.alisurgim.consul.listener.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.web.context.ConfigurableWebServerApplicationContext;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

import java.util.List;

/**
 * @author alisurgim
 * @date 2023/3/30 11:12
 */
@Slf4j
public class DelegatingConsulRegistryListener implements ApplicationListener<WebServerInitializedEvent>, DisposableBean, Ordered {

    private final List<ConsulRegistryInterceptor> consulRegistryInterceptors;
    private final ConsulDiscoveryProperties properties;

    public DelegatingConsulRegistryListener(ConsulDiscoveryProperties consulDiscoveryProperties, List<ConsulRegistryInterceptor> consulRegistryInterceptors) {
        this.consulRegistryInterceptors = consulRegistryInterceptors;
        this.properties = consulDiscoveryProperties;
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        if (context instanceof ConfigurableWebServerApplicationContext) {
            // 忽略management服务的事件
            if ("management".equals(((ConfigurableWebServerApplicationContext) context).getServerNamespace())) {
                return;
            }
        }
        // 未开启自动注册，无需执行额外处理
        if (!properties.isRegister()) {
            log.info("Registration disabled.");
            return;
        }
        log.info("Start ConsulRegistryInterceptor beforeRegister");
        for (ConsulRegistryInterceptor consulRegistryInterceptor : consulRegistryInterceptors) {
            try {
                consulRegistryInterceptor.beforeRegister();
            } catch (Exception e) {
                log.error("doBeforeRegister occurred error.", e);
            }
        }
        log.info("End ConsulRegistryInterceptor beforeRegister");
    }

    @Override
    public void destroy() {
        if (!this.properties.isRegister() || !this.properties.isDeregister()) {
            log.info("Deregistration disabled.");
            return;
        }
        for (ConsulRegistryInterceptor consulRegistryInterceptor : consulRegistryInterceptors) {
            try {
                consulRegistryInterceptor.afterDeregister();
            } catch (Exception e) {
                log.error("doAfterDeregister occurred error.", e);
            }
        }
    }

    @Override
    public int getOrder() {
        // 需要比ConsulAutoServiceRegistrationListener的顺序小
        return -1;
    }
}
