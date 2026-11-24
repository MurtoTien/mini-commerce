# 本地演示数据库

初始化 shop_db.shop、account_db.user_account 和 goods_db.goods。各应用独立管理自己的表；商品表按 shop_id 关联店铺，不创建跨库外键。

## 初始化

先安装并启动 MySQL 8.4，将 mysql 客户端加入 PATH。在 mini-commerce 仓库根目录执行（建表不会迁移已存在表的结构）：

```bash
mysql --default-character-set=utf8mb4 -u root < backend/datas/database/schema.sql
mysql --default-character-set=utf8mb4 -u root < backend/datas/database/seed.sql
```

有密码时给 mysql 命令添加 `-p`。seed.sql 会检查同名演示数据是否存在，不覆盖现有记录。

演示账号 login_name=demo_murto，初次初始化 ID=1；账号被禁用且没有可用密码，仅用于资料查询、更新。脚本在店铺 ID=1 不存在时创建演示店铺，再添加两件上架商品和一件下架商品；已有店铺 1 不会被覆盖。

## 运行

MySQL 保持运行，在 IDEA 重新加载 Maven 后，从 account-service/account-server 启动 AccountServiceApplication（HTTP 8081、Dubbo 20882），并重启 GoodsServiceApplication（8083）。账号与商品应用使用 ZooKeeper，商品还保留店铺 Dubbo 调用，相关服务需按原流程启动。

两应用的数据源支持环境变量：DB_USERNAME（默认 root）、DB_PASSWORD（默认空）。连接 URL 可分别通过 ACCOUNT_DB_URL、GOODS_DB_URL 覆盖。

## 账号接口

| 请求 | 含义 |
|---|---|
| GET /accounts/1 | 按 ID 查询账号资料 |
| GET /accounts/info?userId=1 | 原演示入口，现读取数据库；省略 userId 时默认 1 |
| PUT /accounts/1 | 完整替换昵称和头像资料 |
| DELETE /accounts/1 | 物理删除，成功返回 204；不存在返回 404 |

更新请求示例：

```bash
curl -i -X PUT 'http://localhost:8081/accounts/1' \
  -H 'Content-Type: application/json' \
  -d '{"nickname":"Murto的新昵称","avatarUrl":null}'
```

nickname 必填，长度最多 100 个 Unicode 码点；avatarUrl 最多 1000，null 表示清空。不接受通过此接口修改 login_name、password_hash、status 或 created_at。响应始终不包含密码哈希。非法参数返回 400，不存在的账号返回 404；重复提交相同资料仍然成功。

本次仅实现资料数据操作，尚未实现注册、登录和调用方鉴权，也没有删除店铺等跨应用级联操作。

## 商品列表

```bash
curl 'http://localhost:8083/goods/list?shopId=1&status=1&size=1&afterId=0'
```

- shopId 必填且为正数。
- status：1 上架（默认）、0 下架。
- size：1–100，默认 20。
- afterId：上一页返回的 nextCursor，首次为 0。
- items 按商品 ID 升序返回。
- hasMore=true 时，使用 nextCursor 请求下一页；最后一页 nextCursor=null。
- 空店铺或没有匹配商品时返回空 items，hasMore=false。
- priceCent 单位为人民币分，1200 表示 12 元。
- 列表从 goods 表查询，不依赖远程店铺请求；保留原 /goods/info 和 /goods/info_v2 学习接口。

示例：

```json
{"items":[{"id":1,"shopId":1,"name":"演示面包","priceCent":1200}],"nextCursor":1,"hasMore":true}
```

实际返回还包含 description、stock、status、createdAt 和 updatedAt。

网关原有 /accounts/** 和 /goods/** 路由已覆盖新接口，将 8081/8083 换成 8080 即可通过网关调用。

## 数据库集成测试

测试会用真实 MySQL，通过 MockMvc 验证 HTTP 路由、Controller、Service、DAO 与 SQL。独立测试记录在事务结束时回滚，不删除已有数据。默认跳过，显式启用：

在各自工程目录设置 RUN_DB_TESTS=true 并执行 Maven：

```bash
RUN_DB_TESTS=true mvn -Dtest=AccountApiDatabaseTest,AccountDubboDatabaseTest -Dsurefire.failIfNoSpecifiedTests=false test
RUN_DB_TESTS=true mvn -Dtest=GoodsListDatabaseTest test
```

第一条在 account-service 父工程执行，第二条在 goods-service 执行。账号 RPC 测试使用临时端口 30882，不经过 ZooKeeper，强制通过网络调用；测试自建账号在 finally 中清理。账号 HTTP 测试与商品测试使用事务回滚。没有全局 mvn 时使用 IDEA 自带 Maven，或在对应 Maven 运行配置中设置环境变量和目标。测试 URL 可用 TEST_ACCOUNT_DB_URL、TEST_GOODS_DB_URL 覆盖。
