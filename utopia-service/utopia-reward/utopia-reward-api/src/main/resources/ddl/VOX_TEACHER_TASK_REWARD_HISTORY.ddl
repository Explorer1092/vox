CREATE TABLE `VOX_TEACHER_TASK_REWARD_HISTORY` (
  `ID`              BIGINT(20)  NOT NULL AUTO_INCREMENT,
  `CREATE_DATETIME` DATETIME    NOT NULL,
  `UPDATE_DATETIME` DATETIME    NOT NULL,
  `TEACHER_ID`      BIGINT(20)  NOT NULL DEFAULT '0',
  `TASK_TYPE`       VARCHAR(64) NOT NULL DEFAULT '',
  `REWARD_NAME`     VARCHAR(64) NOT NULL DEFAULT '',
  PRIMARY KEY (`ID`),
  KEY `IDX_TID_T` (`TEACHER_ID`, `TASK_TYPE`) USING BTREE
)
  ENGINE = InnoDB;