CREATE TABLE `VOX_AMBASSADOR_REPORT_INFO` (
  `ID`              BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `CREATE_DATETIME` DATETIME     NOT NULL,
  `UPDATE_DATETIME` DATETIME     NULL     DEFAULT NULL,
  `TEACHER_ID`      BIGINT(20)   NOT NULL,
  `TEACHER_NAME`    VARCHAR(255) NOT NULL,
  `REPORT_ID`       BIGINT(20)   NOT NULL,
  `REASON`          VARCHAR(255) NULL     DEFAULT NULL,
  `TYPE`            INT(11)      NULL     DEFAULT '2',
  `DISABLED`        BIT(1)       NOT NULL DEFAULT b'0',
  `STATUS`          VARCHAR(128) NOT NULL DEFAULT 'REPORTING',
  `COMMENT`         VARCHAR(255) NULL     DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `IDX_CREATE_DATETIME` (`CREATE_DATETIME`) USING BTREE,
  KEY `IDX_REPORT_ID` (`REPORT_ID`) USING BTREE,
  KEY `IDX_TEACHER` (`TEACHER_ID`, `DISABLED`)
)
  ENGINE = InnoDB;