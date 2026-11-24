# 第十一章 并发 事务 连接池与查询性能

## 学习目标

- 区分请求线程、数据库连接和数据库事务。
- 理解可并发调用与业务并发正确性不是一回事。
- 看懂索引、分页、慢 SQL 如何影响整个请求链。

## 工作线程与连接池

当前同步 Spring MVC 店铺接口可以近似理解为，一个正在处理的请求由一个工作线程执行。线程通常由线程池复用，不是每次都创建新线程；可用线程不足时，请求可能排队。

网关使用 WebFlux，Dubbo 也有自己的调度机制，不能把上述模型推广为所有后端框架都永久绑定“一个请求一个线程”。

```text
请求 → 工作线程 → Controller → Service → DAO
                                         ↓ 借连接
                                    数据库连接池
                                         ↓
                                       MySQL
```

线程执行 Java 代码，连接与 MySQL 通信。假设十个工作线程需要查询，而连接池最多五个连接，则部分线程执行 SQL，部分线程等待借连接。

JdbcTemplate 在配置完成后可被多线程共享。当前 SQL 参数、KeyHolder、ResultSet、ShopInfo 都是调用内的数据，没有放到 DAO 的共享请求字段中。物理连接通过数据源借用和归还，不是所有请求同时共享同一个正在执行 SQL 的连接。

## 数据库锁与业务覆盖

InnoDB 用事务和锁协调并发写入，但不会自动理解客户端提交的数据是否过期。

```text
A、B 都读取到旧名称和旧描述
A 更新名称，提交完整可编辑字段
B 更新描述，提交仍带旧名称的完整字段
B 的更新可能覆盖 A 的名称
```

当前店铺 UPDATE 没有 version 校验，会发生这种业务覆盖。删除同一记录时，通常一个请求删除成功，之后的请求影响零行，店铺方法返回 false。

## Transactional 解决什么

当前 ShopService.updateShop 将 UPDATE 与回查放在同一个事务中。在正常通过 Spring 代理调用、事务数据源一致且使用支持事务的引擎时，事务控制连接和提交、回滚边界。

@Transactional 不是全局互斥锁，也不会自动防止客户端过期数据覆盖。事务内执行慢远程请求会延长连接和锁占用时间，尽量让数据库事务聚焦必要的数据操作。

我们讨论过乐观锁：UPDATE 同时检查 version，并原子增加版本；更新失败时告知调用方冲突。当前尚未实现。Java synchronized 也只能协调同一进程内相应对象，不能直接保护多个应用实例。

## 连接池打满

当池中连接全部被占用且迟迟没有归还，新请求会等待，超过借连接超时时间后报错。活跃连接瞬时达到上限不一定是故障，关键看等待和超时是否持续。

常见原因是慢 SQL、锁等待、长事务、流量超过处理能力或连接泄漏。等待连接的工作线程仍占用线程资源，问题可向上游传播：

```text
MySQL 慢 → 店铺连接占用变长 → 店铺线程等待
    → 商品 RPC 等待 → 网关或客户端超时
```

直接增大连接数可能让数据库更忙，先检查 SQL、锁、事务和资源负载。每个应用实例都有自己的连接池，总连接数需要看所有实例，不只是一个配置值。

## 慢查询与索引

慢 SQL 描述耗时，不描述语句字数。阈值按接口目标和慢日志配置决定，没有统一的毫秒数适用于所有业务。可能慢在扫描数据、排序关联、锁等待或 CPU/磁盘压力。

等待连接池借连接不属于 SQL 在数据库内的执行时间，但同样计入接口总耗时。

```sql
SELECT id, name, price_cent
FROM goods
WHERE shop_id = 1
  AND status = 1
  AND id > 100
ORDER BY id ASC
LIMIT 20;
```

当前联合索引是 (shop_id, status, id)，与这类店铺、状态、游标查询配合。索引不是越多越好，最终用 EXPLAIN 和实际负载判断。上亿行不意味着每次都扫描全部记录，但索引本身也有维护和存储成本。

## 当前商品分页

size 指返回条数，DAO 多查一条判断 hasMore。items 按 ID 升序；hasMore=true 时 nextCursor 是本页最后一条 ID；末页为 null。

```text
第一页 afterId=0
    ↓ 返回 nextCursor=120、hasMore=true
第二页 afterId=120，保持 shopId 和 status 不变
```

游标分页不保证跨多次请求看到同一个数据库快照。并发修改状态、删除或插入时，列表仍会变化；当前没有实现快照游览或统计总数。

## 复习练习

在 DBeaver 用 EXPLAIN 查看商品列表 SQL；分别用 size=1 请求连续两页，确认没有重复 ID。不要在正常数据上执行大范围锁表来模拟压力。

## 自测

1. 十个请求、五个连接，意味着只能创建五个 Java 线程吗？
2. JdbcTemplate 线程安全，是否代表两次完整资料更新不会互相覆盖？
3. 为什么不能只靠增大连接池解决慢 SQL？

答案见[复习自测答案](review-answers.md#第十一章)。

## 代码入口

[店铺事务](../../shop-service/shop-server/src/main/java/com/murto/shop/service/ShopService.java) · [商品查询 SQL](../../goods-service/src/main/java/com/murto/goods/dao/GoodsDAO.java) · [分页组装](../../goods-service/src/main/java/com/murto/goods/service/GoodsService.java)
