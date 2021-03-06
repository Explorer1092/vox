CREATE TABLE `AGENT_OPERATION_LOG` (
  `ID`               BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `OPERATOR_ID`      BIGINT(20)   NOT NULL,
  `OPERATOR_NAME`    VARCHAR(50)  NOT NULL,
  `OPERATION_TYPE`   VARCHAR(30)  NOT NULL,
  `ACTION_URL`       VARCHAR(100) NULL     DEFAULT NULL,
  `OPERATION_RESULT` VARCHAR(200) NULL     DEFAULT NULL,
  `OPERATION_NOTES`  VARCHAR(200) NULL     DEFAULT NULL,
  `CREATE_DATETIME`  DATETIME     NOT NULL,
  `UPDATE_DATETIME`  DATETIME     NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `IDX_OPERATOR` (`OPERATOR_ID`),
  KEY `IDX_OPERATION_TYPE` (`OPERATION_TYPE`)
)
  ENGINE = InnoDB;