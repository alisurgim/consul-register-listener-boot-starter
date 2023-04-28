package com.alisurgim.consul.listener.register;

import com.alisurgim.consul.listener.autoconfigure.ConsulRegistryInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.ProtocolHandler;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;

import java.util.concurrent.TimeUnit;

/**
 * @author alisurgim
 * @date 2023/3/30 11:22
 */
@Slf4j
public class ConsulSafeShutdownInterceptor implements ConsulRegistryInterceptor {

    private final ServletWebServerApplicationContext context;
    private SafeOfflineProperties properties;

    public ConsulSafeShutdownInterceptor(ServletWebServerApplicationContext context, SafeOfflineProperties properties) {
        this.context = context;
        this.properties = properties;
    }

    @Override
    public void afterDeregister() {
        log.info("服务已从consul注销，开始检测剩余请求");
        long start = System.currentTimeMillis();
        // 判断tomcat是否已经没有在处理中的请求
        TomcatWebServer webServer = (TomcatWebServer) context.getWebServer();
        Integer maxAttempts = properties.getMaxAttempts();
        Integer intervals = properties.getIntervals();
        Integer delay = properties.getDelay();
        try {
            TimeUnit.MILLISECONDS.sleep(delay.longValue());
        } catch (InterruptedException e) {
            log.error("delay. Interrupted.", e);
        }
        int remainder = maxAttempts;
        while (remainder-- > 0) {
            int activeThread = activeThread(webServer);
            if (activeThread == 0) {
                log.info("Tomcat active count=0, break");
                break;
            }
            log.info("Tomcat active thread.count={}", activeThread);
            try {
                TimeUnit.MILLISECONDS.sleep(intervals.longValue());
            } catch (InterruptedException e) {
                log.error("intervals. Interrupted.", e);
            }
        }
        log.info("ConsulSafeShutdownListener end, cost={}ms", System.currentTimeMillis() - start);
    }

    public static int activeThread(TomcatWebServer webServer) {
        int count = 0;
        for (Connector connector : webServer.getTomcat().getService().findConnectors()) {
            ProtocolHandler protocolHandler = connector.getProtocolHandler();
            if (protocolHandler instanceof AbstractProtocol) {
                ThreadPoolExecutor executor = (ThreadPoolExecutor) protocolHandler.getExecutor();
                if (executor != null) {
                    count += executor.getActiveCount();
                }
            }
        }
        return count;
    }
}
