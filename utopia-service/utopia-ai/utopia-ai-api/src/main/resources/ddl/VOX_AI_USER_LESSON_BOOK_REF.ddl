CREATE TABLE `VOX_AI_USER_LESSON_BOOK_REF` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `USER_ID` bigint(20) NOT NULL,
  `BOOK_ID` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `BOOK_NAME` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `PRODUCT_ID` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `DISABLED` bit(1) NOT NULL,
  `CREATETIME` datetime NOT NULL,
  `UPDATETIME` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_USER_BOOK_PRODUCT` (`USER_ID`,`BOOK_ID`,`PRODUCT_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;