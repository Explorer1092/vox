CREATE TABLE `VOX_USER_APPEAL` (
  `ID`              BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `CREATE_DATETIME` DATETIME     NOT NULL,
  `UPDATE_DATETIME` DATETIME     NOT NULL,
  `USER_ID`         BIGINT(20)   NOT NULL,
  `USER_NAME`       VARCHAR(256) NOT NULL DEFAULT '',
  `SCHOOL_ID`       BIGINT(20)   NOT NULL,
  `SCHOOL_NAME`     VARCHAR(256) NULL     DEFAULT '',
  `PNAME`           VARCHAR(256) NULL     DEFAULT '',
  `CNAME`           VARCHAR(256) NULL     DEFAULT '',
  `ANAME`           VARCHAR(256) NULL     DEFAULT '',
  `REASON`          TEXT         NULL,
  `FILE_NAME`       VARCHAR(256) NULL     DEFAULT '',
  `STATUS`          VARCHAR(64)  NOT NULL DEFAULT '',
  `COMMENT`         VARCHAR(256) NOT NULL DEFAULT '',
  `AUDIT_TIME`      DATETIME     NULL     DEFAULT NULL,
  `AUDIT_ID`        VARCHAR(256) NULL     DEFAULT NULL,
  `TYPE`            VARCHAR(64)  NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `IDX_UID` (`USER_ID`) USING BTREE,
  KEY `IDX_SCHOOL_ID` (`SCHOOL_ID`) USING BTREE
)
  ENGINE = InnoDB;