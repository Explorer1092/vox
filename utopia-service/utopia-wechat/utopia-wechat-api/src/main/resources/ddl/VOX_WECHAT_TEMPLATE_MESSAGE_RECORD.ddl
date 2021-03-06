CREATE TABLE `VOX_WECHAT_NOTICE_HISTORY` (
  `ID`              BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `USER_ID`         BIGINT(20)   NOT NULL,
  `OPEN_ID`         VARCHAR(100) NOT NULL,
  `WECHAT_TYPE`     INT(10)      NOT NULL,
  `MESSAGE_ID`      VARCHAR(20)  NULL     DEFAULT NULL,
  `MESSAGE`         TEXT         NOT NULL,
  `MESSAGE_TYPE`    INT(10)      NOT NULL,
  `STATE`           TINYINT(4)   NOT NULL,
  `ERROR_CODE`      VARCHAR(10)  NULL     DEFAULT NULL,
  `DISABLED`        BIT(1)       NOT NULL,
  `CREATE_DATETIME` DATETIME     NOT NULL,
  `UPDATE_DATETIME` DATETIME     NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_WECHAT_TEMPLATE_MESSAGE_OPENID_WTP_MSGID` (`OPEN_ID`,`WECHAT_TYPE`,`MESSAGE_ID`),
  KEY `IDX_WECHAT_TEMPLATE_MESSAGE_USER` (`USER_ID`)
)
  ENGINE = InnoDB;