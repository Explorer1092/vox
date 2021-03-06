CREATE TABLE VOX_OFFICIAL_ACCOUNTS_TOOLS (
  ID              BIGINT(20)   NOT NULL AUTO_INCREMENT,
  CREATE_DATETIME DATETIME     NOT NULL,
  UPDATE_DATETIME DATETIME     NOT NULL,
  ACCOUNT_ID      BIGINT(20)   NOT NULL COMMENT '公众号ID',
  TOOL_URL        VARCHAR(256)   NOT NULL DEFAULT '' COMMENT '工具栏链接',
  TOOL_NAME       VARCHAR(256)   NOT NULL DEFAULT '' COMMENT '工具栏名称',
  DISABLED        BIT(1)         NOT NULL DEFAULT FALSE,
  BIND_SID        BIT(1)         NOT NULL DEFAULT FALSE,
  PRIMARY KEY (ID),
  INDEX IDX_AID(ACCOUNT_ID) USING BTREE
)
  ENGINE =InnoDB;