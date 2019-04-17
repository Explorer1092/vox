DROP TABLE IF EXISTS AGENT_UNIFIED_EXAM_APPLY;
CREATE TABLE `AGENT_UNIFIED_EXAM_APPLY` (
`ID`  bigint(20) NOT NULL AUTO_INCREMENT ,
`APPLY_TYPE`  varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '申请类型：统考申请' ,
`CREATE_DATETIME`  datetime NOT NULL COMMENT '创建时间' ,
`UPDATE_DATETIME`  datetime NOT NULL COMMENT '修改时间' ,
`USER_PLATFORM`  varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL ,
`ACCOUNT`  varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '申请者账号' ,
`ACCOUNT_NAME`  varchar(50) NOT NULL ,
`STATUS`  varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL ,
`WORKFLOW_ID`  bigint(20) NULL DEFAULT NULL COMMENT '对应的工作流ID' ,
`UNIFIED_EXAM_NAME`  varchar(150) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '统考名称' ,
`SUBJECT`  varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL ,
`GRADE_LEVEL`  varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '年级' ,
`REGION_LEVE`  varchar(30) NOT NULL COMMENT '地域级别 city：市 country：区school：校' ,
`PROVINCE_CODE`  varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '省编码' ,
`CITY_CODE`  varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '市编码' ,
`REGION_CODE`  varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL ,
`PROVINCE_NAME`  varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL ,
`CITY_NAME`  varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL ,
`REGION_NAME`  varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL ,
`UNIFIED_EXAM_SCHOOL`  varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '指定统考学校id以，分割' ,
`TEST_PAPER_SOURCE_TYPE`  varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL ,
`TEST_PAPER_ADDRESS`  varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '试卷地址' ,
`TEST_PAPER_ID`	 varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT  '旧的试卷id标识（用于使用之前已经上传过的试卷id标识）',
`TEST_PAPER_TYPE`  varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '统考试卷类型' ,
`UNIFIED_EXAM_BEGIN_TIME`  datetime NOT NULL COMMENT '考试开始时间' ,
`UNIFIED_EXAM_END_TIME`  datetime NOT NULL COMMENT '考试截止时间' ,
`CORRECTING_TEST_PAPER`  datetime NOT NULL COMMENT '批改作业时间' ,
`ACHIEVEMENT_RELEASE_TIME`  datetime NOT NULL COMMENT '成绩发布时间' ,
`MIN_SUBMITTED_TEST_PAPER`  smallint(6) NOT NULL COMMENT '最短交卷时间' ,
`ORAL_LANGUAGE_FREQUENCY`  smallint(6)  NULL COMMENT '口语可答题次数' ,
`MAX_SUBMITTED_TEST_PAPER`  smallint(6) NOT NULL COMMENT '最长上交试卷时间' ,
`ENTRY_STATUS`  varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '录入状态' ,
`ENTRY_TEST_PAPER_ADDRESS`  varchar(150) CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '录入试卷地址' ,
`ENTRY_TEST_PAPE_NAME`  varchar(150) CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '录入试卷名称' ,
`SEND_EMAIL` varchar(150) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '通知邮箱邮箱格式 xxx@17zy.com;yyy@17zy.com',
`UNIFIED_EXAM_STATUS` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '统考记录状态' ,
PRIMARY KEY (`ID`)
)
;
--2017.5.12 add
ALTER TABLE `AGENT_UNIFIED_EXAM_APPLY`
ADD COLUMN `BOOK_CATALOG_ID`  varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '教材书ID' AFTER `UNIFIED_EXAM_STATUS`,
ADD COLUMN `SCORE`  double(20,2) NULL COMMENT '试卷总分值' AFTER `BOOK_CATALOG_ID`,
ADD COLUMN `BOOK_CATALOG_NAME`  varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '教材书名称' AFTER `SCORE`;

