CREATE TABLE `AGENT_USER` (
  `ID`                    BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `ACCOUNT_NAME`          VARCHAR(50)  NOT NULL,
  `REAL_NAME`             VARCHAR(50)  NOT NULL,
  `PASSWD`                VARCHAR(50)  NOT NULL,
  `PASSWD_SALT`           VARCHAR(50)  NOT NULL,
  `USER_COMMENT`          VARCHAR(200) NULL     DEFAULT NULL,
  `CONTRACT_START_DATE`   DATETIME     NULL     DEFAULT NULL,
  `CONTRACT_END_DATE`     DATETIME     NULL     DEFAULT NULL,
  `CONTRACT_NUMBER`       VARCHAR(50)  NULL     DEFAULT NULL,
  `CASH_AMOUNT`           FLOAT        NOT NULL,
  `POINT_AMOUNT`          FLOAT        NOT NULL,
  `USABLE_CASH_AMOUNT`    FLOAT        NOT NULL,
  `USABLE_POINT_AMOUNT`   FLOAT        NOT NULL,
  `TEL`                   VARCHAR(30)  NULL     DEFAULT NULL,
  `EMAIL`                 VARCHAR(200) NULL     DEFAULT NULL,
  `IM_ACCOUNT`            VARCHAR(20)  NULL     DEFAULT NULL,
  `ADDRESS`               VARCHAR(100) NULL     DEFAULT NULL,
  `CASH_DEPOSIT`          INT(11)      NULL     DEFAULT NULL,
  `CASH_DEPOSIT_RECEIVED` BIT(1)       NULL     DEFAULT NULL,
  `BANK_NAME`             VARCHAR(50)  NULL     DEFAULT NULL,
  `BANK_HOST_NAME`        VARCHAR(30)  NULL     DEFAULT NULL,
  `BANK_ACCOUNT`          VARCHAR(50)  NULL     DEFAULT NULL,
  `STATUS`                TINYINT(4)   NOT NULL DEFAULT '0',
  `CREATE_DATETIME`       DATETIME     NOT NULL,
  `UPDATE_DATETIME`       DATETIME     NOT NULL,
  `SCHOOL_LEVEL`          TINYINT(4)   NULL     DEFAULT '0',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `IDX_ACCOUNT_NAME` (`ACCOUNT_NAME`)
)
  ENGINE = InnoDB;