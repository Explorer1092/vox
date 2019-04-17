CREATE TABLE `AGENT_USER_KPI_RESULT_S2016` (
  `ID`              BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `REGION_ID`       BIGINT(20)   NOT NULL,
  `REGION_NAME`     VARCHAR(200) NULL     DEFAULT NULL,
  `PROVINCE_ID`     BIGINT(20)   NOT NULL,
  `PROVINCE_NAME`   VARCHAR(200) NULL     DEFAULT NULL,
  `COUNTY_CODE`     INT(11)      NOT NULL,
  `COUNTY_NAME`     VARCHAR(200) NULL     DEFAULT NULL,
  `SCHOOL_ID`       BIGINT(20)   NULL     DEFAULT NULL,
  `SCHOOL_NAME`     VARCHAR(200) NULL     DEFAULT NULL,
  `SCHOOL_LEVEL`    SMALLINT(6)  NULL     DEFAULT NULL,
  `SALARY_MONTH`    INT(11)      NOT NULL,
  `USER_ID`         BIGINT(20)   NOT NULL,
  `USER_NAME`       VARCHAR(50)  NULL     DEFAULT NULL,
  `START_DATE`      DATETIME     NOT NULL,
  `END_DATE`        DATETIME     NOT NULL,
  `CPA_TYPE`        VARCHAR(20)  NOT NULL,
  `CPA_TARGET`      INT(11)      NOT NULL,
  `CPA_RESULT`      INT(11)      NOT NULL,
  `CPA_SALARY`      INT(11)      NOT NULL,
  `CPA_NOTE`        VARCHAR(200) NULL     DEFAULT NULL,
  `FINANCE_CHECK`   BIT(1)       NOT NULL DEFAULT b'0',
  `MARKET_CHECK`    BIT(1)       NOT NULL DEFAULT b'0',
  `DISABLED`        BIT(1)       NOT NULL DEFAULT b'0',
  `CREATE_DATETIME` DATETIME     NOT NULL,
  `UPDATE_DATETIME` DATETIME     NOT NULL,
  PRIMARY KEY (`ID`)
)
  ENGINE = InnoDB;