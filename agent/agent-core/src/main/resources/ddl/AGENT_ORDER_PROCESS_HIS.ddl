CREATE TABLE `AGENT_ORDER_PROCESS_HIS` (
  `ID`              BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `ORDER_ID`        BIGINT(20)   NOT NULL,
  `PROCESSOR`       BIGINT(20)   NOT NULL,
  `RESULT`          TINYINT(4)   NOT NULL,
  `PROCESS_NOTES`   VARCHAR(200) NULL     DEFAULT NULL,
  `CREATE_DATETIME` DATETIME     NOT NULL,
  `UPDATE_DATETIME` DATETIME     NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `ORDER_PROCESS_HIS` (`ORDER_ID`),
  KEY `ORDER_PROCESSOR` (`PROCESSOR`)
)
  ENGINE = InnoDB;