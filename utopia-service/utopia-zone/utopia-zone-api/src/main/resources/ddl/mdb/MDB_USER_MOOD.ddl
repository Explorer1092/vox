CREATE TABLE MDB_USER_MOOD (
  ID              BIGINT NOT NULL PRIMARY KEY,
  CREATE_DATETIME TIMESTAMP,
  TITLE           VARCHAR(255),
  DESCRIPTION     VARCHAR(255),
  IMG_URL         VARCHAR(255)
);