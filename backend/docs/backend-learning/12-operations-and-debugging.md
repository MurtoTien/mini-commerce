# 第十二章 本地运行 排错与目录迁移

## 学习目标

- 分清启动失败、路由错误、业务错误和数据库错误。
- 能检查、正常停止基础服务，并理解测试对环境的影响。
- 迁移目录时保留程序、数据和配置之间的关系。

## 启动顺序与端口

完整联调：MySQL、ZooKeeper → 店铺、账号 → 商品 → 网关。端口只在应用或服务实际启动后监听，代码文件存在不等于程序在运行。

| 服务 | 端口 |
| --- | --- |
| MySQL | 3306 |
| ZooKeeper | 2181 |
| 网关 HTTP | 8080 |
| 账号 HTTP / Dubbo | 8081 / 20882 |
| 店铺 HTTP / Dubbo | 8082 / 20881 |
| 商品 HTTP | 8083 |

检查监听：

```bash
lsof -nP -iTCP:3306 -sTCP:LISTEN
lsof -nP -iTCP:2181 -sTCP:LISTEN
lsof -nP -iTCP:8082 -sTCP:LISTEN
```

端口监听只是第一层检查，实际服务响应还需要 ping、status 或请求验证。

## MySQL 的启动与停止

当前 Mac 使用 Homebrew 的 mysql@8.4：

```bash
brew services list
brew services start mysql@8.4
mysqladmin -u root ping
brew services stop mysql@8.4
```

如果设置过密码，给 mysqladmin 添加 -p，交互输入。mysqld is alive 表示服务响应。正常停止 MySQL 不删除表数据；不应该为了重启服务删除其 data 目录。

## ZooKeeper 的启动与停止

在仓库根目录执行统一脚本；脚本定位发行包并固定相对路径的解析目录：

```bash
bash backend/scripts/zookeeper.sh start
bash backend/scripts/zookeeper.sh status
bash backend/scripts/zookeeper.sh stop
```

当前单机运行通常显示 Mode: standalone。客户端 zkCli.sh 中的 quit 只断开客户端，不能替代停止服务。停止命令找不到 PID 文件时，不能只靠提示判断进程状态，结合监听端口和实际进程确认。

## 我们遇到的错误

| 现象 | 所在层次 | 检查方式 |
| --- | --- | --- |
| New 菜单没有 Package | IDE 工程识别 | Maven 导入、源码目录是否为 Sources Root |
| Module shop-server not found | Maven 模块路径 | 目录名与父 POM modules 是否一致 |
| ArtifactHandlerManager / Guice 错误 | Maven 与 IDE 集成 | 检查 Wrapper、Bundled 及版本兼容，不是给业务代码加 Guice |
| 依赖版本 unknown | Maven 版本管理 | Spring Cloud BOM 是否放在 dependencyManagement |
| Unresolved plugin | 插件解析 | 刷新 Maven，查下载与完整构建日志，不能直接认定版本不存在 |
| Address already in use | 端口 | 查实际占用进程，或给测试配置独立端口 |
| Connection refused | 连接 | 目标进程、端口、监听地址是否正确 |
| HTTP 404 | 路由或资源 | 方法、路径、实际部署代码、业务约定 |
| POST 创建后没有记录 | 请求与持久化 | 先看状态码，404 代表并未创建成功 |
| shop={} | JSON 模型映射 | getter、可见性、record、字段名是否正确 |
| SQL 一直出现 -> | MySQL 命令行 | 是否缺少英文分号或未闭合语句 |
| INSERT INFO 报 1064 | SQL 语法 | 正确语法为 INSERT INTO |
| HTTP 正常而 Dubbo 失败 | RPC 链路 | 注册中心、RPC 端口、API 版本、序列化与调用异常 |

修改 Java 代码后，当前未启用自动热重载，通常需要重启。数据库记录提交后，重新查询即可读取；DBeaver 旧数据页需要刷新。

## 从直接访问开始缩小范围

```text
先访问 localhost:8082/shop/info?shopId=实际ID
    ├── 连接失败：看店铺是否启动
    ├── 返回错误：看店铺控制台与数据库
    └── 成功：再检查商品调用、网关转发
```

日志重点看最初的业务触发位置和 Caused by，不能只看最后一行构建失败摘要。Surefire 提示 See .../surefire-reports 只是报告路径，应打开具体测试的 txt 或 XML。

## 测试为什么也会占端口

@SpringBootTest 会加载应用上下文，即使没有启动实际 HTTP 监听，也可能初始化 Dubbo 并绑定 RPC 端口。运行中的店铺已经占用 20881 时，旧启动测试再次使用同端口就会失败。

账号启动测试已使用动态 RPC 端口和 N/A 注册中心；店铺默认启动测试仍依赖实际配置。纯业务单测、数据库集成测试、网络 RPC 测试验证的范围不同，编译或部分单测通过不代表所有环境测试都通过。

## 目录迁移

1. 正常停止相关应用、ZooKeeper、MySQL。
2. 移动项目文件和需要迁移的 ZooKeeper 程序、数据目录。
3. 检查 zoo.cfg 的 dataDir、IDEA 模块、运行配置工作目录及本地脚本路径。
4. 重新导入 Maven，确认父子模块路径与依赖解析。
5. 启动基础服务、业务应用、网关，再逐层验证。

仓库配置 backend/config/zookeeper/zoo.cfg 使用相对路径：

```properties
dataDir=../../datas/zookeeper-data
```

统一脚本先切换到 backend/local-services 下的 ZooKeeper 发行包目录，因此从任何目录调用脚本都会使用同一个数据目录。不要直接从其他工作目录调用发行包的 zkServer.sh。

项目源码目录不是 MySQL 的数据目录，移动仓库不会自动搬走 MySQL 数据。新电脑按数据库说明执行初始化；迁移现有业务数据则另行导出、导入数据库。

local-services 可以用来组织本地基础服务程序，但 Homebrew 安装的 MySQL 仍由 Homebrew 管理，并不是已经被放入该目录。

## 复习练习

在不改变数据的情况下，按端口检查、直接 HTTP 查询、网关查询、Dubbo 调用的顺序验证系统，记录每个请求进入的应用。做迁移演练时先检查路径，不直接删除数据目录。

## 自测

1. HTTP 404 和 Connection refused 有何区别？
2. 为什么重启 MySQL 后数据仍在，而修改 Java 代码通常需要重启应用？
3. 父工程 install 失败提示端口占用，为什么可能是测试而不是正式应用启动？

答案见[复习自测答案](review-answers.md#第十二章)。

## 操作入口

[账号操作](../../account-service/README.md) · [店铺操作](../../shop-service/README.md) · [商品操作](../../goods-service/README.md) · [网关操作](../../gateway-service/README.md) · [数据库脚本](../../datas/database/README.md)
