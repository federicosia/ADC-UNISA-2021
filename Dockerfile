FROM openjdk:11

WORKDIR /app

COPY /target/p2p-git-protocol.jar /app
RUN apt-get update
RUN apt-get install nano

ARG ip
ARG id

CMD java -jar p2p-git-protocol.jar -m ${ip} -id ${id}