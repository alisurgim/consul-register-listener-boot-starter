package com.alisurgim.consul.listener.autoconfigure;

import org.springframework.core.Ordered;

/**
 * @author alisurgim
 * @date 2023/3/30 11:12
 */
public interface ConsulRegistryInterceptor extends Ordered {

    /**
     * 在注册到consul之前
     */
    default void beforeRegister() {
    }

    /**
     * 在从consul注销之后
     */
    default void afterDeregister() {
    }

    /**
     * 执行顺序
     *
     * @return
     */
    @Override
    default int getOrder() {
        return 0;
    }
}
