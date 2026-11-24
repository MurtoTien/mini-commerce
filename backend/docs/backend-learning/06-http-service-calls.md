# 第六章 应用之间通过 HTTP 调用

## 学习目标

- 理解后端应用也可以成为 HTTP 客户端。
- 看懂 RestClient 请求、响应转换与错误处理。
- 区分本地依赖调用与跨进程网络调用。

## 一个应用的两种角色

```text
客户端 → 商品应用 → 店铺应用
            ↑           ↑
          服务端       服务端
            └─调用店铺时又是客户端
```

服务端和客户端是某次交互中的角色，不是某个程序永远固定的身份。我们的商品应用接收商品请求，再通过 RestClient 向店铺发送 HTTP 请求。

## RestClient 与 iOS 对照

```java
return restClient.get()
        .uri("/shop/info?shopId={shopId}", shopId)
        .retrieve()
        .body(ShopDTO.class);
```

| Java 操作 | 含义 | iOS 类比 |
| --- | --- | --- |
| RestClient | 发起 HTTP 请求 | URLSession 的部分职责 |
| get、uri | 指定方法和地址 | 构建 URLRequest |
| body(ShopDTO.class) | 读取响应并转换模型 | JSONDecoder 解码 |
| RestClientException | 客户端调用错误 | 网络层错误处理 |

这是同步调用：当前工作线程等待结果，但其他请求可以由其他线程处理。将地址放在 services.shop.base-url 中，避免把环境地址散落在业务代码里。

## HTTP 客户端不需要共享同一份 Java 类

店铺服务返回 JSON，商品应用可以定义自己的 ShopDTO，只要字段名、类型和反序列化约定匹配。双方甚至可以用不同语言。

当前历史示例中，店铺响应字段是 description，而旧 ShopDTO 仍使用 desc。这会使描述映射不符合预期，具体可能丢失字段或产生解码异常，取决于 JSON 配置。这不是数据库没有保存描述。

## 远程调用需要考虑失败

- 连接地址不可达或端口未监听。
- 响应为 4xx、5xx。
- 请求超时，或者响应不是预期 JSON。
- 下游返回空数据，业务无法继续。

超时不代表对方没有执行操作。查询重试和创建、扣款重试的风险不同；写操作需要后续学习幂等性，不能盲目自动重试。

HTTP 适合内部查询、页面聚合和外部平台接入。同一进程里的普通业务对象通常直接调用方法，不需要通过 HTTP 绕一圈。无需立即获得结果的工作，还可以按需求选择消息队列，当前项目未接入。

## 项目与企业案例

当前 /goods/info 使用固定的演示商品信息，再调用店铺 1；它并没有按 goods_id 查询 goods 表。真实商品列表是 /goods/list。

我们查看的 wsc-app-gateway 在获取闪屏图等业务中使用封装的 HttpClient 发起请求，也同时使用 Dubbo。HTTP 与 RPC 可以在同一系统中并存。

## 复习练习

在 ShopClient 和 ShopController 中分别打断点，请求 /goods/info，观察请求从商品进入店铺的过程。再检查商品 URL、店铺 URL 与数据模型中的字段名。

## 自测

1. 两个应用都在本机，HTTP 调用还是网络调用吗？
2. JSON 字段从 desc 改为 description 后，调用方一定无需改动吗？
3. 调用超时是否可以断定店铺没有执行创建操作？

答案见[复习自测答案](review-answers.md#第六章)。

## 代码入口

[ShopClient](../../goods-service/src/main/java/com/murto/goods/client/ShopClient.java) · [历史商品接口](../../goods-service/src/main/java/com/murto/goods/controller/GoodsController.java)
