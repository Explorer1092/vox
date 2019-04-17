CREATE TABLE `VOX_AFENTI_LEARNING_PLAN_PUSH_EXAMINATION_HISTORY_{}` (
  `ID`                BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `USER_ID`           BIGINT(20)   NOT NULL,
  `RANK`              INT(11)      NULL     DEFAULT NULL,
  `KNOWLEDGE_POINT`   VARCHAR(200) NULL     DEFAULT NULL,
  `EXAM_ID`           VARCHAR(50)  NULL     DEFAULT NULL,
  `RIGHT_NUM`         INT(11)      NULL     DEFAULT NULL,
  `ERROR_NUM`         INT(11)      NULL     DEFAULT NULL,
  `PATTERN`           VARCHAR(50)  NULL     DEFAULT NULL,
  `CREATETIME`        DATETIME     NOT NULL,
  `UPDATETIME`        DATETIME     NOT NULL,
  `SCORE_COEFFICIENT` VARCHAR(200) NULL     DEFAULT NULL,
  `NEW_BOOK_ID`       VARCHAR(255) NOT NULL DEFAULT '',
  `NEW_UNIT_ID`       VARCHAR(255) NOT NULL DEFAULT '',
  `SUBJECT`           VARCHAR(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`ID`),
  KEY `IDX_UID_S_CT` (`USER_ID`, `SUBJECT`(191), `CREATETIME`) USING BTREE,
  KEY `IDX_UID_NBID` (`USER_ID`, `NEW_BOOK_ID`(191))
)
  ENGINE = InnoDB;