# 第九章 MySQL 表与 Java 数据访问

## 学习目标

- 区分数据库服务、逻辑数据库、表、记录和客户端。
- 理解 Java 如何使用连接池和 JDBC 发送 SQL。
- 能追踪一条店铺记录从磁盘到 Java 对象再到 iOS 的过程。

## 五个概念

| 名称 | 当前例子 | 作用 |
| --- | --- | --- |
| 数据库服务程序 | MySQL mysqld | 管理数据、执行 SQL |
| 逻辑数据库 | shop_db | 组织一组业务表 |
| 表 | shop | 定义店铺记录的字段结构 |
| 行 / 列 | 一家店铺 / name 字段 | 具体记录 / 数据属性 |
| 客户端 | mysql 命令行、DBeaver、Java 驱动 | 连接服务并提交操作 |

DBeaver 不保存一套独立的 MySQL 业务数据。它与 Java、mysql 命令行可以连接同一个服务，操作同一张表。

关闭 MySQL 后，DBeaver 可能仍显示已经加载的旧结果，但无法重新查询。正常停止和启动不会清空持久化数据；删除服务的数据目录则是另一回事。

## 一次查询的完整过程

```text
iOS 发起 HTTP 请求
    ↓
网关转发 → ShopController → ShopService
    ↓
ShopDAO 调用 JdbcTemplate
    ↓
从数据源取得连接，发送参数化 SQL
    ↓
MySQL 执行查询并返回记录
    ↓
DAO 将字段映射为 ShopInfo
    ↓
框架将 ShopInfo 编码为 JSON
    ↓
iOS 解码为 Swift Model
```

Java 对象是查询结果在应用内存中的表示，不是磁盘上的记录本身。修改对象不会自动修改 MySQL，当前必须明确执行 UPDATE。

## 数据源与驱动

```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/account_db
spring.datasource.username=root
spring.datasource.password=
```

spring-boot-starter-jdbc 提供 JDBC 工具和连接池的接入，mysql-connector-j 是 Java 连接 MySQL 的驱动。依赖放在执行数据库操作的 server 模块，API 契约库不需要包含它们。

Spring Boot 读取配置，创建 DataSource 与 JdbcTemplate。JdbcTemplate 负责资源获取、SQL 执行、结果遍历与异常转换，业务代码提供 SQL 和映射逻辑。

## 参数化 SQL 和字段映射

```java
String sql = "SELECT id, name FROM shop WHERE id = ?";
```

问号绑定本次 shopId。不要把客户端输入直接拼接进 SQL 字符串。

```java
rs.getLong("id");
rs.getString("name");
rs.getTimestamp("created_at").toLocalDateTime();
```

ResultSet 是本次查询的结果，RowMapper 回调把每一行转换成 Java 对象。queryForObject 查询单行；查不到时当前 DAO 捕获 EmptyResultDataAccessException 返回 null，再由 Service 决定如何表达“店铺不存在”。

## ID 与创建时间

```sql
id BIGINT PRIMARY KEY AUTO_INCREMENT,
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
```

INSERT 省略这两个字段，MySQL 自动分配 ID、填写时间。DAO 使用 RETURN_GENERATED_KEYS 和 KeyHolder 取回 ID。

数据库自增也是服务端系统生成 ID。它不保证连续，不能用最大 ID 当记录总数。其他系统也可能选择 UUID、雪花算法或统一 ID 服务，当前未接入。

record 不会被数据库自动回填；创建前 id=null，插入后再查询，才能得到完整对象。

## DBeaver 与 SQL 命令

DBeaver 选择 MySQL 8 and later 驱动，连接 localhost:3306、目标数据库和账号。执行 INSERT、UPDATE 后，数据页需要刷新；手动提交模式下还要提交事务。

mysql 命令行的 mysql> 表示等待新命令，-> 通常表示等待当前语句结束，英文分号用于执行。INSERT INTO 不能写成 INSERT INFO。

## 复习练习

先用 DBeaver 查询 shop 表，再通过 HTTP 查询同一 ID。仅对演示数据修改名称，提交后刷新 HTTP 接口，确认不重启应用也能读取新值。

## 自测

1. 关闭 DBeaver 和关闭 MySQL 有什么不同？
2. 修改 ShopInfo 对象会自动更新表吗？
3. 自动生成 ID 和 created_at，为什么 INSERT 中可以不写它们？

答案见[复习自测答案](review-answers.md#第九章)。

## 代码入口

[ShopDAO](../../shop-service/shop-server/src/main/java/com/murto/shop/dao/ShopDAO.java) · [AccountDAO](../../account-service/account-server/src/main/java/com/murto/account/dao/AccountDAO.java) · [建表脚本](../../datas/database/schema.sql)
