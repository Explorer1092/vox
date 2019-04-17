CREATE TABLE `UCT_USER_WECHAT_REF` (
  `ID`              BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `USER_ID`         INT(11)      NOT NULL,
  `OPEN_ID`         VARCHAR(100) NOT NULL,
  `UNION_ID`        VARCHAR(100) NOT NULL,
  `DISABLED`        BIT(1)       NOT NULL DEFAULT b'0',
  `CREATE_DATETIME` DATETIME     NOT NULL,
  `UPDATE_DATETIME` DATETIME     NOT NULL,
  `SOURCE`          VARCHAR(50)  NULL     DEFAULT NULL,
  `TYPE`            INT(11)      NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `IDX_OPENID_USERID` (`USER_ID`, `OPEN_ID`),
  KEY `IDX_UPDATETIME` (`UPDATE_DATETIME`),
  KEY `IND_OPENID_DISABLE` (`OPEN_ID`(50), `DISABLED`)
)
  ENGINE = InnoDB;