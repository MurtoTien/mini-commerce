# 第三章 工程分层与依赖注入

## 学习目标

- 为 Controller、Service、Client、DAO 和 DTO 分配正确职责。
- 理解 Spring 如何创建对象并通过构造器连接它们。
- 区分共享依赖对象与每次请求的数据。

## 分层的目的

```text
Controller   接收 HTTP 请求，处理协议相关内容
    ↓
Service      校验参数，执行业务规则，组织操作
    ↓
Client       调用其他应用
或 DAO       访问数据库
```

这条结构不是要求每个方法机械穿过所有层，而是把变化原因分开。修改 SQL 不应要求客户端改接口；HTTP 和 Dubbo 入口应能复用同一个店铺业务方法。

| 后端角色       | iOS 中近似的职责                  |
| ---------- | --------------------------- |
| Controller | 外部事件入口，不等于 UIViewController |
| Service    | UseCase、业务 Service          |
| Client     | APIClient                   |
| DAO        | Core Data Store、数据库访问封装     |
| 请求 DTO     | Encodable 请求模型              |
| 响应 DTO     | Decodable 响应模型              |

当前店铺的两个入口共用 ShopService；账号同样由 HTTP Controller 和 AccountQueryServiceImpl 调用 AccountService。

## 构造器注入

```java
@Service
public class ShopService {
    private final ShopDAO shopDAO;

    public ShopService(ShopDAO shopDAO) {
        this.shopDAO = shopDAO;
    }
}
```

这是依赖注入：类声明自己需要谁，由外部提供对象。Spring 发现 @Repository、@Service、@RestController 等组件后，负责组装它们。概念上类似 Swift 的 init(shopDAO:)；只有一个构造器时，Spring 通常不需要额外的 @Autowired。

有多个必需依赖时，可以全部作为构造参数传入，但不是把整个系统所有 Service 都传入。参数越来越多时，应检查类是否承担了过多职责。

| 方式 | 特点 |
| --- | --- |
| 构造器注入 | 必需依赖明确，可使用 final，便于直接构造和测试 |
| 字段注入 | 字段上使用 @Autowired、@Resource，手动 new 时字段不会自动填入 |
| Setter 注入 | 通过设置方法提供依赖，适用于相应的可选或可调整场景 |

Dubbo 的 @DubboReference 字段由 RPC 框架创建并注入远程代理，不应为了形式统一直接替换为普通的本地构造参数。

## 依赖对象与请求数据

构造器接收 ShopDAO 等长期协作对象，业务方法接收 shopId、CreateShopRequest 等本次操作数据。

Spring 组件默认通常是单例，同一个 Controller、Service、DAO 会被多个请求线程调用。不要把 currentUserId、currentShopId 等本次请求状态存到共享字段；方法参数、局部变量则按调用分别使用。

## DTO 与数据库模型

创建店铺请求只包含 name、description、shopType。完整 ShopInfo 还包含 id、createdAt。客户端不应决定数据库主键、创建时间；账号对外模型更不能包含 password_hash。

record 也是模型的一种形式，会生成构造器和访问方法，字段不可重新赋值；普通 class 则需要合适的构造器、访问方法或映射配置。我们曾因普通类的字段不可访问且没有 getter，得到 shop={}。

小型 Demo 可以适度复用模型，但不要为了少写转换，让数据库内部字段自动成为外部可修改字段。

## 复习练习

从 AccountController 一直跟到 AccountDAO，标出哪个位置负责 HTTP 404，哪个位置负责资料校验，哪个位置执行 SQL。

## 自测

1. 构造器参数传 ShopDAO，业务方法传 shopId，为什么？
2. 同一 Service 被多个线程调用，是否每个请求都会 new 一个 Service？
3. 用 record 替代 Map 后，会自动获得数据库持久化能力吗？

答案见[复习自测答案](review-answers.md#第三章)。

## 代码入口

[AccountService](../../account-service/account-server/src/main/java/com/murto/account/service/AccountService.java) · [ShopInfo](../../shop-service/shop-api/src/main/java/com/murto/shop/api/dto/ShopInfo.java)
