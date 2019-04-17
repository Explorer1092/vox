CREATE TABLE `VOX_REG_FEEDBACK` (
  `ID`                INT(11)      NOT NULL AUTO_INCREMENT,
  `MOBILE`            VARCHAR(200) NOT NULL,
  `VERIFICATION_CODE` VARCHAR(45)  NOT NULL,
  `STATE`             VARCHAR(45)  NOT NULL,
  `CATEGORY`          VARCHAR(45)  NOT NULL,
  `OPERATION`         VARCHAR(255) NULL     DEFAULT NULL,
  `OPERATOR`          VARCHAR(50)  NULL     DEFAULT NULL,
  `CREATE_DATETIME`   DATETIME     NOT NULL,
  `UPDATE_DATETIME`   DATETIME     NOT NULL,
  `USER_ID`           BIGINT(20)   NULL     DEFAULT NULL,
  `CONTENT`           VARCHAR(500) NULL     DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `INDEX_REG_FEEDBACK` (`ID`, `MOBILE`, `STATE`, `CREATE_DATETIME`)
)
  ENGINE = InnoDB;