CREATE TABLE MDB_GIFT (
  ID                BIGINT NOT NULL PRIMARY KEY,
  DISABLED          BOOLEAN,
  CREATE_DATETIME   TIMESTAMP,
  UPDATE_DATETIME   TIMESTAMP,
  NAME              VARCHAR(255),
  GOLD              INT,
  SILVER            INT,
  IMG_URL           VARCHAR(255),
  GIFT_CATEGORY     VARCHAR(32),
  STUDENT_AVAILABLE BOOLEAN,
  TEACHER_AVAILABLE BOOLEAN
);