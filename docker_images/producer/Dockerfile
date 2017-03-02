FROM frolvlad/alpine-oraclejdk8:slim

ADD . /producer

WORKDIR /producer

COPY ikb4stream-1.0-SNAPSHOT-producer.jar ikb4stream-1.0-SNAPSHOT-producer.jar

COPY resources/ resources/

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar ikb4stream-1.0-SNAPSHOT-producer.jar"]