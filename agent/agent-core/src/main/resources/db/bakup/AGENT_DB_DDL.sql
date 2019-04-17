-- 任务表

DROP TABLE IF EXISTS AGENT_GROUP_SCHOOL;
CREATE TABLE AGENT_GROUP_SCHOOL (
  ID                int        NOT NULL AUTO_INCREMENT,   -- ID
  GROUP_ID          smallint   NOT NULL,               -- 组ID
  REGION_CODE       int         NOT NULL,               -- REGION CODE
  SCHOOL_ID         bigint(20)  NOT NULL,               -- 组所关联的学校
  SCHOOL_NAME       varchar(100) NOT NULL,               -- 组所关联的学校名称
  CREATE_DATETIME   datetime NOT NULL,
  UPDATE_DATETIME   datetime NOT NULL,
  PRIMARY KEY (ID),
  UNIQUE KEY GROUP_SCHOOL (GROUP_ID,SCHOOL_ID)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8;


