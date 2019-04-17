CREATE TABLE `VOX_AFENTI_USER_GIFT_RECORD` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `USER_ID` bigint(20) NOT NULL,
  `GIFT_TYPE` varchar(25)  NOT NULL,
  `BEANS_NUM` int(4) DEFAULT '0',
  `STATUS` varchar(25)  DEFAULT NULL,
  `SUBJECT` varchar(25)  NOT NULL,
  `CREATE_DATETIME` datetime NOT NULL,
  `UPDATE_DATETIME` datetime NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UID_SJ_TYPE` (`USER_ID`,`SUBJECT`,`GIFT_TYPE`)
  ) ENGINE=InnoDB;