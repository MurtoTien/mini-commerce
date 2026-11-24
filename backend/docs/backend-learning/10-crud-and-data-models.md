# 第十章 创建 更新 删除与业务表设计

## 学习目标

- 理解读写流程、请求模型和数据库模型的分工。
- 看懂账号、店铺、商品三类数据的标识与关联。
- 区分已经实现的能力与进一步的数据设计。

## 创建与更新的区别

| 操作 | 输入 | SQL | 返回 |
| --- | --- | --- | --- |
| 创建店铺 | 名称、描述、类型 | INSERT | 新生成的店铺 ID 或完整 ShopInfo |
| 查询店铺 | 已有 ID | SELECT | ShopInfo |
| 更新店铺 | 已有 ID 和完整可编辑字段 | UPDATE | 更新后的 ShopInfo |
| 删除店铺 | 已有 ID | DELETE | 删除是否成功 |

HTTP 创建由 CreateShopRequest 接收 JSON，转换为内部使用的 ShopInfo；创建 ID、时间不由客户端提供。当前店铺 HTTP 只暴露创建和查询，更新、删除在 Dubbo 接口中提供。

店铺更新只覆盖 name、description、shop_type，不修改 id、created_at；description=null 会清空描述。这是完整替换可编辑字段，不是自动忽略所有 null 的局部更新。

删除店铺不存在返回 false；删除账号不存在则抛共享异常，HTTP 映射为 404。接口必须明确约定，不能只看到 boolean 就猜测 false 的含义。

## 为什么用模型而不是很多参数

```java
Long createShop(ShopInfo shopInfo);
```

对象便于传递相关字段、扩展和检查类型；Map 则容易拼错字段名。但模型不会自动解决“字段允许谁修改”的问题。账号资料更新专用 UpdateAccountRequest 只包含 nickname、avatarUrl，不包含 loginName、passwordHash、status。

DTO、数据库模型、外部响应在简单项目里可适度复用。实际职责不同时要分开，避免将内部字段暴露给调用方。

## DAO 是否可以进一步抽象

ShopDAO 已经把 SQL 隐藏在 createShop、queryShop、updateShop、deleteShop 后面。可以进一步定义 DAO 接口，再由 JdbcShopDAO 实现；泛型 CrudDAO 只能统一方法签名，不会自动生成所有 SQL。

我们讨论过 JPA/Hibernate 等 ORM，但当前没有接入。配置实体和字段映射后，框架可生成常见 SQL、读取对象字段、转换结果，不必每次手写 setString。JPA 实体通常使用普通 class，不能把当前 record 不加调整直接当作实体。

## 账号数据设计

当前 account_db.user_account 保存注册身份和资料：id、login_name、password_hash、nickname、avatar_url、status、created_at、updated_at。

- id 就是当前的用户 ID，不需要再添加一个含义相同的 user_id。
- login_name 是登录标识，nickname 是展示昵称，二者不是同一概念。
- login_name 的唯一索引在数据库层阻止重复账号。
- password_hash 应存专用密码哈希结果，不能存明文或当作普通资料返回。
- 登录凭证和会话与用户资料是不同概念，不应用单个 is_login 表达多设备状态。

本项目尚未实现注册和登录。演示账号被禁用，密码字段是不可用占位值，不是可用于验证的密码哈希。

## 商品与店铺的关系

当前 goods_db.goods 使用 shop_id 关联店铺：一张表中存多家店的商品，而不是每家店单独建表。

| 字段 | 设计 |
| --- | --- |
| id | 商品主键 |
| shop_id | 归属店铺 |
| price_cent | 单币种人民币金额，单位为分 |
| stock | 简化库存数量 |
| status | 0 下架，1 上架 |
| version | 预留版本字段，当前未实现乐观锁 |

商品表有非负价格、非负库存、状态范围约束，按 shop_id、status、id 建立索引。当前只提供商品列表，没有商品创建、库存扣减、多规格或多仓库能力。

“账号拥有店铺”只是讨论过的下一步：现有 shop 表没有 owner_user_id，也未实现店铺权限检查。当前库间不建外键，数据归属由各应用管理，不能因此认为业务关联已自动校验。

## 数据规模如何演进

多个店铺共用一张表是正常起点。合理索引帮助查询定位记录，不代表大表没有存储和维护成本。出现实际容量和性能瓶颈后，才评估归档、缓存、读副本或分库分表。

例如一万家店各十万件商品是十亿行，但总量、活跃量、热点和查询方式都影响设计。当前 Demo 保持单库单表，不实现分片。

## 复习练习

逐项对照 schema.sql、AccountInfo 和 UpdateAccountRequest，标记“数据库内部字段”“对外只读字段”“允许更新字段”。再说明 version 字段存在但未参与 WHERE 条件时，为什么不能防止并发覆盖。

## 自测

1. 用户表 id、login_name、nickname 的区别是什么？
2. 传入带 createdAt 的 ShopInfo，是否应该允许重写创建时间？
3. 一家店的商品放在共用表中，如何表示归属？

答案见[复习自测答案](review-answers.md#第十章)。

## 代码入口

[表结构](../../datas/database/schema.sql) · [创建店铺入口](../../shop-service/shop-server/src/main/java/com/murto/shop/controller/ShopController.java) · [账号资料请求](../../account-service/account-api/src/main/java/com/murto/account/api/dto/UpdateAccountRequest.java)
