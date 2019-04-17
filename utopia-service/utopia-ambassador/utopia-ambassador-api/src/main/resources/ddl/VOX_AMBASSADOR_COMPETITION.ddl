CREATE TABLE `VOX_AMBASSADOR_COMPETITION` (
  `ID`              BIGINT(20)  NOT NULL AUTO_INCREMENT,
  `CREATE_DATETIME` DATETIME    NOT NULL,
  `UPDATE_DATETIME` DATETIME    NOT NULL,
  `TEACHER_ID`      BIGINT(20)  NOT NULL DEFAULT '0',
  `SUBJECT`         VARCHAR(64) NOT NULL DEFAULT '',
  `SCHOOL_ID`       BIGINT(20)  NOT NULL DEFAULT '0',
  `TOTAL_SCORE`     INT(11)     NOT NULL DEFAULT '0',
  `DISABLED`        BIT(1)      NOT NULL DEFAULT b'0',
  PRIMARY KEY (`ID`),
  KEY `IDX_TID` (`TEACHER_ID`) USING BTREE,
  KEY `IDX_SID_S` (`SCHOOL_ID`, `SUBJECT`) USING BTREE
)
  ENGINE = InnoDB;