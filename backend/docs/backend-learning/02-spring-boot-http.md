# 第二章 启动 Spring Boot 并理解 HTTP 接口

## 学习目标

- 知道 Initializr 生成了什么，理解普通 Java 工程如何成为后端应用。
- 分清主机、端口、请求路径、查询参数和请求体。
- 认识 HTTP 入口的主要注解与状态码。

## 工程从哪里来

Spring Initializr 根据语言、Java 版本、构建工具和依赖选择生成工程骨架：POM、启动类、标准源码目录、配置文件、基础测试、Maven Wrapper 等。它不会实现账号、店铺等业务。

直接 New 一个 Java Maven 工程后，手动添加 Spring Boot 父配置、Web 依赖、启动类和打包插件，也能达到相同效果。框架版本不同，Starter 名称可能不同，应以工程 POM 为准：当前账号、店铺和商品为 Boot 3.5.16，使用 spring-boot-starter-web；网关为 Boot 4.1.3，使用 Gateway WebFlux Starter。

```java
@SpringBootApplication
public class ShopServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShopServiceApplication.class, args);
    }
}
```

Spring 启动后发现组件、组装依赖并配置 Web 服务。默认组件扫描从启动类所在包向下进行，因此 Controller 等通常放在 com.murto.shop 的子包中。

## 包名与目录

com.murto.shop.controller 通常对应源码目录 com/murto/shop/controller。包名也承担命名空间作用，不仅是文件夹名称；不同包中可以有同名类。

IDEA 把连续包压缩显示为一行，不代表实际只有一层目录。新建包时应在 src/main/java 下操作；没有 Package 菜单时，先检查 Maven 导入和 Sources Root。

## 一次 HTTP 请求

```text
http://localhost:8082/shop/info?shopId=1
       └─本机    └端口 └─路径    └─查询参数
```

localhost 指当前设备。iOS 模拟器通常可访问 Mac 的 localhost；iPhone 真机上的 localhost 指手机自身，访问 Mac 时要使用 Mac 的可达局域网地址。

```java
@GetMapping("/shop/info")
public ShopInfo getShopInfo(@RequestParam("shopId") Long shopId) {
    return shopService.queryShop(shopId);
}
```

框架按请求方法和路径找到方法，解析参数、调用代码，再将返回对象转换成 JSON。方法名不决定 URL，注解中的映射才决定 URL。

| 注解              | 作用                   | 示例                |
| --------------- | -------------------- | ----------------- |
| @RestController | HTTP 接口组件，返回值进入响应体转换 | ShopController    |
| @GetMapping     | 处理 GET 请求            | 查询店铺              |
| @PostMapping    | 处理 POST 请求           | 创建店铺              |
| @PutMapping     | 处理 PUT 请求            | 更新账号资料            |
| @DeleteMapping  | 处理 DELETE 请求         | 删除账号              |
| @PathVariable   | 获取路径片段               | /accounts/1 中的 1  |
| @RequestParam   | 获取查询参数               | ?shopId=1         |
| @RequestBody    | 将请求体 JSON 转成对象       | CreateShopRequest |

多个参数可以同时存在：/shops/1/goods?page=2 中，1 是路径参数，page 是查询参数。POST JSON 则放在请求体中。

## 状态码与实际结果

| 状态 | 含义 |
| --- | --- |
| 200 | 正常返回数据 |
| 201 | 资源创建成功 |
| 204 | 成功且无响应体，例如删除账号 |
| 400 | 请求参数、格式或业务校验不合法 |
| 404 | 资源或路由不存在，具体以接口约定为准 |
| 500 | 服务内部错误 |
| 502 | 网关或应用认为下游调用失败 |

浏览器地址栏通常发送 GET，不能直接测试 POST 创建。curl 能打印 JSON 不代表成功，应同时检查状态码；我们曾遇到 POST 返回 404，因此数据库没有插入记录。

## 复习练习

在店铺应用运行时分别访问实际存在和不存在的 shopId，再使用 curl -i 查看响应状态。当前店铺接口将不存在也转换为 400，账号接口则返回 404，不能仅凭习惯推断实际约定。

## 自测

1. 改 Java 方法名会自动改变 URL 吗？
2. @RestController 是否负责线程调度、JSON 编解码的底层实现？
3. iOS 真机与模拟器中的 localhost 有何区别？

答案见[复习自测答案](review-answers.md#第二章)。

## 代码入口

[ShopController](../../shop-service/shop-server/src/main/java/com/murto/shop/controller/ShopController.java) · [AccountController](../../account-service/account-server/src/main/java/com/murto/account/controller/AccountController.java)
