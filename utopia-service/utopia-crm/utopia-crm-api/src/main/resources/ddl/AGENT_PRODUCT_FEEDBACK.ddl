DROP TABLE IF EXISTS AGENT_PRODUCT_FEEDBACK;
CREATE TABLE `AGENT_PRODUCT_FEEDBACK` (
`ID`  bigint NOT NULL AUTO_INCREMENT ,
`APPLY_TYPE`  varchar(30) NOT NULL ,
`FEEDBACK_TYPE`  varchar(30) NOT NULL COMMENT '反馈类型' ,
`FIRST_CATEGORY`  varchar(20) NULL COMMENT '一级分类' ,
`SECOND_CATEGORY`  varchar(20) NULL COMMENT '二级分类' ,
`THIRD_CATEGORY`  varchar(20) NULL COMMENT '三级分类' ,
`TEACHER_ID`  bigint NOT NULL COMMENT '老师ID' ,
`TEACHER_NAME`  varchar(50) NOT NULL COMMENT '老师姓名' ,
`TEACHER_SUBJECT`  varchar(20) NULL COMMENT '老师学科' ,
`NOTICE_FLAG`  bit NOT NULL COMMENT '运行发送消息感谢老师' ,
`NOTICE_CONTENT`  varchar(100) NULL COMMENT '感谢老师的通知内容' ,
`BOOK_NAME`  varchar(25) NULL COMMENT '教材名称' ,
`BOOK_GRADE`  varchar(25) NULL COMMENT '教材对应的年级' ,
`BOOK_UNIT`  varchar(25) NULL COMMENT '教材单元' ,
`BOOK_COVERED_AREA`  varchar(25) NULL COMMENT '教材覆盖的地区' ,
`BOOK_COVERED_STUDENT_COUNT`  int NULL COMMENT '教材覆盖的学生数' ,
`CONTENT`  varchar(120) NOT NULL COMMENT '反馈内容' ,
`PIC1_URL`  varchar(100) NULL ,
`PIC2_URL`  varchar(100) NULL ,
`PIC3_URL`  varchar(100) NULL ,
`PIC4_URL`  varchar(100) NULL ,
`PIC5_URL`  varchar(100) NULL ,
`PM_ACCOUNT`  varchar(50) NULL COMMENT 'PM账号' ,
`PM_ACCOUNT_NAME`  varchar(50) NULL COMMENT 'PM姓名' ,
`ONLINE_ESTIMATE_DATE`  varchar(20) NULL COMMENT '预计上线时间（yyyy-MM）' ,
`ONLINE_FLAG`  bit NULL COMMENT '是否上线' ,
`CALLBACK`  bit NULL COMMENT '是否需要回电' ,
`ONLINE_DATE`  datetime NULL COMMENT '上线日期' ,
`ONLINE_NOTICE`  varchar(100) NULL COMMENT '上线通知' ,
`FEEDBACK_STATUS`  varchar(30) NULL COMMENT '产品反馈的业务状态' ,
`USER_PLATFORM`  varchar(20) NOT NULL ,
`ACCOUNT`  varchar(20) NOT NULL ,
`ACCOUNT_NAME`  varchar(50) NOT NULL ,
`STATUS`  varchar(20) NOT NULL ,
`CREATE_DATETIME`  datetime NOT NULL ,
`UPDATE_DATETIME`  datetime NOT NULL ,
`WORKFLOW_ID`  bigint NULL COMMENT '对应的工作流ID' ,
PRIMARY KEY (`ID`),
INDEX `TEACHER_ID_INDEX` (`TEACHER_ID`) ,
INDEX `PM_ACCOUNT_INDEX` (`PM_ACCOUNT`)
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8
;