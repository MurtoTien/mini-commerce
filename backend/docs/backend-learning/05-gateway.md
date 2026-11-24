# 第五章 网关路由与统一入口

## 学习目标

- 理解网关如何在没有业务 Controller 的情况下转发请求。
- 区分路由转发与业务聚合。
- 根据路径和端口定位请求去了哪个应用。

## 请求如何转发

```text
GET localhost:8080/shop/info?shopId=1
    ↓ 网关匹配 /shop/**
GET localhost:8082/shop/info?shopId=1
    ↓ 店铺应用返回 JSON
网关将响应传回客户端
```

我们引入 Gateway Server WebFlux Starter，由自动配置创建路由处理组件，读取配置后完成匹配、下游 HTTP 通信和响应转发。没有手写转发方法，是因为框架已有实现。

```properties
spring.cloud.gateway.server.webflux.routes[1].id=shop-route
spring.cloud.gateway.server.webflux.routes[1].uri=http://localhost:8082
spring.cloud.gateway.server.webflux.routes[1].predicates[0]=Path=/shop/**
```

| 配置 | 作用 |
| --- | --- |
| id | 标记一条路由规则 |
| uri | 下游服务 HTTP 地址 |
| predicates | 是否匹配当前请求 |

当前没有 StripPrefix 或 RewritePath，路径、参数、请求体会保留。POST、PUT、DELETE 不需要各自写一条 Controller 转发方法。

## 当前路由

| 网关路径 | 下游 |
| --- | --- |
| /accounts/** | localhost:8081 |
| /shop/** | localhost:8082 |
| /goods/** | localhost:8083 |

网关没有连接数据库，也没有直接调用 Dubbo。商品应用内部仍可以通过 Dubbo 查询店铺；“HTTP 入口”和“内部 RPC”是同一条请求链的不同阶段。

## 网关与业务聚合

当前 gateway-service 主要是路由网关。之前分析的 wsc-app-gateway 除了 HTTP 入口，还包含业务组织、HTTP/RPC 调用和数据转换，职责更接近面向 App 的业务聚合层。

```text
路由网关：根据路径，将请求转发给一个下游
业务聚合：主动调用多个能力，整理结果，再返回适合页面的数据
```

例如“商品业务判断依赖店铺类型”适合在商品业务层调用店铺；“页面同时展示商品和店铺名称”也可以在聚合层分别获取后组装。不必让所有内部调用绕回网关。

## 运行与失败

网关不会替你启动下游应用。停止店铺后，店铺路由可能失败，账号路由仍可正常工作。下游直接访问成功而网关失败时，重点检查路径匹配、目标 URI 和运行中的配置。

监听端口通过 server.port=8080 配置，可用 SERVER_PORT 环境变量覆盖；下游地址可通过 ACCOUNT_HTTP_URL、SHOP_HTTP_URL、GOODS_HTTP_URL 覆盖。

## 复习练习

在服务运行时，分别通过 8082 和 8080 查询同一家店铺。停止店铺，只请求读取接口，观察网关与账号接口的不同表现；完成后恢复店铺服务。

## 自测

1. 为什么网关没有 /shop/create 的 Controller 也能转发 POST？
2. 网关是否会自动把下游错误变成成功？
3. 路由网关、BFF 业务聚合、注册中心的职责相同吗？

答案见[复习自测答案](review-answers.md#第五章)。

## 代码入口

[网关配置](../../gateway-service/src/main/resources/application.properties) · [网关 POM](../../gateway-service/pom.xml) · [网关操作说明](../../gateway-service/README.md)
