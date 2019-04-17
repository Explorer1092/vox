DROP TABLE IF EXISTS `VOX_TANGRAM_ACTIVITY_STUDENT`;

CREATE TABLE `VOX_TANGRAM_ACTIVITY_STUDENT` (
  `ID`              BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '学生ID',
  `CREATE_TIME`     DATETIME     NOT NULL  COMMENT '创建时间',
  `UPDATE_TIME`     DATETIME     NOT NULL  COMMENT '更新时间',
  `DISABLED`        BIT(1)       NOT NULL  DEFAULT b'0' COMMENT '删除状态',
  `TEACHER_ID`      BIGINT(20)   NOT NULL  DEFAULT '0'  COMMENT '参与活动老师ID',
  `SCHOOL_ID`       BIGINT(20)   NOT NULL  DEFAULT '0'  COMMENT '老师学校ID',
  `STUDENT_NAME`    VARCHAR(50)  NOT NULL  DEFAULT ''   COMMENT '学生姓名',
  `STUDENT_CODE`    VARCHAR(50)  NOT NULL  DEFAULT ''   COMMENT '学生编号',
  `GRADE_NAME`      VARCHAR(20)  NOT NULL  DEFAULT 'FOURTH_GRADE'   COMMENT '年级名称',
  `CLASS_NAME`      VARCHAR(20)  NOT NULL  DEFAULT ''   COMMENT '班级名称',
  `MASTERPIECE_1`   VARCHAR(255) NULL      COMMENT '上传作品1',
  `MASTERPIECE_2`   VARCHAR(255) NULL      COMMENT '上传作品2',
  `MASTERPIECE_3`   VARCHAR(255) NULL      COMMENT '上传作品3',
  `SCORE`           VARCHAR(10)  NOT NULL  DEFAULT 'UNTITLED'   COMMENT '作品打分',
  `COMMENT`         TEXT         NULL      COMMENT '作品评价',
  `AUDITOR_ID`      VARCHAR(50)  NULL      COMMENT '打分人ID',
  `AUDIT_TIME`      DATETIME     NULL      COMMENT '学生姓名',
  PRIMARY KEY (`ID`),
  KEY `TEACHER_KEY` (`TEACHER_ID`),
  KEY `SCHOOL_KEY` (`SCHOOL_ID`)
)
  ENGINE = InnoDB;