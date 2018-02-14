FROM anapsix/alpine-java
MAINTAINER jukkagrao

ENV DIR /home/foonk
ENV CONF_DIR ${DIR}/conf

COPY target/scala-2.12/foonk.jar "${DIR}/foonk.jar"

RUN mkdir $CONF_DIR

VOLUME $CONF_DIR

EXPOSE 8000
EXPOSE 8001

CMD java -Dconfig.file=$CONF_DIR/foonk.conf -jar $DIR/foonk.jar
