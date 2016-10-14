FROM frolvlad/alpine-oraclejdk8:slim

VOLUME /tmp
ADD docker-startup.sh /
ADD tds-exam-service-0.0.1-SNAPSHOT.jar tds-exam-service.jar
RUN sh -c 'touch /docker-startup.sh && touch /tds-exam-service.jar'
ENTRYPOINT ["/docker-startup.sh"]