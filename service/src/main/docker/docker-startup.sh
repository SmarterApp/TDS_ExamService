#!/bin/sh
#-----------------------------------------------------------------------------------------------------------------------
# File:  docker-startup.sh
#
# Desc:  Start the tds-exam-service.jar with the appropriate properties.
#
#-----------------------------------------------------------------------------------------------------------------------

java \
    -Dspring.ds_queries.jdbcUrl="jdbc:mysql://exam_mysql/${EXAM_DB_NAME}" \
    -Dspring.ds_queries.username="${EXAM_DB_USER}" \
    -Dspring.ds_queries.password="${EXAM_DB_PASSWORD}" \
    -Dspring.ds_queries.driver-class-name=com.zaxxer.hikari.HikariDataSource \
    -Dspring.ds_commands.jdbcUrl="jdbc:mysql://exam_mysql/${EXAM_DB_NAME}" \
    -Dspring.ds_commands.username="${EXAM_DB_USER}" \
    -Dspring.ds_commands.password="${EXAM_DB_PASSWORD}" \
    -Dspring.ds_commands.driver-class-name=com.zaxxer.hikari.HikariDataSource \
    -Dexam-service.session-url=http://session/ \
    -Dexam-service.student-url=http://student/ \
    -Dexam-service.assessment-url=http://assessment/ \
    -Dexam-service.config-url=http://config/ \
    -jar /tds-exam-service.jar \
    --server-port="8080" \
    --server.undertow.buffer-size=16384 \
    --server.undertow.buffers-per-region=20 \
    --server.undertow.io-threads=64 \
    --server.undertow.worker-threads=512 \
    --server.undertow.direct-buffers=true