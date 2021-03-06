CREATE TABLE `VOX_AMBASSADOR_REPORT_STUDENT_FEEDBACK` (
  `ID`              BIGINT(20) NOT NULL AUTO_INCREMENT,
  `CREATE_DATETIME` DATETIME   NOT NULL,
  `UPDATE_DATETIME` DATETIME   NOT NULL,
  `TEACHER_ID`      BIGINT(20) NOT NULL,
  `STUDENT_ID`      BIGINT(20) NOT NULL,
  `CONFIRM`         BIT(1)     NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `IDX_TS_ID` (`TEACHER_ID`, `STUDENT_ID`) USING BTREE
)
  ENGINE = InnoDB;