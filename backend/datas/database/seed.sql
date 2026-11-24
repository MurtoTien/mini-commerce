-- Create the demo shop only when ID 1 is unused; preserve existing records.
INSERT INTO shop_db.shop (id, name, description, shop_type)
SELECT 1, 'Murto的店', '一家演示店铺', 'PERSONAL'
WHERE NOT EXISTS (SELECT 1 FROM shop_db.shop WHERE id = 1);

-- 演示账号没有可用密码且被禁用；本次不实现注册或登录。
INSERT INTO account_db.user_account (login_name, password_hash, nickname, status)
SELECT 'demo_murto', '!login-not-configured', 'Murto', 0
WHERE NOT EXISTS (SELECT 1 FROM account_db.user_account WHERE login_name = 'demo_murto');

-- 关联现有店铺 1；重复执行不重复添加。
INSERT INTO goods_db.goods (shop_id, name, description, price_cent, stock, status)
SELECT 1, '演示面包', '香香糯糯的面包', 1200, 50, 1
WHERE EXISTS (SELECT 1 FROM shop_db.shop WHERE id = 1)
  AND NOT EXISTS (SELECT 1 FROM goods_db.goods WHERE shop_id = 1 AND name = '演示面包');

INSERT INTO goods_db.goods (shop_id, name, description, price_cent, stock, status)
SELECT 1, '演示牛奶', '早餐牛奶', 650, 100, 1
WHERE EXISTS (SELECT 1 FROM shop_db.shop WHERE id = 1)
  AND NOT EXISTS (SELECT 1 FROM goods_db.goods WHERE shop_id = 1 AND name = '演示牛奶');

INSERT INTO goods_db.goods (shop_id, name, description, price_cent, stock, status)
SELECT 1, '演示下架商品', '用于验证上下架筛选', 990, 10, 0
WHERE EXISTS (SELECT 1 FROM shop_db.shop WHERE id = 1)
  AND NOT EXISTS (SELECT 1 FROM goods_db.goods WHERE shop_id = 1 AND name = '演示下架商品');
