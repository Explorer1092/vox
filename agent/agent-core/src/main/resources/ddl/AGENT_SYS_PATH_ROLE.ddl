CREATE TABLE `AGENT_SYS_PATH_ROLE` (
  `ID`              SMALLINT(6) NOT NULL AUTO_INCREMENT,
  `ROLE_ID`         SMALLINT(6) NOT NULL,
  `PATH_ID`         SMALLINT(6) NOT NULL,
  `CREATE_DATETIME` DATETIME    NOT NULL,
  `UPDATE_DATETIME` DATETIME    NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `ROLE_PATH` (`ROLE_ID`, `PATH_ID`)
)
  ENGINE = InnoDB;