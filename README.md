# mini-commerce

面向 iOS 开发者学习 Java 后端的电商 Demo：账号、店铺、商品、网关，包含 HTTP、Dubbo、ZooKeeper 和 MySQL 实践。

## 项目结构

```text
mini-commerce/
├── app/                          客户端预留目录
├── web/                          Web 预留目录
└── backend/
    ├── account-service/          账号 API 与服务端模块
    ├── shop-service/             店铺 API 与服务端模块
    ├── goods-service/            商品应用
    ├── gateway-service/          HTTP 网关
    ├── config/zookeeper/         注册中心配置
    ├── scripts/                  本地基础服务脚本
    ├── datas/database/           可提交的初始化 SQL
    ├── local-services/           下载的基础服务程序（不提交 Git）
    └── docs/                     学习文档
```

## 新电脑快速开始

以下为 macOS / Linux 的 Bash 操作。Windows 可使用 WSL。所有相对路径以仓库根目录为基准，IDE 中分别导入四个应用的父 pom.xml。

### 1. 准备工具

安装 JDK 21、MySQL 8.4、Maven 3.9.x；确保 java、mvn、mysql 在 PATH 中。IDEA 自带 Maven 也可使用。首次构建需联网下载 Maven Central 依赖。

```bash
java -version
mvn -version
mysql --version
```

若 macOS 使用 Homebrew 安装 mysql@8.4，可按当前机器的安装位置配置 PATH：

```bash
export PATH="$(brew --prefix mysql@8.4)/bin:$PATH"
brew services start mysql@8.4
```

Linux 按所用发行版的方式启动 MySQL。按[本地服务说明](backend/local-services/README.md)下载并放置 ZooKeeper，然后启动：

```bash
bash backend/scripts/zookeeper.sh start
bash backend/scripts/zookeeper.sh status
```

### 2. 初始化数据库

```bash
mysql --default-character-set=utf8mb4 -u root -p < backend/datas/database/schema.sql
mysql --default-character-set=utf8mb4 -u root -p < backend/datas/database/seed.sql
```

无密码的本地实例可省略 -p。脚本创建三个业务库、表和演示数据，不覆盖已有表结构。Git 只保存脚本，不包含原电脑的 MySQL 数据；已有数据迁移需要另行导出、导入。

应用默认连接本机 3306、root、空密码。若设置了数据库密码，在每个启动应用的终端中配置，或在 IDEA 各运行配置的环境变量中设置：

```bash
export DB_USERNAME=root
read -r -s -p 'MySQL password: ' DB_PASSWORD
printf '\n'
export DB_PASSWORD
```

不要将真实密码提交到仓库。.env 文件不会被 Spring Boot 或脚本自动读取；应使用终端或 IDEA 的环境变量设置。

### 3. 构建

先安装店铺共享接口，再构建商品。这里跳过环境相关测试，不代表测试已执行。

```bash
mvn -f backend/shop-service/pom.xml -DskipTests install
mvn -f backend/account-service/pom.xml -DskipTests install
mvn -f backend/goods-service/pom.xml -DskipTests package
mvn -f backend/gateway-service/pom.xml -DskipTests package
```

### 4. 启动应用

以下每条命令在独立终端运行，工作目录均为仓库根目录；先启动店铺、账号，再商品，最后网关。

```bash
java -jar backend/shop-service/shop-server/target/shop-server-1.0.0-SNAPSHOT.jar
java -jar backend/account-service/account-server/target/account-server-1.0.0-SNAPSHOT.jar
java -jar backend/goods-service/target/goods-service-0.0.1-SNAPSHOT.jar
java -jar backend/gateway-service/target/gateway-service-0.0.1-SNAPSHOT.jar
```

| 应用 | HTTP | Dubbo |
| --- | --- | --- |
| 网关 | 8080 | — |
| 账号 | 8081 | 20882 |
| 店铺 | 8082 | 20881 |
| 商品 | 8083 | 调用方 |

```bash
curl 'http://localhost:8080/accounts/1'
curl 'http://localhost:8080/shop/info?shopId=1'
curl 'http://localhost:8080/goods/list?shopId=1'
```

干净数据库初始化后演示账号 ID 为 1；已有数据时使用实际 ID。演示账号不可登录，项目尚未实现注册、登录、Token 或权限验证。

## 配置与目录迁移

| 环境变量 | 默认值 / 用途 |
| --- | --- |
| DB_USERNAME / DB_PASSWORD | root / 空，三个业务应用共用 |
| ACCOUNT_DB_URL | jdbc:mysql://127.0.0.1:3306/account_db |
| SHOP_DB_URL | jdbc:mysql://127.0.0.1:3306/shop_db |
| GOODS_DB_URL | jdbc:mysql://127.0.0.1:3306/goods_db |
| DUBBO_REGISTRY_ADDRESS | zookeeper://127.0.0.1:2181 |
| ACCOUNT_HTTP_URL / SHOP_HTTP_URL / GOODS_HTTP_URL | 网关下游地址，默认本机 8081 / 8082 / 8083；商品 HTTP 客户端也使用 SHOP_HTTP_URL |
| ACCOUNT_DUBBO_PORT / SHOP_DUBBO_PORT | 20882 / 20881 |
| SERVER_PORT | 对当前启动的应用覆盖 HTTP 端口 |

localhost 是当前电脑上的服务地址，与仓库路径无关；改端口时也需调整调用方地址。保留仓库内部结构即可移动或改名仓库。ZooKeeper 必须用统一脚本启动，相对 dataDir 才有固定基准。

.idea、target、ZooKeeper 发行包、运行数据和日志均被 Git 忽略；本机文件仍保留。新电脑安装 JDK、MySQL、Maven，下载 ZooKeeper，重新导入 Maven 并初始化数据库后即可启动。

## 文档与测试

- [学习复习手册](backend/docs/backend-learning/README.md)
- [数据库初始化与集成测试](backend/datas/database/README.md)
- [账号](backend/account-service/README.md) · [店铺](backend/shop-service/README.md) · [商品](backend/goods-service/README.md) · [网关](backend/gateway-service/README.md)

各应用 README 记录测试方式。店铺和商品的启动测试仍需本地服务；测试端口可能与已启动的应用冲突，请按说明准备环境。
