CREATE TABLE MDB_VENDOR_RESG_CONTENT (
  ID              BIGINT NOT NULL  PRIMARY KEY,
  RESG_ID         BIGINT,
  RES_NAME        VARCHAR(50),
  CREATE_DATETIME TIMESTAMP,
  UPDATE_DATETIME TIMESTAMP
);