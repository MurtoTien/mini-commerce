# 第四章 Maven 依赖 模块与构建

## 学习目标

- 看懂 Maven 坐标、依赖与版本管理。
- 区分 Java 包、Maven 模块、应用进程。
- 理解运行 main、package、install 的不同结果。

## Maven 管理什么

Maven 依据 pom.xml 管理依赖和构建。一个依赖通常由 groupId、artifactId、version 唯一定位，例如 com.murto:shop-api:1.0.0-SNAPSHOT。

```xml
<dependency>
    <groupId>com.murto</groupId>
    <artifactId>shop-api</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

父 POM、BOM 可以统一管理版本，所以某些依赖不需要重复填写 version。Starter 则集中引入一组配套依赖，减少逐项选择的工作。

| 配置 | 作用 |
| --- | --- |
| dependencies | 当前模块实际引入的依赖；放在父 POM 中会被子模块继承 |
| dependencyManagement | 统一管理依赖版本等默认配置，不会自动引入所有依赖 |
| parent | 继承父配置 |
| modules | 把子模块纳入同一个 Maven Reactor 构建 |
| build/plugins | 配置编译、测试、打包等构建插件 |

Spring Cloud BOM 需要在 dependencyManagement 中以 type=pom、scope=import 导入。我们曾把它放入普通 dependencies，导致网关依赖版本显示 unknown。

## 多模块组织

```text
account-service/              父工程，packaging=pom
├── pom.xml
├── account-api/              普通 JAR
│   └── pom.xml
└── account-server/           Spring Boot 应用
    └── pom.xml
```

类比 iOS 的内部 Pod 或 Swift Package Target：子模块有自己的依赖和产物。模块不等于进程，account-api 不运行服务。

modules 只组织构建，不代表兄弟模块自动互相依赖。account-server 仍需显式声明对 account-api 的 dependency。子模块目录名称必须与父 POM 的 module 路径一致；我们曾将 shop-server 建成内层 shop-service，导致 Module not found。

## 三种 JAR 用途

| 类型        | 内容               | 执行位置               |
| --------- | ---------------- | ------------------ |
| 公共实现库     | 工具、通用逻辑          | 当前应用进程             |
| 接口契约库     | Java 接口、DTO、共享异常 | 只提供类型定义，具体实现仍在远端   |
| 网络客户端 SDK | 封装 HTTP、RPC 等客户端 | 本地执行 SDK，再通过网络访问远端 |

仅看到“依赖了一个 JAR”不能判断是否是远程调用。

## 运行与打包

| 操作 | 结果 |
| --- | --- |
| IDEA 运行 main | 编译后启动 Java 进程，适合调试 |
| mvn compile | 编译主代码 |
| mvn test | 执行测试，可能启动测试上下文 |
| mvn package | 生成 target 中的构建产物 |
| mvn install | 先完成构建，再把 JAR 和 POM 安装到本机 Maven 仓库 |
| mvn clean | 清理构建产物，不清理 MySQL 数据 |

Spring Boot 可执行 JAR 能用 java -jar 启动；普通 API JAR 没有 main，不是独立服务。

第一次在父工程执行 install，可一起安装父 POM 和 API JAR。以后只改 API，可以按需单独构建，但其父配置和依赖也必须能被解析。install 不是部署到服务器，也不是发布到远程私服。

## Maven 来自哪里

Use Maven Wrapper 使用 .mvn/wrapper/maven-wrapper.properties 指定的 Maven 版本；Bundled 使用 IDEA 自带版本。切换 IDEA 的选项不会改写 Wrapper 文件，终端 ./mvnw 与 IDE 可能使用不同版本。

旧 IDEA 与较新 Maven 可能存在导入兼容问题。Guice 的 ArtifactHandlerManager 错误发生在构建工具层，不是业务代码需要添加一个 Guice 实现。

## 复习练习

打开账号父 POM 和两个子 POM，找出模块组织、父配置继承、API 依赖和 Boot 打包插件的位置。解释数据库依赖为什么在 server 模块，而不在 API 模块。

## 自测

1. modules 和 dependency 有什么区别？
2. 为什么另一个工程依赖 account-api 时也可能需要父 POM？
3. install 结束后，HTTP 服务是否自动运行？

答案见[复习自测答案](review-answers.md#第四章)。

## 代码入口

[账号父 POM](../../account-service/pom.xml) · [账号 API POM](../../account-service/account-api/pom.xml) · [账号服务 POM](../../account-service/account-server/pom.xml)
