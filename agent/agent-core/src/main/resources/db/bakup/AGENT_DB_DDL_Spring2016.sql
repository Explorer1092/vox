DROP TABLE IF EXISTS AGENT_USER_KPI_RESULT_S2016;
CREATE TABLE AGENT_USER_KPI_RESULT_S2016 (
  ID                    BIGINT(20)  NOT NULL AUTO_INCREMENT,    -- ID
  REGION_ID             BIGINT(20) NOT NULL        comment '大区组ID',
  REGION_NAME           VARCHAR(200)               comment '大区组名',
  PROVINCE_ID           BIGINT(20) NOT NULL        comment '区域组ID',
  PROVINCE_NAME         VARCHAR(200)               comment '区域组名',
  COUNTY_CODE           INT  NOT NULL              comment '地区编码',
  COUNTY_NAME           VARCHAR(200)               comment '地区名称',
  SCHOOL_ID             BIGINT(20)                 comment '学校ID',
  SCHOOL_NAME           VARCHAR(200)               comment '学校名称',
  SCHOOL_LEVEL          SMALLINT                   comment '1-小学,2-中学',
  SALARY_MONTH          INT NOT NULL               comment '结算月',
  USER_ID               BIGINT(20) NOT NULL        comment '用户ID',
  USER_NAME             VARCHAR(50)                comment '用户名称',
  START_DATE            DATETIME NOT NULL          comment '绩效开始日期',
  END_DATE              DATETIME NOT NULL          comment '绩效结束日期',
  CPA_TYPE              VARCHAR(20) NOT NULL       comment 'CPA类型',
  CPA_TARGET            INT NOT NULL               comment 'CPA目标',
  CPA_RESULT            INT NOT NULL               comment '业绩',
  CPA_SALARY            INT NOT NULL               comment '工资',
  CPA_NOTE              VARCHAR(200)               comment '备注',
  FINANCE_CHECK         BIT(1) NOT NULL DEFAULT b'0' comment '财务确认',
  MARKET_CHECK          BIT(1) NOT NULL DEFAULT b'0' comment '市场确认',
  DISABLED              BIT(1) NOT NULL DEFAULT b'0',         -- 无效状态 0：有效 1：无效
  CREATE_DATETIME       DATETIME NOT NULL,
  UPDATE_DATETIME       DATETIME NOT NULL,
  PRIMARY KEY (ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS AGENT_USER_KPI_CONFIG_S2016;
CREATE TABLE AGENT_USER_KPI_CONFIG_S2016 (
  ID                      bigint(20)  NOT NULL AUTO_INCREMENT,   -- ID
  USER_ID                 bigint(20) NOT NULL     comment '市场人员ID',
  USER_NAME               VARCHAR(50)             comment '市场人员姓名',
  USER_ROLE               VARCHAR(20)  NOT NULL   comment '身份 代理/直营',
  SALARY_START_DATE       DATETIME  NOT NULL      comment '工资结算开始时间',
  SALARY_END_DATE         DATETIME  NOT NULL      comment '工资结算结束时间',
  REGION_CODE             INT  NOT NULL           comment '地区编码',
  REGION_NAME             VARCHAR(200)            comment '地区名称',
  SETTLEMENT_TYPE         VARCHAR(20)  NOT NULL   comment '类型 高渗/低渗/双渗',
  NEW_AUTH_TARGET         INT  NOT NULL           comment '新增认证绩效目标',
  MAR_SL_TARGET           INT  NOT NULL           comment '3月高覆盖绩效目标',
  APR_SL_TARGET           INT  NOT NULL           comment '4月高覆盖绩效目标',
  MAY_SL_TARGET           INT  NOT NULL           comment '5月高覆盖绩效目标',
  JUN_SL_TARGET           INT  NOT NULL           comment '6月高覆盖绩效目标',
  SL_DSA_TARGET           INT  NOT NULL           comment '双科认证绩效目标',
  AUTH_GRADE_MATH_TARGET  INT  NOT NULL           comment '1-2年级新增绩效目标',
  USER_CPA_FACTOR         SMALLINT DEFAULT 100    comment 'CPA计算系数,市经理计算时使用',
  MARKET_STU_LEVEL        VARCHAR(50)             comment '中学/小学',
  NOTE                    VARCHAR(200)            comment '备注',
  DISABLED                BIT(1) NOT NULL DEFAULT b'0',         -- 无效状态 0：有效 1：无效
  CREATE_DATETIME   datetime NOT NULL,
  UPDATE_DATETIME   datetime NOT NULL,
  PRIMARY KEY (ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
