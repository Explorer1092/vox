CREATE TABLE `VOX_STUDENT_MAGIC_VALUE_HISTORY` (
  `ID`              BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `CREATE_DATETIME` DATETIME     NOT NULL,
  `UPDATE_DATETIME` DATETIME     NOT NULL,
  `MAGICIAN_ID`     BIGINT(20)   NOT NULL DEFAULT '0',
  `VALUE_TYPE`      VARCHAR(128) NOT NULL DEFAULT '',
  `LEVEL_VALUE`     INT(11)      NOT NULL DEFAULT '0',
  PRIMARY KEY (`ID`),
  KEY `IDX_MID_CT` (`MAGICIAN_ID`, `CREATE_DATETIME`) USING BTREE
)
  ENGINE = InnoDB;