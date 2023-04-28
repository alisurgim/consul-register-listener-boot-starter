# Consul注册注销事件监听器

## 简介

使用Consul作为注册中心，在服务注册前、注销后，增加了额外的扩展点，用于应用中一些事物的处理。比如

- 热点数据加载。在注册前，需要从数据库中加载一些热点数据到本地缓存。
- 服务上线通知。在注册前，发送钉钉消息到群里，以感知服务发布进度。
- 优雅下线。在注销后，需要等待若干秒，使其他服务将当前节点从本地负载均衡服务列表中移除（已提供此实现）。

## 使用方式

自定义类，继承`ConsulRegistryInterceptor`，按需实现`beforeRegister`方法或`afterDeregister`。

```java
import org.springframework.stereotype.Component;

@Component
public class ConsulCustomizeInterceptor implements ConsulRegistryInterceptor {

    @Override
    public void beforeRegister() {
        // do something
    }

    @Override
    public void afterDeregister() {
        // do something
    }
}
```

## 优雅下线

默认关闭，使用以下配置开启，将会在服务从Consul注销后，等待一段时间，在此期间会检查Tomcat的线程数，线程数为0或超时将继续往下执行。

```yaml
spring:
  cloud:
    consul:
      safe-shutdown:
        enabled: true
```

可以通过`max-attempts`和`intervals`控制等待时长，分别表示最大尝试次数与间隔时间。默认为30次，每次1000ms。

```yaml
spring:
  cloud:
    consul:
      safe-shutdown:
        enabled: true
        delay: 2000
        max-attempts: 30
        intervals: 1000
```

> 需要注意，在等待期间如果被脚本kill -9，会直接终止程序。