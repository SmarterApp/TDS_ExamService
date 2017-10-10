# Spring Configuration

The following is a brief explanation of the Spring properties that are available for the exam service.  You can edit and alter these with Spring cloud configuration and in the `application.yml` file within the main src for local development

```
management.security.roles: MANAGEMENT #Default security role mainly for health endpoints

---
spring:
  profiles: local-development #the profile for the sprint configuration
  ds_queries: # this is the query database information
    url: # JDBC Url pointing to the exam schema
    username: # DB username with read permission
    password: # DB password
    driver-class-name: 'com.mysql.jdbc.Driver'
  ds_commands: # this is the writeable database information.
    url: # JDBC Url pointing to the exam schema
    username: # DB username with write permission
    password: # DB password
    driver-class-name: 'com.mysql.jdbc.Driver'

#Port defined in rabbit deployment configuration
  rabbitmq:
    port: # port for rabbit
    publisher-confirms: true
    template:
      retry:
        enabled: # if 'true' failed messages will be retried

tds:
  cache:
    enabled: # 'true' or 'false'.  Usually 'true' unless doing local development

management:
  health:
    redis:
      enabled: # 'true'/'false' dependencing on if health checks include redis
    rabbit:
      enabled: # 'true'/'false' dependencing on if health checks include redis

#Logging
logging:
  level:
    tds.common.web: # the log level for the application e.g. DEBUG

#Configure the default Spring Security user with the MANAGEMENT role
security:
  user:
    name: user
    password: password
    role: MANAGEMENT

server:
  port: 8081 # port that the application will run on
  undertow: # undertow configuration information
    buffer-size: 16384
    buffers-per-region: 20
    io-threads: 64
    worker-threads: 512
    direct-buffers: true

flyway:
  enabled: true
  url: # JDBC Url pointing to the exam schema
  username: # DB username
  password: # DB password
  out-of-order: true

tds:
  cache:
    implementation: # implementation for the cache i.e. 'redis'

#Hosts defined in docker-compose or deployment yml files
exam-service: # other microservice urls.  Examples below are used with kubernetes
  session-url: http://tds-session-service
  student-url: http://tds-student-service
  assessment-url: http://tds-assessment-service
  config-url: http://tds-config-service
  content-url: http://tds-content-service

exam:
  scoring:
    engine: # this scores certain items.  Url for kubernetes
      pythonScoringUrl: http://tds-equation-scoring-service/
    s3: # Item/stimuli are stored in s3
      bucketName: # S3 Bucket Name
      itemPrefix: # S3 folder location
      accessKey: # S3 access key
      secretKey: # S3 secret key that should be used

```