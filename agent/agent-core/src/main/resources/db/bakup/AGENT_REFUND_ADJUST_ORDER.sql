-- 退款申请需要对AGENT_ORDER表稍微有些调整以适配ADMIN传过来的信息
-- 1.修改用户ID为String类型
-- 2.增加用户姓名字段
ALTER TABLE AGENT_ORDER
MODIFY COLUMN CREATOR VARCHAR(200) NOT NULL COMMENT '用户ID';

ALTER TABLE AGENT_ORDER
ADD COLUMN CREATOR_NAME VARCHAR(200) COMMENT '用户姓名' AFTER CREATOR;

ALTER TABLE AGENT_ORDER
MODIFY COLUMN LATEST_PROCESSOR VARCHAR(200) NOT NULL COMMENT '最近处理人';

ALTER TABLE AGENT_ORDER
ADD COLUMN LATEST_PROCESSOR_NAME VARCHAR(200) COMMENT '最近处理人姓名' AFTER LATEST_PROCESSOR;

ALTER TABLE AGENT_ORDER
ADD COLUMN PAYMENT_MODE TINYINT(4) DEFAULT 1 NOT NULL COMMENT '支付方式';

ALTER TABLE AGENT_ORDER
ADD COLUMN CITY_COST_MONTH INTEGER(6)  COMMENT '城市费用月份';

ALTER TABLE AGENT_ORDER
ADD COLUMN PAYMENT_VOUCHER VARCHAR(300)  COMMENT '支付凭证';

ALTER TABLE AGENT_ORDER
ADD COLUMN `APPLY_TYPE`  varchar(30) NULL COMMENT '申请类型：修改字典表申请,物料申请';

ALTER TABLE AGENT_ORDER
ADD COLUMN `USER_PLATFORM`  varchar(20) NULL COMMENT '用户平台（ADMIN, AGENT）';

ALTER TABLE AGENT_ORDER
ADD COLUMN `ACCOUNT`  varchar(50) NULL COMMENT '创建者账号';

ALTER TABLE AGENT_ORDER
ADD COLUMN `ACCOUNT_NAME`  varchar(50) NULL COMMENT '创建者名字';

ALTER TABLE AGENT_ORDER
ADD COLUMN `WORKFLOW_ID`  bigint NULL COMMENT '对应的工作流ID' ;

ALTER TABLE AGENT_ORDER
ADD COLUMN  `STATUS`  varchar(20) NULL COMMENT '申请状态';

ALTER TABLE AGENT_ORDER
ADD INDEX AGENT_ORDER_ACCOUNT_INDEX('ACCOUNT');     #增加查询索引 账号

ALTER TABLE AGENT_ORDER
ADD INDEX AGENT_ORDER_WORKFLOW_ID_INDEX('WORKFLOW_ID');     #增加查询索引 工作流ID

ALTER TABLE AGENT_ORDER
ADD INDEX AGENT_ORDER_CREATOR_INDEX('CREATOR');       #增加创建人索引


ALTER TABLE AGENT_ORDER
ADD INDEX AGENT_ORDER_INVOICE_ID_INDEX('INVOICE_ID'); #增加发货单ID的索引
