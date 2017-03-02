FROM frolvlad/alpine-oraclejdk8:slim

ADD . /consumer

WORKDIR /consumer

COPY ikb4stream-1.0-SNAPSHOT-consumer.jar ikb4stream-1.0-SNAPSHOT-consumer.jar

COPY resources/ resources/

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar ikb4stream-1.0-SNAPSHOT-consumer.jar"]