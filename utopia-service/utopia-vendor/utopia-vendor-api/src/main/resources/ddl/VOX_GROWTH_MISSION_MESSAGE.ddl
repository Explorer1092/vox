CREATE TABLE `VOX_GROWTH_MISSION_MESSAGE` (
  `ID`              BIGINT(20)  NOT NULL AUTO_INCREMENT,
  `MISSION_ID`       VARCHAR(255)  NOT NULL,
  `TYPE`        VARCHAR(20) NOT NULL,
  `POSITION`        VARCHAR(50)  NOT NULL ,
  `RENDER_RULE`        VARCHAR(50)  NOT NULL ,
  `CONDITION`        INT  NOT NULL ,
  `TITLE`        VARCHAR(255)  NOT NULL ,
  `STATUS`        VARCHAR(10)  NOT NULL ,
  `CONTENT`        VARCHAR(2000)  NOT NULL ,
  `CREATE_DATETIME` DATETIME    NOT NULL,
  `UPDATE_DATETIME` DATETIME    NOT NULL,
  `START_DATETIME` DATETIME    NOT NULL,
  `END_DATETIME` DATETIME    NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `IDX_MISSION_TIME` (`CREATE_DATETIME`,`START_DATETIME`,`END_DATETIME`,`STATUS`)
)
  ENGINE = InnoDB;