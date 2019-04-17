CREATE TABLE `VOX_AMBASSADOR_SCHOOL_REF` (
  `ID`              BIGINT(20) NOT NULL AUTO_INCREMENT,
  `CREATE_DATETIME` DATETIME   NOT NULL,
  `UPDATE_DATETIME` DATETIME   NOT NULL,
  `AMBASSADOR_ID`   BIGINT(20) NOT NULL,
  `SCHOOL_ID`       BIGINT(20) NOT NULL,
  `DISABLED`        BIT(1)     NOT NULL DEFAULT b'0',
  PRIMARY KEY (`ID`),
  KEY `IDX_SCHOOL_ID` (`SCHOOL_ID`) USING BTREE,
  KEY `IDX_AMBASSADOR_ID` (`AMBASSADOR_ID`) USING BTREE
)
  ENGINE = InnoDB;