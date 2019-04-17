
DROP TABLE IF EXISTS AGENT_USER;
CREATE TABLE AGENT_USER (
  ID                        bigint(20)  NOT NULL AUTO_INCREMENT,    -- ID
  ACCOUNT_NAME             varchar(50) NOT NULL        comment '登录账户名',
  REAL_NAME                varchar(50) NOT NULL        comment '用户真实姓名',
  PASSWD                   varchar(50) NOT NULL         comment '密码',
  PASSWD_SALT              varchar(50) NOT NULL        comment '密码SALT',
  USER_COMMENT             varchar(200)                 comment '备注',
  CONTRACT_START_DATE     datetime                     comment '合同开始日期',
  CONTRACT_END_DATE       datetime                     comment '合同结束日期',
  CASH_AMOUNT             float NOT NULL              comment '现金帐户余额',
  POINT_AMOUNT            float NOT NULL              comment '点数帐户余额',
  USABLE_CASH_AMOUNT     float NOT NULL              comment '可用现金账户余额',
  USABLE_POINT_AMOUNT    float NOT NULL              comment '可用点数账户余额',
  TEL                      varchar(30)                 comment '用户电话',
  EMAIL                    varchar(50)                 comment '用户邮箱',
  IM_ACCOUNT              varchar(20)                 comment '用户IM',
  ADDRESS                 varchar(100)                comment '用户地址',
  CASH_DEPOSIT           int                          comment '帐户保证金，元单位',
  CASH_DEPOSIT_RECEIVED bit(1)                     comment '保证金是否已收到 0：未 1：已收到',
  BANK_NAME              varchar(50)                 comment '开户行名称',
  BANK_HOST_NAME         varchar(30)                 comment '银行帐号开户者姓名',
  BANK_ACCOUNT           varchar(50)                 comment '银行帐号',
  STATUS            tinyint     NOT NULL DEFAULT 0     comment '用户状态，0:新建，强制更新密码，1:有效，9:关闭',
  CREATE_DATETIME   datetime NOT NULL,
  UPDATE_DATETIME   datetime NOT NULL,
  PRIMARY KEY (ID),
  UNIQUE INDEX IDX_ACCOUNT_NAME(ACCOUNT_NAME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO AGENT_USER(ID, ACCOUNT_NAME, REAL_NAME, PASSWD, PASSWD_SALT, STATUS, CASH_AMOUNT, POINT_AMOUNT, USABLE_CASH_AMOUNT, USABLE_POINT_AMOUNT, CREATE_DATETIME, UPDATE_DATETIME)
    VALUES(1, 'admin', 'admin', '0000a34789159f8984f23a9d9435ca8a489da5ee', 'ZF1vEB', 0,0,0,0,0, now(), now());

DROP TABLE IF EXISTS AGENT_ROLE;
CREATE TABLE AGENT_ROLE (
  ID                smallint  NOT NULL AUTO_INCREMENT,    -- ID
  ROLE_NAME         varchar(50) NOT NULL,                 -- 角色名
  DESCRIPTION       varchar(200) ,                        -- 角色描述
  CREATE_DATETIME   datetime NOT NULL,
  UPDATE_DATETIME   datetime NOT NULL,
  PRIMARY KEY (ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO AGENT_ROLE VALUES (1, '管理员', '系统管理员', now(), now());
INSERT INTO AGENT_ROLE VALUES (2, '财务', '财务人员', now(), now());
INSERT INTO AGENT_ROLE VALUES (10, '全国总监', '全国总监', now(), now());
INSERT INTO AGENT_ROLE VALUES (11, '大区总监', '东区、西区总监', now(), now());
INSERT INTO AGENT_ROLE VALUES (12, '省级市场人员', '省级市场人员', now(), now());
INSERT INTO AGENT_ROLE VALUES (13, '市级市场人员', '市级市场人员', now(), now());

DROP TABLE IF EXISTS AGENT_GROUP;
CREATE TABLE AGENT_GROUP (
  ID                smallint  NOT NULL AUTO_INCREMENT,    -- ID
  PARENT_ID        smallint  NOT NULL,                   -- 父ID
  GROUP_NAME        varchar(50) NOT NULL,                 -- 代理组名
  DESCRIPTION       varchar(200) ,                        -- 代理组说明
  ROLE_ID           smallint  NOT NULL,                   -- 所属角色ID
  DISABLED          bit(1) NOT NULL DEFAULT b'0',         -- 无效状态 0：有效 1：无效
  CREATE_DATETIME   datetime NOT NULL,
  UPDATE_DATETIME   datetime NOT NULL,
  PRIMARY KEY (ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS AGENT_GROUP_USER;
CREATE TABLE AGENT_GROUP_USER (
  ID                smallint  NOT NULL AUTO_INCREMENT,    -- ID
  USER_ID           bigint(20)  NOT NULL,                 -- 用户ID
  GROUP_ID          smallint    NOT NULL,                 -- 所属组ID
  CREATE_DATETIME   datetime NOT NULL,
  UPDATE_DATETIME   datetime NOT NULL,
  PRIMARY KEY (ID),
  UNIQUE KEY USER_GROUP (USER_ID,GROUP_ID)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS AGENT_USER_SCHOOL;
CREATE TABLE AGENT_USER_SCHOOL (
  ID                smallint  NOT NULL AUTO_INCREMENT,    -- ID
  USER_ID           bigint(20)  NOT NULL,                 -- 用户ID
  REGION_CODE       int       NOT NULL,                  -- REGION CODE
  SCHOOL_ID         bigint(20)    NOT NULL,               -- 用户所关联学校
  CREATE_DATETIME   datetime NOT NULL,
  UPDATE_DATETIME   datetime NOT NULL,
  PRIMARY KEY (ID),
  UNIQUE KEY USER_SCHOOL (USER_ID,SCHOOL_ID)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS AGENT_GROUP_REGION;
CREATE TABLE AGENT_GROUP_REGION (
  ID                smallint  NOT NULL AUTO_INCREMENT,    -- ID
  GROUP_ID         smallint  NOT NULL,                 -- 组ID
  REGION_CODE      int       NOT NULL,                  -- REGION CODE
  REGION_NAME     varchar(50) NOT NULL,                 -- REGION NAME
  CREATE_DATETIME   datetime NOT NULL,
  UPDATE_DATETIME   datetime NOT NULL,
  PRIMARY KEY (ID),
  UNIQUE KEY GROUP_REGION (GROUP_ID,REGION_CODE)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS AGENT_SYS_PATH;
CREATE TABLE AGENT_SYS_PATH (
  ID                smallint  NOT NULL AUTO_INCREMENT,    -- ID
  APP_NAME          varchar(50) NOT NULL,                 -- 应用名
  PATH_NAME         varchar(80) NOT NULL,                 -- 系统路径名
  DESCRIPTION       varchar(200) NOT NULL,                -- 系统路径描述
  CREATE_DATETIME   datetime NOT NULL,
  UPDATE_DATETIME   datetime NOT NULL,
  PRIMARY KEY (ID),
  UNIQUE KEY APP_PATH (APP_NAME, PATH_NAME)
) ENGINE=InnoDB AUTO_INCREMENT=372 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS AGENT_SYS_PATH_ROLE;
CREATE TABLE AGENT_SYS_PATH_ROLE (
  ID                smallint  NOT NULL AUTO_INCREMENT,    -- ID
  ROLE_ID           smallint NOT NULL,                    -- ROLE ID
  PATH_ID           smallint NOT NULL,                    -- 系统路径ID
  CREATE_DATETIME   datetime NOT NULL,
  UPDATE_DATETIME   datetime NOT NULL,
  PRIMARY KEY (ID),
  UNIQUE KEY ROLE_PATH (ROLE_ID,PATH_ID)
) ENGINE=InnoDB AUTO_INCREMENT=232 DEFAULT CHARSET=utf8;

-- 产品物料表
DROP TABLE IF EXISTS AGENT_PRODUCT;
CREATE TABLE AGENT_PRODUCT (
  ID                  smallint  NOT NULL AUTO_INCREMENT,
  PRODUCT_NAME       varchar(50) NOT NULL                  comment '产品物料名称' ,
  PRODUCT_DESC       varchar(200)                           comment '产品物料描述',
  PRODUCT_IMG1       varchar(100)                           comment '产品物料图片1',
  PRODUCT_IMG2       varchar(100)                           comment '产品物料图片2',
  PRODUCT_IMG3       varchar(100)                           comment '产品物料图片3',
  PRODUCT_IMG4       varchar(100)                           comment '产品物料图片4',
  PRICE               float  NOT NULL                       comment '产品价格',
  DISCOUNT_PRICE     float NOT NULL                        comment '产品打折价格',
  VALID_FROM         datetime                               comment '起始有效期',
  VALID_TO           datetime                               comment '截至有效期',
  REGION_LIMIT       bit(1) NOT NULL DEFAULT b'0'         comment '是否区域限定 0：否 1：是',
  LATEST_EDITOR       bigint(20) NOT NULL                    comment '最后编辑人',
  CREATE_DATETIME   datetime NOT NULL,
  UPDATE_DATETIME   datetime NOT NULL,
  PRIMARY KEY (ID)
) ENGINE=InnoDB AUTO_INCREMENT=232 DEFAULT CHARSET=utf8;

-- 对于区域限定的产品所对应的用户组关系表
DROP TABLE IF EXISTS AGENT_GROUP_PRODUCT;
CREATE TABLE AGENT_GROUP_PRODUCT (
  ID                  smallint  NOT NULL AUTO_INCREMENT,   -- ID
  GROUP_ID           smallint NOT NULL                    comment '用户组ID',
  PRODUCT_ID         smallint NOT NULL                    comment '产品ID',
  CREATE_DATETIME   datetime NOT NULL,
  UPDATE_DATETIME   datetime NOT NULL,
  PRIMARY KEY (ID),
  UNIQUE KEY GROUP_PRODUCT(GROUP_ID,PRODUCT_ID)
) ENGINE=InnoDB AUTO_INCREMENT=232 DEFAULT CHARSET=utf8;


-- 绩效指标定义
DROP TABLE IF EXISTS AGENT_KPI_DEF;
CREATE TABLE AGENT_KPI_DEF (
  ID                  smallint  NOT NULL AUTO_INCREMENT,   -- ID
  KPI_NAME           varchar(50) NOT NULL              comment '绩效指标名称',
  KPI_DESC           varchar(200)                       comment '绩效指标描述',
  KPI_ROLE           smallint   NOT NULL               comment '所属角色ID',
  KPI_CODE           varchar(30) NOT NULL              comment '绩效CODE，根据此CODE进行KPI结果计算',
  LATEST_EDITOR      bigint(20) NOT NULL                comment '最后编辑人',
  CREATE_DATETIME   datetime NOT NULL,
  UPDATE_DATETIME   datetime NOT NULL,
  PRIMARY KEY (ID)
) ENGINE=InnoDB AUTO_INCREMENT=232 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS AGENT_KPI_ASSESSMENT;
CREATE TABLE AGENT_KPI_ASSESSMENT (
  ID                 smallint  NOT NULL AUTO_INCREMENT,   -- ID
  KPI_ID             smallint NOT NULL                  comment '绩效指标ID',
  BASELINE           int                                 comment '完成度结果,去百分号的数字',
  CASH_REWARD       float                               comment '完成度结果的现金奖励',
  POINT_REWARD      float                               comment '完成度结果的点数奖励',
  CREATE_DATETIME   datetime NOT NULL,
  UPDATE_DATETIME   datetime NOT NULL,
  PRIMARY KEY (ID),
  KEY KPI_ASSESSMENT(KPI_ID)
) ENGINE=InnoDB AUTO_INCREMENT=232 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS AGENT_KPI_EVAL;
CREATE TABLE AGENT_KPI_EVAL (
  ID                    smallint  NOT NULL AUTO_INCREMENT,   -- ID
  KPI_ID                smallint NOT NULL                  comment '绩效指标ID',
  EVAL_DATE            datetime  NOT NULL                  comment 'KPI考核结算日期',
  EVAL_DURATION_FROM  datetime  NOT NULL                  comment 'KPI考核期间开始日期',
  EVAL_DURATION_TO    datetime  NOT NULL                  comment 'KPI考核期间结束日期',
  CREATE_DATETIME     datetime NOT NULL,
  UPDATE_DATETIME     datetime NOT NULL,
  PRIMARY KEY (ID),
  KEY KPI_EVAL(KPI_ID)
) ENGINE=InnoDB AUTO_INCREMENT=232 DEFAULT CHARSET=utf8;

-- 用户绩效目标定义
DROP TABLE IF EXISTS AGENT_USER_KPI;
CREATE TABLE AGENT_USER_KPI (
  ID                       int  NOT NULL AUTO_INCREMENT,        -- ID
  USER_ID                 smallint NOT NULL             comment '用户ID',
  KPI_EVAL_ID             smallint NOT NULL            comment '绩效考核ID',
  KPI_TARGET              int     NOT NULL              comment '绩效目标',
  CREATE_DATETIME   datetime NOT NULL,
  UPDATE_DATETIME   datetime NOT NULL,
  PRIMARY KEY (ID),
  KEY USER_KPI(USER_ID,KPI_EVAL_ID)
) ENGINE=InnoDB AUTO_INCREMENT=232 DEFAULT CHARSET=utf8;

-- 用户绩效结果数据
DROP TABLE IF EXISTS AGENT_USER_KPI_RESULT;
CREATE TABLE AGENT_USER_KPI_RESULT (
  ID                 bigint(20)  NOT NULL AUTO_INCREMENT,   -- ID
  KPI_EVAL_DATE     datetime   NOT NULL                    comment '绩效考核月份',
  KPI_EVAL_ID       smallint  NOT NULL                    comment '绩效考核ID',
  USER_ID            bigint(20)  NOT NULL                   comment '用户ID',
  KPI_TARGET         int     NOT NULL                      comment '绩效目标',
  KPI_RESULT         int     NOT NULL                      comment '绩效结果',
  CASH_REWARD        float   NOT NULL                      comment '现金回馈',
  POINT_REWARD       float  NOT NULL                       comment '点数回馈',
  FINANCE_CHECK      BIT(1)  NOT NULL DEFAULT b'0'         comment '财务确认',
  MANAGER_CHECK      BIT(1)  NOT NULL DEFAULT b'0'         comment '领导确认',
  CREATE_DATETIME   datetime NOT NULL,
  UPDATE_DATETIME   datetime NOT NULL,
  PRIMARY KEY (ID),
  KEY USER_KPI_RESULT(USER_ID,KPI_EVAL_ID,KPI_EVAL_DATE)
) ENGINE=InnoDB AUTO_INCREMENT=232 DEFAULT CHARSET=utf8;


-- 用户帐户变动历史
DROP TABLE IF EXISTS AGENT_USER_ACCOUNT_HIS;
CREATE TABLE AGENT_USER_ACCOUNT_HIS (
  ID                  bigint(20)  NOT NULL AUTO_INCREMENT,   -- ID
  USER_ID            bigint(20)  NOT NULL                   comment '用户ID',
  CASH_BEFORE        float       NOT NULL                  comment '现金帐户变更前余额',
  CASH_AMOUNT        float       NOT NULL                  comment '现金帐户变更金额',
  CASH_AFTER         float       NOT NULL                  comment '现金帐户变更后金额',
  POINT_BEFORE       float       NOT NULL                  comment '现金帐户变更前余额',
  POINT_AMOUNT       float       NOT NULL                  comment '现金帐户变更金额',
  POINT_AFTER        float       NOT NULL                  comment '现金帐户变更后金额',
  ORDER_ID            bigint(20)                             comment '关联订单ID',
  COMMENTS           varchar(200)                           comment '帐户变更说明',
  CREATE_DATETIME   datetime NOT NULL,
  UPDATE_DATETIME   datetime NOT NULL,
  PRIMARY KEY (ID),
  KEY USER_ACCOUNT(USER_ID)
) ENGINE=InnoDB AUTO_INCREMENT=232 DEFAULT CHARSET=utf8;

-- 订单信息
DROP TABLE IF EXISTS AGENT_ORDER;
CREATE TABLE AGENT_ORDER (
  ID                  bigint(20)  NOT NULL AUTO_INCREMENT,   -- ID
  CREATOR            bigint(20)   NOT NULL                  comment '用户ID',
  CREATOR_GROUP     smallint     NOT NULL                 comment '用户所属组',
  ORDER_TYPE         tinyint     NOT NULL                  comment '订单类型',
  ORDER_AMOUNT       float       NOT NULL                 comment '订单金额',
  ORDER_NOTES        varchar(200)                          comment '订单补充说明',
  ORDER_STATUS       tinyint     NOT NULL                  comment '订单状态',
  LATEST_PROCESSOR   bigint(20) NOT NULL                  comment '最近处理人',
  LATEST_PROCESSOR_GROUP smallint NOT NULL              comment '最近处理人所属组',
  CREATE_DATETIME   datetime NOT NULL,
  UPDATE_DATETIME   datetime NOT NULL,
  PRIMARY KEY (ID),
  KEY USER_ACCOUNT(CREATOR)
) ENGINE=InnoDB AUTO_INCREMENT=232 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS AGENT_ORDER_PRODUCT;
CREATE TABLE AGENT_ORDER_PRODUCT (
  ID                  bigint(20)  NOT NULL AUTO_INCREMENT,   -- ID
  ORDER_ID            bigint(20)   NOT NULL    comment '订单ID',
  PRODUCT_ID         smallint     NOT NULL    comment '商品ID',
  PRODUCT_QUANTITY  smallint                  comment '商品数量',
  RANK                smallint    NOT NULL    comment '顺序',
  CREATE_DATETIME   datetime NOT NULL,
  UPDATE_DATETIME   datetime NOT NULL,
  PRIMARY KEY (ID),
  KEY IDX_ORDER_ID(ORDER_ID)
) ENGINE=InnoDB AUTO_INCREMENT=232 DEFAULT CHARSET=utf8;

-- 订单处理信息表
DROP TABLE IF EXISTS AGENT_ORDER_PROCESS;
CREATE TABLE AGENT_ORDER_PROCESS (
  ID                  bigint(20)  NOT NULL AUTO_INCREMENT,   -- ID
  ORDER_ID            bigint(20)   NOT NULL                 comment '订单ID',
  TARGET_GROUP       smallint                              comment '处理组ID',
  TARGET_USER        smallint                              comment '处理用户ID',
  CREATE_DATETIME   datetime NOT NULL,
  UPDATE_DATETIME   datetime NOT NULL,
  PRIMARY KEY (ID),
  KEY PROCESS_GROUP(TARGET_GROUP),
  KEY PROCESS_USER(TARGET_USER)
) ENGINE=InnoDB AUTO_INCREMENT=232 DEFAULT CHARSET=utf8;

-- 订单处理信息历史表
DROP TABLE IF EXISTS AGENT_ORDER_PROCESS_HIS;
CREATE TABLE AGENT_ORDER_PROCESS_HIS (
  ID                  bigint(20)  NOT NULL AUTO_INCREMENT,   -- ID
  ORDER_ID            bigint(20)   NOT NULL                 comment '订单ID',
  PROCESSOR           bigint(20)   NOT NULL                 comment '处理者ID',
  RESULT              tinyint      NOT NULL                 comment '订单处理结果  0：同意，1：拒绝',
  PROCESS_NOTES      varchar(200)                          comment '处理备注',
  CREATE_DATETIME   datetime NOT NULL,
  UPDATE_DATETIME   datetime NOT NULL,
  PRIMARY KEY (ID),
  KEY ORDER_PROCESS_HIS(ORDER_ID),
  KEY ORDER_PROCESSOR(PROCESSOR)
) ENGINE=InnoDB AUTO_INCREMENT=232 DEFAULT CHARSET=utf8;


-- 操作日志记录表
DROP TABLE IF EXISTS AGENT_OPERATION_LOG;
CREATE TABLE AGENT_OPERATION_LOG (
  ID                  bigint(20)  NOT NULL AUTO_INCREMENT,   -- ID
  OPERATOR_ID        bigint(20)   NOT NULL                 comment '操作者ID',
  OPERATOR_NAME     varchar(50) NOT NULL                 comment '操作者real name',
  OPERATION_TYPE    varchar(30) NOT NULL                 comment '操作类型,login/logout,create/close group, create/close account, update user kpi...',
  ACTION_URL        varchar(100)                          comment '操作动作的URL',
  OPERATION_RESULT   varchar(200)                         comment '操作结果',
  OPERATION_NOTES   varchar(200)                         comment '备注',
  CREATE_DATETIME   datetime NOT NULL,
  UPDATE_DATETIME   datetime NOT NULL,
  PRIMARY KEY (ID),
  KEY IDX_OPERATOR(OPERATOR_ID),
  KEY IDX_OPERATION_TYPE(OPERATION_TYPE)
) ENGINE=InnoDB AUTO_INCREMENT=232 DEFAULT CHARSET=utf8;

-- 站内通知
DROP TABLE IF EXISTS AGENT_NOTIFY;
CREATE TABLE AGENT_NOTIFY (
  ID                  bigint(20)  NOT NULL AUTO_INCREMENT,   -- ID
  NOTIFY_TYPE         varchar(30) NOT NULL               comment '通知类型',
  NOTIFY_TITLE        varchar(100) NOT NULL DEFAULT ''   comment '通知title',
  NOTIFY_CONTENT      varchar(200) NOT NULL              comment '通知内容',
  FILE1               varchar(100)                       comment '通知附件1',
  FILE2               varchar(100)                       comment '通知附件2',
  CREATE_DATETIME     datetime NOT NULL,
  UPDATE_DATETIME     datetime NOT NULL,
  PRIMARY KEY (ID),
  KEY IDX_NOTIFY_TYPE(NOTIFY_TYPE)
) ENGINE=InnoDB AUTO_INCREMENT=232 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS AGENT_NOTIFY_USER;
CREATE TABLE AGENT_NOTIFY_USER (
  ID                  bigint(20)  NOT NULL AUTO_INCREMENT,   -- ID
  NOTIFY_ID          bigint(20)  NOT NULL                 comment '通知ID',
  USER_ID            bigint(20)  NOT NULL                  comment '通知对象ID',
  READ_FLAG          bit(1) NOT NULL DEFAULT b'0'         comment '阅读状态0:未阅 1:已阅',
  CREATE_DATETIME   datetime NOT NULL,
  UPDATE_DATETIME   datetime NOT NULL,
  PRIMARY KEY (ID),
  KEY IDX_USER_NOTIFY(USER_ID, NOTIFY_ID)
) ENGINE=InnoDB AUTO_INCREMENT=232 DEFAULT CHARSET=utf8;

-- 代理对老师的评价表
DROP TABLE IF EXISTS AGENT_TEACHER_NOTE;
CREATE TABLE AGENT_TEACHER_NOTE (
  ID                 bigint(20)  NOT NULL AUTO_INCREMENT,   -- ID
  USER_ID            bigint(20)  NOT NULL                 comment '用户ID',
  TEACHER_ID         bigint(20)  NOT NULL                 comment '老师ID',
  NOTES              varchar(200)                         comment '评价',
  CREATE_DATETIME    datetime NOT NULL,
  UPDATE_DATETIME    datetime NOT NULL,
  PRIMARY KEY (ID),
  KEY IDX_TEACHER(TEACHER_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
