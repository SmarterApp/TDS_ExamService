FROM frolvlad/alpine-oraclejdk8:slim

VOLUME /tmp
ADD docker-startup.sh /
ADD tds-exam-service-*.jar tds-exam-service.jar
RUN sh -c 'touch /docker-startup.sh && touch /tds-exam-service.jar'
RUN apk --no-cache add curl
ENTRYPOINT ["/docker-startup.sh"]