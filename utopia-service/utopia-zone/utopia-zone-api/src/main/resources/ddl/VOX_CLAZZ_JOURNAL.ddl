CREATE TABLE `VOX_CLAZZ_JOURNAL` (
  `ID`                 BIGINT(20)    NOT NULL   AUTO_INCREMENT,
  `CREATE_DATETIME`    DATETIME      NOT NULL,
  `UPDATE_DATETIME`    DATETIME      NOT NULL,
  `DISABLED`           BIT(1)        NOT NULL   DEFAULT b'0',
  `CLAZZ_ID`           BIGINT(20)    NOT NULL,
  `RELEVANT_USER_ID`   BIGINT(20)    NOT NULL,
  `JOURNAL_TYPE`       VARCHAR(64)   NOT NULL,
  `CONTENT`            VARCHAR(1000) NULL       DEFAULT NULL,
  `RELEVANT_USER_TYPE` VARCHAR(32)   NOT NULL,
  `LIKE_COUNT`         INT(11)       NOT NULL   DEFAULT '0',
  `INFO`               VARCHAR(255)  NULL       DEFAULT NULL,
  `LEAK_YES`           INT(11)       NULL       DEFAULT '0',
  `LEAK_NO`            INT(11)       NULL       DEFAULT '0',
  `LEAK_MAYBE`         INT(11)       NULL       DEFAULT '0',
  `JOURNAL_JSON`       MEDIUMTEXT,
  `JOURNAL_CATEGORY`   VARCHAR(64)   NOT NULL   DEFAULT 'MISC',
  `CLAZZ_GROUP_ID`     BIGINT(20)    NULL       DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `IDX_USERID_JOURNALTYPE_CREATETIME` (`RELEVANT_USER_ID`, `JOURNAL_TYPE`, `CREATE_DATETIME`),
  KEY `IDX_JOURNALTYPE_CREATETIME` (`JOURNAL_TYPE`, `CREATE_DATETIME`),
  KEY `IDX_CLAZZID_CATEGORY` (`CLAZZ_ID`, `JOURNAL_CATEGORY`) USING BTREE,
  KEY `IDX_CLAZZ_GROUP_ID` (`CLAZZ_GROUP_ID`)
)
  ENGINE = InnoDB;