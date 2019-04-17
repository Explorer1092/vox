CREATE TABLE `AGENT_USER_ACCOUNT_HIS` (
  `ID`              BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `USER_ID`         BIGINT(20)   NOT NULL,
  `CASH_BEFORE`     FLOAT        NOT NULL,
  `CASH_AMOUNT`     FLOAT        NOT NULL,
  `CASH_AFTER`      FLOAT        NOT NULL,
  `POINT_BEFORE`    FLOAT        NOT NULL,
  `POINT_AMOUNT`    FLOAT        NOT NULL,
  `POINT_AFTER`     FLOAT        NOT NULL,
  `ORDER_ID`        BIGINT(20)   NULL     DEFAULT NULL,
  `COMMENTS`        VARCHAR(200) NULL     DEFAULT NULL,
  `CREATE_DATETIME` DATETIME     NOT NULL,
  `UPDATE_DATETIME` DATETIME     NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `USER_ACCOUNT` (`USER_ID`)
)
  ENGINE = InnoDB;