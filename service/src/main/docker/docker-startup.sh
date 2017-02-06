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
    -Dspring.ds_queries.driver-class-name=com.mysql.jdbc.Driver \
    -Dspring.ds_commands.jdbcUrl="jdbc:mysql://exam_mysql/${EXAM_DB_NAME}" \
    -Dspring.ds_commands.username="${EXAM_DB_USER}" \
    -Dspring.ds_commands.password="${EXAM_DB_PASSWORD}" \
    -Dspring.ds_commands.driver-class-name=com.mysql.jdbc.Driver \
    -Dexam-service.session-url=http://session:8080/ \
    -Dexam-service.student-url=http://student:8080/ \
    -Dexam-service.assessment-url=http://assessment:8080/ \
    -Dexam-service.config-url=http://config:8080/ \
    -Dflyway.enabled=${EXAM_FLYWAY_ENABLED} \
    -Dflyway.url="jdbc:mysql://exam_mysql/${EXAM_DB_NAME}" \
    -Dflyway.user="${EXAM_DB_USER}" \
    -Dflyway.password="${EXAM_DB_PASSWORD}" \
    -jar /tds-exam-service.jar \
    --server-port="8080" \
    --server.undertow.buffer-size=16384 \
    --server.undertow.buffers-per-region=20 \
    --server.undertow.io-threads=64 \
    --server.undertow.worker-threads=512 \
    --server.undertow.direct-buffers=true