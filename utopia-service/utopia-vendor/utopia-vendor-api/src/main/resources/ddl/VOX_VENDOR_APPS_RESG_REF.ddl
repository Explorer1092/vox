CREATE TABLE `VOX_VENDOR_APPS_RESG_REF` (
  `ID`              BIGINT(20)  NOT NULL AUTO_INCREMENT,
  `APP_ID`          BIGINT(20)  NOT NULL,
  `APP_KEY`         VARCHAR(30) NOT NULL,
  `RESG_ID`         BIGINT(20)  NOT NULL,
  `CREATE_DATETIME` DATETIME    NOT NULL,
  `UPDATE_DATETIME` DATETIME    NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `IDX_APP_KEY` (`APP_KEY`)
)
  ENGINE = InnoDB;