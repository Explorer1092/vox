CREATE TABLE `AGENT_ROLE_PAGE_ELEMENT` (
`ID`  bigint NOT NULL AUTO_INCREMENT ,
`ROLE_ID`  int NOT NULL ,
`PAGE_ELEMENT_ID`  bigint NOT NULL ,
`DISABLED`  bit NOT NULL DEFAULT b'0' ,
`CREATE_DATETIME`  datetime NOT NULL ,
`UPDATE_DATETIME`  datetime NOT NULL ,
PRIMARY KEY (`ID`),
INDEX `ROIE_ID` (`ROLE_ID`, `DISABLED`) ,
INDEX `ELEMENT_ID` (`PAGE_ELEMENT_ID`, `DISABLED`)
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8
;
