CREATE TABLE `VOX_TA_CLAZZ_RANK_REWARD_HISTORY` (
  `ID`              BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `CREATE_DATETIME` DATETIME     NOT NULL,
  `UPDATE_DATETIME` DATETIME     NOT NULL,
  `CLAZZ_ID`        BIGINT(20)   NOT NULL,
  `MONTH`           VARCHAR(128) NOT NULL,
  `USER_ID`         BIGINT(11)   NOT NULL,
  `REWARDED`        BIT(1)       NOT NULL DEFAULT b'1',
  `SPECIAL_FLAG`    BIT(1)       NOT NULL DEFAULT b'0',
  PRIMARY KEY (`ID`),
  KEY `IDX_MONTH_USER_ID` (`MONTH`, `USER_ID`) USING BTREE,
  KEY `IDX_CLAZZ_ID` (`CLAZZ_ID`) USING BTREE,
  KEY `IDX_USER_ID` (`USER_ID`)
)
  ENGINE = InnoDB;