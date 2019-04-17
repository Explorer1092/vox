CREATE TABLE `VOX_SCHOOL_AMBASSADOR` (
  `ID`                            BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `CREATE_DATETIME`               DATETIME     NOT NULL,
  `USER_ID`                       BIGINT(20)   NOT NULL,
  `NAME`                          VARCHAR(255) NOT NULL,
  `MOBILE`                        VARCHAR(255) NOT NULL,
  `QQ`                            VARCHAR(255) NOT NULL,
  `EMAIL`                         VARCHAR(255) NOT NULL,
  `LEADER`                        VARCHAR(255) NOT NULL,
  `TOTAL_COUNT`                   INT(11)      NOT NULL,
  `USING_COUNT`                   INT(11)      NOT NULL,
  `SUGGESTION`                    TEXT         NOT NULL,
  `GENDER`                        VARCHAR(255) NULL     DEFAULT NULL,
  `ADDRESS`                       VARCHAR(255) NULL     DEFAULT NULL,
  `ENGLISH_COUNT`                 INT(11)      NOT NULL DEFAULT '0',
  `MATH_COUNT`                    INT(11)      NOT NULL DEFAULT '0',
  `CHINESE_COUNT`                 INT(11)      NOT NULL DEFAULT '0',
  `STUDENT_COUNT`                 INT(11)      NOT NULL DEFAULT '0',
  `CLAZZ_COUNT`                   INT(11)      NOT NULL DEFAULT '0',
  `EDU_SYSTEM_TYPE`               VARCHAR(64)  NULL     DEFAULT NULL,
  `SOURCE`                        VARCHAR(64)  NULL     DEFAULT NULL,
  `PNAME`                         VARCHAR(255) NULL     DEFAULT NULL,
  `CNAME`                         VARCHAR(255) NULL     DEFAULT NULL,
  `ANAME`                         VARCHAR(255) NULL     DEFAULT NULL,
  `T_YEAR`                        INT(11)      NULL     DEFAULT NULL,
  `B_YEAR`                        INT(11)      NULL     DEFAULT NULL,
  `B_MONTH`                       INT(11)      NULL     DEFAULT NULL,
  `B_DAY`                         INT(11)      NULL     DEFAULT NULL,
  `IS_FX`                         BIT(1)       NOT NULL DEFAULT b'0',
  `FX_CLASS`                      VARCHAR(255) NULL     DEFAULT NULL,
  `SCHOOL_NAME`                   VARCHAR(255) NULL     DEFAULT NULL,
  `SCHOOL_LEVEL`                  VARCHAR(255) NULL     DEFAULT NULL,
  `ONE_GRADE_CLAZZ_COUNT_BEGIN`   INT(11)      NULL     DEFAULT NULL,
  `ONE_GRADE_CLAZZ_COUNT_END`     INT(11)      NULL     DEFAULT NULL,
  `ONE_CLAZZ_STUDENT_COUNT_BEGIN` INT(11)      NULL     DEFAULT NULL,
  `ONE_CLAZZ_STUDENT_COUNT_END`   INT(11)      NULL     DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `IDX_CREATE_DATETIME` (`CREATE_DATETIME`),
  KEY `IDX_USER_ID` (`USER_ID`)
)
  ENGINE = InnoDB;