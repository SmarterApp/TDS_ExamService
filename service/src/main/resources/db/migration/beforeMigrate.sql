/***********************************************************************************************************************
  File: beforeMigrate.sql

  Desc: The database needs to be utf8 character set and collation of utf8_unicode_ci.  Once set any future tables
  created will use the same character set and collation.

  Since the legacy system stores related data in two separate schemas we need to modify both config and session

***********************************************************************************************************************/

ALTER DATABASE exam CHARACTER SET utf8 COLLATE utf8_unicode_ci;