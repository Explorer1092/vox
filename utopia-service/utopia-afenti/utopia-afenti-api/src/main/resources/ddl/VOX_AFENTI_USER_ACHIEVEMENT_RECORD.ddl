CREATE TABLE `VOX_AFENTI_USER_ACHIEVEMENT_RECORD` (
  `ID` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `USER_ID` BIGINT(20) NOT NULL,
  `ACHIEVEMENT_TYPE` VARCHAR(25) NOT NULL,
  `SUBJECT` VARCHAR(25) NOT NULL,
  `LEVEL` INT(4) NOT NULL DEFAULT 0,
  `STATUS` VARCHAR(25) NOT NULL,
  `UPDATE_DATETIME` DATETIME NULL,
  `CREATE_DATETIME` DATETIME NULL,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `UI_UID_SJ_AT_LE` (`USER_ID` ASC, `SUBJECT` ASC, `ACHIEVEMENT_TYPE` ASC,`LEVEL` ASC)
  ) ENGINE=InnoDB;