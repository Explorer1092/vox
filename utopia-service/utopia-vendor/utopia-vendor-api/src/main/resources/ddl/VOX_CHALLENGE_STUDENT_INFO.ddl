CREATE TABLE `VOX_CHALLENGE_STUDENT_INFO` (
  `ID`              BIGINT(20) NOT NULL AUTO_INCREMENT,
  `STUDENT_ID`      BIGINT(20) NOT NULL,
  `CLAZZ_ID`        BIGINT(20) NOT NULL,
  `SCORE`           INT(11)    NOT NULL DEFAULT '0',
  `DISABLED`        BIT(1)     NOT NULL DEFAULT b'0',
  `CREATE_DATETIME` DATETIME   NOT NULL,
  `UPDATE_DATETIME` DATETIME   NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `IDX_STUDENT_ID` (`STUDENT_ID`),
  KEY `IDX_CLAZZ_ID` (`CLAZZ_ID`)
)
  ENGINE = InnoDB;