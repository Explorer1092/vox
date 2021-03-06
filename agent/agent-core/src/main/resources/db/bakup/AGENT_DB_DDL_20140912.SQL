------------------------------------------
-- 17zuoye_Agent
-- 更新AGENT_PRODUCT
ALTER TABLE AGENT_PRODUCT ADD COLUMN PRODUCT_TYPE tinyint NOT NULL DEFAULT 1 comment '产品类型, 1:物料 2:实物卡' after ID;
ALTER TABLE AGENT_ORDER ADD COLUMN POINT_CHARGE_AMOUNT FLOAT DEFAULT 0 COMMENT '点数冲抵现金额' after ORDER_AMOUNT;
ALTER TABLE AGENT_ORDER_PRODUCT ADD COLUMN PRODUCT_TYPE tinyint NOT NULL DEFAULT 1 COMMENT '商品类型' after ORDER_ID;





------------------------------------------------------
-- HomeworkSitter
-- 更新VOX_Product_Card
ALTER TABLE VOX_PRODUCT_CARD MODIFY COLUMN CARD_TYPE varchar(30) NOT NULL COMMENT '卡类型,WalkerFantasyAdventure:沃克大冒险之奇幻探险 88元, WalkerArdourRunning:沃克大冒险之热血酷跑 158元';
UPDATE VOX_PRODUCT_CARD SET CARD_TYPE='WalkerFantasyAdventure' WHERE CARD_TYPE=1;
UPDATE VOX_PRODUCT_CARD SET CARD_TYPE='WalkerArdourRunning' WHERE CARD_TYPE=2;

ALTER TABLE VOX_PRODUCT_CARD ADD COLUMN AGENT_USER_ID bigint(20) COMMENT '销售市场人员用户ID' after MAKE_DATETIME;
ALTER TABLE VOX_PRODUCT_CARD MODIFY COLUMN OPEN_REGION varchar(420) COMMENT '可使用区域CODE';
