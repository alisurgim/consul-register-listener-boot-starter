package com.alisurgim.consul.listener.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistry;

/**
 * @author alisurgim
 * @date 2023/3/31 11:13
 */
@Slf4j
public class ConsulServiceRegistryBeanPostProcessor implements InstantiationAwareBeanPostProcessor, BeanFactoryAware {

    private BeanFactory beanFactory;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // 在ConsulServiceRegistry创建前，需先创建DelegatingConsulRegistryListener，保证销毁顺序
        if (bean.getClass() == ConsulServiceRegistry.class) {
            log.info("Find class => ConsulServiceRegistry. loading another class first=> DelegatingConsulRegistryListener");
            beanFactory.getBean(DelegatingConsulRegistryListener.class);
        }
        return bean;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
