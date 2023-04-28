package com.alisurgim.consul.listener.register;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author alisurgim
 * @date 2023/3/31 18:57
 */
@Data
@ConfigurationProperties(prefix = "spring.cloud.consul.safe-shutdown")
public class SafeOfflineProperties {

    /**
     * 是否开启
     */
    private boolean enabled = false;
    /**
     * 最大尝试次数
     */
    private Integer maxAttempts = 30;

    /**
     * 间隔时间, 毫秒
     */
    private Integer intervals = 1000;

    /**
     * 延迟多久开始尝试, 毫秒
     */
    private Integer delay = 2000;
}
