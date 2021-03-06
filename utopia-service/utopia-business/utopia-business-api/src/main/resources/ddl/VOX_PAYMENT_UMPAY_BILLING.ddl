CREATE TABLE `VOX_PAYMENT_UMPAY_BILLING` (
  `ID`              BIGINT(20)  NOT NULL AUTO_INCREMENT,
  `CREATE_DATETIME` DATETIME    NOT NULL,
  `BILLING_TYPE`    VARCHAR(45) NOT NULL,
  `BILLING_DAY`     VARCHAR(45) NOT NULL,
  `BILLING_CONTENT` LONGTEXT,
  PRIMARY KEY (`ID`),
  KEY `BILLING_DAY_TYPE` (`BILLING_DAY`, `BILLING_TYPE`),
  KEY `CREATE_DATETIME` (`CREATE_DATETIME`)
)
  ENGINE = InnoDB;