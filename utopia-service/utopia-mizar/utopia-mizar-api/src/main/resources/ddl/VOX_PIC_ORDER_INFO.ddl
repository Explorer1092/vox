CREATE TABLE `VOX_PIC_ORDER_INFO` (
  `ID`                         VARCHAR(32)    NOT NULL,
  `USER_ID`                    BIGINT(20)     NOT NULL,
  `ORDER_ID`                   VARCHAR(32)    NULL     DEFAULT NULL,
  `PRODUCT_NAME`               VARCHAR(100)   NULL     DEFAULT NULL,
  `PAYMENT_STATUS`             VARCHAR(25)    NULL     DEFAULT NULL,
  `PAY_AMOUNT`                 DECIMAL(14, 4) NOT NULL,
  `SERVICE_START_TIME`         DATETIME       NULL     DEFAULT NULL,
  `SERVICE_END_TIME`           DATETIME       NULL     DEFAULT NULL,
  `ORDER_CREATE_TIME`          DATETIME       NULL     DEFAULT NULL,
   `DISABLED`                  BIT(1)         NOT NULL DEFAULT FALSE,
   `USER_ID`                   VARCHAR (100)  NULL  DEFAULT NULL,
  `CREATE_DATETIME`            DATETIME       NOT NULL,
  `UPDATE_DATETIME`            DATETIME       NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `IDX_USER_ID` (`USER_ID`) USING BTREE,
  KEY `IDX_CTIME` (`ORDER_CREATE_TIME`)
)
  ENGINE = InnoDB;