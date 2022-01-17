FROM openjdk:11

WORKDIR /app

COPY /target/Project-ADC-1.0-SNAPSHOT-jar-with-dependencies.jar /app

ARG m
ARG id

CMD java -jar Project-ADC-1.0-SNAPSHOT-jar-with-dependencies.jar -m ${m} -id ${id}