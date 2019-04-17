DROP TABLE IF EXISTS VOX_WORKFLOW_PROCESS_HISTORY;
CREATE TABLE VOX_WORKFLOW_PROCESS_HISTORY (
  ID                      BIGINT(20)    NOT NULL    AUTO_INCREMENT,
  WORKFLOW_RECORD_ID      BIGINT(20)    NOT NULL    COMMENT '工作流记录ID',
  SOURCE_APP              VARCHAR(16)   NOT NULL    COMMENT '处理源',
  PROCESSOR_ACCOUNT       VARCHAR(255)  NOT NULL    COMMENT '处理者账号',
  PROCESSOR_NAME          VARCHAR(255)  NOT NULL    COMMENT '处理者姓名',
  RESULT                  VARCHAR(16)  NOT NULL    COMMENT '处理结果',
  PROCESS_NOTES           VARCHAR(512)  NOT NULL    COMMENT '处理结果备注',
  CREATE_DATETIME    DATETIME     NOT NULL,
  UPDATE_DATETIME    DATETIME     NOT NULL,
  PRIMARY KEY (ID),
  INDEX SOURCE_PROCESSOR_ACCOUNT  (SOURCE_APP,PROCESSOR_ACCOUNT) USING BTREE,
  INDEX WORKFLOW_RECORD_ID  (WORKFLOW_RECORD_ID) USING BTREE
)ENGINE = InnoDB;
ALTER TABLE VOX_WORKFLOW_PROCESS_HISTORY ADD COLUMN WORK_FLOW_TYPE VARCHAR(255);
ALTER TABLE `VOX_WORKFLOW_PROCESS_HISTORY` ADD INDEX `WORK_FLOW_TYPE` (`WORK_FLOW_TYPE`) USING BTREE;