CREATE TABLE `AGENT_GROUP_PRODUCT` (
  `ID`              SMALLINT(6) NOT NULL AUTO_INCREMENT,
  `GROUP_ID`        SMALLINT(6) NOT NULL,
  `PRODUCT_ID`      SMALLINT(6) NOT NULL,
  `CREATE_DATETIME` DATETIME    NOT NULL,
  `UPDATE_DATETIME` DATETIME    NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `GROUP_PRODUCT` (`GROUP_ID`, `PRODUCT_ID`)
)
  ENGINE = InnoDB;