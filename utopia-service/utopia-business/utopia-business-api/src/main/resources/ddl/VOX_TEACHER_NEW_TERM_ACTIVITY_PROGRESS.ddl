DROP TABLE IF EXISTS `VOX_TEACHER_NEW_TERM_ACTIVITY_PROGRESS`;

CREATE TABLE `VOX_TEACHER_NEW_TERM_ACTIVITY_PROGRESS` (
  `ID`              BIGINT(20)  NOT NULL  AUTO_INCREMENT COMMENT '记录ID',
  `CREATE_TIME`     DATETIME    NOT NULL  COMMENT '创建时间',
  `UPDATE_TIME`     DATETIME    NOT NULL  COMMENT '更新时间',
  `ACTIVITY_ID`     BIGINT(20)  NOT NULL  DEFAULT '0'  COMMENT '活动ID',
  `TEACHER_ID`      BIGINT(20)  NOT NULL  DEFAULT '0'  COMMENT '参与活动老师ID',
  `SCHOOL_ID`       BIGINT(20)  NOT NULL  DEFAULT '0'  COMMENT '参与活动老师学校ID',
  `AUTH_STU_CNT`    INT(10)     NOT NULL  DEFAULT '0'  COMMENT '认证学生数',
  `DAILY_RANK`      INT(10)     NOT NULL  DEFAULT '0'  COMMENT '今日老师排名',
  `LEVEL_1`         BIT(1)      NOT NULL  DEFAULT b'0' COMMENT '是否发放过第一档奖励',
  `LEVEL_2`         BIT(1)      NOT NULL  DEFAULT b'0' COMMENT '是否发放过第二档奖励',
  `LEVEL_3`         BIT(1)      NOT NULL  DEFAULT b'0' COMMENT '是否发放过第三档奖励',
  `REWARD_TIME_1`   DATETIME    NULL COMMENT '发放第一档奖励的时间',
  `REWARD_TIME_2`   DATETIME    NULL COMMENT '发放第二档奖励的时间',
  `REWARD_TIME_3`   DATETIME    NULL COMMENT '发放第三档奖励的时间',
  PRIMARY KEY (`ID`),
  KEY `ACTIVITY_KEY` (`ACTIVITY_ID`),
  KEY `TEACHER_KEY` (`TEACHER_ID`),
  KEY `DAILY_RANK_KEY` (`ACTIVITY_ID`, `DAILY_RANK`),
  KEY `AUTH_STU_CNT_RANK` (`ACTIVITY_ID`, `AUTH_STU_CNT`, `CREATE_TIME`)
)
  ENGINE = InnoDB;