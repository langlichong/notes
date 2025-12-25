# Service Registry & Discovery (服务注册与发现模式)

微服务架构的**基础设施核心**。通过将服务实例的网络位置信息集中存储在注册中心，让服务消费者可以动态地发现和调用服务提供者，从而消除硬编码的服务地址依赖，实现服务的动态扩缩容和高可用。

## 1. 痛点：硬编码地址的"脆弱依赖"

在没有服务发现的微服务环境中：

**灾难场景**：
```java
// Order Service 的代码
RestTemplate restTemplate = new RestTemplate();
String userInfo = restTemplate.getForObject(
    "http://192.168.1.10:8080/users/123",  // ❌ 硬编码 IP 和端口
    String.class
);
```

**问题爆发**：
1.  **服务器 IP 变更**：User Service 重启后 IP 从 `192.168.1.10` 变成了 `192.168.1.20`。Order Service 的所有请求全部失败。
2.  **水平扩容无法利用**：运维增加了 3 台 User Service 实例，但 Order 只知道第一台的地址，流量无法分散，新实例完全闲置。
3.  **故障无法自愈**：User Service 的 `192.168.1.10` 实例挂了，但 Order 还在疯狂调用它，导致大量超时。
4.  **环境切换困难**：从测试环境切到生产环境，需要修改几十个微服务的配置文件。

**本质问题**：微服务的网络拓扑是动态的（容器化、弹性伸缩），但硬编码地址是静态的。两者产生了根本性矛盾。

---

## 2. 解决方案：注册中心作为"电话簿"

引入一个集中式的注册中心（Registry），所有服务都向它报告自己的位置：

### 运行流程
```
[服务提供者 - User Service]
    |
    v (1. 启动时注册)
[Nacos / Eureka / Consul 注册中心]
    ^ (2. 定期续约心跳)
    |
    | (3. 查询服务列表)
    v
[服务消费者 - Order Service]
```

### 详细步骤
1.  **服务注册 (Service Registration)**：
    *   User Service 启动时，向 Nacos 注册：`user-service -> [192.168.1.10:8080, 192.168.1.20:8080, ...]`
2.  **健康检查 (Health Check)**：
    *   User Service 每 5 秒向 Nacos 发送心跳。如果 30 秒没心跳，Nacos 自动将其标记为下线，从服务列表中...剔除。
3.  **服务发现 (Service Discovery)**：
    *   Order Service 启动时，从 Nacos 拉取 `user-service` 的实例列表，得到 `[192.168.1.10:8080, 192.168.1.20:8080]`。
4.  **负载均衡 (Client-side Load Balancing)**：
    *   Order Service 调用时，从本地缓存的实例列表中轮询或随机选择一个。

---

## 3. 实现策略

### 技术选型
*   **Nacos**: 阿里开源。兼具服务发现 + 配置管理。云原生首选。
*   **Eureka**: Netflix 开源，Spring Cloud 早期标配（已停止迭代，但仍可用）。
*   **Consul**: HashiCorp 出品。支持多数据中心，强在一致性。
*   **ZooKeeper / Etcd**: 更底层的分布式协调服务，通常用于 Dubbo 或 K8s。

### Spring Cloud 集成示例 (Nacos)
#### 服务提供者 (User Service)
```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```

```yaml
spring:
  application:
    name: user-service  # 服务名
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848  # Nacos 地址
```

**启动类**：
```java
@SpringBootApplication
@EnableDiscoveryClient  // 开启服务注册
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

#### 服务消费者 (Order Service)
```java
@RestController
public class OrderController {
    
    @Autowired
    private RestTemplate restTemplate;  // 需要配置 @LoadBalanced
    
    @GetMapping("/order/{id}")
    public String getOrder(@PathVariable Long id) {
        // 不再写死 IP，用服务名代替
        String userInfo = restTemplate.getForObject(
            "http://user-service/users/123",  // ✅ 服务名自动解析
            String.class
        );
        return "Order info with " + userInfo;
    }
}

@Configuration
class Config {
    @Bean
    @LoadBalanced  // 关键注解：启用客户端负载均衡
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

---

## 4. 关键机制

### 心跳与摘除
*   **心跳间隔**：通常是 5 秒 (可配置)。
*   **剔除阈值**：连续 3 次心跳失败（15 秒）标记为不健康，30 秒后彻底摘除。
*   **自我保护模式 (Eureka)**：如果短时间内大量实例掉线（可能是网络分区而非真挂了），Eureka 会拒绝摘除，保留旧的服务列表。

### 客户端缓存
*   消费者不会每次调用都去查注册中心（太慢）。它会在本地缓存服务列表，定期（如 30 秒）刷新一次。
*   **优点**：性能高，注册中心故障时仍能短期工作。
*   **缺点**：最多有 30 秒的延迟（新实例上线 30 秒后才被发现）。

---

## 5. 注意事项与挑战

*   **注册中心是单点吗？**：是的。必须做集群（Nacos 支持 Raft 协议，3-5 节点）。
*   **网络分区问题**：如果注册中心与某个数据中心断网，会导致该数据中心的服务全部"失联"。
*   **服务名冲突**：团队协作时，务必约定服务命名规范（如加项目前缀 `payment-user-service`）。

---

## 6. 与 K8s Service 的关系

*   **K8s Service**: 通过 DNS 实现服务发现（如 `user-service.default.svc.cluster.local`）。
*   **Nacos/Eureka**: 应用层的服务发现。
*   **选型建议**：
    *   如果全部服务都在 K8s 里，用 K8s Service 足够。
    *   如果有虚拟机 + K8s 混合部署，或需要跨集群，用 Nacos。

## 7. 总结
Service Registry & Discovery 是微服务架构的 **"神经网络"**。
*   **信条**：服务位置不应写死，应由注册中心动态告知。
*   **心法**：没有服务发现，微服务就是一盘散沙。有了它，才能形成有机整体。
