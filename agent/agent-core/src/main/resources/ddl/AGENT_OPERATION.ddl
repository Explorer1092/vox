CREATE TABLE `AGENT_OPERATION` (
`ID`  bigint(20) NOT NULL AUTO_INCREMENT ,
`MODULE`  varchar(20)  NOT NULL ,
`SUB_MODULE`  varchar(20) NOT NULL ,
`OPERATION_CODE`  varchar(40) NOT NULL ,
`OPERATION_NAME`  varchar(20) NOT NULL ,
`COMMENT`  varchar(50) NULL DEFAULT NULL ,
`DISABLED`  bit(1) NOT NULL DEFAULT b'0' ,
`CREATE_DATETIME`  datetime NOT NULL ,
`UPDATE_DATETIME`  datetime NOT NULL ,
PRIMARY KEY (`ID`),
INDEX `DISABLED` (`DISABLED`) USING BTREE
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8
;