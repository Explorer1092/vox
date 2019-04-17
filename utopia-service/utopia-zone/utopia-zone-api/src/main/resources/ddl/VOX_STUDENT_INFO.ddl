CREATE TABLE VOX_STUDENT_INFO (
  STUDENT_ID         BIGINT(20) NOT NULL,
  CREATE_DATETIME    DATETIME   NOT NULL,
  UPDATE_DATETIME    DATETIME   NOT NULL,
  STUDY_MASTER_COUNT INT(11)    NOT NULL DEFAULT 0,
  LIKE_COUNT         INT(11)    NOT NULL DEFAULT 0,
  BUBBLE_ID          BIGINT(11) NOT NULL DEFAULT 0,
  SIGN_IN_COUNT      INT(11)    NOT NULL DEFAULT 0,
  PRIMARY KEY (STUDENT_ID)
)
  ENGINE =InnoDB;
