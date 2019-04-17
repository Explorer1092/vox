CREATE TABLE `VOX_WECHAT_NOTICE_HISTORY` (
  `ID`              BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `USER_ID`         BIGINT(20)   NOT NULL,
  `OPEN_ID`         VARCHAR(100) NULL     DEFAULT NULL,
  `MESSAGE`         TEXT         NOT NULL,
  `MESSAGE_TYPE`    SMALLINT(6)  NOT NULL,
  `STATE`           TINYINT(4)   NOT NULL,
  `DISABLED`        BIT(1)       NOT NULL,
  `CREATE_DATETIME` DATETIME     NOT NULL,
  `UPDATE_DATETIME` DATETIME     NOT NULL,
  `MESSAGE_ID`      VARCHAR(20)  NULL     DEFAULT NULL,
  `SEND_TIME`       DATETIME     NOT NULL,
  `EXPIRE_TIME`     DATETIME     NOT NULL,
  `ERROR_CODE`      VARCHAR(10)  NULL     DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `IDX_OPENID_MSGID` (`OPEN_ID`, `MESSAGE_ID`),
  KEY `IDX_CREATE_DATETIME` (`CREATE_DATETIME`),
  KEY `IDX_USERID` (`USER_ID`),
  KEY `IDX_MESSAGETYPE_STATE_DISABLED` (`STATE`, `MESSAGE_TYPE`, `DISABLED`, `SEND_TIME`)
)
  ENGINE = InnoDB;