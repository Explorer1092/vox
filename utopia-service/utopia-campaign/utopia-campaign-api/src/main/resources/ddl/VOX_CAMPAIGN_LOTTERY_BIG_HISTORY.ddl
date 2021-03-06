CREATE TABLE `VOX_CAMPAIGN_LOTTERY_BIG_HISTORY` (
  `ID`              BIGINT(20) NOT NULL AUTO_INCREMENT,
  `USER_ID`         BIGINT(20) NOT NULL,
  `CAMPAIGN_ID`     INT(11)    NOT NULL,
  `AWARD_ID`        INT(11)    NOT NULL DEFAULT '0',
  `CREATE_DATETIME` DATETIME   NOT NULL,
  `UPDATE_DATETIME` DATETIME   NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `IDX_CAMPAIGN` (`CAMPAIGN_ID`)
)
  ENGINE = InnoDB;