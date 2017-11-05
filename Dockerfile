FROM anapsix/alpine-java
MAINTAINER jukkagrao

COPY ./target/scala-2.12/foonk.jar /home/foonk.jar

EXPOSE 8000
EXPOSE 8001

CMD ["java", "-jar","/home/foonk.jar"]
