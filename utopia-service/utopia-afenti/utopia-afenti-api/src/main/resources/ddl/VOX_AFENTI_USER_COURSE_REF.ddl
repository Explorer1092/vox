CREATE TABLE `VOX_AFENTI_USER_COURSE_REF` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `CREATE_DATETIME` datetime NOT NULL,
  `UPDATE_DATETIME` datetime NOT NULL,
  `DISABLED` bit(1) NOT NULL DEFAULT b'0',
  `USER_ID` bigint(20) NOT NULL,
  `COURSE_ID` varchar(32) NOT NULL,
  `SUBJECT` varchar(32) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `IDX_USER_ID_SUBJECT` (`USER_ID`,`SUBJECT`) USING BTREE
) ;