# 第七章 Dubbo 契约与远程代理

## 学习目标

- 看懂 API JAR、Provider、Consumer 三者的关系。
- 理解 @DubboService、@DubboReference、@EnableDubbo 的作用。
- 知道共享模型变化后为什么要重新构建调用双方。

## 从 HTTP 到 Dubbo

HTTP 示例约定请求方法、URL 和 JSON。当前 Dubbo 示例通过共享 Java 接口约定方法、参数和返回类型。

```java
public interface ShopQueryService {
    ShopInfo queryShop(Long shopId);
}
```

```text
编译依赖
商品应用 → shop-api.jar ← 店铺应用

运行调用
商品应用中的远程代理 → 网络 → 店铺应用中的具体实现
```

依赖 API JAR 只是获得协议和模型，不会把店铺实现带到商品应用中执行。即使两个模块在同一个父工程里，也要看实际对象和网络配置，不能由目录位置判断通信方式。

## 提供方与调用方

提供方：

```java
@DubboService
public class ShopQueryServiceImpl implements ShopQueryService {
    private final ShopService shopService;

    public ShopQueryServiceImpl(ShopService shopService) {
        this.shopService = shopService;
    }

    @Override
    public ShopInfo queryShop(Long shopId) {
        return shopService.queryShop(shopId);
    }
}
```

调用方：

```java
@DubboReference(timeout = 3000, retries = 0)
private ShopQueryService shopQueryService;
```

| 配置              | 作用                 |
| --------------- | ------------------ |
| @DubboService   | 暴露具体实现为远程服务        |
| @DubboReference | 创建并注入远程代理          |
| @EnableDubbo    | 启用相应注解扫描           |
| timeout         | 调用超时设置，示例为 3000 毫秒 |
| retries=0       | 失败后不额外重试           |

直接执行 shopQueryService.queryShop(1L) 时，代理完成参数编码、网络通信和响应解码。Java 方法外观没有消除网络错误或超时。

## 我们走过的两个阶段

1. 直连：在 @DubboReference 中指定 dubbo://127.0.0.1:端口/完整接口名。
2. 注册发现：移除 url，连接同一 ZooKeeper，由框架发现提供方地址。

HTTP 的 server.port 与 dubbo.protocol.port 是两个入口。同一个店铺进程可以同时监听 HTTP 8082、Dubbo 20881；浏览器不能直接把 Dubbo 协议端口当作 HTTP 接口访问。

## 共享模型和错误

当前 ShopInfo、AccountInfo 等共享模型位于各自 API 模块。将返回 Map 改为 ShopInfo，需要同步 API 接口、实现、调用方局部变量与导入，然后重新 install API、刷新调用方依赖并重启双方。

record 实现 Serializable 不等于所有序列化协议和配置都自动兼容。需要用实际协议验证 DTO、日期字段和异常。当前账号网络测试覆盖了这些情况。

账号的 AccountNotFoundException 放在 API 模块，精确加入 security/serialize.allowlist，供双方识别并通过严格序列化检查。不要用 Spring Web 的 ResponseStatusException 表达 Dubbo 业务错误；HTTP 状态转换应留在 HTTP 边界。

## 框架选择

Dubbo、gRPC 都能跨应用通信。选择通常取决于公司既有基础设施、语言、接口治理和运维能力。Dubbo 也支持多语言，Triple 可与 gRPC 协作，不能简单理解为 Java 与非 Java 的二选一。

当前项目为练习已有 Java 接口体系使用 Dubbo 3.3.6，账号、店铺和商品采用 Boot 3.5.16；不能把这个已验证组合推导为任意框架版本都兼容。

## 复习练习

对照账号的 AccountQueryService、AccountQueryServiceImpl、AccountService，解释接口、RPC 入口和实际业务实现的不同作用。

## 自测

1. 引入 account-api 后，为什么还需要 Dubbo 依赖和配置？
2. Dubbo 方法返回类型改变后，只重启提供方是否足够？
3. RPC 调用中捕获 RestClientException 能处理 Dubbo 网络异常吗？

答案见[复习自测答案](review-answers.md#第七章)。

## 代码入口

[账号 API](../../account-service/account-api/src/main/java/com/murto/account/api/AccountQueryService.java) · [账号实现](../../account-service/account-server/src/main/java/com/murto/account/service/AccountQueryServiceImpl.java) · [店铺客户端](../../goods-service/src/main/java/com/murto/goods/client/ShopDubboClient.java)
