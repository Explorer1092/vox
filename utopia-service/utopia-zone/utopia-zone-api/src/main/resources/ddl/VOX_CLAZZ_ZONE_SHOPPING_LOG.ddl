CREATE TABLE `VOX_CLAZZ_ZONE_SHOPPING_LOG` (
  `ID`                 BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `CREATE_DATETIME`    DATETIME     NOT NULL,
  `USER_ID`            BIGINT(20)   NOT NULL,
  `PRODUCT_ID`         BIGINT(20)   NOT NULL,
  `CURRENCY`           VARCHAR(255) NOT NULL,
  `PRICE`              INT(11)      NOT NULL,
  `PERIOD_OF_VALIDITY` BIGINT(20)   NOT NULL DEFAULT '0',
  PRIMARY KEY (`ID`)
)
  ENGINE = InnoDB;