CREATE TABLE `AGENT_KPI_EVAL` (
  `ID`                 SMALLINT(6) NOT NULL AUTO_INCREMENT,
  `KPI_ID`             SMALLINT(6) NOT NULL,
  `EVAL_DATE`          DATETIME    NOT NULL,
  `EVAL_DURATION_FROM` DATETIME    NOT NULL,
  `EVAL_DURATION_TO`   DATETIME    NOT NULL,
  `CREATE_DATETIME`    DATETIME    NOT NULL,
  `UPDATE_DATETIME`    DATETIME    NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `KPI_EVAL` (`KPI_ID`)
)
  ENGINE = InnoDB;