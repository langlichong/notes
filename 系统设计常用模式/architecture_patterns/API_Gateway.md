# API Gateway (API 网关模式)

作为微服务架构体系中的"统一入口"和"交通枢纽"，API 网关将所有外部流量汇聚到一个集中的控制点，在此完成身份验证、协议转换、路由分发、监控日志等横切关注点（Cross-cutting Concerns）的统一处理。

## 1. 痛点：分散的横切关注点与混乱的入口

在没有网关的微服务架构中：

**混乱场景**：
1.  **前端直连微服务**：iOS App 需要知道 `order-service` 在 `192.168.1.10:8080`，`user-service` 在 `192.168.1.20:8081`。服务 IP 一变，所有客户端都要更新代码重新发版。
2.  **重复的认证逻辑**：每个微服务都要写一遍 JWT 验证代码。50 个服务 = 50 份重复代码。
3.  **无法统一限流**：恶意用户疯狂调用某个服务。由于每个服务独立限流，攻击者可以分散火力绕过防护。
4.  **协议不统一**：前端需要 RESTful JSON，但内网微服务用的是 gRPC 或 Thrift。

**本质问题**：微服务强调"单一职责"，但认证、限流、日志这些"脏活"如果每个服务都自己做，就会产生巨大冗余。

---

## 2. 解决方案：所有请求的"安检口"

网关作为微服务体系内外的唯一边界：

### 核心职责
1.  **路由 (Routing)**：根据 URL 路径或 Header 将请求转发到对应的后端服务。
    *   `/api/orders/*` -> Order Service
    *   `/api/users/*` -> User Service
2.  **认证与授权 (Authentication & Authorization)**：验证 JWT Token 或 OAuth2 令牌。拒绝未授权请求。
3.  **协议转换 (Protocol Translation)**：前端 HTTP/JSON -> 后端 gRPC/Protobuf。
4.  **限流与熔断 (Rate Limiting & Circuit Breaking)**：全局限流（每秒 10 万）+ 单服务熔断。
5.  **日志与监控 (Logging & Monitoring)**：统一记录所有请求的耗时、状态码、错误信息。
6.  **响应聚合 (Response Aggregation)**：有时候网关会聚合多个微服务的响应（类似 BFF）。

---

## 3. 实现策略

### 技术选型
*   **Kong**: 基于 Nginx + Lua，性能极高，插件丰富（认证、限流、日志全都有）。
*   **Spring Cloud Gateway**: Java 生态首选。基于 WebFlux（响应式），天然支持 Spring Boot 整合。
*   **Traefik**: 云原生，与 K8s 深度整合，自动服务发现。
*   **Envoy**: Service Mesh（Istio）的数据面代理，功能极其强大。

### 配置示例 (Spring Cloud Gateway)
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: order_route
          uri: lb://order-service  # 从注册中心动态发现
          predicates:
            - Path=/api/orders/**
          filters:
            - StripPrefix=1       # 去掉 /api 前缀
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10  # 每秒补充10个令牌
```

---

## 4. 关键挑战

*   **单点故障 (SPOF)**：网关挂了，整个系统都挂了。必须做 **高可用集群 + 健康检查**。
*   **性能瓶颈**：所有流量都经过网关。必须优化网关性能（异步非阻塞、连接池调优）。
*   **过度臃肿**：不要在网关里写业务逻辑。它只能做"交通警察"的事，不能干"法官"的活。

---

## 5. 与 BFF 的区别

*   **API Gateway**: 面向所有客户端，处理通用的横切关注点（认证、限流、路由）。
*   **BFF (Backend for Frontend)**: 面向特定客户端（如 Mobile BFF），处理数据聚合和裁剪。
*   **组合使用**：`Client -> API Gateway -> BFF -> Microservices`。

## 6. 总结
API Gateway 是微服务架构的 **"城门守卫"**。
*   **信条**：将混乱的外部世界与有序的内部体系隔离开来。
*   **心法**：网关越轻量越好。复杂的业务逻辑交给 BFF 或微服务本身。
