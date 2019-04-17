CREATE TABLE `AGENT_RESEARCHERS` (
  `ID`                    BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `NAME`                  VARCHAR(50)  NOT NULL         comment '教研员姓名',
  `GENDER`                TINYINT(2)    NOT NULL         comment '教研员性别',
  `PHONE`                 VARCHAR(30)  NOT NULL         comment '电话号码',
  `JOB`                   TINYINT(2)    NOT NULL         comment '职务',
  `LEVEL`                 TINYINT(2)     NULL            comment '级别',
  `PROVINCE`              INT(11)    NULL            comment '省Code',
  `CITY`                  INT(11)    NULL            comment '市Code',
  `COUNTY`                INT(11)    NULL            comment '区Code',
  `SCHOOL_PHASE`          TINYINT(4)     NULL            comment '学校阶段',
  `GRADE`                 VARCHAR(50)   NULL            comment '年级',
  `SUBJECT`               VARCHAR(25) NOT NULL DEFAULT 'UNKNOWN'           comment '学科',
  `AGENT_USER_ID`        BIGINT(20)   NOT NULL           comment '所属的AgentUser' ,
  `CREATE_DATETIME`       DATETIME     NOT NULL,
  `UPDATE_DATETIME`       DATETIME     NOT NULL,

  PRIMARY KEY (`ID`),
  INDEX AGENT_RESEARCHERS_AGENT_USER_ID(AGENT_USER_ID)
)
  ENGINE = InnoDB DEFAULT CHARSET=utf8;