CREATE TABLE `VOX_TERM_BEGIN_STUDENT_APP_RECORD` (
  `ID`              BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `CREATE_DATETIME` DATETIME     NOT NULL,
  `UPDATE_DATETIME` DATETIME     NOT NULL,
  `TEACHER_ID`      BIGINT(20)   NOT NULL DEFAULT '0',
  `STUDENT_ID`      BIGINT(20)   NOT NULL DEFAULT '0',
  `CLAZZ_ID`        BIGINT(20)   NOT NULL DEFAULT '0',
  `HOMEWORK_ID`     VARCHAR(128) NOT NULL DEFAULT '',
  `HOMEWORK_TYPE`   VARCHAR(64)  NOT NULL DEFAULT '',
  `STUDENT_NAME`    VARCHAR(64)  NOT NULL DEFAULT '',
  `CLAZZ_NAME`      VARCHAR(64)  NOT NULL DEFAULT '',
  PRIMARY KEY (`ID`),
  KEY `IDX_TID` (`TEACHER_ID`) USING BTREE,
  KEY `IDX_TID_SID` (`TEACHER_ID`, `STUDENT_ID`) USING BTREE
)
  ENGINE = InnoDB;