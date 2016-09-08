/***********************************************************************************************************************
  File: beforeMigrate.sql

  Desc: Create the exam schema that the Exam service depends on.  This script will be executed as a
  part of Flyway's run cycle before any of the migrations are executed.  For more details on how to hook into Flyway,
  check here:  https://flywaydb.org/documentation/callbacks

***********************************************************************************************************************/
CREATE SCHEMA IF NOT EXISTS exam;