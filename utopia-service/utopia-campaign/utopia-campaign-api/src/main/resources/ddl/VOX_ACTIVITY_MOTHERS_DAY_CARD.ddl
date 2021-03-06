CREATE TABLE `VOX_ACTIVITY_MOTHERS_DAY_CARD` (
  `STUDENT_ID`      BIGINT(20)   NOT NULL,
  `CREATE_DATETIME` DATETIME     NOT NULL,
  `UPDATE_DATETIME` DATETIME     NOT NULL,
  `IMAGE`           VARCHAR(255) NOT NULL DEFAULT '',
  `VOICE`           VARCHAR(255) NOT NULL DEFAULT '',
  `SENDED`          BIT(1)       NOT NULL DEFAULT b'0',
  `SHARED`          BIT(1)       NOT NULL DEFAULT b'0',
  PRIMARY KEY (`STUDENT_ID`)
)
  ENGINE = InnoDB;