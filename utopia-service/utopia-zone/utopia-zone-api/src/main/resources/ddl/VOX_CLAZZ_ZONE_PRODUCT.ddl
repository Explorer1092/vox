CREATE TABLE `VOX_CLAZZ_ZONE_PRODUCT` (
  `ID`                 BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `NAME`               VARCHAR(255) NOT NULL,
  `PRICE`              INT(11)      NOT NULL,
  `CURRENCY`           VARCHAR(255) NOT NULL,
  `SPECIES`            VARCHAR(255) NOT NULL,
  `SUBSPECIES`         VARCHAR(255) NOT NULL,
  `PERIOD_OF_VALIDITY` BIGINT(20)   NOT NULL DEFAULT '0',
  PRIMARY KEY (`ID`),
  KEY `IDX_SPECIES` (`SPECIES`(30))
)
  ENGINE = InnoDB;