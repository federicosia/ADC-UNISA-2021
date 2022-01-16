FROM openjdk:11

WORKDIR /app

COPY . /app
RUN mvn package

ARG m
ARG id

CMD ["java", "Main"]


#FROM openjdk:16-alpine3

#WORKDIR /app

#COPY .mvn/ .mvn
#COPY mvn pom.xml ./
#RUN ./mvn dependency:go-offline

#COPY src ./src

#ARG m
#ARG id

#CMD 